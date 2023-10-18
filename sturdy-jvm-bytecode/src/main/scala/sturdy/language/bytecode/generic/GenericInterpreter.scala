package sturdy.language.bytecode.generic

import org.opalj.br.instructions.*
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.data.MayJoin
import sturdy.data.noJoin
import sturdy.effect.callframe.{DecidableCallFrame, DecidableMutableCallFrame}
import sturdy.effect.store.Store
import sturdy.values.booleans.BooleanBranching

/*

1. alles was einfach ist
2. invoke static: how to manage operand stack
3. read up on exceptions in Sturdy
4. how do jumps/branching work on the JVM

  i1
  i2
l0:
  i3
  jump l3
  i5
l3:
  jump l0

val jumpTargets: Map[String, InstructionIndex]

 */

trait GenericInterpreter[V]:
  val bytecodeOps: BytecodeOps[V]

  val stack: DecidableOperandStack[V]

  type FrameData = Unit
  val frame: DecidableMutableCallFrame[FrameData, Int, V]

  lazy val num = new GenericInterpreterNumerics[V](bytecodeOps)

  def run(insts: Iterable[Instruction]): Unit =
    insts.foreach(eval)

  def eval(inst: Instruction): Unit = inst.opcode match
    // No Op
    case x if (x == 0) =>
      ???

    // push NULL on stack
    case x if (x == 1) =>
      ???

    // Lit Ops
    case x if (2 <= x && x <= 17) =>
      stack.push(num.evalNumericOp(inst))

    // LDC
    case x if (x == 18) =>
      inst match
        case inst: LoadInt =>
          stack.push(num.evalNumericOp(inst))
        case inst: LoadFloat =>
          stack.push(num.evalNumericOp(inst))
        case inst: LoadClass =>
          ???
        case inst: LoadString =>
          ???
        case inst: LoadMethodHandle =>
          ???
        case inst: LoadMethodType =>
          ???

    // LDC_W
    case x if (x == 19) =>
      inst match
        case inst: LoadInt_W =>
          ???
        case inst: LoadFloat_W =>
          ???
        case inst: LoadClass_W =>
          ???
        case inst: LoadString_W =>
          ???
        case inst: LoadMethodHandle_W =>
          ???
        case inst: LoadMethodType_W =>
          ???

    // LDC2_W
    case x if (x == 20) =>
      stack.push(num.evalNumericOp(inst))

    // load Local variable
    case x if (21 <= x && x <= 45) =>
      stack.push(eval_local_load(inst))

    //load from array
    case x if (46 <= x && x <= 53) =>
      ???

    // store local variable
    case x if (54 <= x && x <= 78) =>
      val v1 = stack.popOrAbort()
      eval_local_store(inst, v1)

    // store in array
    case x if (79 <= x && x <= 86) =>
      val v1 = stack.popOrAbort()
      ???

    // Manip stack
    case x if (87 <= x && x <= 95) =>
      inst match
        case inst: POP.type =>
          stack.pop()
        case inst: POP2.type =>
          stack.pop2()
        case inst: DUP.type =>
          ???
        case inst: DUP_X1.type =>
          ???
        case inst: DUP_X2.type =>
          ???
        case inst: DUP2.type =>
          ???
        case inst: DUP2_X1.type =>
          ???
        case inst: DUP2_X2.type =>
          ???
        case inst: SWAP.type =>
          ???


    // Arithmetic Ops
    case x if (96 <= x && x <= 115) =>
      val (v1, v2) = stack.pop2OrAbort()
      stack.push(num.evalNumericBinOp(inst, v1, v2))

    // Negation Ops
    case x if (116 <= x && x <= 119) =>
      val v1 = stack.popOrAbort()
      stack.push(num.evalNumericUnOp(inst, v1))

    // Bitshift Ops
    case x if (120 <= x && x <= 131) =>
      val (v1, v2) = stack.pop2OrAbort()
      stack.push(num.evalNumericBinOp(inst, v1, v2))

    // iinc
    case x if (x == 132) =>
      ???

    // Conversions
    case x if (133 <= x && x <= 147) =>
      val v1 = stack.popOrAbort()
      stack.push(num.evalConvertOp(inst, v1))

    // Numeric Comparison
    case x if (148 <= x && x <= 152) =>
      val (v1, v2) = stack.pop2OrAbort()
      stack.push(num.evalNumericBinOp(inst, v1, v2))

    // Branching
    case x if (153 <= x && x <= 166) =>
      ???

    // JUMPS
    case x if (167 <= x && x <= 171) =>
      ???

    // Return
    case x if (172 <= x && x <= 177) =>
      ???

    // Load and Store Statics
    case x if (178 <= x && x <= 179) =>
      ???

    // Load and Store Fields
    case x if (180 <= x && x <= 181) =>
      ???

    // Invoke Functions
    case x if (182 <= x && x <= 186) =>
      val newFrameData = ()
      val args: List[V] = ???
      frame.withNew(newFrameData, args.zipWithIndex.map(_.swap)) {

      }
      ???

    // NEW
    case x if (x == 187) =>
      ???

    // Arrays
    case x if (188 <= x && x <= 190) =>
      ???

    // athrow
    case x if (x == 191) =>
      ???

    // checkcast
    case x if (x == 192) =>
      ???

    // instanceof
    case x if (x == 193) =>
      ???

    // monitorenter
    case x if (x == 194) =>
      ???

    // monitorexit
    case x if (x == 195) =>
      ???

    // WIDE
    case x if (x == 196) =>
      ???

    // multianewarray
    case x if (x == 197) =>
      ???

    // ifnull, ifnonnull
    case x if (198 <= x && x <= 199) =>
      ???

    // goto_w
    case x if (x == 200) =>
      ???

    // jsr_wt
    case x if (x == 201) =>
      ???

    // breakpoint
    case x if (x == 202) =>
      ???

  def eval_local_load(inst: Instruction): V = inst match
    case inst: LoadLocalVariableInstruction => ???//frame.getLocal(inst.lvIndex)

  def eval_local_store(inst: Instruction, v: V): Unit = inst match
    case inst: StoreLocalVariableInstruction => frame.setLocal(inst.lvIndex, v)

  def eval_array_load(inst: Instruction): V = inst match
    case inst: IALOAD.type =>
      ???

  def eval_array_store(inst: Instruction, v: V): Unit = inst match
    case inst: IASTORE.type =>
      ???

  /*def invokeStatic(c: String, m: String, args: List[V]) =
    val cls = ???
    val mth = ???
    val params = ???

    stack.withNewFrame {
      frame.withNew(???, ???) {

      }
    }*/

