package sturdy.language.wasm.analyses

import apron.*
import sturdy.apron.{*, given}
import sturdy.apron.ApronExpr.*
import sturdy.control.{ControlEvent, ControlObservable, FixpointControlEvent, RecordingControlObserver}
import sturdy.data.{*, given}
import sturdy.effect.{EffectList, EffectStack, TrySturdy, bytememory}
import sturdy.effect.bytememory.{*, given}
import sturdy.effect.callframe.{ConcreteCallFrame, DecidableCallFrame, JoinableDecidableCallFrame, RelationalCallFrame}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{DecidableOperandStack, JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.{ConstantIntervalMappedSymbolTable, ConstantSymbolTable, IntervalMappedSymbolTable, IntervalSymbolTable, JoinableDecidableSymbolTable, RelationalSymbolTable}
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
import sturdy.values.simd.{*, given}
import sturdy.util.{*, given}
import swam.syntax.*
import swam.{FuncType, OpCode, ReferenceType, syntax}

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

object RelationalAnalysis extends Interpreter, RelationalTypes, RelationalAddresses, RelationalValues, RelationalMemory, RelationalConvert, ExceptionByTarget, Control:
  final type J[A] = WithJoin[A]
  final type Index = NumericInterval[Int]
  final type FunV = Powerset[FunctionInstance]
  final type RefV = Powerset[RefValue]

  import Value.*
  import NumValue.*
  import Type.*
  import RelI32.*

  val topSize: Top[Size] = new Top[Size]:
    override def top: Size = constant(ApronExpr.topInterval, I32Type)

  override def topExternRef: ExternReference = ???
  override def topFuncRef: FuncReference = ???
  override def topV128: V128 = ???

  given RelationalSpecialWasmOperations
      (using domLogger: DomLogger[FixIn],
             f: Failure,
             effectStack: EffectStack,
             apronState: ApronState[VirtAddr, Type],
             integerOps: IntegerOps[Int, ApronExpr[VirtAddr, Type]],
             unsignedOrderingOps: UnsignedOrderingOps[I32, Bool],
             joinExpr: Join[ApronExpr[VirtAddr, Type]]
      ): SpecialWasmOperations[Value, Addr, Bytes, Size, Index, FunV, RefV, WithJoin] with

    override def valToAddr(v: Value): Addr = v match
      case Num(Int32(n@NumExpr(_))) => n
      case Num(Int32(a@AllocationSites(_, _))) => a
      case Num(Int32(b@BoolExpr(_))) => NumExpr(b.asNumExpr)
      case TopValue => NumExpr(constant(ApronExpr.topInterval, I32Type))
      case _ => f.fail(TypeError, s"Expected i32 but got $this")

    override def valToIdx(v: Value): Index =
      val expr = v.asInt32.asNumExpr
      val (l,u) = apronState.getIntInterval(expr)
      NumericInterval(scala.math.max(0,l), u)

    override def valToSize(v: Value): Size = v match
      case Num(Int32(v)) => v.asNumExpr
      case TopValue => constant(ApronExpr.topInterval, I32Type)
      case _ => f.fail(TypeError, s"Expected i32 but got $this")

    override def sizeToVal(sz: Size): Value =
      Num(Int32(NumExpr(sz)))

    override def addOffsetToAddr(newOffset: Int, addr: Addr): Addr = {
      addr match
        case _ if newOffset == 0 =>
          addr
        case AllocationSites(sites, size) =>
          AllocationSites(PowVirtualAddress(sites.iterator.map {
            case VirtualAddress(AddrCtx.Heap(HeapCtx.Alloc(site,initOffset)), n, addrTrans) =>
              apronState.alloc(AddrCtx.Heap(HeapCtx.Alloc(site, initOffset + newOffset)))
            case v@VirtualAddress(AddrCtx.Heap(HeapCtx.Static(0)), n, addrTrans) =>
              v
            case virt => throw new IllegalArgumentException(s"Expected HeapCtx.Alloc, but got ${virt.ctx}")
          }), size)
        case _ =>
          val expr = addr.asNumExpr
          val resAddr = ApronExpr.intAdd[VirtAddr, Type](expr, ApronExpr.lit[VirtAddr, Type](newOffset, expr._type), expr._type)
          NumExpr(apronState.ifThenElse(effectStack)(unsignedOrderingOps.ltUnsigned(NumExpr(resAddr), NumExpr(ApronExpr.lit[VirtAddr, Type](newOffset, expr._type)))) {
            f.fail(MemoryAccessOutOfBounds, s"$addr + $newOffset")
          } {
            addr match
              case NumExpr(ApronExpr.Constant(_,floatSpecials,tpe)) => ApronExpr.Constant(apronState.getInterval(resAddr), floatSpecials, tpe)
              case _ => resAddr
          })
    }

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      val expr = ix.asInt32.asNumExpr
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

    override def makeNullRefV(t: ReferenceType): RefV = Powerset(RefValue.ExternNull)
    override def isNullRef(r: Value): Value = ???
    override def funcInstToFunV(f: FunctionInstance): FunV = ???
    override def funVToFuncInst(f: Powerset[FunctionInstance]): FunctionInstance = ???
    override def funVToRefV(i: FunV): RefV = ???
    override def refVToFunV(r: RefV): FunV = ???
    override def valToRef(v: Value, funcs: Vector[FunctionInstance]): RefV = ???
    override def refToVal(r: RefV): Value = ???
    override def liftBytes(b: Seq[Byte]): Bytes =
      Bytes.StoredBytes(
        value = b.map(x => (Num(Int32(NumExpr(ApronExpr.lit(x.toInt, I32Type)))), 1)).toList,
        byteOrder = Topped.Actual(ByteOrder.LITTLE_ENDIAN)
      )

    def assignFreshTempVar(expr: ApronExpr[VirtAddr, Type]): List[Value] =
      List(apronState.withTempVars(expr._type)((v, _) =>
        apronState.assign(v, expr)
        Num(Int32(NumExpr(ApronExpr.addr(v, expr._type))))
      ))

    def signedMin(numBytes: Int): BigInt = - BigInt(2).pow(8 * numBytes - 1)
    def signedMax(numBytes: Int): BigInt = BigInt(2).pow(8 * numBytes - 1) - 1

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] = hostFunc.name match
      case "proc_exit" =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case "malloc" =>
        args match
          case List(Num(Int32(size))) =>
            val allocSite = domLogger.getDoms(1)
            val virt = apronState.alloc(AddrCtx.Heap(HeapCtx.Alloc(allocSite,0)): AddrCtx)
            List(Num(Int32(AllocationSites(PowVirtualAddress(virt), size.asNumExpr))))
          case _ => f.fail(WasmFailure.TypeError, s"Expected i32 as argument to malloc, but got $args")
      case "free" =>
        args match
          case List(Num(Int32(ptr))) =>
            println(s"free($ptr)")
            List()
          case _ =>
            f.fail(WasmFailure.TypeError, s"Expected i32 as argument to free, but got $args")
      case "__VERIFIER_nondet_bool" =>
        assignFreshTempVar(ApronExpr.interval(0, 1, I32Type))
      case "__VERIFIER_nondet_char" | "__VERIFIER_nondet_uchar" =>
        assignFreshTempVar(ApronExpr.interval(signedMin(1).toInt, signedMax(1).toInt, I32Type))
      case "__VERIFIER_nondet_short" | "__VERIFIER_nondet_ushort" =>
        assignFreshTempVar(ApronExpr.interval(signedMin(2).toInt, signedMax(2).toInt, I32Type))
      case "__VERIFIER_nondet_int" | "__VERIFIER_nondet_long" | "__VERIFIER_nondet_uint" | "__VERIFIER_nondet_ulong" =>
        assignFreshTempVar(ApronExpr.interval(signedMin(4).toInt, signedMax(4).toInt, I32Type))
      case "__VERIFIER_nondet_longlong" | "__VERIFIER_nondet_ulonglong" =>
        assignFreshTempVar(ApronExpr.interval(signedMin(8).toLong, signedMax(8).toLong, I64Type))
      case "__VERIFIER_nondet_float" =>
        assignFreshTempVar(ApronExpr.interval(Float.MinValue, Float.MaxValue, FloatSpecials.Top, F32Type))
      case "__VERIFIER_nondet_double" =>
        assignFreshTempVar(ApronExpr.interval(Double.MinValue, Double.MaxValue, FloatSpecials.Top, F64Type))
      case "__blackhole_int" | "__blackhole_int_p" =>
        args
      case _ =>
        throw new IllegalArgumentException(s"Unknown host function $hostFunc")

  class Instance(val apronManager: apron.Manager, val rootFrameData: FrameData, val rootFrameValues: Iterable[Value], val config: WasmConfig) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    private given Instance = this

    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
    override def jvBytes: WithJoin[Bytes] = implicitly
    override def jvRefV: WithJoin[RefV] = implicitly
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
          case Num(Int32(NumExpr(expr))) => Some(expr)
          case Num(Int32(BoolExpr(_))) => None
          case Num(Int32(AllocationSites(_, _))) => None
          case Num(Int64(expr)) => Some(expr)
          case Num(Float32(expr)) => Some(expr)
          case Num(Float64(expr)) => Some(expr)
          case Value.TopValue => None

      override def makeRelationalExpr(expr: ApronExpr[VirtAddr, Type]): Value =
        expr._type match
          case I32Type => Num(Int32(NumExpr(expr)))
          case I64Type => Num(Int64(expr))
          case F32Type => Num(Float32(expr))
          case F64Type => Num(Float64(expr))

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
        case TopValue => Iterator.empty
        case Num(n) => valueIterator(n)
        case Int32(n) => valueIterator(n)
        case NumExpr(n) => valueIterator(n)
        case BoolExpr(n) => valueIterator(n)
        case AllocationSites(sites, size) => valueIterator(sites) ++ valueIterator(size)
        case Int64(n) => valueIterator(n)
        case Float32(n) => valueIterator(n)
        case Float64(n) => valueIterator(n)
        case virts: PowVirtAddr => virts.iterator
        case expr: ApronExpr[VirtAddr,Type] @unchecked => expr.addrs.iterator
        case cons: ApronCons[VirtAddr,Type] @unchecked => cons.addrs.iterator
        case bool: ApronBool[VirtAddr,Type] @unchecked => bool.addrs.iterator
        case excV: ExcV =>
          for(listVals <- excV.values.iterator;
              value <- listVals.iterator;
              addr <- valueIterator(value))
            yield(addr)
        case physAddr: PhysAddr => Iterator.empty
        case _ =>
          throw IllegalArgumentException("Unknown Value "+value)

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

    val memory: AlignedMemory[MemoryAddr, HeapCtx, Addr, Value, ApronExpr[VirtAddr, Type]] = new AlignedMemory[MemoryAddr, HeapCtx, Addr, Value, ApronExpr[VirtAddr, Type]](
      Bytes.ReadBytes(Topped.Top, Topped.Actual(ByteOrder.LITTLE_ENDIAN)),
      heapAlloc(rootFrameData)
    )

    val globals: RelationalSymbolTable[Unit, GlobalAddr, Value, AddrCtx, Type] = new RelationalSymbolTable(new AAllocatorFromContext(
        (key: Unit, sym: GlobalAddr) =>
          AddrCtx.Global(sym.addr)
    ))

    val tables: IntervalSymbolTable[Value, TableAddr, Index, RefV, Size]  = new IntervalSymbolTable[Value, TableAddr, Index, RefV, Size]

    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    def getInterval(v: Value): Value =
      v match
        case Num(Int32(i32)) =>
          val expr = i32.asNumExpr
          Num(Int32(
            NumExpr(ApronExpr.constant(
                apronState.getFloatInterval(expr).meet(
                  sturdy.apron.FloatInterval(MpqScalar(Int.MinValue), MpqScalar(Int.MaxValue), FloatSpecials.Bottom)
                ),
                expr._type
              )
            )
          ))
        case Num(Int64(expr)) =>
          Num(Int64(
            ApronExpr.constant(
              apronState.getFloatInterval(expr).meet(
                sturdy.apron.FloatInterval(MpqScalar(BigInteger.valueOf(Long.MinValue)), MpqScalar(BigInteger.valueOf(Long.MaxValue)), FloatSpecials.Bottom)
              ),
              expr._type
            )
          ))
        case Num(Float32(expr)) =>
          Num(Float32(ApronExpr.constant(
            apronState.getFloatInterval(expr).meet(
              sturdy.apron.FloatInterval(Float.MinValue, Float.MaxValue, FloatSpecials.Top)
            ), expr._type)))
        case Num(Float64(expr)) =>
          Num(Float64(ApronExpr.constant(
            apronState.getFloatInterval(expr).meet(
              sturdy.apron.FloatInterval(Double.MinValue, Double.MaxValue, FloatSpecials.Top)
            ), expr._type)))
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

      override def dummyValue: Value = Num(Int32(NumExpr(ApronExpr.constant(Interval(0, 0), I32Type))))
      
      def getInfo(value: Value): Interval = value match
        case Value.TopValue => Interval(Double.NegativeInfinity, Double.PositiveInfinity)
        case Num(Int32(NumExpr(v))) => apronState.getInterval(v)
        case Num(Int32(BoolExpr(v))) =>
          apronState.getBoolean(v) match
            case Topped.Actual(true)  => Interval(1,1)
            case Topped.Actual(false) => Interval(0,0)
            case Topped.Top           => Interval(0,1)
        case Num(Int64(v)) => apronState.getInterval(v)
        case Num(Float32(v)) => apronState.getInterval(v)
        case Num(Float64(v)) => apronState.getInterval(v)


      def get: Map[InstLoc, List[Interval]] = instructionInfo.filter(_._2.forall (
        iv => iv.inf.isEqual(iv.sup)
      ))

      def grouped: Map[String, Map[InstLoc, List[Interval]]] =
        get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

      def groupedCount: Map[String, Int] =
        get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap


    override def newEffectStack: EffectStack =
      lazy val allEffects = RecencyClosure(recencyStore, EffectList(stack, memory, globals, tables, callFrame, except, failure))
      lazy val inEffectsFunction = RecencyClosure(recencyStore, EffectList(memory, globals, tables, callFrame))
      lazy val inEffectsEval = RecencyClosure(recencyStore, EffectList(stack, memory, globals, tables, callFrame))
      lazy val outEffectsFunction = RecencyClosure(recencyStore, EffectList(stack, memory, globals, tables, failure))
      lazy val outEffectsEval = RecencyClosure(recencyStore, EffectList(stack, memory, globals, tables, callFrame, except))

      new EffectStack(allEffects,
        {
          case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => inEffectsFunction
          case _: FixIn.Eval => inEffectsEval
        }, {
          case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => outEffectsFunction
          case _: FixIn.Eval => outEffectsEval
        }
      )

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, RefV, WithJoin] = implicitly

    val observedConfig = config.withObservers(Seq(this.triggerControlEvent))

    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[Value]] = new fix.ContextualFixpoint[FixIn, FixOut[Value]] {
      override type Ctx = observedConfig.ctx.Ctx
      val (contextPreparation, sensitivity) = observedConfig.ctx.make[Value]
      import observedConfig.ctx.finiteCtx
      override protected def contextFree = phi =>
        fix.checkThreadInterrupted(fix.log(controlEventLogger(Instance.this, effectStack, except), contextPreparation(phi)))
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = observedConfig.fix.get
      addContextFreeLogger(domLogger)
      addContextFreeLogger(new FunctionCallLogger)
    }

    override def toString: String = s"constant $config"
