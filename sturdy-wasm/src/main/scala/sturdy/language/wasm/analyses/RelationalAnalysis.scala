package sturdy.language.wasm.analyses

import apron.*
import sturdy.apron.{*, given}
import sturdy.apron.ApronExpr.*
import sturdy.control.{ControlEvent, ControlObservable, FixpointControlEvent, RecordingControlObserver}
import sturdy.data.{*, given}
import sturdy.effect.{EffectList, EffectStack, TrySturdy, bytememory}
import sturdy.effect.bytememory.{*, given}
import sturdy.effect.callframe.{ConcreteCallFrame, DecidableCallFrame, JoinableDecidableCallFrame, MutableCallFrame, RelationalCallFrame}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{DecidableOperandStack, JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.{ConstantIntervalMappedSymbolTable, ConstantSymbolTable, DecidableSymbolTable, FiniteSymbolTableWithDrop, IntervalMappedSymbolTable, IntervalSymbolTable, JoinableDecidableSymbolTable, RelationalSymbolTable, SymbolTable, SymbolTableWithDrop}
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
import swam.{FuncType, OpCode, ReferenceType, ValType, syntax}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.{IndexedSeqView, mutable}
import WasmFailure.*
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.stack.RelationalStack
import sturdy.effect.store.{AStoreThreaded, RecencyClosure, RecencyStore, RelationalStore}
import sturdy.effect.bytememory.Bytes as BTS
import sturdy.fix.{DomLogger, Logger}

import java.math.BigInteger
import scala.collection.immutable.List
import scala.math
import scala.util.boundary
import scala.util.boundary.break

object RelationalAnalysis extends Interpreter, RelationalTypes, RelationalAddresses, RelationalValues, RelationalMemory, RelationalConvert, ExceptionByTarget, Control:
  final type J[A] = WithJoin[A]
  final type Index = NumericInterval[Int]

  import Value.*
  import NumValue.*
  import Type.*
  import RelI32.*
  import RelationalInfo.{*,given}

  val topSize: Top[Size] = new Top[Size]:
    override def top: Size = constant(ApronExpr.topInterval, I32Type)

  override def topV128: V128 = ???

  given RelationalSpecialWasmOperations
      (using domLogger: DomLogger[FixIn],
             f: Failure,
             effectStack: EffectStack,
             apronState: ApronState[VirtAddr, Type],
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

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      val expr = ix.asInt32.asNumExpr
      val elems = vec.indices.filter(i =>
        apronState.assert(ApronCons.eq(expr, ApronExpr.lit(i, I32Type))) != Topped.Actual(false)
      ).map(vec(_))
      if (elems.isEmpty) {
        // no elems in range
        JOptionPowerset.None()
      } else if (elems.size < vec.size) {
        // all indices in range
        JOptionPowerset.Some(Powerset(elems.toSet))
      } else {
        // some indices in range, but not all
        JOptionPowerset.NoneSome(Powerset(elems.toSet))
      }

    override def isNullRef(r: Value): Value =
      r match
        case Value.Ref(f) =>
          if(f.set.contains(ExternReference.Null))
            if(f.size == 1)
              makeBool(ApronBool.Constant(Topped.Actual(true)))
            else
              makeBool(ApronBool.Constant(Topped.Top))
          else
            makeBool(ApronBool.Constant(Topped.Actual(false)))
        case Value.TopValue => makeBool(ApronBool.Constant(Topped.Top))
        case _ => makeBool(ApronBool.Constant(Topped.Actual(false)))

    override def funcInstToRefV(f: FunctionInstance): RefV = Powerset[FunctionInstance | ExternReference](f)
    override def valToRef(v: Value, funcs: Vector[FunctionInstance]): RefV =
      v match
        case Value.Ref(f) => f
        case Value.TopValue => Powerset[FunctionInstance | ExternReference](funcs *) ++ Powerset[FunctionInstance | ExternReference](ExternReference.ExternReference, ExternReference.Null)
        case _ => f.fail(TypeError, s"Expected reference, but got $v")

    override def refToVal(r: RefV): Value = Ref(r)
    override def liftBytes(b: Seq[Byte]): Bytes =
      Bytes.StoredBytes(
        value = b.map(x =>
          (Num(Int32(NumExpr(ApronExpr.lit(byteToUnsignedInt(x), I8Type)))), 1)
        ).toList,
        byteOrder = Topped.Actual(ByteOrder.LITTLE_ENDIAN)
      )
    private def byteToUnsignedInt(b: Byte): Int = b & 0xff

  class Instance(val apronManager: apron.Manager, val rootFrameData: FrameData, val rootFrameValues: Iterable[Value], val config: WasmConfig) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    private given Instance = this

    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
    override def jvBytes: WithJoin[Bytes] = implicitly
    override def jvRefV: WithJoin[RefV] = implicitly
    override def jvElem: WithJoin[Elem] = implicitly
    //    override def widenState: Widen[State] = implicitly

    var exprConverter: ApronExprConverter[AddrCtx, Type, Value] = null
    var apronState: ApronRecencyState[AddrCtx, Type, Value] = null
    given Lazy[ApronRecencyState[AddrCtx, Type, Value]] = Lazy(apronState)
    given Lazy[ApronExprConverter[AddrCtx, Type, Value]] = Lazy(exprConverter)
    given Join[ApronExpr[VirtAddr, Type]] = JoinApronExpr[VirtAddr, Type]
    given Widen[ApronExpr[VirtAddr, Type]] = WidenApronExpr[VirtAddr, Type]

    given StatelessRelationalExpr[Value, VirtAddr, Type] with
      override def getRelationalExpr(v: Value): Option[ApronExpr[VirtAddr, Type]] =
        v match
          case Num(Int32(NumExpr(expr))) => Some(expr)
          case Num(Int32(BoolExpr(_))) => None
          case Num(Int32(AllocationSites(_, _))) => None
          case Num(Int64(expr)) => Some(expr)
          case Num(Float32(expr)) => Some(expr)
          case Num(Float64(expr)) => Some(expr)
          case Value.Vec(_) => None
          case Value.Ref(_) => None
          case Value.TopValue => None

      override def makeRelationalExpr(expr: ApronExpr[VirtAddr, Type]): Value =
        expr._type match
          case I32Type => Num(Int32(NumExpr(expr)))
          case I64Type => Num(Int64(expr))
          case F32Type => Num(Float32(expr))
          case F64Type => Num(Float64(expr))
          case I8Type  => throw IllegalArgumentException("I8 type only allowed in memory")

      override def getMetaData(v: Value): Option[(FloatSpecials, Type)] =
        v match
          case Num(_: Int32) => Some((FloatSpecials.Bottom, I32Type))
          case Num(_: Int64) => Some((FloatSpecials.Bottom, I64Type))
          case Num(Float32(expr)) => Some((expr.floatSpecials, expr._type))
          case Num(Float64(expr)) => Some((expr.floatSpecials, expr._type))
          case Value.Vec(_) => None
          case Value.Ref(_) => None
          case Value.TopValue => None

    given domLogger: DomLogger[FixIn] = new DomLogger

    val relationalStore: RelationalStore[AddrCtx, Type, PowPhysAddr, Value] = new RelationalStore[AddrCtx, Type, PowPhysAddr, Value](
      Map(),
      manager = apronManager,
      initialAbs1 = apron.Abstract1(apronManager, new apron.Environment()),
      initialNonRelationalStore = Map()
    )
    import relationalStore.given
    val recencyStore: RecencyStore[AddrCtx, PowVirtAddr, Value] = new RecencyStore(relationalStore)
    exprConverter = ApronExprConverter(recencyStore, relationalStore)
    apronState =
      if(config.relational)
        new ApronRecencyState[AddrCtx, Type, Value](tempRelationalAlloc(rootFrameData), recencyStore, relationalStore)
      else
        new NonRelationalApronState[AddrCtx, Type, Value](tempRelationalAlloc(rootFrameData), recencyStore, relationalStore)
    given ApronRecencyState[AddrCtx, Type, Value] = apronState

    val callFrame: MutableCallFrame[FrameData, Int, Value, InstLoc, MayJoin.NoJoin] =
      if(config.relational)
        new RelationalCallFrame(
          initData = rootFrameData,
          initVars = Iterable.empty,
          localVariableAllocator = localAlloc(ssa = config.localSSA, rootFrameData),
          apronState
        )
      else
        new JoinableDecidableCallFrame(
          initData = rootFrameData,
          initVars = Iterable.empty
        )

    val stack: DecidableOperandStack[Value] =
      if(config.relational)
        new RelationalStack(stackAlloc[Int, Value, InstLoc, NoJoin](rootFrameData, callFrame))
      else
        JoinableDecidableOperandStack[Value]

    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    val memory: AlignedMemory[MemoryAddr, HeapCtx, Addr, Value, ApronExpr[VirtAddr, Type]] = new AlignedMemory[MemoryAddr, HeapCtx, Addr, Value, ApronExpr[VirtAddr, Type]](
      Bytes.ReadBytes(Topped.Top, Topped.Actual(ByteOrder.LITTLE_ENDIAN)),
      heapAlloc(rootFrameData),
      moveMemLoc(rootFrameData)
    )

    val globals: DecidableSymbolTable[Unit, GlobalAddr, Value] =
      if(config.relational)
        new RelationalSymbolTable(new AAllocatorFromContext(
            (key: Unit, sym: GlobalAddr) =>
              module.exportedName(ExternalValue.Global(sym.addr)).map(AddrCtx.Global(_)).getOrElse(AddrCtx.Global(sym.addr))
        ))
      else
        JoinableDecidableSymbolTable[Unit, GlobalAddr, Value]()

    val elems: SymbolTableWithDrop[Unit, ElemAddr, Elem, J] = FiniteSymbolTableWithDrop[Unit, ElemAddr, Elem](Seq.empty)(using CombineEquiSeq, CombineEquiSeq, implicitly, implicitly)
    val tables: IntervalSymbolTable[TableAddr, Index, RefV, Size]  = new IntervalSymbolTable[TableAddr, Index, RefV, Size]
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept

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

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, RefV, WithJoin] =
      if (config.relational)
        ValueWasmOps
      else
        ValueWasmOps(using
          i32Ops = new NonRelationalI32IntegerOps,
          i64Ops = NonRelationalIntegerOps[Long, VirtAddr, Type],
          f32Ops = NonRelationalFloatOps[Float, VirtAddr, Type],
          f64Ops = NonRelationalFloatOps[Double, VirtAddr, Type],
          i32EqOps = NonRelationalEqOps[I32, VirtAddr, Type],
          i64EqOps = NonRelationalEqOps[I64, VirtAddr, Type],
          f32EqOps = NonRelationalEqOps[F32, VirtAddr, Type],
          f64EqOps = NonRelationalEqOps[F64, VirtAddr, Type],
          i32CompareOps = NonRelationalOrderingOps[I32, VirtAddr, Type],
          i64CompareOps = NonRelationalOrderingOps[I64, VirtAddr, Type],
          f32CompareOps = NonRelationalOrderingOps[F32, VirtAddr, Type],
          f64CompareOps = NonRelationalOrderingOps[F64, VirtAddr, Type],
          i32UnsignedCompareOps = NonRelationalUnsignedOrderingOps[I32, VirtAddr, Type],
          i64UnsignedCompareOps = NonRelationalUnsignedOrderingOps[I64, VirtAddr, Type],
          convertI32I64 = NonRelationalConvert[Int, Long, I32, VirtAddr, Type, BitSign],
          convertI32F32 = NonRelationalConvert[Int, Float, I32, VirtAddr, Type, BitSign],
          convertI32F64 = NonRelationalConvert[Int, Double, I32, VirtAddr, Type, BitSign],
          convertI64I32 = NonRelationalI32Convert[Long, I64, NilCC.type],
          convertI64F32 = NonRelationalConvert[Long, Float, I64, VirtAddr, Type, BitSign],
          convertI64F64 = NonRelationalConvert[Long, Double, I64, VirtAddr, Type, BitSign],
          convertF32I32 = NonRelationalI32Convert[Float, F32, Overflow && BitSign],
          convertF32I64 = NonRelationalConvert[Float, Long, F32, VirtAddr, Type, Overflow && BitSign],
          convertF32F64 = NonRelationalConvert[Float, Double, F32, VirtAddr, Type, NilCC.type],
          convertF64I32 = NonRelationalI32Convert[Double, F64, Overflow && BitSign],
          convertF64I64 = NonRelationalConvert[Double, Long, F64, VirtAddr, Type, Overflow && BitSign],
          convertF64F32 = NonRelationalConvert[Double, Float, F32, VirtAddr, Type, NilCC.type]
        )

    private def addressIterator: Iterator[VirtAddr] =
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
        case Ref(_) => Iterator.empty
        case Vec(_) => Iterator.empty
        case virts: PowVirtAddr @unchecked => virts.iterator
        case expr: ApronExpr[VirtAddr, Type] @unchecked => expr.addrs.iterator
        case cons: ApronCons[VirtAddr, Type] @unchecked => cons.addrs.iterator
        case bool: ApronBool[VirtAddr, Type] @unchecked => bool.addrs.iterator
        case excV: ExcV @unchecked =>
          for ((ops, cond) <- excV.values.iterator;
               addr <- valueIterator(ops) ++ valueIterator(cond))
          yield addr
        case physAddr: PhysAddr @unchecked => Iterator.empty
        case _ =>
          throw IllegalArgumentException("Unknown Value " + value)

      effectStack.addressIterator[VirtAddr](valueIterator)

    def garbageCollect(): Unit =
      val alive = PowVirtualAddress(this.addressIterator)
      val dead = relationalStore.deadPhysicalAddresses(alive, relationalStore.internalState)
      val stateBefore = effectStack.getState
      recencyStore.collectGarbage(alive)
      val stateAfter = effectStack.getState
      println(s"Alive: $alive")
      println(s"Dead: $dead")
      println(s"State Before: $stateBefore")
      println(s"State After: $stateAfter")

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
        case Value.Vec(_) | Value.Ref(_) | Value.TopValue => v

//    extension(expr: ApronExpr[VirtAddr,Type])
//      def isConstant: Boolean =
//        val iv = apronState.getInterval(expr)
//        iv.inf().isEqual(iv.sup())

    private class FunctionCallLogger extends Logger[FixIn, FixOut[Value]]:
      val stack: mutable.Stack[(FixIn, IndexedSeq[(Value,Value)], effectStack.State)] = mutable.Stack.empty

      override def enter(dom: FixIn): Unit =
        dom match
          case FixIn.EnterWasmFunction(id,_,FuncType(params,_)) =>
            enterFunction(dom, id, params)
          case FixIn.EnterHostFunction(id, HostFunction(_, FuncType(params, _))) =>
            enterFunction(dom, id, params)
          case _ => ()

      private def enterFunction(dom: FixIn, id: FuncId, params: Vector[ValType]): Unit =
        val args = params.indices.map {
          i =>
            val v = callFrame.getLocal(i).get
            (v, getInterval(v))
        }

        val state = effectStack.getState
        print("  ".repeat(stack.size))
        println(s"CALL ${id}(${args.mkString(",")}) @ ${state.hashCode()}")
        stack.push((dom, args, state))


      override def exit(dom: FixIn, codom: TrySturdy[FixOut[Value]]): Unit =
        dom match
          case FixIn.EnterWasmFunction(id, _, _) => exitFunction(id, codom)
          case FixIn.EnterHostFunction(id, _) => exitFunction(id, codom)
          case _ => {}

      private def exitFunction(id: FuncId, codom: TrySturdy[FixOut[Value]]) =
        val (_, args, inState) = stack.pop
        val result =
          codom.map {
            case FixOut.ExitWasmFunction(returns) => FixOut.ExitWasmFunction(returns.map(v => (v, getInterval(v))))
            case FixOut.ExitHostFunction(returns) => FixOut.ExitHostFunction(returns.map(v => (v, getInterval(v))))
            case FixOut.Eval() => FixOut.Eval()
            case FixOut.MostGeneralClient() => FixOut.MostGeneralClient()
          }
        val outState = effectStack.getState
        print("  ".repeat(stack.size))
        println(s"RETURN $id(${args.mkString(",")}) @ ${inState.hashCode} = $result @ ${outState.hashCode()}")


    def constrainedInstructionsLogger: ConstrainedInstructionsLogger =
      val intervalLogger = new ConstrainedInstructionsLogger
      this.fixpoint.addContextFreeLogger(intervalLogger)
      intervalLogger

    class ConstrainedInstructionsLogger extends InstructionResultLogger[Info, Value](stack):
      override def boolValue(v: Value): Value = booleanToVal(asBoolean(v))

      def getInfo(value: Value): Info = Profiler.disableMeasurement {
        value match
          case Num(Int32(v32)) => v32 match
            case NumExpr(v) => Info.Numeric(apronState.getFloatInterval(v).meet(I32Type.signedTop), I32Type, isConstrained(v))
            case BoolExpr(v) => Info.Boolean(apronState.assert(v), isConstrained(v))
            case AllocationSites(ref, size) => Info.AllocationSites(ref.mapAddr(sites => new Powerset(sites.physicalAddresses.asInstanceOf)), apronState.getInterval(size), isConstrained(size))
          case Num(Int64(v)) => Info.Numeric(apronState.getFloatInterval(v).meet(I64Type.signedTop), I64Type, isConstrained(v))
          case Num(Float32(v)) => Info.Numeric(apronState.getFloatInterval(v).meet(F32Type.signedTop), F32Type, isConstrained(v))
          case Num(Float64(v)) => Info.Numeric(apronState.getFloatInterval(v).meet(F64Type.signedTop), F64Type, isConstrained(v))
          case Value.Ref(_) | Value.Vec(_) | Value.TopValue => Info.Top
      }

      private def isConstrained(v: ApronExpr[VirtAddr, Type] | ApronBool[VirtAddr, Type]): IsConstrained =
        if (v match {
          case expr: ApronExpr[VirtAddr, Type] => apronState.isUnconstrained(expr);
          case bool: ApronBool[VirtAddr, Type] => apronState.isUnconstrained(bool)
        })
          IsConstrained.Unconstrained
        else
          IsConstrained.Constrained

      def getAllInstructionInfos: Map[(InstLoc,syntax.Inst), List[Info]] =
        for((loc,info) <- instructionInfo)
          yield ((loc,instructions(loc)), info)

      def getConstrained: Map[InstLoc, List[Info]] =
        instructionInfo.filter((_, infos) => infos.forall(_.isConstrained))

      def grouped: Map[String, Map[InstLoc, List[Info]]] =
        getConstrained.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

      def groupedCount: Map[String, Int] =
        getConstrained.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] = hostFunc.name match
      case "proc_exit" =>
        val exitCode = args.head
        failure.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case "memcpy" =>
        args match
          case List(dst, src, len) =>
            memory.copy(MemoryAddr(0), wasmOps.specialOps.valToAddr(dst), wasmOps.specialOps.valToAddr(src), wasmOps.specialOps.valToSize(len))
            List(dst)
          case _ => failure.fail(WasmFailure.TypeError, s"Expected (i32,i32,i32) as argument to $hostFunc, but got $args")
      case "memmove" =>
        args match
          case List(dst, src, len) =>
            val lenSize = wasmOps.specialOps.valToSize(len)
            val (l,h) = apronState.getIntInterval(lenSize)
            if(l == h) {
              val bytes = memory.read(MemoryAddr(0), wasmOps.specialOps.valToAddr(src), l).getOrElse(failure.fail(WasmFailure.MemoryAccessOutOfBounds, s"Cannot access memory at $src"))
              bytes match
                case Bytes.ReadBytes(Topped.Actual(bs), byteOrder) => memory.write(MemoryAddr(0), wasmOps.specialOps.valToAddr(dst), Bytes.StoredBytes(bs, byteOrder))
                case _ => memory.fill(MemoryAddr(0), wasmOps.specialOps.valToAddr(src), lenSize, Bytes.StoredBytes(List((Num(Int32(NumExpr(ApronExpr.top(I8Type)))),1)), Topped.Actual(ByteOrder.LITTLE_ENDIAN)))
            } else {
              memory.fill(MemoryAddr(0), wasmOps.specialOps.valToAddr(src), lenSize, Bytes.StoredBytes(List((Num(Int32(NumExpr(ApronExpr.top(I8Type)))),4)), Topped.Actual(ByteOrder.LITTLE_ENDIAN)))
            }
            List(dst)
          case _ => failure.fail(WasmFailure.TypeError, s"Expected (i32,i32,i32) as argument to $hostFunc, but got $args")
      case "malloc" =>
        args match
          case List(Num(Int32(size))) =>
            val allocSite = domLogger.getDoms(1)
            val virt = apronState.alloc(AddrCtx.Heap(HeapCtx.Alloc(allocSite,0)): AddrCtx)
            List(Num(Int32(AllocationSites(AbstractReference.Addr(PowVirtualAddress(virt), definitelyManaged = false), size.asNumExpr))))
          case _ => failure.fail(WasmFailure.TypeError, s"Expected i32 as argument to malloc, but got $args")
      case "free" =>
        args match
          case List(Num(Int32(ptr))) =>
            println(s"free($ptr)")
            List()
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32 as argument to free, but got $args")
      case "pow" =>
        args match
          case List(base, exponent) =>
            List(wasmOps.f64ops.pow(base, exponent))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected f64,f64 as arguments to pow, but got $args")
      case "exp2" =>
        args match
          case List(exponent) =>
            List(wasmOps.f64ops.pow(wasmOps.f64ops.floatingLit(2), exponent))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected f64 as arguments to exp2, but got $args")
      case "fwrite" =>
        args match
          case List(data@Num(Int32(_)), Num(Int32(size)), c@Num(Int32(count)), Num(Int32(stream))) =>
            val (l,h) = apronState.getIntInterval(count.asNumExpr)
            if(l == h) {
              val bytes = memory.read(MemoryAddr(0), wasmOps.specialOps.valToAddr(data), l)
              println(s"fwrite($data, $size, $count, $stream) = $bytes")
            } else {
              println(s"fwrite($data, $size, $count, $stream)")
            }
            List(c)
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32,i32,i32,i32 as arguments to fwrite, but got $args")
      case "strlen" =>
        args match
          case List(ptr@Num(Int32(_))) => boundary:
            val topLen = List(Num(Int32(NumExpr(ApronExpr.top(I32Type)))))
            val stringSeparator = Interval('\u0000'.toByte, '\u0000'.toByte)
            val ptrAddr = wasmOps.specialOps.valToAddr(ptr)
            var len = 0
            while {
              val optBytes = memory.read(MemoryAddr(0), wasmOps.addressOffset.addOffsetToAddr(len, ptrAddr), 1).asInstanceOf[JOptionA[BTS[Value]]]
              len += 1
              optBytes match
                case JOptionA.Some(BTS.ReadBytes(Topped.Actual(List((Num(Int32(v)),1))), _)) =>
                  val iv = apronState.getInterval(v.asNumExpr)
                  if(iv.isEqual(stringSeparator))
                    break(List(Num(Int32(NumExpr(ApronExpr.lit(len, I32Type))))))
                  else if(stringSeparator.isLeq(iv))
                    break(topLen)
                  else /* if(!stringSeparator.isLeq(iv)) */
                    true
                case _ => break(topLen)
            } do ()
            throw new Exception("Unreachable")
          case _ => failure.fail(WasmFailure.TypeError, s"Expected i32 as argument to malloc, but got $args")
      case "assert" =>
        args match
          case List(v@Num(Int32(_))) =>
            given BooleanBranching[Value,Unit] = wasmOps.branchOpsUnit
            assert(v.asInstanceOf[Value], domLogger.currentDom)
            List()
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected f64,f64 as arguments to pow, but got $args")
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

    private def assignFreshTempVar(expr: ApronExpr[VirtAddr, Type]): List[Value] =
      List(apronState.withTempVars(expr._type)((v, _) =>
        apronState.assign(v, expr)
        Num(Int32(NumExpr(ApronExpr.addr(v, expr._type))))
      ))

    private def signedMin(numBytes: Int): BigInt = -BigInt(2).pow(8 * numBytes - 1)

    private def signedMax(numBytes: Int): BigInt = BigInt(2).pow(8 * numBytes - 1) - 1

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
