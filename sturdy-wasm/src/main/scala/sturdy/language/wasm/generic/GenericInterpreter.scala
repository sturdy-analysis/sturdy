package sturdy.language.wasm.generic

import sturdy.data.{*, given}
import sturdy.effect.{ComputationJoiner, EffectList, EffectStack}
import sturdy.effect.callframe.{DecidableMutableCallFrame, MutableCallFrame}
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.{IsSound, Soundness, fix}
import sturdy.effect.operandstack.OperandStack
import sturdy.effect.bytememory.Memory
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.symboltable.{DecidableSymbolTable, JoinableDecidableSymbolTable, SizedSymbolTable, SymbolTable, SymbolTableWithDrop}
import sturdy.effect.{EffectList, EffectStack}
import sturdy.values.{*, given}
import sturdy.values.booleans.BooleanBranching
import sturdy.values.convert.*
import sturdy.{IsSound, Soundness, fix}
import swam.*
import swam.ReferenceType.{ExternRef, FuncRef}
import swam.syntax.*
import swam.syntax.i32.Store8

import scala.collection.immutable.VectorBuilder
import scala.collection.mutable
import WasmFailure.*
import scodec.bits.ByteVector

case class FrameData(funcIx: Option[Int], returnArity: Int, module: ModuleInstance):
  override def toString: String =
    if (module == null)
      s"Function $funcIx"
    else funcIx match
      case Some(ix) =>
        module.exports.find{ case (name,ExternalValue.Function(ix2)) => ix == ix2; case _ => false } match
          case Some((name,_)) => name
          case None => ix.toString
      case None => s"Unknown Function"

given FiniteFrameData: Finite[FrameData] with {}
given Ordering[FrameData] = Ordering.by(data => (data.funcIx, data.returnArity, data.module.hashCode))

given frameDataIsSound: Soundness[FrameData, FrameData] with
  override def isSound(c: FrameData, a: FrameData): IsSound =
    if (c.funcIx != a.funcIx)
      return IsSound.NotSound(s"Function index do not match: $c $a.")
    if (c.returnArity != a.returnArity)
      return IsSound.NotSound(s"Return arities do not match: $c $a.")
    if (c.module == null && a.module == null)
      return IsSound.Sound
    if (c.module != null && a.module != null)
      return IsSound.NotSound(s"Concrete module ${c.module} not approximated by ${a.module}")
    summon[Soundness[ModuleInstance, ModuleInstance]].isSound(c.module, a.module)

object FrameData:
  val empty: FrameData = FrameData(None, 0, null)

enum JumpTarget:
  case Jump(labelIndex: LabelIdx)
  case Return

given Finite[JumpTarget] with {}

case class WasmException[V](target: JumpTarget, operands: List[V])

type Imports = Map[String, ModuleInstance]

case class FuncId(mod: ModuleInstance, funcIx: Int):
  override def toString: String = s"$mod.$funcIx"

given Ordering[FuncId] = Ordering.by[FuncId, (ModuleInstance,Int)](funcid => (funcid.mod, funcid.funcIx))

enum InstLoc:
  case InFunction(func: FuncId, pc: Int)
  case InInit(mod: ModuleInstance, pc: Int)
  case InvokeExported(mod: ModuleInstance, funName: String)

  override def hashCode(): Int = this match
    case InFunction(FuncId(mod, funcIx), pc) => mod.hashCode() + 7 * funcIx + 3 * pc
    case InInit(mod, pc) => mod.hashCode() + 101 * pc
    case InvokeExported(mod, funName) => mod.hashCode() + 331 * funName.hashCode

  override def toString: String = this match
    case InFunction(func, pc) => s"$func:$pc"
    case InInit(mod, pc) => s"$mod.INIT:$pc"
    case InvokeExported(mod, funName) => s"$mod.$funName"

  def +(i: Int): InstLoc = this match
    case InFunction(func, pc) => InFunction(func, pc + i)
    case InInit(mod, pc) => InInit(mod, pc + i)
    case InvokeExported(mod, funName) => throw new IllegalStateException

  def -(that: InstLoc): Int = (this, that) match
    case (InFunction(func1, pc1), InFunction(func2, pc2)) if func1 == func2 => pc2 - pc1
    case (InInit(mod1, pc1), InInit(mod2, pc2)) if mod1 == mod2 => pc2 - pc1
    case _ => throw new MatchError((this, that))

given Ordering[InstLoc] = Ordering.by[InstLoc, Either[(FuncId,Int), Either[(Int,Int), (Int,String)]]] {
  case InstLoc.InFunction(fid,pc) => Left((fid,pc))
  case InstLoc.InInit(mod, pc) => Right(Left((mod.hashCode(),pc)))
  case InstLoc.InvokeExported(mod, funName) => Right(Right((mod.hashCode(),funName)))
}

enum FixIn:
  case Eval(inst: Inst, loc: InstLoc)
  case EnterWasmFunction(id: FuncId, func: Func, ft: FuncType)
  case EnterHostFunction(id: FuncId, hostFfunc: HostFunction)
  case MostGeneralClientLoop(modInst: ModuleInstance)

  override def toString: String = this match
    case Eval(i, loc) => i match
      case Block(_, _) => s"Block @$loc"
      case Loop(_, _) => s"Loop @$loc"
      case If(_, _, _) => s"If @$loc"
      case _ => s"$i @$loc"
    case EnterWasmFunction(id, _, _) => s"Enter $id"
    case EnterHostFunction(id, _) => s"Enter $id"
    case MostGeneralClientLoop(modInst) => s"Most general client for $modInst"

given Ordering[FixIn] = {
  case (FixIn.Eval(_, loc1), FixIn.Eval(_, loc2)) => Ordering[InstLoc].compare(loc1, loc2)
  case (FixIn.EnterWasmFunction(fun1, _, _), FixIn.EnterWasmFunction(fun2, _, _)) => Ordering[FuncId].compare(fun1, fun2)
  case (FixIn.EnterHostFunction(fun1, _), FixIn.EnterHostFunction(fun2, _)) => Ordering[FuncId].compare(fun1, fun2)
  case (FixIn.MostGeneralClientLoop(mod1), FixIn.MostGeneralClientLoop(mod2)) => Ordering.by[ModuleInstance, Option[Int]](
    mod =>
      if(mod == null)
        None
      else
        Some(mod.hashCode())
  ).compare(mod1, mod2)
  case (in1, in2) => Ordering.by[FixIn, Int]{
    case _: FixIn.Eval => 1
    case _: FixIn.EnterWasmFunction => 2
    case _: FixIn.EnterHostFunction => 3
    case _: FixIn.MostGeneralClientLoop => 4
  }.compare(in1, in2)
}

enum FixOut[V]:
  case Eval()
  case ExitWasmFunction(vals: List[V])
  case ExitHostFunction(vals: List[V])
  case MostGeneralClient()

given finiteFixIn: Finite[FixIn] with {}

trait GenericInterpreter[V, Addr, Bytes, Size, ExcV, Index, FunV, RefV, J[_] <: MayJoin[_]]:

  type Elem = Seq[V]

  // fixpoint
  val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[V]]
  type Fixed = FixIn => FixOut[V]

  // joins
  implicit def jvUnit: J[Unit]
  implicit def jvBytes: J[Bytes]
  implicit def jvV: J[V]
  implicit def jvRefV: J[RefV]
  implicit def jvFunV: J[FunV]
  implicit def jvElem: J[Elem]

  // value components
  val wasmOps: WasmOps[V, Addr, Bytes, Size, ExcV, Index, FunV, RefV, J]

  import wasmOps.*
  import specialOps.*

  // effect components
  val stack: OperandStack[V, MayJoin.NoJoin]
  val memory: Memory[MemoryAddr, Addr, Bytes, Size, J]
  val globals: DecidableSymbolTable[Unit, GlobalAddr, V]
  val elems: SymbolTableWithDrop[Unit, ElemAddr, Elem, J]
  val tables: SizedSymbolTable[V, TableAddr, Index, RefV, Size, J]
  val callFrame: MutableCallFrame[FrameData, Int, V, InstLoc, MayJoin.NoJoin]
  val except: Except[WasmException[V], ExcV, J]
  val failure: Failure

  import except.*

  // effect stack
  def newEffectStack: EffectStack =
    new EffectStack(EffectList(stack, memory, globals, elems, tables, callFrame, except, failure), {
      case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => EffectList(elems, tables, memory, globals, callFrame)
      case _: FixIn.Eval => EffectList(elems, tables, stack, memory, globals, callFrame)
    }, {
      case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => EffectList(elems, tables, stack, memory, globals, failure)
      case _: FixIn.Eval => EffectList(elems, tables, stack, memory, globals, callFrame, except)
    })

  val effectStack: EffectStack = newEffectStack
  given EffectStack = effectStack

  private given Failure = failure

  lazy val num = new GenericInterpreterNumerics[V, J](stack, wasmOps)
  lazy val simd = new GenericInterpreterSIMD[V, Addr, Bytes, J](stack, memory, wasmOps)

  private val labelStack = new LabelStack
  private var memCount = 0
  private var tabCount = 0
  private var globCount = 0

  private def fail(k: FailureKind, what: String) = failure.fail(k, s"$what in $module")

  def module: ModuleInstance = callFrame.data.module

  def evalVarInst(inst: VarInst): Unit = inst match
    case LocalGet(ix) =>
      val v = callFrame.getLocalOrElse(ix, fail(UnboundLocal, ix.toString))
      stack.push(v)
    case LocalSet(ix) =>
      val v = stack.popOrAbort()
      callFrame.setLocalOrElse(ix, v, fail(UnboundLocal, ix.toString))
    case LocalTee(ix) =>
      val v = stack.peekOrAbort()
      callFrame.setLocalOrElse(ix, v, fail(UnboundLocal, ix.toString))
    case GlobalGet(globalIx) =>
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val global = getGlobalValue(globalIdx)
      stack.push(global)
    case GlobalSet(globalIx) =>
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val v = stack.popOrAbort()
      val _ = getGlobalValue(globalIdx)
      writeGlobalValue(globalIdx, v)

  private def toTableAddr(ix: Int): TableAddr = module.tableAddrs.lift(ix).getOrElse(fail(TableAccessOutOfBounds, ix.toString))

  def evalTableInst(inst: Inst, loc: InstLoc): Unit = external {
    inst match {
      case TableGet(ix) =>
        val elemIdx = stack.popOrAbort()
        val ref = tables.get(toTableAddr(ix), valToIdx(elemIdx)).getOrElse(fail(TableAccessOutOfBounds, "Invalid table.get access"))
        stack.push(refToVal(ref))
      case TableSet(ix) =>
        val v = stack.popOrAbort()
        val elemIdx = stack.popOrAbort()
        tables.set(toTableAddr(ix), valToIdx(elemIdx), valToRef(v, module.functions)).getOrElse(fail(TableAccessOutOfBounds, "Invalid table.set access"))
      case TableSize(ix) =>
        val sz = tables.size(toTableAddr(ix))
        stack.push(sizeToVal(sz))
      case TableGrow(ix) =>
        val n = stack.popOrAbort()
        val initVal = stack.popOrAbort()
        val addr = toTableAddr(ix)
        val tableSize = tables.size(addr)
        val newSize = num.evalIBinop(i64.Add, num.evalConvertop(i64.ExtendUI32, sizeToVal(tableSize)), num.evalConvertop(i64.ExtendUI32, n))
        // assert that newSize <= 0xFFFFFFFF
        val newSizeCheck = num.evalIRelop(i64.GtU, newSize, num.evalNumeric(i64.Const(0xFFFFFFFF)))
        branchOpsUnit.boolBranch(newSizeCheck) {
          stack.push(num.evalNumeric(i32.Const(-1)))
        } {
          val newSize = num.evalIBinop(i32.Add, sizeToVal(tableSize), n)
          val result = tables.grow(addr, valToSize(newSize), valToRef(initVal, module.functions)).option
            (num.evalNumeric(i32.Const(0xFFFFFFFF))) // 0xFFFFFFFF ~= -1
            (sizeToVal)
          stack.push(result)
        }

      case TableFill(ix) =>
        val n = stack.popOrAbort()
        val ref = stack.popOrAbort() // val
        val offset = stack.popOrAbort() // i
        // assert unsigned offset + n <= tableSize
        val tableSize = tables.size(toTableAddr(ix))
        val offsetCheck = num.evalIRelop(i32.GtU, num.evalIBinop(i32.Add, offset, n), sizeToVal(tableSize))
        branchOpsUnit.boolBranch(offsetCheck)(fail(TableAccessOutOfBounds, "Invalid table.fill access")) {
          tables.fillTable(toTableAddr(ix), valToRef(ref, module.functions), offset, n).getOrElse(fail(TableAccessOutOfBounds, "Invalid table.fill access"))
        }
      case TableCopy(x, y) =>
        val n = stack.popOrAbort()
        val s = stack.popOrAbort()
        val d = stack.popOrAbort()
        tables.copy(toTableAddr(x), toTableAddr(y), d, s, n).getOrElse(fail(TableAccessOutOfBounds, "Invalid table.copy access"))
      case TableInit(elementIndex, tableIndex) =>
        val elem = elems.get((), ElemAddr(elementIndex)).getOrElse(fail(TableAccessOutOfBounds, s"Element at index $elementIndex could not be found"))
        val n = stack.popOrAbort()
        val s = stack.popOrAbort()
        val d = stack.popOrAbort()
        val refs = elem.map(funcIx =>
          tables.getOrElse(module.tableAddrs(tableIndex), valToIdx(funcIx), fail(UnboundFunctionIndex, funcIx.toString))
        ).toVector
        tables.init(toTableAddr(tableIndex), refs, s, d, n).getOrElse(fail(TableAccessOutOfBounds, "Invalid table.init access"))

      case ElemDrop(el) =>
        elems.drop((), ElemAddr(el))

      case _ => throw new IllegalArgumentException(s"Expected table instruction, but got $inst")
    }
  }

  def evalRefInst(inst: Inst): Unit = inst match {
    case RefNull(t) =>
      stack.push(refToVal(makeNullRefV(t)))
    case RefIsNull =>
      val ref = stack.popOrAbort()
      stack.push(isNullRef(ref))
    case RefFunc(funcIdx) =>
      val funInst = module.functions.lift(funcIdx).getOrElse(fail(UnboundFunctionIndex, funcIdx.toString))
      stack.push(refToVal(funcInstToRefV(funInst)))
    case RefExtern(_) =>
      fail(UnboundFunctionIndex, "Cannot call extern reference")
  }

  def evalVectorInst(inst: Inst): Unit = inst match {
    case i: VectorLoadInst =>
      i match {
        case vector: LoadVector =>
          val memAddr = effectiveAddr(vector.offset)
          simd.evalLoadVector(vector, memoryIndex, memAddr).orElseAndThen(fail(MemoryAccessOutOfBounds, s"Cannot read vector at address $memAddr")) {
            v => stack.push(v)
          }
        case splat: LoadVectorSplat =>
          val memAddr = effectiveAddr(splat.offset)
          simd.evalLoadVector(splat, memoryIndex, memAddr).orElseAndThen(fail(MemoryAccessOutOfBounds, s"Cannot read vector splat at address $memAddr")) {
            v => stack.push(v)
          }
        case zero: LoadVectorZero =>
          val memAddr = effectiveAddr(zero.offset)
          simd.evalLoadVector(zero, memoryIndex, memAddr).orElseAndThen(fail(MemoryAccessOutOfBounds, s"Cannot read vector zero at address $memAddr")) {
            v => stack.push(v)
          }
        case lane: LoadVectorLane =>
          val v = stack.popOrAbort()
          val memAddr = effectiveAddr(i.offset)
          stack.push(v)
          simd.evalLoadVector(i, memoryIndex, memAddr).orElseAndThen(fail(MemoryAccessOutOfBounds, s"Cannot read vector lane at address $memAddr")) {
            v => stack.push(v)
          }
        case v128.Load(align, offset) =>
          val memAddr = effectiveAddr(i.offset)
          simd.evalLoadVectorBytes(i, memoryIndex, memAddr).orElseAndThen(fail(MemoryAccessOutOfBounds, s"Cannot read vector at address $memAddr")) {
            bytes =>
              val v = decode(bytes, SomeCC(i, false))
              stack.push(v)
          }
      }
    case i: VectorStoreInst =>
      val v = stack.popOrAbort()
      val memAddr = effectiveAddr(i.offset)
      stack.push(v)
      simd.evalStoreVector(i, memoryIndex, memAddr).getOrElse(fail(MemoryAccessOutOfBounds, s"Cannot write vector at address ${effectiveAddr(i.offset)}"))
    case i: VectorInst => stack.push(simd.evalSIMD(i))
    case op: SatConvertop =>
      val v = stack.popOrAbort()
      stack.push(num.evalMiscop(op, v))
  }


  val pageSize: Int = 65536
  val maxPageNum: Int = 65536

  def evalMemoryInst(inst: Inst): Unit = inst match
    case i: LoadInst => load(i)
    case i: LoadNInst => load(i)

    case i: StoreInst => store(i)
    case i: StoreNInst => store(i)

    case MemorySize =>
      val sz = memory.size(memoryIndex)
      stack.push(sizeToVal(sz))
    case MemoryGrow =>
      val delta = valToSize(stack.popOrAbort())
      val res = memory.grow(memoryIndex, delta).option
        (num.evalNumeric(i32.Const(0xFFFFFFFF))) // 0xFFFFFFFF ~= -1
        (sizeToVal)
      stack.push(res)
    case MemoryFill =>
      val n = stack.popOrAbort()
      val v = stack.popOrAbort() // val
      val d = stack.popOrAbort()
      val memSize = num.evalIBinop(i32.Mul, sizeToVal(memory.size(memoryIndex)), num.evalNumeric(i32.Const(pageSize)))
      val offsetCheck = num.evalIRelop(i32.GtU, num.evalIBinop(i32.Add, d, n), memSize)
      branchOpsUnit.boolBranch(offsetCheck)(fail(MemoryAccessOutOfBounds, "Invalid memory.fill access")) {
        memory.fill(memoryIndex, valToAddr(d), valToSize(n), encode(v, SomeCC(i32.Store8(0, 0), false))).getOrElse(fail(MemoryAccessOutOfBounds, "Invalid memory.fill access"))
      }
    case MemoryCopy =>
      val n = stack.popOrAbort()
      val s = stack.popOrAbort()
      val d = stack.popOrAbort()
      // assert unsigned s + n <= memSize && d + n <= memSize
      val memSize = num.evalIBinop(i32.Mul, sizeToVal(memory.size(memoryIndex)), num.evalNumeric(i32.Const(pageSize)))
      val srcCheck = num.evalIRelop(i32.GtU, num.evalIBinop(i32.Add, s, n), memSize)
      val dstCheck = num.evalIRelop(i32.GtU, num.evalIBinop(i32.Add, d, n), memSize)
      val check = num.evalIBinop(i32.Or, srcCheck, dstCheck)
      branchOpsUnit.boolBranch(check)(fail(MemoryAccessOutOfBounds, "Invalid memory.copy access")) {
        memory.copy(memoryIndex, valToAddr(s), valToAddr(d), valToSize(n)).getOrElse(fail(MemoryAccessOutOfBounds, "Invalid memory.copy access"))
      }
    case MemoryInit(ix) =>
      val n = stack.popOrAbort()
      val s = stack.popOrAbort()
      val d = stack.popOrAbort()

      val data = module.data.lift(ix).getOrElse(fail(DataSegmentOutOfBounds, ix.toString))
      memory.init(memoryIndex, valToAddr(d), valToAddr(s), valToSize(n), liftBytes(data.data.toIterable.toSeq)).getOrElse(
        fail(MemoryAccessOutOfBounds, s"Cannot initialize memory with $data at address $d with size $n from offset $s")
      )

    case DataDrop(ix) =>
      module.data.lift(ix).getOrElse(fail(MemoryAccessOutOfBounds, ix.toString))
      module.data = module.data.updated(ix, DataInstance(ByteVector.empty))
    case _ => throw new IllegalArgumentException(s"Expected memory instruction, but got $inst")

  def load(inst: LoadInst | LoadNInst): Unit =
    val startAddr = valToAddr(stack.popOrAbort())
    val addr = addOffsetToAddr(inst.offset, startAddr)
    val memIdx = memoryIndex
    val length = getBytesToRead(inst)
    memory.read(memIdx, addr, length).orElseAndThen(fail(MemoryAccessOutOfBounds, s"Cannot read $length bytes at address $addr")) {
      bytes =>
        val v = decode(bytes, SomeCC(inst, false))
        stack.push(v)
    }

  def store(inst: StoreInst | StoreNInst): Unit =
    val v = stack.popOrAbort()
    val bytes = encode(v, SomeCC(inst, false))

    // add offset to base address (which is already on the stack)
    val startAddr = valToAddr(stack.popOrAbort())
    val addr = addOffsetToAddr(inst.offset, startAddr)

    val memIdx = memoryIndex
    memory.write(memIdx, addr, bytes).getOrElse(
      fail(MemoryAccessOutOfBounds, s"Cannot write $bytes at address $addr in current memory.")
    )

  def getBytesToRead(inst: MemoryInst): Int = inst match
    case Load(tpe, _, _) => tpe.width / 8
    case LoadN(_, n, _, _) => n / 8
    case _ => throw new IllegalArgumentException(s"Expected load instruction, but got $inst")

  def memoryIndex: MemoryAddr =
    module.memoryAddrs(0)

  def globalTableIndex: TableAddr =
    TableAddr(0)

  def getGlobalValue(ga: GlobalAddr): V =
    globals.getOrElse((), ga, fail(UnboundGlobal, ga.toString))

  def writeGlobalValue(ga: GlobalAddr, v: V): Unit =
    globals.set((), ga, v)

  def eval_open(inst: Inst, loc: InstLoc)(using Fixed): Unit =
    val opcode = inst.opcode
    if (opcode >= OpCode.I32Const && opcode <= OpCode.I64Extend32S)
      stack.push(num.evalNumeric(inst))
    else if (opcode >= OpCode.I32Load && opcode <= OpCode.MemoryGrow)
      evalMemoryInst(inst)
    else if (opcode >= OpCode.Unreachable && opcode <= OpCode.CallIndirect)
      evalControlInst(inst, loc)
    else if (opcode >= OpCode.TableGet && opcode <= OpCode.TableSet)
      evalTableInst(inst, loc)
    else if (opcode >= OpCode.RefNull && opcode <= OpCode.RefFunc)
      evalRefInst(inst)
    else inst match
      case i: VarInst => evalVarInst(i)
      case i: TableInst => evalTableInst(i, loc)
      case i: TableMiscOp => evalTableInst(i, loc)
      case i: MemoryMiscOp => evalMemoryInst(i)
      case i: ReferenceInst => evalRefInst(i)
      case i: VectorInst => evalVectorInst(i)
      case Drop => stack.popOrAbort()
      case Select | SelectReturns(_) =>
        val isZero = num.evalNumeric(i32.Eqz)
        branchOpsUnit.boolBranch(isZero) {
          // v == 0: else branch
          val (_, v2) = stack.pop2OrAbort()
          stack.push(v2)
        } {
          stack.popOrAbort()
        }
      case _ => throw new IllegalArgumentException(s"Unexpected instruction $inst")

  def evalControlInst(inst: Inst, loc: InstLoc)(using Fixed): Unit = inst match
    case Nop => // nothing
    case Unreachable => fail(UnreachableInstruction, inst.toString)
    case b@Block(bt, insts) =>
      label(BlockId(b), labelArities(bt, isLoop = false), insts, None)
    case l@Loop(bt, insts) =>
      label(BlockId(l), labelArities(bt, isLoop = true), insts, Some((l, loc)))
    case ifInst@If(bt, thnInsts, elsInsts) =>
      val isZero = num.evalNumeric(i32.Eqz)
      val ars = labelArities(bt, isLoop = false)
      branchOpsUnit.boolBranch(isZero) {
        // v == 0: else branch
        label(BlockId(ifInst -> false), ars, elsInsts, None)
      } {
        label(BlockId(ifInst -> true), ars, thnInsts, None)
      }
    case Br(labelIndex) =>
      branch(labelIndex)
    case BrIf(labelIndex) =>
      val isZero = num.evalNumeric(i32.Eqz)
      branchOpsUnit.boolBranch(isZero) {
        // v == 0: else branch
        // do nothing
      } {
        branch(labelIndex)
      }
    case BrTable(labels, defaultLabel) =>
      val ix = stack.popOrAbort()
      indexLookup(ix, labels).orElseAndThen(defaultLabel)(branch)
    case Return =>
      val operands = stack.popNOrAbort(callFrame.data.returnArity)
      throws(WasmException(JumpTarget.Return, operands))
    case Call(funcIdx) =>
      val func = module.functions.lift(funcIdx).getOrElse(fail(UnboundFunctionIndex, funcIdx.toString))
      invoke(func, loc)
    case CallIndirect(tableIdx, typeIdx) =>
      val ftExpected = module.functionTypes(typeIdx)
      val funcIx = stack.popOrAbort()
      val fRef = tables.getOrElse(module.tableAddrs(tableIdx), valToIdx(funcIx), fail(UnboundFunctionIndex, funcIx.toString))
      branchOpsUnit.boolBranch(isNullRef(refToVal(fRef))) {
        fail(UnboundFunctionIndex, s"Cannot call function with null reference $fRef.")
      } {
        val funV = refVToFunV(fRef)
        invokeIndirect(funV, ftExpected, funcIx, loc)
      }
    case _ => throw new IllegalArgumentException(s"Expected control instruction, but got $inst")

  def branch(labelIndex: LabelIdx): Unit =
    val returnArity = labelStack.lookupReturnArity(labelIndex)
    val operands = stack.popNOrAbort(returnArity)
    throws(WasmException(JumpTarget.Jump(labelIndex), operands))

  /** Arities used by a label. Results equals jumpOperands if branchTarget is None. */
  case class LabelArities(params: Int, results: Int, jumpOperands: Int)

  private inline def assertFrameSize(size: Int): Unit = {
//    if (Debug.DEBUG_GENERIC_WASM_STACK && stack.frameSize != size)
//      throw new AssertionError(s"Expected stack frame of size $size, but current stack frame has size ${stack.frameSize}")
  }

  def label(block: BlockId, arities: LabelArities, insts: Iterable[Inst], branchTarget: Option[(Inst, InstLoc)])(using Fixed): Unit =
    stack.withNewFrame(arities.params) {
      tryCatch {
        labelStack.pushLabel(arities.jumpOperands)
        try {
          val modInst = module
          for ((inst, ix) <- insts.zipWithIndex) {
            val loc = modInst.blockInstLocs((block, ix))
            eval(inst, loc)
          }
          assertFrameSize(arities.results)
        } finally labelStack.popLabel()
      } { ex =>
        stack.clearCurrentOperandFrame()
        ex match {
          case WasmException(JumpTarget.Jump(labelIndex), operands) =>
            if (labelIndex == 0) {
              stack.pushN(operands)
              assertFrameSize(arities.jumpOperands)
              for ((i, loc) <- branchTarget)
                eval(i, loc)
              assertFrameSize(arities.results)
            } else {
              assertFrameSize(0)
              throws(WasmException(JumpTarget.Jump(labelIndex - 1), operands))
            }
          case WasmException(JumpTarget.Return, _) =>
            assertFrameSize(0)
            throws(ex)
        }
      }
    }

  def invoke(fun: FunctionInstance, loc: InstLoc)(using Fixed): Unit =
    val funcType = fun.funcType
    val frameData = FrameData(Some(fun.funcIdx), funcType.t.size, fun.module)
    val args = stack.popNOrAbort(funcType.params.size)

    fun match
      case FunctionInstance.Wasm(mod, ix, func, funcType) =>
        val vars = args.view.map(Some.apply) ++ func.locals.map {
          case VecType.V128 => Some(simd.defaultValue())
          case ty@(i: ValType) => Some(num.defaultValue(ty))
        }
        labelStack.withNew(stack.withNewFrame(0)(callFrame.withNew(frameData, vars.zipWithIndex.map(_.swap), loc) {
          enterFunction(FuncId(mod, ix), func, funcType)
        }))
      case FunctionInstance.Host(mod, ix, hostFunc) =>
        val vars = args.view.map(Some.apply)
        labelStack.withNew(stack.withNewFrame(0)(callFrame.withNew(frameData, vars.zipWithIndex.map(_.swap), loc) {
          enterHostFunction(FuncId(mod, ix), hostFunc)
        }))

  private def enterFunction_open(id: FuncId, func: Func, funcType: FuncType)(using Fixed): List[V] =
    val returnN = funcType.t.size
    tryCatch {
      label(BlockId(id), LabelArities(0, returnN, returnN), func.body, None)
    } { ex =>
      stack.clearCurrentOperandFrame()
      ex match {
        case WasmException(JumpTarget.Return, operands) =>
          stack.pushN(operands)
        case WasmException(JumpTarget.Jump(_), _) =>
          fail(InvalidModule, s"Tried to jump through a function boundary.")
      }
    }
    stack.peekNOrAbort(returnN)

  def invokeIndirect(funV: FunV, ftExpected: swam.FuncType, funcIx: V, loc: InstLoc)(using Fixed): Unit =
    functionOps.invokeFun(funV, ftExpected) {
      case (func, _) =>
        val ftActual = func.funcType
        if (ftExpected != ftActual)
          fail(IndirectCallTypeMismatch, s"Expected function of type $ftExpected but $funcIx has type $ftActual")
        invoke(func, loc)
    }


  inline def eval(inst: Inst, loc: InstLoc)(using rec: Fixed): FixOut[V] =
    rec(FixIn.Eval(inst, loc))

  inline def enterFunction(id: FuncId, func: Func, ft: FuncType)(using rec: Fixed): FixOut[V] =
    rec(FixIn.EnterWasmFunction(id, func, ft))

  inline def enterHostFunction(id: FuncId, hostFunc: HostFunction)(using rec: Fixed): FixOut[V] =
    rec(FixIn.EnterHostFunction(id, hostFunc))


  private def fixed: Fixed = fixpoint {
    case FixIn.Eval(inst, loc) =>
      eval_open(inst, loc)
      FixOut.Eval()
    case FixIn.EnterWasmFunction(id, func, funcType) =>
      FixOut.ExitWasmFunction(enterFunction_open(id, func, funcType))
    case FixIn.EnterHostFunction(id, hostFunc) =>
      FixOut.ExitHostFunction({
        val args = hostFunc.funcType.params.zipWithIndex.map(p => callFrame.getLocal(p._2).getOrElse(fail(UnboundLocal, s"Host arg $p")))
        val res = invokeHostFunction(hostFunc, args.toList)
        stack.pushN(res)
        res
      })
    case FixIn.MostGeneralClientLoop(modInst) =>
      runMostGeneralClient_open(modInst)
      FixOut.Eval()
  }

  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  private var typedTop: ValType => V = _

  def runMostGeneralClient(modInst: ModuleInstance, typedTop: ValType => V): Unit = external { rec ?=>
    this.typedTop = typedTop
    rec(FixIn.MostGeneralClientLoop(modInst))
  }

  private def runMostGeneralClient_open(modInst: ModuleInstance)(using rec: Fixed): Unit = {
    if (modInst.exportedFunctions.nonEmpty)
      effectStack.joinFold(modInst.exportedFunctions, { case (funcName, ExternalValue.Function(funcIx)) =>
        val fun = modInst.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
        val paramTys = fun.funcType.params
        val args = paramTys.map(typedTop).toList
        invokeExported_open(modInst, funcName, args)
        ()
      })
    rec(FixIn.MostGeneralClientLoop(modInst))
  }

  def invokeExported(modInst: ModuleInstance, funcName: String, args: List[V]): List[V] = external {
    invokeExported_open(modInst, funcName, args)
  }

  private def invokeExported_open(modInst: ModuleInstance, funcName: String, args: List[V])(using Fixed): List[V] = {
    stack.withNewStack {
      modInst.exportedFunctions.get(funcName) match
        case Some(ExternalValue.Function(funcIx)) =>
          val fun = modInst.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
          val paramTys = fun.funcType.params
          if (paramTys.length != args.length)
            throw new Error(s"Wrong number of arguments in external invocation. Expected ${paramTys.length} but got ${args.length}.")
          // paramTys.zip(args).map(???) // TODO: check for right type -> we need some kind of generic language feature here
          val rtLength = fun.funcType.t.length
          stack.pushN(args)
          val loc = InstLoc.InvokeExported(modInst, funcName)
          callFrame.withNew(FrameData(Some(funcIx), 0, modInst), Iterable.empty, loc) {
            eval(Call(funcIx), loc)
          }
          stack.popNOrAbort(rtLength)
        case _ => throw new Error(s"Function with name $funcName was not found in module's exports.")
    }
  }


  private def labelArities(bt: BlockType, isLoop: Boolean): LabelArities = bt match
    case swam.BlockType.NoType => LabelArities(0, 0, 0)
    case _: swam.BlockType.ValueType =>
      if (isLoop)
        LabelArities(0, 1, 0)
      else
        LabelArities(0, 1, 1)
    case swam.BlockType.FunctionType(tpe) =>
      val ft = module.functionTypes(tpe)
      val params = ft.params.size
      val results = ft.t.size
      if (isLoop)
        LabelArities(params, results, params)
      else
        LabelArities(params, results, results)


  def evalInstructionSequence(block: BlockId, insts: Vector[Inst], mod: ModuleInstance, loc: InstLoc)(using Fixed): V =
    val frameData = FrameData(None, 1, mod)
    labelStack.withNew(stack.withNewStack(callFrame.withNew(frameData, Iterable.empty, loc) {
      for ((inst, ix) <- insts.zipWithIndex) {
        val loc = mod.blockInstLocs((block, ix))
        eval(inst, loc)
      }
      stack.popOrAbort()
    }))

  /** add offset to base address (which is already on the stack) */
  def effectiveAddr(offset: Int): Addr =
    val v1 = i32ops.integerLit(offset)
    val v2 = stack.popOrAbort()
    val res = i32ops.add(v1, v2)
    val cmp = unsignedCompareOps.ltUnsigned(res, v1)
    val v = branchOpsV.boolBranch(cmp, fail(MemoryAccessOutOfBounds, s"$v1 + $v2"), res)
    valToAddr(v)

  def resolveImports(module: Module, imports: Imports, hostModules: HostModules):
    (Vector[FunctionInstance], Vector[GlobalAddr], Vector[GlobalType], Vector[TableAddr], Vector[MemoryAddr]) =
    val funcs: VectorBuilder[FunctionInstance] = VectorBuilder()
    val globs: VectorBuilder[GlobalAddr] = VectorBuilder()
    val globTpes: VectorBuilder[GlobalType] = VectorBuilder()
    val tabs: VectorBuilder[TableAddr] = VectorBuilder()
    val mems: VectorBuilder[MemoryAddr] = VectorBuilder()

    module.imports.foreach { imp =>
      // handle host functions
      if (hostModules.containsModule(imp.moduleName)) {
        imp match
          case Import.Function(_, funcName, funcType) =>
            val (hostModule, ix, hf) = hostModules.getHostFunction(imp.moduleName, funcName).getOrElse(
              throw IllegalArgumentException(s"No host function $funcName found in module ${imp.moduleName}.")
            )
            if (hf.funcType != module.types(funcType))
              throw new Error(s"Importing host function $funcName with wrong type: expected ${hf.funcType}, but imported with ${module.types(funcType)}")
            funcs += FunctionInstance.Host(hostModule, ix, hf)
          case _ => throw new Error(s"Import from runtime: expected a function, but got $imp.")
      } else {
        // get the module to import from
        val from = imports.getOrElse(imp.moduleName, throw new Error(s"No module with name ${imp.moduleName} in imports."))
        // get the exported field
        val (_, exp) = from.exports.find((name, _) => name == imp.fieldName)
          .getOrElse(throw new Error(s"No export with name ${imp.fieldName} in module."))
        imp match
          case Import.Function(_, _, tpe) =>
            exp match
              case ExternalValue.Function(addr) =>
                val expectedType = module.types(tpe)
                val func = from.functions(addr)
                if (expectedType == func.funcType) {
                  funcs += func
                } else {
                  throw new Error(s"Type mismatch: expected $expectedType but found ${func.funcType}.")
                }
              case _ => throw new Error(s"Import mismatch: expected a function but found $exp.")
          case Import.Global(_, _, globType) =>
            exp match
              case ExternalValue.Global(addr) =>
                val fromTpe = from.globalTypes(addr)
                if (fromTpe != globType)
                  throw new Error(s"Type mismatch: expected global of type $globType but found ${fromTpe}.")
                val glob = from.globalAddrs(addr)
                // TODO: check mutability (=> add mut to GlobalInstance)
                globs += glob
                globTpes += globType
              case _ => throw new Error(s"Import mismatch: expected a global but found $exp.")
          case Import.Table(_, _, tpe) =>
            exp match
              case ExternalValue.Table(addr) =>
                val tab = from.tableAddrs(addr)
                // TODO: check table type
                tabs += tab
              case _ => throw new Error(s"Import mismatch: expected a table but found $exp.")
          case Import.Memory(_, _, tpe) =>
            exp match
              case ExternalValue.Memory(addr) =>
                val mem = from.memoryAddrs(addr)
                // TODO: check memory type
                mems += mem
              case _ => throw new Error(s"Import mismatch: expected a memory but found $exp.")
      }
    }

    (funcs.result(), globs.result(), globTpes.result(), tabs.result(), mems.result())

  // we assume a valid module here
  def initializeModule(module: Module,
                       imports: Imports = Map.empty,
                       moduleId: Option[Any] = None,
                       hostModules: HostModules = defaultHostModules): ModuleInstance = external {
    initializeThis()

    val modInst = new ModuleInstance(moduleId)
    val (funcImports, globImports, globTypes, tabImports, memImpors) = resolveImports(module, imports, hostModules)

    var loc = InstLoc.InInit(modInst, 0)
    initializeGlobalTypes(modInst, globTypes)
    initializeFunctions(module, modInst, funcImports)
    initializeTables(module, modInst, tabImports)
    initializeExports(module, modInst)
    callFrame.withNew(FrameData(None, 1, modInst), Iterable.empty, loc) {
      loc = initializeGlobals(module, modInst, globImports, loc)
      initializeElements(module, modInst, loc)
    }
    callFrame.withNew(FrameData(None, 1, modInst), Iterable.empty, loc) {
      initializeMemory(module, modInst, memImpors)
      loc = initializeData(module, modInst, loc)
    }
    invokeStartFunction(module, modInst)
    modInst
  }

  private var initialized: Boolean = false

  inline def initializeThis(): Unit =
    if (!initialized) {
      globals.putNew(globalTableIndex)
      initialized = true
    }

  private inline def initializeExports(module: Module, modInst: ModuleInstance): Unit = {
    modInst.exports = module.exports.map {
      case Export(fieldName, kind, index) =>
        kind match {
          case ExternalKind.Function => (fieldName, ExternalValue.Function(index))
          case ExternalKind.Global => (fieldName, ExternalValue.Global(index))
          case ExternalKind.Memory => (fieldName, ExternalValue.Memory(index))
          case ExternalKind.Table => (fieldName, ExternalValue.Table(index))
        }
    }
  }

  private inline def initializeFunctions[J[_] <: MayJoin[_], FunV, FuncIx, ExcV, Size, Bytes, Addr, V](module: Module, modInst: ModuleInstance, funcImports: Vector[FunctionInstance]): Unit = {
    // types
    modInst.functionTypes = module.types

    // functions
    val funcImportsSize = funcImports.size
    funcImports.foreach(modInst.addFunction)
    module.funcs.view.zipWithIndex.map { (func, ix) =>
      FunctionInstance.Wasm(modInst, funcImportsSize + ix, func, module.types(func.tpe))
    }.foreach(modInst.addFunction)
  }

  private inline def initializeGlobalTypes(modInst: ModuleInstance, globTypes: Vector[GlobalType]): Unit =
    modInst.globalTypes = globTypes

  private inline def initializeGlobals(module: Module, modInst: ModuleInstance, globImports: Vector[GlobalAddr], initLoc: InstLoc)(using Fixed): InstLoc = {
    var loc = initLoc

    modInst.globalAddrs = globImports
    val globValues = module.globals.map { glob =>
      val id = BlockId(glob)
      loc = modInst.registerBlockSizes(id, loc, glob.init)
      evalInstructionSequence(id, glob.init, modInst, loc)
    }

    modInst.globalAddrs = modInst.globalAddrs :++ module.globals.zip(globValues).map {
      case (Global(GlobalType(tpe, _), _), value) =>
        val globalAddr = GlobalAddr(globCount)
        globCount += 1
        writeGlobalValue(globalAddr, value)
        globalAddr
    }

    modInst.globalTypes = modInst.globalTypes :++ module.globals.map {
      case Global(GlobalType(tpe, mut), _) => GlobalType(tpe, mut)
    }

    loc
  }

  private inline def initializeTables(module: Module, modInst: ModuleInstance, tabImports: Vector[TableAddr]): Unit = {
    // tables
    modInst.tableAddrs = tabImports ++ module.tables.map {
      case TableType(ty, Limits(min, max)) =>
        val tabAddr = TableAddr(tabCount)
        tables.putNew(tabAddr, SizedSymbolTable.Limit(valToSize(i32ops.integerLit(min)), max.map(m => valToSize(i32ops.integerLit(m)))))
        tabCount += 1
        tabAddr
    }
  }

  private inline def initializeMemory(module: Module, modInst: ModuleInstance, memImports: Vector[MemoryAddr]): Unit = {
    // memory
    modInst.memoryAddrs = memImports ++ module.mems.map {
      case MemType(Limits(min, max)) =>
        val initSize = valToSize(i32ops.integerLit(min))
        val sizeLimit = max.map(i => valToSize(i32ops.integerLit(i)))
        val memAddr = MemoryAddr(memCount)
        memory.putNew(memAddr, initSize, sizeLimit)
        memCount += 1
        memAddr
    }
  }

  private inline def initializeData(module: Module, modInst: ModuleInstance, initLoc: InstLoc)(using Fixed): InstLoc = {
    var loc = initLoc
    modInst.data = module.data.map {
      case Data(init, _) => DataInstance(init.toByteVector)
    }
    module.data.zipWithIndex.foreach {
      case (data@Data(init, mode), i) =>
        mode match {
          case DataMode.Passive => ()
          case DataMode.Active(memoryIdx, offset) =>
            val id = BlockId(data)
            loc = modInst.registerBlockSizes(id, loc, offset)
            callFrame.withNew(FrameData(None, 1, modInst), Iterable.empty, loc) {
              val baseAddr = evalInstructionSequence(id, offset, modInst, loc)
              stack.push(baseAddr)
              stack.push(num.evalNumeric(i32.Const(0)))
              stack.push(num.evalNumeric(i32.Const((init.size / 8).toInt))) //is it ok to convert long to int here?
              evalMemoryInst(MemoryInit(i))
              evalMemoryInst(DataDrop(i))
            }
        }
    }

    loc
  }

  private inline def initializeElements(module: Module, modInst: ModuleInstance, initLoc: InstLoc)(using Fixed): InstLoc =
    var loc = initLoc
    elems.putNew(())


    module.elem.zipWithIndex.foreach {
      case (elem, i) =>
        val id = BlockId(elem)
        val elemRefs = elem.init.map(expr => {
          loc = modInst.registerBlockSizes(id, loc, expr)
          evalInstructionSequence(id, expr, modInst, loc)
        })
        elems.set((), ElemAddr(i), elemRefs)
    }

    module.elem.zipWithIndex.foreach {
      case (elem@Elem(_, init, mode),i) =>
        mode match {
          case ElemMode.Passive() => ()
          case ElemMode.Declarative() =>
            callFrame.withNew(FrameData(None, 1, modInst), Iterable.empty, loc) {
              evalTableInst(ElemDrop(i), loc)
            }
          case ElemMode.Active(tableIdx, offset) =>
            val id = BlockId(module.elem(i))
            loc = modInst.registerBlockSizes(id, loc, offset)
            callFrame.withNew(FrameData(None, 1, modInst), Iterable.empty, loc) {
              val baseIdx = evalInstructionSequence(id, offset, modInst, loc)
              stack.push(baseIdx)
              stack.push(num.evalNumeric(i32.Const(0)))
              stack.push(num.evalNumeric(i32.Const(elem.init.length)))
              evalTableInst(TableInit(i, tableIdx), loc)
              evalTableInst(ElemDrop(i), loc)
            }
        }
    }

    loc


  private inline def invokeStartFunction(module: Module, modInst: ModuleInstance)(using Fixed): Unit = {
    // invoke the start function
    module.start.foreach {
      funcIdx =>
        val func = modInst.functions(funcIdx)
        func match
          case FunctionInstance.Wasm(mod, ix, func, funcType) =>
            val frameData = FrameData(None, funcType.t.size, mod)
            val vars = func.locals.map(ty => Some(num.defaultValue(ty)))
            val loc = InstLoc.InvokeExported(mod, "$start")
            labelStack.withNew(stack.withNewFrame(0)(callFrame.withNew(frameData, vars.view.zipWithIndex.map(_.swap), loc) {
              enterFunction(FuncId(mod, ix), func, funcType)
            }))
          case _: FunctionInstance.Host => ??? // TODO: is it allowed to use host functions as start function?
    }
  }