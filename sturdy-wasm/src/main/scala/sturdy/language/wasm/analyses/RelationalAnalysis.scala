package sturdy.language.wasm.analyses

import apron.*
import sturdy.apron.{*, given}
import sturdy.apron.ApronExpr.*
import sturdy.control.{ControlEvent, ControlObservable, FixpointControlEvent, RecordingControlObserver}
import sturdy.data.{*, given}
import sturdy.effect.{EffectList, EffectStack, TrySturdy, bytememory}
import sturdy.effect.bytememory.{Bytes as BTS, *, given}
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
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst, *}
import swam.{FuncType, GlobalType, NumType, OpCode, ReferenceType, ValType, syntax}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.{IndexedSeqView, mutable}
import WasmFailure.*
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.stack.RelationalStack
import sturdy.effect.store.{AStoreThreaded, RecencyClosure, RecencyStore, RelationalStore}
import sturdy.fix.{DomLogger, Logger}

import java.math.BigInteger
import scala.collection.immutable.{List, SortedMap}
import scala.math
import scala.util.boundary
import scala.util.boundary.break

object RelationalAnalysis extends Interpreter, RelationalTypes, RelationalAddresses, RelationalValues, RelationalMemory, RelationalConvert, ExceptionByTarget, Control:
  final type J[A] = WithJoin[A]
  final type Index = NumericInterval[Int]

  import Value.*
  import NumValue.*
  import Type.*
  import RelI32.{NumExpr, BoolExpr, StackAddr, HeapAddr}
  import RelationalInfo.{*,given}

  val topSize: Top[Size] = new Top[Size]:
    override def top: Size = constant(ApronExpr.topInterval, I32Type)

  override def topV128: V128 = Topped.Top

  given RelationalSpecialWasmOperations
      (using domLogger: DomLogger[FixIn],
             f: Failure,
             effectStack: EffectStack,
             apronState: ApronState[VirtAddr, Type],
             joinExpr: Join[ApronExpr[VirtAddr, Type]]
      ): SpecialWasmOperations[Value, Addr, Bytes, Size, Index, FunV, RefV, WithJoin] with

    override def valToAddr(v: Value): Addr = v match
      case Num(Int32(n:NumExpr)) => n
      case Num(Int32(b:BoolExpr)) => NumExpr(b.asNumExpr)
      case Num(Int32(g:RelI32.GlobalAddr)) => g
      case Num(Int32(s:StackAddr)) => s
      case Num(Int32(a:HeapAddr)) => a
      case TopValue => NumExpr(constant(ApronExpr.topInterval, I32Type))
      case _ => f.fail(TypeError, s"Expected i32 but got $v")

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
      BTS.StoredBytes(
        value = b.map(x =>
          (Num(Int32(NumExpr(ApronExpr.lit(byteToUnsignedInt(x), I8Type)))), 1)
        ).toList,
        byteOrder = Topped.Actual(ByteOrder.LITTLE_ENDIAN)
      )
    private def byteToUnsignedInt(b: Byte): Int = b & 0xff
    
  class Instance(val apronManager: apron.Manager, val rootFrameData: FrameData, val rootFrameValues: Iterable[Value], val config: WasmConfig) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    private given Instance = this
    given Manager = apronManager

    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
    override def jvBytes: WithJoin[Bytes] = implicitly
    override def jvRefV: WithJoin[RefV] = implicitly
    override def jvElem: WithJoin[Elem] = implicitly
    //    override def widenState: Widen[State] = implicitly

    private var exprConverter: ApronExprConverter[AddrCtx, Type, Value] = null
    var apronState: ApronRecencyState[AddrCtx, Type, Value] = null
    given Lazy[ApronRecencyState[AddrCtx, Type, Value]] = Lazy(apronState)
    given Lazy[ApronExprConverter[AddrCtx, Type, Value]] = Lazy(exprConverter)
    given Join[ApronExpr[VirtAddr, Type]] = JoinApronExpr[VirtAddr, Type]
    given Widen[ApronExpr[VirtAddr, Type]] = WidenApronExpr[VirtAddr, Type]

    given StatelessRelationalExpr[Value, VirtAddr, Type] with
      override def getRelationalExpr(v: Value): Option[ApronExpr[VirtAddr, Type]] =
        v match
          case Num(Int32(NumExpr(expr))) => Some(expr)
          case Num(Int32(_: BoolExpr)) => None
          case Num(Int32(_: HeapAddr)) => None
          case Num(Int32(_: StackAddr)) => None
          case Num(Int32(_: RelI32.GlobalAddr)) => None
          case Num(Int64(expr)) => Some(expr)
          case Num(Float32(expr)) => Some(expr)
          case Num(Float64(expr)) => Some(expr)
          case _: Value.Vec => None
          case _: Value.Ref => None
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
          case _: Value.Vec => None
          case _: Value.Ref => None
          case Value.TopValue => None

    given domLogger: DomLogger[FixIn] = new DomLogger

    val relationalStore: RelationalStore[AddrCtx, Type, PowPhysAddr, Value] = new RelationalStore[AddrCtx, Type, PowPhysAddr, Value](
      Map(),
      initialAbs1 = apron.Abstract1(apronManager, new apron.Environment()),
      initialNonRelationalStore = Map()
    )
    import relationalStore.given
    private val recencyStore: RecencyStore[AddrCtx, PowVirtAddr, Value] = new RecencyStore(relationalStore)
    exprConverter = ApronExprConverter(recencyStore, relationalStore)
    apronState =
      if(config.relational)
        new ApronRecencyState[AddrCtx, Type, Value](tempRelationalAlloc(rootFrameData), combineExprAlloc(rootFrameData), recencyStore, relationalStore)
      else
        new NonRelationalApronState[AddrCtx, Type, Value](tempRelationalAlloc(rootFrameData), combineExprAlloc(rootFrameData), recencyStore, relationalStore)
    given ApronRecencyState[AddrCtx, Type, Value] = apronState

    val callFrame: MutableCallFrame[FrameData, Int, Value, InstLoc, MayJoin.NoJoin] =
      if(config.relational)
        new RelationalCallFrame(
          initData = rootFrameData,
          initVars = Iterable.empty,
          localVariableAllocator = localAlloc(ssa = config.localSSA, rootFrameData),
          apronState,
          ssa = config.localSSA
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

    val globals: DecidableSymbolTable[Unit, GlobalAddr, Value] =
      if(config.relational)
        new RelationalSymbolTable(new AAllocatorFromContext(
            (key: Unit, sym: GlobalAddr) =>
              module.exportedName(ExternalValue.Global(sym.addr)).map(AddrCtx.Global(_)).getOrElse(AddrCtx.Global(sym.addr))
        ))
      else
        JoinableDecidableSymbolTable[Unit, GlobalAddr, Value]()
    given DecidableSymbolTable[Unit, GlobalAddr, Value] = globals

    given heapAlloc: HeapAlloc = new HeapAlloc(rootFrameData)
    val memory: AlignedMemory[MemoryAddr, ByteMemoryCtx, Addr, Value, ApronExpr[VirtAddr, Type]] = new AlignedMemory[MemoryAddr, ByteMemoryCtx, Addr, Value, ApronExpr[VirtAddr, Type]](
      BTS.ReadBytes(Topped.Top, Topped.Actual(ByteOrder.LITTLE_ENDIAN)),
      heapAlloc
    )

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

    given overflowHandling: OverflowHandling = {
      if(config.soundOverflowHandling)
        OverflowHandling.WrapAround
      else
        OverflowHandling.Fail
    }

    given I32IntegerOps = new I32IntegerOps(
      rootFrameData = rootFrameData,
      globals = optionStaticMemoryLayout.map(_.globalRanges).getOrElse(Vector()),
      stackRange = optionStaticMemoryLayout.map(_.stackRange).getOrElse(ApronExpr.bottomInterval))

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

    // Hook to initialize static memory layout after global variables have been instantiated.
    override protected def instantiateGlobals(module: Module, modInst: ModuleInstance, globImports: Vector[GlobalAddr], globImportsTypes: Vector[GlobalType], initLoc: InstLoc)(using Fixed): InstLoc =
      val loc = super.instantiateGlobals(module, modInst: ModuleInstance, globImports, globImportsTypes, initLoc)
      //HERE StaticMemoryLayout can receive DwarfInformation through modInst
      optionStaticMemoryLayout = parseStaticMemoryLayout(using moduleInstance = modInst, globals = globals)
      loc

    var assertions: Map[FixIn, (Bool, Topped[Boolean])] = Map()
    def failedAssertions: Map[FixIn, (Bool, Topped[Boolean])] =
      assertions.filter {
        case (_,(_,Topped.Top)) | (_, (_,Topped.Actual(false))) => true
        case (_,(_,Topped.Actual(true))) => false
      }

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] = hostFunc.name match
      case "proc_exit" =>
        val exitCode = args.head
        failure.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case "memcpy" =>
        args match
          case List(dst, src, len) =>
            val effectiveSrcAddr = wasmOps.addressOffset.addOffsetToAddr(0, wasmOps.specialOps.valToAddr(src))
            memory.copy(
              key = MemoryAddr(0),
              srcAddr = effectiveSrcAddr,
              dstAddr = wasmOps.specialOps.valToAddr(dst),
              byteAmount = wasmOps.specialOps.valToSize(len)
            )
            List(dst)
          case _ => failure.fail(WasmFailure.TypeError, s"Expected (i32,i32,i32) as argument to $hostFunc, but got $args")
      case "memmove" =>
        // There is no difference between memcpy and memmove, since memory.copy does a weak update.
        invokeHostFunction(HostFunction("memcpy", hostFunc.funcType), args)

      case "memset" =>
        args match
          case List(ptr@Num(Int32(_)), value@Num(Int32(_)), size@Num(Int32(_))) =>
            memory.fill(
              MemoryAddr(0),
              wasmOps.specialOps.valToAddr(ptr),
              wasmOps.specialOps.valToSize(size),
              BTS.StoredBytes(List((value,1)), Topped.Actual(ByteOrder.LITTLE_ENDIAN))
            )
            List(ptr)
          case _ => failure.fail(WasmFailure.TypeError, s"Expected (i32,i32,i32) as argument to $hostFunc, but got $args")
      case "memcmp" =>
        args match
          case List(Num(Int32(ptr1)), Num(Int32(ptr2)), Num(Int32(count))) =>
            List(Num(Int32(NumExpr(ApronExpr.top(I32Type)))))
          case _ => failure.fail(WasmFailure.TypeError, s"Expected (i32,i32,i32) as argument to $hostFunc, but got $args")
      case "malloc" =>
        args match
          case List(Num(Int32(size))) =>
            domLogger.getDoms(0) match {
              case FixIn.Eval(_, allocationSite) =>
                val virt = apronState.alloc(AddrCtx.ByteMemory(ByteMemoryCtx.Heap(allocationSite,0)): AddrCtx)
                val heapAddr = HeapAddr(
                  sites = AbstractReference.Addr(PowVirtualAddress(virt), definitelyManaged = false),
                  size = size.asNumExpr,
                  initialOffset = Powerset(),
                  otherOffset = ApronExpr.lit(0, I32Type)
                )
                List(Num(Int32(heapAddr)))
              case dom => throw Error(s"Malloc: Expected FixIn.Eval, but got $dom")
            }
          case _ => failure.fail(WasmFailure.TypeError, s"Expected i32 as argument to $hostFunc, but got $args")
      case "realloc" =>
        args match
          case List(Num(Int32(sourceAddr: HeapAddr)), Num(Int32(size))) =>
            domLogger.getDoms(0) match {
              case FixIn.Eval(_, allocationSite) =>
                val virt = apronState.alloc(AddrCtx.ByteMemory(ByteMemoryCtx.Heap(allocationSite,0)): AddrCtx)
                val reallocedAddr: HeapAddr = HeapAddr(
                  sites = sourceAddr.sites.mapAddr(sites => sites.add(virt)),
                  size = size.asNumExpr,
                  initialOffset = sourceAddr.initialOffset,
                  otherOffset = Join(sourceAddr.otherOffset, ApronExpr.lit(0, I32Type)).get
                )
                memory.copy(MemoryAddr(0), sourceAddr, reallocedAddr: Addr, sourceAddr.size)
                List(Num(Int32(reallocedAddr)))
              case dom => throw Error(s"Malloc: Expected FixIn.Eval, but got $dom")
            }
          case List(ptr@Num(Int32(_)), Num(Int32(size))) =>
            List(ptr)
          case _ => failure.fail(WasmFailure.TypeError, s"Expected i32,i32 as argument to $hostFunc, but got $args")
      case "calloc" =>
        args match
          case List(Num(Int32(num)), sizeVal@Num(Int32(size))) =>
            invokeHostFunction(HostFunction("malloc", FuncType(Vector(NumType.I32), Vector(NumType.I32))), List(Num(Int32(NumExpr(ApronExpr.intMul(num.asNumExpr, size.asNumExpr, I32Type)))))) match
              case List(ptr@Num(Int32(_))) =>
                memory.fill(
                  MemoryAddr(0),
                  wasmOps.specialOps.valToAddr(ptr),
                  wasmOps.specialOps.valToSize(sizeVal),
                  BTS.StoredBytes(List((Num(Int32(NumExpr(ApronExpr.lit(0, I8Type)))),1)), Topped.Actual(ByteOrder.LITTLE_ENDIAN))
                )
                List(ptr)
              case ret => failure.fail(WasmFailure.TypeError, s"Expected i32 as result of malloc, but got $ret")
          case _ => failure.fail(WasmFailure.TypeError, s"Expected i32,i32 as argument to $hostFunc, but got $args")
      case "free" =>
        args match
          case List(Num(Int32(ptr))) =>
            println(s"free($ptr)")
            List()
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32 as argument to $hostFunc, but got $args")
      case "pow" =>
        args match
          case List(base, exponent) =>
            List(wasmOps.f64ops.pow(base, exponent))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected f64,f64 as arguments to $hostFunc, but got $args")
      case "exp2" =>
        args match
          case List(exponent) =>
            List(wasmOps.f64ops.pow(wasmOps.f64ops.floatingLit(2), exponent))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected f64 as arguments to exp2, but got $args")
      case "read" =>
        args match
          case List(data@Num(Int32(fd)), buffer@Num(Int32(_)), count@Num(Int32(_))) =>
            memory.fill(
              MemoryAddr(0),
              wasmOps.specialOps.valToAddr(buffer),
              wasmOps.specialOps.valToSize(count),
              BTS.StoredBytes(List((Num(Int32(NumExpr(ApronExpr.top(I8Type)))),1)), Topped.Actual(ByteOrder.LITTLE_ENDIAN)))
            List(count)
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32,i32,i32 as arguments to $hostFunc, but got $args")
      case "write" =>
        args match
          case List(data@Num(Int32(fd)), Num(Int32(buffer)), sizeVal@Num(Int32(size))) =>
            println(s"write($data, $buffer, $size)")
            List(sizeVal)
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32,i32,i32 as arguments to $hostFunc, but got $args")
      case "putchar" =>
        args match
          case List(Num(Int32(char))) =>
            println(s"putchar($char)")
            List(Num(Int32(topI32)))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32 as arguments to $hostFunc, but got $args")
      case "puts" =>
        args match
          case List(Num(Int32(strPtr))) =>
            println(s"fputs($strPtr)")
            List(Num(Int32(topI32)))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32 as arguments to $hostFunc, but got $args")
      case "fputs" =>
        args match
          case List(Num(Int32(strPtr)), Num(Int32(fd))) =>
            println(s"fputs($strPtr, $fd)")
            List(Num(Int32(topI32)))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32,i32 as arguments to $hostFunc, but got $args")
      case "fgets" =>
        args match
          case List(buffer@Num(Int32(_)), count@Num(Int32(_)), Num(Int32(stream))) =>
            println(s"fgets($buffer, $count, $stream)")
            memory.fill(
              MemoryAddr(0),
              wasmOps.specialOps.valToAddr(buffer),
              wasmOps.specialOps.valToSize(count),
              BTS.StoredBytes(List((Num(Int32(NumExpr(ApronExpr.top(I8Type)))),1)), Topped.Actual(ByteOrder.LITTLE_ENDIAN))
            )
            List(Num(Int32(topI32)))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32,i32,i32 as arguments to $hostFunc, but got $args")
      case "fwrite" =>
        args match
          case List(Num(Int32(strPtr)), Num(Int32(size)), Num(Int32(count)),Num(Int32(fd))) =>
            println(s"fwrite($strPtr, $size, $count, $fd)")
            List(Num(Int32(topI32)))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32,i32,i32,i32 as arguments to $hostFunc, but got $args")
      case "printf" =>
        args match
          case List(Num(Int32(strPtr)), c@Num(Int32(varargs))) =>
            println(s"printf($strPtr, $varargs)")
            List(Num(Int32(topI32)))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32,i32 as arguments to $hostFunc, but got $args")
      case "fprintf" =>
        args match
          case List(data@Num(Int32(fd)), Num(Int32(strPtr)), c@Num(Int32(varargs))) =>
            val res = List(Num(Int32(NumExpr(apronState.withTempVars(I32Type)((tmp,_) =>
              apronState.assign(tmp, ApronExpr.interval(Integer.MIN_VALUE, Integer.MAX_VALUE, I32Type))
              ApronExpr.addr(tmp,I32Type)
            )))))
            println(s"fwrite($fd, $strPtr, $varargs) = ${res.head}")
            res
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32,i32,i32 as arguments to $hostFunc, but got $args")
      case "fileno" =>
        args match
          case List(Num(Int32(fd))) =>
            val iv = apronState.getInterval(fd.asNumExpr)
            val res = if(iv.cmp(Interval(0,2)) <= 0) {
              args
            } else {
              List(Num(Int32(NumExpr(apronState.withTempVars(I32Type)((tmp, _) =>
                apronState.assign(tmp, ApronExpr.interval(-1, Integer.MAX_VALUE, I32Type))
                ApronExpr.addr(tmp, I32Type)
              )))))
            }
            println(s"fileno($fd) = ${res.head}")
            res
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32,i32,i32 as arguments to $hostFunc, but got $args")
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
          case _ => failure.fail(WasmFailure.TypeError, s"Expected i32 as argument to $hostFunc, but got $args")
      case "toupper" =>
        args match
          case List(v@Num(Int32(char))) =>
            val charExpr = char.asNumExpr
            List(Num(Int32(NumExpr(apronState.ifThenElse(ApronBool.And(ApronBool.Constraint(ApronCons.le(ApronExpr.lit('a'.toInt, I32Type), charExpr)), ApronBool.Constraint(ApronCons.le(charExpr, ApronExpr.lit('z'.toInt, I32Type))))) {
              ApronExpr.intSub(charExpr, ApronExpr.lit('a'.toInt - 'A'.toInt, I32Type), I32Type)
            } {
              charExpr
            }))))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32 as arguments to $hostFunc, but got $args")
      case "tolower" =>
        args match
          case List(v@Num(Int32(char))) =>
            val charExpr = char.asNumExpr
            List(Num(Int32(NumExpr(apronState.ifThenElse(ApronBool.And(ApronBool.Constraint(ApronCons.le(ApronExpr.lit('A'.toInt, I32Type), charExpr)), ApronBool.Constraint(ApronCons.le(charExpr, ApronExpr.lit('Z'.toInt, I32Type))))) {
              ApronExpr.intAdd(charExpr, ApronExpr.lit('a'.toInt - 'A'.toInt, I32Type), I32Type)
            } {
              charExpr
            }))))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32 as arguments to $hostFunc, but got $args")
      case "i32.interval" =>
        args match
          case List(Num(Int32(x)), Num(Int32(y))) =>
            val ivX = apronState.getInterval(x.asNumExpr)
            val ivY = apronState.getInterval(y.asNumExpr)
            x.asNumExpr.addrs.foreach(apronState.makeNonRelational)
            y.asNumExpr.addrs.foreach(apronState.makeNonRelational)
            List(Num(Int32(RelI32.NumExpr(ApronExpr.constant(Join(ivX, ivY).get, I32Type)))))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32, i32 as arguments to $hostFunc, but got $args")
      case "f32.interval" =>
        args match
          case List(Num(Float32(x)), Num(Float32(y))) =>
            val ivX = apronState.getInterval(x)
            val ivY = apronState.getInterval(y)
            List(Num(Float32(ApronExpr.constant(Join(ivX, ivY).get, F32Type))))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected f32, f32 as arguments to $hostFunc, but got $args")
      case "f64.interval" =>
        args match
          case List(Num(Float64(x)), Num(Float64(y))) =>
            val ivX = apronState.getInterval(x)
            val ivY = apronState.getInterval(y)
            List(Num(Float64(ApronExpr.constant(Join(ivX, ivY).get, F64Type))))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected f64, f64 as arguments to $hostFunc, but got $args")
      case "i32.phi" =>
        args match
          case List(Num(Int32(x)), Num(Int32(y))) =>
            List(Num(Int32(Join(x,y).get)))
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32, i32 as arguments to $hostFunc, but got $args")
      case "assert" =>
        args match
          case List(v@Num(Int32(RelI32.BoolExpr(condition)))) =>
            val dom = domLogger.getDoms(0)
            val intervals = SortedMap.from(relationalStore._internalState.abs1.getEnvironment.getVars.zip(relationalStore._internalState.abs1.toBox(apronManager)))
            val currentResult = apronState.assert(condition).binary(_ && _, Topped.Actual(!relationalStore._internalState.abs1.isBottom(apronManager)))
            val assertionResult = assertions.get(dom).map((_,previousResult) => Join(previousResult,currentResult).get).getOrElse(currentResult)
            assertions += dom -> (condition,assertionResult)
            List()
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32 as arguments to $hostFunc, but got $args")
      case "exit" =>
        args match
          case List(v@Num(Int32(code))) =>
            failure.fail(WasmFailure.ProcExit, s"exit code = $code")
            List()
          case _ =>
            failure.fail(WasmFailure.TypeError, s"Expected i32 as arguments to $hostFunc, but got $args")
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

    private def addressIterator: Iterator[VirtAddr] =
      def valueIterator(value: Any): Iterator[VirtAddr] = value match
        case TopValue => Iterator.empty
        case Num(n) => valueIterator(n)
        case Int32(n) => valueIterator(n)
        case NumExpr(n) => valueIterator(n)
        case BoolExpr(n) => valueIterator(n)
        case HeapAddr(sites, size, _, otherOffset) => valueIterator(sites) ++ valueIterator(size) ++ valueIterator(otherOffset)
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
        case AbstractReference.Null => Iterator.empty
        case _ =>
          throw IllegalArgumentException("Unknown Value " + value)

      effectStack.addressIterator[VirtAddr](valueIterator)

    def garbageCollect(): Unit =
      val alive = PowVirtualAddress.from(this.addressIterator)
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

    def toNonRelational(using apronState: ApronState[VirtAddr, Type])(v: Value): Value =
      v match {
        case Value.Num(NumValue.Int32(x)) => Value.Num(NumValue.Int32(i32ToNonRelational(x)))
        case Value.Num(NumValue.Int64(x)) => Value.Num(NumValue.Int64(apronState.toNonRelational(x)))
        case Value.Num(NumValue.Float32(x)) => Value.Num(NumValue.Float32(apronState.toNonRelational(x)))
        case Value.Num(NumValue.Float64(x)) => Value.Num(NumValue.Float64(apronState.toNonRelational(x)))
        case _ => v
      }

    private class FunctionCallLogger extends Logger[FixIn, FixOut[Value]]:
      private val stack: mutable.Stack[(FixIn, IndexedSeq[(Value,Value)], effectStack.State)] = mutable.Stack.empty

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
            (v, toNonRelational(v))
        }

        val state = effectStack.getState
        print("  ".repeat(stack.size))
        println(s"CALL $id(${args.mkString(",")}) @ ${state.hashCode()}")
        stack.push((dom, args, state))


      override def exit(dom: FixIn, codom: TrySturdy[FixOut[Value]]): Unit =
        dom match
          case FixIn.EnterWasmFunction(id, _, _) => exitFunction(id, codom)
          case FixIn.EnterHostFunction(id, _) => exitFunction(id, codom)
          case _ => ()

      private def exitFunction(id: FuncId, codom: TrySturdy[FixOut[Value]]): Unit =
        val (_, args, inState) = stack.pop
        val result =
          codom.map {
            case FixOut.ExitWasmFunction(returns) => FixOut.ExitWasmFunction(returns.map(v => (v, toNonRelational(v))))
            case FixOut.ExitHostFunction(returns) => FixOut.ExitHostFunction(returns.map(v => (v, toNonRelational(v))))
            case FixOut.Eval() => FixOut.Eval()
            case FixOut.MostGeneralClient() => FixOut.MostGeneralClient()
          }
        val outState = effectStack.getState
        print("  ".repeat(stack.size))
        println(s"RETURN $id(${args.mkString(",")}) @ ${inState.hashCode} = $result @ ${outState.hashCode()}")


    def abstractDomainSizeLogger: AbstractDomainSizeLogger =
      val logger = new AbstractDomainSizeLogger()
      this.fixpoint.addContextFreeLogger(logger)
      logger

    class AbstractDomainSizeLogger extends fix.Logger[FixIn, FixOut[Value]]:
      private var maxEnvSize: Int = 0
      private var maxByteSize: Int = 0
      def getEnvSize: Int = maxEnvSize
      def getByteSize: Int = maxByteSize

      override def enter(dom: FixIn): Unit = updateSizes()
      override def exit(dom: FixIn, codom: TrySturdy[FixOut[Value]]): Unit = updateSizes()

      private def updateSizes(): Unit =
        if (relationalStore._internalState != null) {
          maxEnvSize = scala.math.max(maxEnvSize, relationalStore._internalState.abs1.getEnvironment.getSize)
          maxByteSize = scala.math.max(maxByteSize, relationalStore._internalState.abs1.getSize(apronManager))
        }


    def memoryLogger: MemoryLogger =
      val memLogger = new MemoryLogger()
      this.fixpoint.addContextFreeLogger(memLogger)
      memLogger

    class MemoryLogger(using memOps: LanguageSpecificMemOps[ByteMemoryCtx, Addr, Size, Value]) extends fix.Logger[FixIn, FixOut[Value]]:
      case class LoadInfo(
        loadInst: LoadInst | LoadNInst,
        baseAddr: Addr,
        effectiveAddr: Addr,
        size: Size,
        alignment: Int,
        matchingRegions: Iterable[(PhysicalAddress[ByteMemoryCtx], MemoryRegion[Addr, Size, memory.Timestamp, Value], AlignedRead)]
      )

      case class StoreInfo(
        storeInst: StoreInst | StoreNInst,
        baseAddr: Addr,
        effectiveAddr: Addr,
        alignment: Int,
        heapCtx: Iterable[ByteMemoryCtx]
      )

      private var loads: SortedMap[InstLoc, List[LoadInfo]] = SortedMap()
      private var stores: SortedMap[InstLoc, List[StoreInfo]] = SortedMap()

      private def loadContexts: SortedMap[InstLoc, (LoadInst | LoadNInst, Set[ByteMemoryCtx])] =
        loads.view.mapValues(loadInfos =>
          val loadInst = loadInfos.head.loadInst
          val heapCtxs = loadInfos.iterator.flatMap(_.matchingRegions.map(_._1.ctx).toSet).toSet
          (loadInst,heapCtxs)
        ).to(SortedMap)

      private def storeContexts: SortedMap[InstLoc, (StoreInst | StoreNInst, Set[ByteMemoryCtx])] =
        stores.view.mapValues(storeInfos =>
          val storeInst = storeInfos.head.storeInst
          val heapCtxs = storeInfos.iterator.flatMap(_.heapCtx.toSet).toSet
          (storeInst,heapCtxs)
        ).to(SortedMap)

      private type MemOpsMap = SortedMap[InstLoc, (LoadInst | LoadNInst | StoreInst | StoreNInst, Set[ByteMemoryCtx])]
      def heapCtxs: MemOpsMap =
        loadContexts ++ storeContexts

      case class Precision(
        notAnalyzed: Set[InstLoc],
        precise: MemOpsMap,
        imprecise: MemOpsMap,
        deadCode: MemOpsMap
      ):
        override def toString: String =
          "Precision:" +
            s"not analyzed:\n${notAnalyzed.mkString("\n")}" +
            s"precise:\n${memOpsMapToString(precise)}\n" +
            s"imprecise:\n${memOpsMapToString(imprecise)}\n" +
            s"deadCode:\n${memOpsMapToString(deadCode)}\n"

      def computePrecision(expected: SortedMap[InstLoc, Set[ByteMemoryCtx]]): Precision = {
        val allContexts = heapCtxs
        val notAnalyzed = expected.keySet.filter(loc => !allContexts.contains(loc))
        val (executable, deadCode) = allContexts.partition((loc,_) => expected.contains(loc))
        val (precise, imprecise) = executable.partition { case (instLoc, (inst, heapCtxs)) =>
          val ctxs = if(heapCtxs.size > 1) heapCtxs.filter{ case _: ByteMemoryCtx.Fill => false; case _ => true } else heapCtxs
          ctxs == expected(instLoc)
        }
        Precision(notAnalyzed, precise, imprecise, deadCode)
      }

      def memOpsMapToString(memOps: MemOpsMap): String =
        memOps.iterator.map { case (loc, (inst, heapCtxs)) => s"$loc, ${memoryInstToString(inst)}, ${heapCtxs.toList.sorted.mkString(";")}" }.mkString("\n")

      private inline def memoryInstToString(memoryInst: LoadInst | LoadNInst | StoreInst | StoreNInst): String =
        memoryInst match
          case Load(tpe, align, offset) => s"$tpe.load offset=$offset align=$align"
          case LoadN(tpe, n, align, offset) => s"$tpe.load n=$n offset=$offset align=$align"
          case Store(tpe, align, offset) => s"$tpe.store offset=$offset align=$align"
          case StoreN(tpe, n, align, offset) => s"$tpe.store n=$n offset=$offset align=$align"
          case _ => throw IllegalArgumentException(s"Unexpected memory instruction $memoryInst")

      override def enter(dom: FixIn): Unit =
        dom match
          case FixIn.Eval(load: (LoadInst | LoadNInst), loc) =>
            val baseAddr = wasmOps.specialOps.valToAddr(stack.peek().toOption.get)
            val effectiveAddr = wasmOps.addressOffset.addOffsetToAddr(load.offset, baseAddr)
            val size = ApronExpr.lit(getBytesToRead(load), I32Type): Size
            val memIdx = memoryIndex
            val mem = memory.memories(memIdx)
            val matchingRegions = memOps.matchRegion(effectiveAddr, size, load.align, mem)
            val loadInfo = LoadInfo(
              loadInst = load,
              baseAddr = baseAddr,
              effectiveAddr = effectiveAddr,
              size = size,
              alignment = load.align,
              matchingRegions
            )
            loads += loc -> (loadInfo :: loads.getOrElse(loc, List()))

          case FixIn.Eval(store: (StoreInst | StoreNInst), loc) =>
            val List(_value, addr) = stack.peekNOrAbort(2)
            val baseAddr = wasmOps.specialOps.valToAddr(addr)
            val effectiveAddr = wasmOps.addressOffset.addOffsetToAddr(store.offset, baseAddr)
            val memIdx = memoryIndex
            val heapCtx = Iterable.from(heapAlloc.alloc((ByteMemoryAllocationContext.Write, memIdx, effectiveAddr)))
            val storeInfo = StoreInfo(
              storeInst = store,
              baseAddr = baseAddr,
              effectiveAddr = effectiveAddr,
              alignment = store.align,
              heapCtx = heapCtx
            )
            stores += loc -> (storeInfo :: stores.getOrElse(loc, List()))

          case _ => ()

      override def exit(dom: FixIn, codom: TrySturdy[FixOut[Value]]): Unit = ()

      override def toString: String =
        s"MemoryLogger(loads: $loads, stores: $stores)"

    def constrainedInstructionsLogger: ConstrainedInstructionsLogger =
      val intervalLogger = new ConstrainedInstructionsLogger
      this.fixpoint.addContextFreeLogger(intervalLogger)
      intervalLogger

    class ConstrainedInstructionsLogger extends InstructionResultLogger[UnconstrainedInfo, Value](stack):
      override def boolValue(v: Value): Value = booleanToVal(asBoolean(v))

      def getInfo(value: Value): UnconstrainedInfo = Profiler.disableMeasurement {
        value match
          case Num(Int32(v32)) => v32 match
            case NumExpr(v) => UnconstrainedInfo.Numeric(apronState.getFloatInterval(v).meet(I32Type.signedTop), I32Type, isConstrained(v))
            case BoolExpr(v) => UnconstrainedInfo.Boolean(apronState.assert(v), isConstrained(v))
            case RelI32.GlobalAddr(nameAndStart, offset) => UnconstrainedInfo.GlobalAddr(nameAndStart, isConstrained(offset))
            case StackAddr(function, frameSize, _stackPointer, initialOffset, otherOffset) => UnconstrainedInfo.StackAddr(function, isConstrained(frameSize), initialOffset, isConstrained(otherOffset))
            case HeapAddr(sites, size, initialOffset, otherOffset) => UnconstrainedInfo.HeapAddr(sites.mapAddr(virts => new Powerset(virts.physicalAddresses.asInstanceOf[Set[PhysicalAddress[Any]]])), isConstrained(size), isConstrained(otherOffset))
          case Num(Int64(v)) => UnconstrainedInfo.Numeric(apronState.getFloatInterval(v).meet(I64Type.signedTop), I64Type, isConstrained(v))
          case Num(Float32(v)) => UnconstrainedInfo.Numeric(apronState.getFloatInterval(v).meet(F32Type.signedTop), F32Type, isConstrained(v))
          case Num(Float64(v)) => UnconstrainedInfo.Numeric(apronState.getFloatInterval(v).meet(F64Type.signedTop), F64Type, isConstrained(v))
          case Value.Ref(_) | Value.Vec(_) | Value.TopValue => UnconstrainedInfo.Top
      }

      private def isConstrained(v: ApronExpr[VirtAddr, Type] | ApronBool[VirtAddr, Type]): IsConstrained =
        if (v match {
          case expr: ApronExpr[VirtAddr, Type] => apronState.isUnconstrained(expr);
          case bool: ApronBool[VirtAddr, Type] => apronState.isUnconstrained(bool)
        })
          IsConstrained.Unconstrained
        else
          IsConstrained.Constrained

      def getAllInstructionInfos: Map[(InstLoc,syntax.Inst), List[UnconstrainedInfo]] =
        for((loc,info) <- instructionInfo)
          yield ((loc,instructions(loc)), info)

      def getConstrained: Map[InstLoc, List[UnconstrainedInfo]] =
        instructionInfo.filter((_, infos) => infos.forall(_.isConstrained))

      def grouped: Map[String, Map[InstLoc, List[UnconstrainedInfo]]] =
        getConstrained.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

      def groupedCount: Map[String, Int] =
        getConstrained.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap

    private def assignFreshTempVar(expr: ApronExpr[VirtAddr, Type]): List[Value] =
      List(apronState.withTempVars(expr._type)((v, _) =>
        apronState.assign(v, expr)
        Num(Int32(NumExpr(ApronExpr.addr(v, expr._type))))
      ))

    private def signedMin(numBytes: Int): BigInt = -BigInt(2).pow(8 * numBytes - 1)

    private def signedMax(numBytes: Int): BigInt = BigInt(2).pow(8 * numBytes - 1) - 1

    val observedConfig: WasmConfig = config.withObservers(Seq(this.triggerControlEvent))

    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[Value]] = new fix.ContextualFixpoint[FixIn, FixOut[Value]] {
      override type Ctx = observedConfig.ctx.Ctx
      val (contextPreparation, sensitivity) = observedConfig.ctx.make[Value]
      import observedConfig.ctx.finiteCtx
      override protected def contextFree = phi =>
        fix.checkThreadInterrupted(fix.log(controlEventLogger(Instance.this, effectStack, except), contextPreparation(phi)))
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = observedConfig.fix.get
      addContextFreeLogger(domLogger)
//      addContextFreeLogger(new FunctionCallLogger)
    }

    override def toString: String = s"constant $config"
