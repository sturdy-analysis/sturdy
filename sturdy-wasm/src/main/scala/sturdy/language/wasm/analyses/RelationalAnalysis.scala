package sturdy.language.wasm.analyses

import apron.*
import sturdy.apron.{*, given}
import sturdy.apron.ApronExpr.*
import sturdy.control.{ControlEvent, ControlObservable, FixpointControlEvent, RecordingControlObserver}
import sturdy.data.{*, given}
import sturdy.effect.{EffectList, EffectStack}
import sturdy.effect.bytememory.{ConstantAddressMemory, TopMemory}
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.{ConcreteCallFrame, DecidableCallFrame, JoinableDecidableCallFrame, RelationalCallFrame}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{DecidableOperandStack, JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.{ConstantSymbolTable, IntervalSymbolTable, JoinableDecidableSymbolTable}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.language.wasm.abstractions.Control.Exc
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.config.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.{*, given}
import sturdy.util.{*, given}
import swam.syntax.*
import swam.FuncType

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView
import WasmFailure.*
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.store.{RecencyClosure, RecencyStore, RelationalStore}
import sturdy.fix.DomLogger

object RelationalAnalysis extends Interpreter, ExceptionByTarget, ControlFlow, Control:
  type J[A] = WithJoin[A]
  type Addr = BaseType[Int]
  type Size = I32
  type FuncIx = apron.Interval
  type FunV = Powerset[FunctionInstance]

  enum RelAddr:
    case Local(callFrameAddr: Int, frame: FrameData)
    case Temp(fixin: FixIn)

  import RelAddr.*

  given Ordering[RelAddr] = Ordering.by {
    case RelAddr.Local(callFrameAddr, data) => Left(callFrameAddr, data)
    case RelAddr.Temp(fixin) => Right(fixin)
  }
  given Finite[RelAddr] with {}

  type VirtAddr = VirtualAddress[RelAddr]
  type PhysAddr = PhysicalAddress[RelAddr]
  type PowVirtAddr = PowVirtualAddress[RelAddr]
  type PowPhysAddr = PowersetAddr[PhysAddr, PhysAddr]

  given Ordering[VirtAddr] = VirtualAddressOrdering

  enum Type:
    case I32Type
    case I64Type
    case F32Type
    case F64Type

    def asI32(using f: Failure): BaseType[Int] =
      this match
        case I32Type => BaseType[Int]
        case _ => f.fail(WasmFailure.TypeError, s"Expected i32, but got $this")

    def asI64(using f: Failure): BaseType[Long] =
      this match
        case I64Type => BaseType[Long]
        case _ => f.fail(WasmFailure.TypeError, s"Expected i64, but got $this")

    def asF32(using f: Failure): BaseType[Float] =
      this match
        case F32Type => BaseType[Float]
        case _ => f.fail(WasmFailure.TypeError, s"Expected f32, but got $this")

    def asF64(using f: Failure): BaseType[Double] =
      this match
        case F64Type => BaseType[Double]
        case _ => f.fail(WasmFailure.TypeError, s"Expected f64, but got $this")

    override def toString: String =
      this match
        case I32Type => "i32"
        case I64Type => "i64"
        case F32Type => "f32"
        case F64Type => "f64"

  import Type.*

  given CombineType[W <: Widening]: Combine[Type, W] = {
    case (I32Type, I32Type) => Unchanged(I32Type)
    case (I64Type, I64Type) => Unchanged(I64Type)
    case (F32Type, F32Type) => Unchanged(F32Type)
    case (F64Type, F64Type) => Unchanged(F64Type)
    case (t1, t2) => throw new IllegalArgumentException(s"Incompatible types $t1 and $t2")
  }

  given ApronType[Type] with
    extension (t: Type)
      def apronRepresentation: ApronRepresentation =
        t match
          case I32Type => BaseType[Int].apronRepresentation
          case I64Type => BaseType[Long].apronRepresentation
          case F32Type => BaseType[Float].apronRepresentation
          case F64Type => BaseType[Double].apronRepresentation
      def roundingDir: RoundingDir =
        t match
          case I32Type => BaseType[Int].roundingDir
          case I64Type => BaseType[Long].roundingDir
          case F32Type => BaseType[Float].roundingDir
          case F64Type => BaseType[Double].roundingDir
      def roundingType: RoundingType =
        t match
          case I32Type => BaseType[Int].roundingType
          case I64Type => BaseType[Long].roundingType
          case F32Type => BaseType[Float].roundingType
          case F64Type => BaseType[Double].roundingType
      def byteSize: Int =
        t match
          case I32Type => BaseType[Int].byteSize
          case I64Type => BaseType[Long].byteSize
          case F32Type => BaseType[Float].byteSize
          case F64Type => BaseType[Double].byteSize


  final type I32 = ApronExpr[VirtAddr, Type]
  final type I64 = ApronExpr[VirtAddr, Type]
  final type F32 = ApronExpr[VirtAddr, Type]
  final type F64 = ApronExpr[VirtAddr, Type]
  final type Bytes = Interval
  final type Bool = ApronCons[VirtAddr, Type]


  import Value.*

  final def topI32: I32 = ApronExpr.constant(Interval(Int.MinValue, Int.MaxValue), I32Type)

  final def topI64: I64 = constant(Interval(BigInt(Long.MinValue).bigInteger, BigInt(Long.MaxValue).bigInteger), I64Type)

  final def topF32: F32 = ApronExpr.top(F32Type)

  final def topF64: F64 = ApronExpr.top(F64Type)

  val topSize: Top[Size] = new Top[Size]:
    override def top: Size = topI32

  final def asBoolean(v: Value)(using failure: Failure): Bool =
    v match
      case Int32(i) => ApronCons.neq[VirtAddr, Type](i, intLit(0, i._type))
      case Int64(l) => ApronCons.neq[VirtAddr, Type](l, intLit(0, l._type))
      case Float32(f) => ApronCons.neq[VirtAddr, Type](f, intLit(0, f._type))
      case Float64(d) => ApronCons.neq[VirtAddr, Type](d, intLit(0, d._type))
      case TopValue => ApronCons.top(I32Type)

  final def boolean(b: Bool): Value =
    ???

  given RelationalSpecialWasmOperations(using f: Failure, eff: EffectStack, apronState: ApronState[VirtAddr, Type]): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v match
      case Int32(_) => BaseType[Int]
      case TopValue => BaseType[Int]
      case _ => f.fail(TypeError, s"Expected i32 but got $this")

    override def valueToFuncIx(v: Value): FuncIx =
      val expr = v.asInt32
      apronState.getInterval(expr)

    override def valToSize(v: Value): Size = v match
      case Int32(v) => v
      case TopValue => topI32
      case _ => f.fail(TypeError, s"Expected i32 but got $this")

    override def sizeToVal(sz: Size): Value =
      Int32(topI32)

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      val expr = ix.asInt32
      val (l,h) = apronState.getIntInterval(expr)
      val elems = for (i <- l.max(0) to h.min(vec.size - 1))
        yield vec(i)
      if (elems.isEmpty) {
        // no elems in range
        JOptionPowerset.None()
      } else if (h < vec.size) {
        // all indices in range
        JOptionPowerset.Some(Powerset(elems.toSet))
      } else {
        // some indices in range, but not all
        JOptionPowerset.NoneSome(Powerset(elems.toSet))
      }

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] = hostFunc match
      case HostFunction.proc_exit =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        eff.joinWithFailure(result)(f.fail(FileError, s"in ${hostFunc.name}"))

  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(intLit(i, I32Type))
      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(longLit(l, I64Type))
      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(doubleLit(f, F32Type))
      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(doubleLit(d, F64Type))

  class Instance(apronManager: apron.Manager, rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]
    //      , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
    :
    private given Instance = this

    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
    //    override def widenState: Widen[State] = implicitly

    implicit val tempRelationalAlloc: AAllocatorFromContext[Type, RelAddr] = AAllocatorFromContext(
      _ => RelAddr.Temp(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)))
    )
    implicit val localRelationaAlloc: AAllocatorFromContext[(Int,FrameData,Option[InstLoc]), RelAddr] = AAllocatorFromContext(
      (i,data,_) => RelAddr.Local(i,data)
    )

    var exprConverter: ApronExprConverter[RelAddr, Type, Value] = null
    var apronState: ApronRecencyState[RelAddr, Type, Value] = null
    given Lazy[ApronState[VirtAddr, Type]] = Lazy(apronState)
    given Join[ApronExpr[VirtAddr, Type]] = JoinApronExpr[VirtAddr, Type]
    given Widen[ApronExpr[VirtAddr, Type]] = WidenApronExpr[VirtAddr, Type]

    val relationalStore: RelationalStore[RelAddr, Type, PowPhysAddr, Value] = new RelationalStore[RelAddr, Type, PowPhysAddr, Value](
      manager = apronManager,
      initialState = apron.Abstract1(apronManager, new apron.Environment()),
      initialTypeEnv = Map()
    ):
      override def getRelationalVal(v: Value): Option[ApronExpr[PhysAddr, Type]] =
        v match
          case Value.Int32(iv) => Some(exprConverter.virtToPhys(iv))
          case Value.Int64(iv) => Some(exprConverter.virtToPhys(iv))
          case Value.Float32(iv) => Some(exprConverter.virtToPhys(iv))
          case Value.Float64(iv) => Some(exprConverter.virtToPhys(iv))
          case Value.TopValue => None

      override def makeRelationalVal(expr: ApronExpr[PhysAddr, Type]): Value =
        callFrame.makeRelationalVal(exprConverter.physToVirt(expr))

    val recencyStore: RecencyStore[RelAddr, PowVirtAddr, Value] = new RecencyStore(relationalStore)
    exprConverter = ApronExprConverter(recencyStore, relationalStore)
    apronState = new ApronRecencyState[RelAddr, Type, Value](tempRelationalAlloc, recencyStore, relationalStore)
    given ApronState[VirtAddr, Type] = apronState

    final class WasmCallFrame extends RelationalCallFrame[FrameData, Int, Value, InstLoc, RelAddr, Type](
      initData = rootFrameData,
      initVars = Iterable.empty,
      localVariableAllocator = localRelationaAlloc,
      apronState
    ):
      override def makeRelationalVal(expr: ApronExprVirtAddr): Value =
        expr._type match
          case I32Type => Value.Int32(expr)
          case I64Type => Value.Int64(expr)
          case F32Type => Value.Float32(expr)
          case F64Type => Value.Float64(expr)

    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: TopMemory[MemoryAddr, Addr, Bytes, Size] = new TopMemory(using implicitly[Top[Bytes]], topSize)
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val funTable: IntervalSymbolTable[TableAddr, FuncIx, Powerset[FunctionInstance]] = new IntervalSymbolTable
    val callFrame: WasmCallFrame = new WasmCallFrame
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    given IntegerOps[Int, Type] = LiftedIntegerOps[Int, Type, BaseType[Int]](extract = _.asI32, inject = _ => Type.I32Type)
    given IntegerOps[Long, Type] = LiftedIntegerOps[Long, Type, BaseType[Long]](extract = _.asI64, inject = _ => Type.I64Type)

    given FloatOps[Float, Type] = LiftedFloatOps[Float, Type, BaseType[Float]](extract = _.asF32, inject = _ => Type.F32Type)
    given FloatOps[Double, Type] = LiftedFloatOps[Double, Type, BaseType[Double]](extract = _.asF64, inject = _ => Type.F64Type)

    given ConvertIntLong[Type, Type] = LiftedConvert[Int, Long, Type, Type, BaseType[Int], BaseType[Long], Bits](extract = _.asI32, inject = _ => Type.I64Type)
    given ConvertIntFloat[Type, Type] = LiftedConvert[Int, Float, Type, Type, BaseType[Int], BaseType[Float], Bits](extract = _.asI32, inject = _ => Type.F32Type)
    given ConvertIntDouble[Type, Type] = LiftedConvert[Int, Double, Type, Type, BaseType[Int], BaseType[Double], Bits](extract = _.asI32, inject = _ => Type.F64Type)

    given ConvertLongInt[Type, Type] = LiftedConvert[Long, Int, Type, Type, BaseType[Long], BaseType[Int], NilCC.type](extract = _.asI64, inject = _ => Type.I32Type)
    given ConvertLongFloat[Type, Type] = LiftedConvert[Long, Float, Type, Type, BaseType[Long], BaseType[Float], Bits](extract = _.asI64, inject = _ => Type.F32Type)
    given ConvertLongDouble[Type, Type] = LiftedConvert[Long, Double, Type, Type, BaseType[Long], BaseType[Double], Bits](extract = _.asI64, inject = _ => Type.F64Type)

    given ConvertFloatInt[Type, Type] = LiftedConvert[Float, Int, Type, Type, BaseType[Float], BaseType[Int], Overflow && Bits](extract = _.asF32, inject = _ => Type.I32Type)
    given ConvertFloatLong[Type, Type] = LiftedConvert[Float, Long, Type, Type, BaseType[Float], BaseType[Long], Overflow && Bits](extract = _.asF32, inject = _ => Type.I64Type)
    given ConvertFloatDouble[Type, Type] = LiftedConvert[Float, Double, Type, Type, BaseType[Float], BaseType[Double], NilCC.type](extract = _.asF32, inject = _ => Type.F64Type)

    given ConvertDoubleInt[Type, Type] = LiftedConvert[Double, Int, Type, Type, BaseType[Double], BaseType[Int], Overflow && Bits](extract = _.asF64, inject = _ => Type.I32Type)
    given ConvertDoubleLong[Type, Type] = LiftedConvert[Double, Long, Type, Type, BaseType[Double], BaseType[Long], Overflow && Bits](extract = _.asF64, inject = _ => Type.I64Type)
    given ConvertDoubleFloat[Type, Type] = LiftedConvert[Double, Float, Type, Type, BaseType[Double], BaseType[Float], NilCC.type](extract = _.asF64, inject = _ => Type.F32Type)

    def constantInstructions: ConstantInstructionsLogger =
      val constants = new ConstantInstructionsLogger
      this.fixpoint.addContextFreeLogger(constants)
      constants

    extension(expr: ApronExpr[VirtAddr,Type])
      def isConstant: Boolean =
        val iv = apronState.getInterval(expr)
        iv.inf().isEqual(iv.sup())

    class ConstantInstructionsLogger extends InstructionResultLogger[Value](stack):
      override def boolValue(v: Value): Value = boolean(asBoolean(v))

      override def dummyValue: Value = Value.Int32(ApronExpr.constant(Interval(0, 0), I32Type))

      def get: Map[InstLoc, List[Value]] = instructionInfo.filter(_._2.forall {
        case Value.TopValue => false
        case Value.Int32(v) => v.isConstant
        case Value.Int64(v) => v.isConstant
        case Value.Float32(v) => v.isConstant
        case Value.Float64(v) => v.isConstant
      })

      def grouped: Map[String, Map[InstLoc, List[Value]]] =
        get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

      def groupedCount: Map[String, Int] =
        get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap

    override def newEffectStack: EffectStack = super.newEffectStack
      new EffectStack(
        RecencyClosure(recencyStore, EffectList(stack, memory, globals, funTable, callFrame, except, failure)),
        {
          case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => RecencyClosure(recencyStore, EffectList(memory, globals, callFrame))
          case _: FixIn.Eval => RecencyClosure(recencyStore, EffectList(stack, memory, globals, callFrame))
        }, {
          case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => RecencyClosure(recencyStore, EffectList(stack, memory, globals, failure))
          case _: FixIn.Eval => RecencyClosure(recencyStore, EffectList(stack, memory, globals, callFrame, except))
        }
      )

    given UnsignedOrderingOps[I32, Bool] = ???

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    val observedConfig = config.withObservers(Seq(this.triggerControlEvent))
    val domLogger: DomLogger[FixIn] = new DomLogger

    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[Value]] = new fix.ContextualFixpoint[FixIn, FixOut[Value]] {
      override type Ctx = observedConfig.ctx.Ctx
      val (contextPreparation, sensitivity) = observedConfig.ctx.make[Value]
      import observedConfig.ctx.finiteCtx
      override protected def contextFree = phi =>
        fix.log(controlEventLogger(Instance.this, effectStack, except), contextPreparation(phi))
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = observedConfig.fix.get
      addContextFreeLogger(domLogger)
    }

    override def toString: String = s"constant $config"
