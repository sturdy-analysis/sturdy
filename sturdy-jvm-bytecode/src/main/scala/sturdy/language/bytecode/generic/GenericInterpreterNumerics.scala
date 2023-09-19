package sturdy.language.bytecode.generic

import sturdy.data.MayJoin
import sturdy.data.noJoin
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.config
import sturdy.values.convert.*
import sturdy.values.floating.*
import sturdy.values.integer.*
import org.opalj.br.instructions.*
import sturdy.language.bytecode.generic.AnnotatedInstruction


class GenericInterpreterNumerics[V]
  (stack: DecidableOperandStack[V], bytecodeOps: BytecodeOps[V]):


  import bytecodeOps.*


  def evalNumeric(inst: AnnotatedInstruction): V =
    val instruction = inst.instruction
    val tempOP = inst.annoOP

    tempOP match
      case op.unOP =>
        evalUnOP(instruction)

      case op.binOP =>
        val (v1, v2) = stack.pop2OrAbort()
        evalBinOP(inst.instruction, v1, v2)

  def evalUnOP(inst: Instruction): V =
    inst match
      case inst: BIPUSH =>
        i32ops.integerLit(inst.value)
  def evalBinOP(inst: Instruction, v1: V, v2: V): V =
    inst match
      case IADD =>
        i32ops.add(v1, v2)



