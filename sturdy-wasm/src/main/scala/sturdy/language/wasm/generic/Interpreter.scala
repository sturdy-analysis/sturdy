package sturdy.language.wasm.generic

import sturdy.effect.noJoin
import sturdy.effect.callframe.CMutableCallFrameInt
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import swam.syntax.*
import swam.OpCode
import swam.ValType
import sturdy.effect.operandstack.OperandStack
import sturdy.effect.binarymemory.{Serialize, Memory, MemSize, EffectiveAddress}
import sturdy.effect.branching.BoolBranching
import sturdy.values.conversion.*
import sturdy.values.doubles.DoubleOps
import sturdy.values.floats.FloatOps
import sturdy.values.ints.IntCompareOps
import sturdy.values.ints.IntOps
import sturdy.values.longs.LongOps
import sturdy.values.relational.CompareOps
import sturdy.values.relational.EqOps
import sturdy.values.unit
import swam.BlockType
import swam.LabelIdx

object Interpreter:
  case class FrameData[V](returnArity: Int, module: ModuleInstance[V])

  enum WasmException[V]:
    case Jump(labelIndex: LabelIdx, operands: List[V])
    case Return(operands: List[V])

  type WasmMemory[Addr,Bytes,Size,V] = Memory[Addr,Bytes,Size]
    with EffectiveAddress[V,Int,Addr]
    with MemSize[V,Size]
    with Serialize[V,Bytes,ValType,LoadType,StoreType]

  type Effects[V,Addr,Bytes,Size] =
    OperandStack[V]
      with WasmMemory[Addr,Bytes,Size,V]
      with CMutableCallFrameInt[FrameData[V], V]
      with BoolBranching[V]
      with Except[WasmException[V]]
      with Failure


import Interpreter.*

trait Interpreter[V,Addr,Bytes,Size]
  (using effectOps: Effects[V,Addr,Bytes,Size])
  (using IntOps[V], LongOps[V], FloatOps[V], DoubleOps[V], EqOps[V, V], CompareOps[V, V], IntCompareOps[V, V],
   ConvertIntLongOps[V, V], ConvertIntDoubleOps[V, V], ConvertLongDoubleOps[V, V], ConvertIntFloatOps[V, V], ConvertLongFloatOps[V, V], ConvertFloatDoubleOps[V, V])
  (using wasmOps: WasmOperations[V])
  (using effectOps.BoolBranchJoin[Unit],
   effectOps.MemoryJoin[Unit], effectOps.MemoryJoin[V], effectOps.MemoryJoinComp,
   wasmOps.WasmOpsJoin[Unit], wasmOps.WasmOpsJoinComp):

  import effectOps.*
  val stack = effectOps.asInstanceOf[OperandStack[V]]
  //val memory = effectOps.asInstanceOf[WasmMemory[Addr,Bytes,Size,V]]

  val numerics = new InterpretNumerics[V]
  import numerics.*
  import wasmOps.*

  val labelStack = new LabelStack

  inline private def fail(k: FailureKind, what: String) = effectOps.fail(k, s"$what in $module")

  def module: ModuleInstance[V] = getFrameData.module

  def eval(inst: Inst): Unit =
    val opcode = inst.opcode
    if (opcode >= OpCode.I32Const && opcode <= OpCode.I64Extend32S)
      val v = evalNumeric(inst)
      stack.push(v)
    else if (opcode >= OpCode.I32Load8S && opcode <= OpCode.MemoryGrow)
      evalMemoryInst(inst)
    else if (opcode >= OpCode.Nop && opcode <= OpCode.CallIndirect)
      evalControlInst(inst)
    else inst match
      case i: VarInst => evalVarInst(i)
      case op: Miscop =>
        val v = stack.pop()
        evalMiscop(op, v)
      case Drop => stack.pop()
      case Select =>
        val isZero = evalNumeric(i32.Eqz)
        boolBranch[Unit](isZero) {
          // v == 0: else branch
          val (_, v2) = stack.pop2()
          stack.push(v2)
        } {
          stack.pop()
        }
      case _ => throw new IllegalArgumentException(s"Unexpected instruction $inst")

  def evalVarInst(inst: VarInst): Unit = inst match
    case LocalGet(ix) =>
      val v = getLocal(ix).orElse(fail(UnboundLocal, ix.toString))
      stack.push(v)
    case LocalSet(ix) =>
      val v = stack.pop()
      setLocal(ix, v).orElse(fail(UnboundLocal, ix.toString))
    case LocalTee(ix) =>
      val v = stack.peek()
      setLocal(ix, v).orElse(fail(UnboundLocal, ix.toString))
    case GlobalGet(globalIx) =>
      val global = module.globals.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      stack.push(global.value)
    case GlobalSet(globalIx) =>
      val global = module.globals.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val v = stack.pop()
      global.value = v


  def evalControlInst(inst: Inst): Unit = inst match
    case Nop => // nothing
    case Unreachable => fail(UnreachableInstruction, inst.toString)
    case Block(bt, insts) => label(returnArity(bt), insts, None)
    case Loop(bt, insts) => label(paramsArity(bt), insts, Some(inst))
    case If(bt, thnInsts, elsInsts) =>
      val isZero = evalNumeric(i32.Eqz)
      val rt = returnArity(bt)
      boolBranch[Unit](isZero) {
        // v == 0: else branch
        label(rt, elsInsts, None)
      } {
        label(rt, thnInsts, None)
      }
    case Br(labelIndex) => branch(labelIndex)
    case BrIf(labelIndex) =>
      val isZero = evalNumeric(i32.Eqz)
      boolBranch[Unit](isZero) {
        // v == 0: else branch
        // do nothing
      } {
        branch(labelIndex)
      }
    case BrTable(labels, defaultLabel) =>
      val ix = stack.pop()
      indexLookup(ix, labels).orElseAndThen(defaultLabel)(branch)
    case Return =>
      val operands = stack.popN(getFrameData.returnArity)
      throws(WasmException.Return(operands))
    case Call(funcIx) =>
      val func = module.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
      invoke(func)
    case CallIndirect(typeIx) =>
      val table = module.tables(0)
      val ftExpected = module.functionTypes(typeIx)
      val funcIx = stack.pop()
      indexLookup(funcIx, table.functions).orElseAndThen(fail(UnboundFunctionIndex, funcIx.toString)) { func =>
        if (func == null)
          fail(UninitializedFunction, funcIx.toString)
        val ftActual = func.funcType
        if (ftExpected != ftActual)
          fail(IndirectCallTypeMismatch, s"Expected function of type $ftExpected but $funcIx has type $ftActual")
        invoke(func)
      }
    case _ => throw new IllegalArgumentException(s"Expected control instruction, but got $inst")


  def branch(labelIndex: LabelIdx): Unit =
    val returnArity: Int = labelStack.lookupLabel(labelIndex)
    val operands = stack.popN(returnArity)
    throws(WasmException.Jump(labelIndex, operands))

  def label(returnArity: Int, insts: Iterable[Inst], branchTarget: Option[Inst]): Unit =
    catchFinally {
      labelStack.pushLabel(returnArity)
      stack.restoreAfter {
        insts.foreach(eval)
      }
    } { // catch
      case WasmException.Jump(labelIndex, operands) =>
        if (labelIndex == 0) {
          stack.pushN(operands)
          branchTarget.foreach(eval)
        } else {
          throws(WasmException.Jump(labelIndex - 1, operands))
        }
      case ex => throws(ex)
    } { // finally
      labelStack.popLabel()
    }

  def invoke(func: FunctionInstance[V]): Unit = func match
    case FunctionInstance.Wasm(mod, func, funcType) =>
      val args = stack.popN(funcType.params.size)
      val frameData = FrameData(funcType.t.size, mod)
      val vars = args.view.reverse ++ func.locals.map(defaultValue)
      withFreshOperandStack(labelStack.withFresh(inNewFrame(frameData, vars) {
        label(funcType.t.size, func.body, None)
      }))


  private def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => evalNumeric(i32.Const(0))
    case ValType.I64 => evalNumeric(i64.Const(0))
    case ValType.F32 => evalNumeric(f32.Const(0))
    case ValType.F64 => evalNumeric(f64.Const(0))

  private def returnArity(bt: BlockType): Int =
    val returnArity = bt.arity(module.functionTypes)
    if (returnArity < 0)
      fail(UnboundFunctionType, bt.toString)
    else
      returnArity

  private def paramsArity(bt: BlockType): Int =
    bt.params(module.functionTypes) match
      case Some(params) => params.size
      case None => fail(UnboundFunctionType, bt.toString)


  def evalMemoryInst(inst: Inst): Unit = inst match
    case i32.Load(align, offset) => load(4, LoadType.L_I32, ValType.I32, offset)
    case i64.Load(align, offset) => load(8, LoadType.L_I64, ValType.I64, offset)
    case f32.Load(align, offset) => load(4, LoadType.L_F32, ValType.F32, offset)
    case f64.Load(align, offset) => load(8, LoadType.L_F64, ValType.F64, offset)
    case i32.Load8S(align, offset) => load(1, LoadType.L_I8S, ValType.I32, offset)
    case i32.Load8U(align, offset) => load(1, LoadType.L_I8U, ValType.I32, offset)
    case i32.Load16S(align, offset) => load(2, LoadType.L_I16S, ValType.I32, offset)
    case i32.Load16U(align, offset) => load(2, LoadType.L_I16U, ValType.I32, offset)
    case i64.Load8S(align, offset) => load(1, LoadType.L_I8S, ValType.I64, offset)
    case i64.Load8U(align, offset) => load(1, LoadType.L_I8U, ValType.I64, offset)
    case i64.Load16S(align, offset) => load(2, LoadType.L_I16S, ValType.I64, offset)
    case i64.Load16U(align, offset) => load(2, LoadType.L_I16U, ValType.I64, offset)
    case i64.Load32S(align, offset) => load(4, LoadType.L_I32S, ValType.I64, offset)
    case i64.Load32U(align, offset) => load(4, LoadType.L_I32U, ValType.I64, offset)

    case i32.Store(align, offset) => store(StoreType.S_I32, ValType.I32, offset)
    case i64.Store(align, offset) => store(StoreType.S_I64, ValType.I64, offset)
    case f32.Store(align, offset) => store(StoreType.S_F32, ValType.F32, offset)
    case f64.Store(align, offset) => store(StoreType.S_F64, ValType.F64, offset)
    case i32.Store8(align, offset) => store(StoreType.S_I8, ValType.I32, offset)
    case i32.Store16(align, offset) => store(StoreType.S_I16, ValType.I32, offset)
    case i64.Store8(align, offset) => store(StoreType.S_I8, ValType.I64, offset)
    case i64.Store16(align, offset) => store(StoreType.S_I16, ValType.I64, offset)
    case i64.Store32(align, offset) => store(StoreType.S_I32, ValType.I64, offset)

    case MemorySize =>
      val memIdx = 0 //TODO: memoryIndex()
      stack.push(sizeToVal(memSize(memIdx)))
    case MemoryGrow =>
      val delta = valToSize(stack.pop())
      val memIdx = 0 //TODO: memoryIndex()
      val res = memGrow(memIdx, delta).withDefault
        (evalNumeric(i32.Const(0xFFFFFFFF))) // 0xFFFFFFFF ~= -1
        {sizeToVal(_)}
    case _ => throw new IllegalArgumentException(s"Expected memory instruction, but got $inst")

  def load(byteSize: Int, loadType: LoadType, valType: ValType, offset: Int): Unit =
    val base = stack.pop()
    val addr = effectiveAddress(base, offset)
    val memIdx = 0 //TODO: memoryIndex()
    val bytes = memRead(memIdx,addr,byteSize).withDefault
      (fail(MemoryAccessOutOfBounds, s"Cannot read $byteSize bytes at address $addr in current memory."))
      {(b: Bytes) =>
        val v = decode(b, loadType, valType)
        stack.push(v)}

  def store(storeType: StoreType, valType: ValType, offset: Int): Unit =
    val v = stack.pop()
    val bytes = encode(v, valType, storeType)
    val base = stack.pop()
    val addr = effectiveAddress(base, offset)
    val memIdx = 0 //TODO: memoryIndex()
    memStore(memIdx, addr, bytes).orElse(
      fail(MemoryAccessOutOfBounds, s"Cannot write $bytes at address $addr in current memory.")
    )

// TODO: the instructions provide sufficient information, no need to duplicate that
enum LoadType:
  case L_I32, L_I64, L_F32, L_F64, L_I8S, L_I8U, L_I16S, L_I16U, L_I32S, L_I32U

enum StoreType:
  case S_I32, S_I64, S_F32, S_F64, S_I8, S_I16