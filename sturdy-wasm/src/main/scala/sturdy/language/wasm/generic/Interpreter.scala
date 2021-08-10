package sturdy.language.wasm.generic

import sturdy.effect.callframe.CMutableCallFrameInt
import sturdy.effect.failure.{Failure, FailureKind}
import swam.syntax.*
import swam.OpCode
import swam.ValType
import sturdy.effect.operandstack.OperandStack
import sturdy.effect.binarymemory.{EffectiveAddress, Memory, Serialize, MemSize}
import sturdy.values.unit

object Interpreter:
  case object UnreachableInstruction extends FailureKind
  case object UnboundLocal extends FailureKind

  type WasmMemory[Addr,Bytes,Size,V] = Memory[Addr,Bytes,Size]
    with EffectiveAddress[V,Int,Addr]
    with MemSize[V,Size]
    with Serialize[V,Bytes,ValType,LoadType,StoreType]

  type Effects[V,Addr,Bytes,Size] =
    OperandStack[V]
      with WasmMemory[Addr,Bytes,Size,V]
      with CMutableCallFrameInt[Unit, V]
      with Failure

  case class Trap() extends FailureKind

import Interpreter.*

trait Interpreter[V,Addr,Bytes,Size]
  (using effectOps: Effects[V,Addr,Bytes,Size])
  (using valueOps: ValueOps[V])
  :

  import effectOps.*
  val stack = effectOps.asInstanceOf[OperandStack[V]]
  val memory = effectOps.asInstanceOf[WasmMemory[Addr,Bytes,Size,V]]

  import valueOps.*

  val numerics = new InterpreterNumerics[V]
  import numerics.*

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
        val c = stack.pop()
        i32NeqZero(c, stack.pop(), {
          val (_, v2) = stack.pop2()
          stack.push(v2)
        })
      case _ => throw new IllegalArgumentException(s"Unexpected instruction $inst")

  def evalMemoryInst(inst: Inst): Unit = inst match
    case i32.Load(align, offset) => ???
    case _: LoadNInst => ???
    case _: StoreInst => ???
    case _: StoreNInst => ???
    case MemorySize => ???
    case MemoryGrow => ???
    case _ => throw new IllegalArgumentException(s"Expected memory instruction, but got $inst")

  def evalVarInst(inst: VarInst): Unit = inst match
    case LocalGet(ix) =>
      getLocalOrElseAndThen(ix, fail(UnboundLocal, ix.toString))(
        stack.push
      )
    case LocalSet(ix) =>
      val v = stack.pop()
      setLocal(ix, v, fail(UnboundLocal, ix.toString))
    case LocalTee(ix) =>
      val v = stack.peek()
      setLocal(ix, v, fail(UnboundLocal, ix.toString))
    case _: GlobalGet => ???
    case _: GlobalSet => ???

  def evalControlInst(inst: Inst): Unit = inst match
    case Nop => // nothing
    case Unreachable => fail(UnreachableInstruction, inst.toString)
//    case _: Block => ???
//    case _: Loop => ???
//    case _: If => ???
//    case _: Br => ???
//    case _: BrIf => ???
//    case _: BrTable => ???
//    case _: Return => ???
//    case _: Call => ???
//    case _: CallIndirect => ???
    case _ => throw new IllegalArgumentException(s"Expected control instruction, but got $inst")

  def load(byteSize: Int, loadType: LoadType, valType: ValType, offset: Int): Unit =
    val base = stack.pop()
    val addr = memory.effectiveAddress(base, offset)
    val memIdx = 0 //TODO: memoryIndex()
    memory.memRead(memIdx,addr,byteSize,
      bytes => {
        val v = memory.decode(bytes, loadType, valType)
        stack.push(v)
      },
      () => fail(Trap(), s"Memory access out of bounds: Cannot read $byteSize bytes at address $addr in current memory."))

sealed abstract class LoadType
case class L_I32() extends LoadType
case class L_I64() extends LoadType
case class L_F32() extends LoadType
case class L_F64() extends LoadType
case class L_I8S() extends LoadType
case class L_I8U() extends LoadType
case class L_I16S() extends LoadType
case class L_I16U() extends LoadType
case class L_I32S() extends LoadType
case class L_I32U() extends LoadType

sealed abstract class StoreType
case class S_I32() extends StoreType
case class S_I64() extends StoreType
case class S_F32() extends StoreType
case class S_F64() extends StoreType
case class S_I8() extends StoreType
case class S_I16() extends StoreType
