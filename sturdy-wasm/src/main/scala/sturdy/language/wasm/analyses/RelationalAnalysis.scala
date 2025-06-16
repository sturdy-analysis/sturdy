package sturdy.language.wasm.analyses

import apron.*
import sturdy.apron.{*, given}
import sturdy.apron.ApronExpr.*
import sturdy.control.{ControlEvent, ControlObservable, FixpointControlEvent, RecordingControlObserver}
import sturdy.data.{*, given}
import sturdy.effect.{EffectList, EffectStack, TrySturdy}
import sturdy.effect.bytememory.{ConstantAddressMemory, HasSize, RelationalMemory, TopMemory}
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.{ConcreteCallFrame, DecidableCallFrame, JoinableDecidableCallFrame, RelationalCallFrame}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{DecidableOperandStack, JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.{ConstantSymbolTable, IntervalSymbolTable, JoinableDecidableSymbolTable, RelationalSymbolTable}
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
import sturdy.values.bytes.{*, given}
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
import scala.collection.{IndexedSeqView, mutable}
import WasmFailure.*
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.stack.RelationalStack
import sturdy.effect.store.{AStoreThreaded, RecencyClosure, RecencyStore, RelationalStore}
import sturdy.fix.{DomLogger, Logger}

import java.math.BigInteger
import scala.collection.immutable.List
import scala.math

object RelationalAnalysis extends Interpreter, RelationalTypes, RelationalAddresses, RelationalI32Values, ExceptionByTarget, Control:
  final type J[A] = WithJoin[A]
  final type Addr = ApronExpr[VirtAddr, Type]
  final type Size = ApronExpr[VirtAddr, Type]
  final type FuncIx = apron.Interval
  final type FunV = Powerset[FunctionInstance]
  final type I64 = ApronExpr[VirtAddr, Type]
  final type F32 = ApronExpr[VirtAddr, Type]
  final type F64 = ApronExpr[VirtAddr, Type]
  final type Bytes = sturdy.values.bytes.Bytes[VirtAddr, Type]

  import Value.*
  import Type.*

  final override def topI64: I64 = constant(ApronExpr.topInterval, I64Type)
  final override def topF32: F32 = ApronExpr.floatConstant(ApronExpr.topInterval, FloatSpecials.Top, F32Type)
  final override def topF64: F64 = ApronExpr.floatConstant(ApronExpr.topInterval, FloatSpecials.Top, F64Type)

  val topSize: Top[Size] = new Top[Size]:
    override def top: Size = constant(ApronExpr.topInterval, I32Type)

  final override def asBoolean(v: Value)(using failure: Failure): Bool =
    v match
      case Int32(Left(i)) => ApronBool.Constraint(ApronCons.neq[VirtAddr, Type](i, intLit(0, i._type)))
      case Int32(Right(cons)) => cons
      case Int64(l) => ApronBool.Constraint(ApronCons.neq[VirtAddr, Type](l, intLit(0, l._type)))
      case Float32(f) => ApronBool.Constraint(ApronCons.neq[VirtAddr, Type](f, intLit(0, f._type)))
      case Float64(d) => ApronBool.Constraint(ApronCons.neq[VirtAddr, Type](d, intLit(0, d._type)))
      case TopValue => ApronBool.Constraint(ApronCons.top(I32Type))

  given RelationalSpecialWasmOperations(using f: Failure, eff: EffectStack, apronState: ApronState[VirtAddr, Type]): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v match
      case Int32(v) => v.asApronExpr
      case TopValue => constant(ApronExpr.topInterval, I32Type)
      case _ => f.fail(TypeError, s"Expected i32 but got $this")

    override def valueToFuncIx(v: Value): FuncIx =
      val expr = v.asInt32.asApronExpr
      apronState.getInterval(expr)

    override def valToSize(v: Value): Size = v match
      case Int32(v) => v.asApronExpr
      case TopValue => constant(ApronExpr.topInterval, I32Type)
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

    def assignFreshTempVar(expr: ApronExpr[VirtAddr, Type]): List[Value] =
      List(apronState.withTempVars(expr._type)((v, _) =>
        apronState.assign(v, expr)
        Value.Int32(Left(ApronExpr.addr(v, expr._type)))
      ))

    def signedMin(numBytes: Int): BigInt = - BigInt(2).pow(8 * numBytes - 1)
    def signedMax(numBytes: Int): BigInt = BigInt(2).pow(8 * numBytes - 1) - 1

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] = hostFunc.name match
      case "proc_exit" =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case "__VERIFIER_nondet_bool" =>
        assignFreshTempVar(ApronExpr.intInterval(0, 1, I32Type))
      case "__VERIFIER_nondet_char" | "__VERIFIER_nondet_uchar" =>
        assignFreshTempVar(ApronExpr.intInterval(signedMin(1).toInt, signedMax(1).toInt, I32Type))
      case "__VERIFIER_nondet_short" | "__VERIFIER_nondet_ushort" =>
        assignFreshTempVar(ApronExpr.intInterval(signedMin(2).toInt, signedMax(2).toInt, I32Type))
      case "__VERIFIER_nondet_int" | "__VERIFIER_nondet_long" | "__VERIFIER_nondet_uint" | "__VERIFIER_nondet_ulong" =>
        assignFreshTempVar(ApronExpr.intInterval(signedMin(4).toInt, signedMax(4).toInt, I32Type))
      case "__VERIFIER_nondet_longlong" | "__VERIFIER_nondet_ulonglong" =>
        assignFreshTempVar(ApronExpr.longInterval(signedMin(8).toLong, signedMax(8).toLong, I64Type))
      case "__VERIFIER_nondet_float" =>
        assignFreshTempVar(ApronExpr.doubleInterval(Float.MinValue, Float.MaxValue, FloatSpecials.Top, F32Type))
      case "__VERIFIER_nondet_double" =>
        assignFreshTempVar(ApronExpr.doubleInterval(Double.MinValue, Double.MaxValue, FloatSpecials.Top, F64Type))
      case "__blackhole_int" | "__blackhole_int_p" =>
        args
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        eff.joinWithFailure(result)(f.fail(FileError, s"in ${hostFunc.name}"))

  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(Left(intLit(i, I32Type)))
      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(longLit(l, I64Type))
      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(FloatingLit(f, F32Type))
      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(FloatingLit(d, F64Type))

  class Instance(val apronManager: apron.Manager, val rootFrameData: FrameData, val rootFrameValues: Iterable[Value], val config: WasmConfig) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    private given Instance = this

    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
    //    override def widenState: Widen[State] = implicitly

    val addressTranslation: AddressTranslation[AddrCtx] = AddressTranslation.empty
    var exprConverter: ApronExprConverter[AddrCtx, Type, Value] = null
    var apronState: ApronRecencyState[AddrCtx, Type, Value] = null
    given Lazy[ApronState[VirtAddr, Type]] = Lazy(apronState)
    given Lazy[ApronExprConverter[AddrCtx, Type, Value]] = Lazy(exprConverter)
    given Join[ApronExpr[VirtAddr, Type]] = JoinApronExpr[VirtAddr, Type]
    given Widen[ApronExpr[VirtAddr, Type]] = WidenApronExpr[VirtAddr, Type]

    given RelationalExpr[Value, VirtAddr, Type] with
      override def getRelationalExpr(v: Value): Option[ApronExpr[VirtAddr, Type]] =
        v match
          case Value.Int32(i32: I32) => Some(i32.asApronExprLazy)
          case Value.Int64(expr) => Some(expr)
          case Value.Float32(expr) => Some(expr)
          case Value.Float64(expr) => Some(expr)
          case Value.TopValue => None

      override def makeRelationalExpr(expr: ApronExpr[VirtAddr, Type]): Value =
        expr._type match
          case I32Type => Value.Int32(Left(expr))
          case I64Type => Value.Int64(expr)
          case F32Type => Value.Float32(expr)
          case F64Type => Value.Float64(expr)

    given domLogger: DomLogger[FixIn] = new DomLogger

    val relationalStore: RelationalStore[AddrCtx, Type, PowPhysAddr, Value] = new RelationalStore[AddrCtx, Type, PowPhysAddr, Value](
      manager = apronManager,
      initialState = apron.Abstract1(apronManager, new apron.Environment()),
      initialMetaData = Map()
    )
    val recencyStore: RecencyStore[AddrCtx, PowVirtAddr, Value] = new RecencyStore(relationalStore, addressTranslation)
    exprConverter = ApronExprConverter(recencyStore, relationalStore)
    apronState = new ApronRecencyState[AddrCtx, Type, Value](tempRelationalAlloc(rootFrameData), recencyStore, relationalStore)
    given ApronRecencyState[AddrCtx, Type, Value] = apronState

    def addressIterator: Iterator[VirtAddr] =
      def valueIterator(value: Any): Iterator[VirtAddr] = value match
        case Value.Int32(Left(expr)) => expr.addrs.iterator
        case Value.Int32(Right(cons)) => cons.addrs.iterator
        case Value.Int64(expr) => expr.addrs.iterator
        case Value.Float32(expr) => expr.addrs.iterator
        case Value.Float64(expr) => expr.addrs.iterator
        case excV: ExcV =>
          for(listVals <- excV.values.iterator;
              value <- listVals.iterator;
              addr <- valueIterator(value))
            yield(addr)
        case _ =>
          println("Unknown Value "+value)
          Iterator()

      effectStack.addressIterator[VirtAddr](valueIterator)

    def garbageCollect(): Unit =
      val alive = PowVirtualAddress(this.addressIterator)
      val dead = recencyStore.addressTranslation.deadPhysicalAddresses(alive)
      val stateBefore = effectStack.getState
      recencyStore.collectGarbage(alive)
      val stateAfter = effectStack.getState
      println(s"Alive: $alive")
      println(s"Dead: $dead")
      println(s"State Before: $stateBefore")
      println(s"State After: $stateAfter")

    val callFrame: RelationalCallFrame[FrameData, Int, Value, InstLoc, AddrCtx, Type] = new RelationalCallFrame(
      initData = rootFrameData,
      initVars = Iterable.empty,
      localVariableAllocator = localAlloc(ssa = config.localSSA, rootFrameData),
      apronState
    )

    val stack: RelationalStack[Value, AddrCtx, Type] = new RelationalStack(stackAlloc[Int, Value, InstLoc, NoJoin](rootFrameData, callFrame))

    val byteStore: RecencyStore[AddrCtx, VirtualAddress[AddrCtx], Bytes] = new RecencyStore(AStoreThreaded(Map()), addressTranslation)
    val memory: RelationalMemory[MemoryAddr, AddrCtx, Type, Bytes] = new RelationalMemory[MemoryAddr, AddrCtx, Type, Bytes](
      apronState,
      byteStore,
      sturdy.values.bytes.Bytes(Value.Int64(ApronExpr.intLit(0, I64Type)), NumericInterval(4,4), Topped.Actual(ByteOrder.LITTLE_ENDIAN)),
      heapAlloc(rootFrameData))

    val globals: RelationalSymbolTable[Unit, GlobalAddr, Value, AddrCtx, Type] = new RelationalSymbolTable(new AAllocatorFromContext(
        (key: Unit, sym: GlobalAddr, tpe: Type) =>
          AddrCtx.Global(sym.addr)
    ))

    val funTable: IntervalSymbolTable[TableAddr, FuncIx, Powerset[FunctionInstance]] = new IntervalSymbolTable

    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    def getInterval(v: Value): Value =
      v match
        case Value.Int32(i32) =>
          val expr = i32.asApronExpr
          Value.Int32(
            Left(ApronExpr.constant(
              apronState.getFloatInterval(expr).meet(
                sturdy.apron.FloatInterval(MpqScalar(Int.MinValue), MpqScalar(Int.MaxValue), FloatSpecials.Bottom)
              ),
              expr._type
            )
            )
          )
        case Value.Int64(expr) =>
          Value.Int64(
            ApronExpr.constant(
              apronState.getFloatInterval(expr).meet(
                sturdy.apron.FloatInterval(MpqScalar(BigInteger.valueOf(Long.MinValue)), MpqScalar(BigInteger.valueOf(Long.MaxValue)), FloatSpecials.Bottom)
              ),
              expr._type
            )
          )
        case Value.Float32(expr) =>
          Value.Float32(ApronExpr.constant(
            apronState.getFloatInterval(expr).meet(
              sturdy.apron.FloatInterval(Float.MinValue, Float.MaxValue, FloatSpecials.Top)
            ), expr._type))
        case Value.Float64(expr) =>
          Value.Float64(ApronExpr.constant(
            apronState.getFloatInterval(expr).meet(
              sturdy.apron.FloatInterval(Double.MinValue, Double.MaxValue, FloatSpecials.Top)
            ), expr._type))
        case Value.TopValue => Value.TopValue

    def constantInstructions: ConstantInstructionsLogger =
      val constants = new ConstantInstructionsLogger
      this.fixpoint.addContextFreeLogger(constants)
      constants

    extension(expr: ApronExpr[VirtAddr,Type])
      def isConstant: Boolean =
        val iv = apronState.getInterval(expr)
        iv.inf().isEqual(iv.sup())


    class FunctionCallLogger extends Logger[FixIn, FixOut[Value]]:
      val stack: mutable.Stack[(FixIn, IndexedSeq[(Value,Value)], effectStack.State)] = mutable.Stack.empty

      override def enter(dom: FixIn): Unit =
        dom match
          case FixIn.EnterWasmFunction(id,_,FuncType(params,_)) =>
            val args = params.indices.map {
              i =>
                val v = callFrame.getLocal(i).get
                (v, getInterval(v))
            }
            val state = effectStack.getState
            print("  ".repeat(stack.size))
            println(s"CALL   f${id.funcIx}(${args.mkString(",")} @ ${state.hashCode()}")
            stack.push((dom,args,state))
          case _ => {}

      override def exit(dom: FixIn, codom: TrySturdy[FixOut[Value]]): Unit =
        dom match
          case FixIn.EnterWasmFunction(id, _, ft) =>
            val (_,args,inState) = stack.pop
            val result =
              codom.map{
                case FixOut.ExitWasmFunction(returns) =>  FixOut.ExitWasmFunction(returns.map(v => (v, getInterval(v))))
                case FixOut.ExitHostFunction(returns) =>  FixOut.ExitHostFunction(returns.map(v => (v, getInterval(v))))
                case FixOut.Eval() =>  FixOut.Eval()
                case FixOut.MostGeneralClient() => FixOut.MostGeneralClient()
              }
            val outState = effectStack.getState
            print("  ".repeat(stack.size))
            println(s"RETURN f${id.funcIx}(${args.mkString(",")} @ ${inState.hashCode} = $result @ ${outState.hashCode()}")
          case _ => {}


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
      lazy val allEffects = RecencyClosure(recencyStore, EffectList(stack, memory, globals, funTable, callFrame, except, failure))
      lazy val inEffectsFunction = RecencyClosure(recencyStore, EffectList(memory, globals, funTable, callFrame))
      lazy val inEffectsEval = RecencyClosure(recencyStore, EffectList(stack, memory, globals, funTable, callFrame))
      lazy val outEffectsFunction = RecencyClosure(recencyStore, EffectList(stack, memory, globals, funTable, failure))
      lazy val outEffectsEval = RecencyClosure(recencyStore, EffectList(stack, memory, globals, funTable, callFrame, except))

      new EffectStack(allEffects,
        {
          case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => inEffectsFunction
          case _: FixIn.Eval => inEffectsEval
        }, {
          case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => outEffectsFunction
          case _: FixIn.Eval => outEffectsEval
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
      addContextFreeLogger(new FunctionCallLogger)
    }

    override def toString: String = s"constant $config"
