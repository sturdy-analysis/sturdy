package sturdy.language.wasm.generic

import sturdy.effect.failure.{Failure, FailureKind}
import swam.syntax.*
import swam.OpCode
import sturdy.effect.operandstack.OperandStack


object Interpreter:
  case object UnreachableInstruction extends FailureKind

  type Effects[V] = OperandStack[V] with Failure

import Interpreter.*

trait Interpreter[V]
  (using effectOps: Effects[V])
  (using valueOps: ValueOps[V])
  :

  import effectOps.*
  val stack = effectOps.asInstanceOf[OperandStack[V]]

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
    case _: LoadInst => ???
    case _: LoadNInst => ???
    case _: StoreInst => ???
    case _: StoreNInst => ???
    case MemorySize => ???
    case MemoryGrow => ???
    case _ => throw new IllegalArgumentException(s"Expected memory instruction, but got $inst")

  def evalVarInst(inst: VarInst): Unit = inst match
    case _: LocalGet => ???
    case _: LocalSet => ???
    case _: LocalTee => ???
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
