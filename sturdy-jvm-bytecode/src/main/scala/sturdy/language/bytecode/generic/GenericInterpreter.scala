package sturdy.language.bytecode.generic

import org.opalj.br.instructions.*
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.data.MayJoin
import sturdy.data.noJoin

trait GenericInterpreter[V]:
  val bytecodeOps: BytecodeOps[V]

  val stack: DecidableOperandStack[V]

  lazy val num = new GenericInterpreterNumerics[V](bytecodeOps)

  def eval(inst: Instruction): Unit = inst.opcode match
    //Lit Ops
    case x if (2 <= x && x <= 17) =>
      stack.push(num.evalNumericOp(inst))

    //LDC
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

    //LDC_W
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

    //LDC2_W
    case x if (x == 20) =>
      stack.push(num.evalNumericOp(inst))

    //Arithmetic Ops
    case x if (96 <= x && x <= 115) =>
      val (v1, v2) = stack.pop2OrAbort()
      stack.push(num.evalNumericBinOP(inst, v1, v2))

    //Negation Ops
    case x if (116 <= x && x <= 119) =>
      val v1 = stack.popOrAbort()
      stack.push(num.evalNumericUnOP(inst, v1))

    //Bitshift Ops
    case x if (120 <= x && x <= 131) =>
      val (v1, v2) = stack.pop2OrAbort()
      stack.push(num.evalNumericBinOP(inst, v1, v2))


