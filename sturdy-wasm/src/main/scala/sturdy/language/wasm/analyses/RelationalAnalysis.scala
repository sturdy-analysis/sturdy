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
import swam.{FuncType, OpCode, syntax}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView
import WasmFailure.*
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.store.{RecencyClosure, RecencyStore, RelationalStore}
import sturdy.fix.DomLogger

import scala.collection.immutable.List

object RelationalAnalysis extends Interpreter, RelationalTypes, RelationalAddresses, RelationalI32Values, ExceptionByTarget, ControlFlow, Control:
  final type J[A] = WithJoin[A]
  final type Addr = BaseType[Int]
  final type Size = I32
  final type FuncIx = apron.Interval
  final type FunV = Powerset[FunctionInstance]
  final type I64 = ApronExpr[VirtAddr, Type]
  final type F32 = ApronExpr[VirtAddr, Type]
  final type F64 = ApronExpr[VirtAddr, Type]
  final type Bytes = Interval

  import Value.*
  import Type.*

  final override def topI64: I64 = constant(Interval(BigInt(Long.MinValue).bigInteger, BigInt(Long.MaxValue).bigInteger), I64Type)

  final override def topF32: F32 = ApronExpr.top(F32Type)

  final override def topF64: F64 = ApronExpr.top(F64Type)

  val topSize: Top[Size] = new Top[Size]:
    override def top: Size = topI32

  final override def asBoolean(v: Value)(using failure: Failure): Bool =
    v match
      case Int32(Left(i)) => ApronCons.neq[VirtAddr, Type](i, intLit(0, i._type))
      case Int32(Right(cons)) => cons
      case Int64(l) => ApronCons.neq[VirtAddr, Type](l, intLit(0, l._type))
      case Float32(f) => ApronCons.neq[VirtAddr, Type](f, intLit(0, f._type))
      case Float64(d) => ApronCons.neq[VirtAddr, Type](d, intLit(0, d._type))
      case TopValue => ApronCons.top(I32Type)

  given RelationalSpecialWasmOperations(using f: Failure, eff: EffectStack, apronState: ApronState[VirtAddr, Type]): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v match
      case Int32(_) => BaseType[Int]
      case TopValue => BaseType[Int]
      case _ => f.fail(TypeError, s"Expected i32 but got $this")

    override def valueToFuncIx(v: Value): FuncIx =
      val expr = v.asInt32.asApronExpr
      apronState.getInterval(expr)

    override def valToSize(v: Value): Size = v match
      case Int32(v) => v
      case TopValue => topI32
      case _ => f.fail(TypeError, s"Expected i32 but got $this")

    override def sizeToVal(sz: Size): Value =
      Int32(topI32)

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      val expr = ix.asInt32.asApronExpr
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
      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(Left(intLit(i, I32Type)))
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

    var exprConverter: ApronExprConverter[RelAddr, Type, Value] = null
    var apronState: ApronRecencyState[RelAddr, Type, Value] = null
    given Lazy[ApronState[VirtAddr, Type]] = Lazy(apronState)
    given Join[ApronExpr[VirtAddr, Type]] = JoinApronExpr[VirtAddr, Type]
    given Widen[ApronExpr[VirtAddr, Type]] = WidenApronExpr[VirtAddr, Type]


    final class WasmRelationalStore extends RelationalStore[RelAddr, Type, PowPhysAddr, Value](
      manager = apronManager,
      initialState = apron.Abstract1(apronManager, new apron.Environment()),
      initialTypeEnv = Map()
    ):
      override def getRelationalVal(v: Value): Option[ApronExpr[PhysAddr, Type]] =
        v match
          case Value.Int32(i32: I32) => Some(exprConverter.virtToPhys(i32.asApronExprLazy))
          case Value.Int64(expr) => Some(exprConverter.virtToPhys(expr))
          case Value.Float32(expr) => Some(exprConverter.virtToPhys(expr))
          case Value.Float64(expr) => Some(exprConverter.virtToPhys(expr))
          case Value.TopValue => None

      override def makeRelationalVal(expr: ApronExpr[PhysAddr, Type]): Value =
        callFrame.makeRelationalVal(exprConverter.physToVirt(expr))
    given domLogger: DomLogger[FixIn] = new DomLogger

    val relationalStore: RelationalStore[RelAddr, Type, PowPhysAddr, Value] = new WasmRelationalStore
    val recencyStore: RecencyStore[RelAddr, PowVirtAddr, Value] = new RecencyStore(relationalStore)
    exprConverter = ApronExprConverter(recencyStore, relationalStore)
    apronState = new ApronRecencyState[RelAddr, Type, Value](tempRelationalAlloc(rootFrameData), recencyStore, relationalStore)
    given apState: ApronState[VirtAddr, Type] = if(apronState != null) apronState else throw new IllegalArgumentException("this.apronState is null")

    final class WasmCallFrame extends RelationalCallFrame[FrameData, Int, Value, InstLoc, RelAddr, Type](
      initData = rootFrameData,
      initVars = Iterable.empty,
      localVariableAllocator = localRelationaAlloc,
      apronState
    ):
      override def makeRelationalVal(expr: ApronExprVirtAddr): Value =
        expr._type match
          case I32Type => Value.Int32(Left(expr))
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

    def getInterval(v: Value): Value =
      v match
        case Value.Int32(i32) =>
          val expr = i32.asApronExpr
          Value.Int32(Left(ApronExpr.constant(IntervalLattice.meet(apronState.getInterval(expr), Interval(Int.MinValue, Int.MaxValue)), expr._type)))
        case Value.Int64(expr) => Value.Int64(ApronExpr.constant(IntervalLattice.meet(apronState.getInterval(expr), Interval(BigInt(Long.MinValue).bigInteger, BigInt(Long.MaxValue).bigInteger)), expr._type))
        case Value.Float32(expr) => Value.Float32(ApronExpr.constant(apronState.getInterval(expr), expr._type))
        case Value.Float64(expr) => Value.Float64(ApronExpr.constant(apronState.getInterval(expr), expr._type))
        case Value.TopValue => Value.TopValue

    def constantInstructions: ConstantInstructionsLogger =
      val constants = new ConstantInstructionsLogger
      this.fixpoint.addContextFreeLogger(constants)
      constants

    extension(expr: ApronExpr[VirtAddr,Type])
      def isConstant: Boolean =
        val iv = apronState.getInterval(expr)
        iv.inf().isEqual(iv.sup())


    class ConstantInstructionsLogger extends InstructionResultLogger[Interval, Value](stack):
      override def boolValue(v: Value): Value = boolean(asBoolean(v))

      override def dummyValue: Value = Value.Int32(Left(ApronExpr.constant(Interval(0, 0), I32Type)))
      
      def getInfo(value: Value): Interval = value match
        case Value.TopValue => Interval(Double.NegativeInfinity, Double.PositiveInfinity)
        case Value.Int32(Left(v)) => apronState.getInterval(v)
        case Value.Int32(Right(v)) =>
          apronState.getBoolean(v) match
            case Topped.Actual(true)  => Interval(1,1)
            case Topped.Actual(false) => Interval(0,0)
            case Topped.Top           => Interval(0,1)
        case Value.Int64(v) => apronState.getInterval(v)
        case Value.Float32(v) => apronState.getInterval(v)
        case Value.Float64(v) => apronState.getInterval(v)


      def get: Map[InstLoc, List[Interval]] = instructionInfo.filter(_._2.forall (
        iv => iv.inf.isEqual(iv.sup)
      ))

      def grouped: Map[String, Map[InstLoc, List[Interval]]] =
        get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

      def groupedCount: Map[String, Int] =
        get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap


    override def newEffectStack: EffectStack =
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

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    val observedConfig = config.withObservers(Seq(this.triggerControlEvent))

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
