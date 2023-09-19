package sturdy.language.bytecode.generic

import org.opalj.br.instructions.*
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.floating.*
import sturdy.values.integer.*

trait GenericInterpreter[V]:
  val bytecodeOps: BytecodeOps[V]

  val stack: DecidableOperandStack[V]

  lazy val num = new GenericInterpreterNumerics[V](stack, bytecodeOps)

  def eval(inst: Instruction): Unit = inst match
    case inst =>
      stack.push(num.evalNumeric(AnnotatedInstruction(inst)))


