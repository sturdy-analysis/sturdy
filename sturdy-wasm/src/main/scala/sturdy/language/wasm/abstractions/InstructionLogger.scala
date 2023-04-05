package sturdy.language.wasm.abstractions

import sturdy.data.{noJoin, CombineEquiList}
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.fix
import sturdy.language.wasm.generic.{FixIn, FixOut}
import sturdy.language.wasm.generic.InstLoc
import sturdy.values.*
import swam.OpCode
import swam.syntax
import swam.syntax.Inst

trait InstructionLogger[Info, V](using Join[Info]) extends fix.Logger[FixIn, FixOut[V]]:

  def enterInfo(inst: syntax.Inst): Option[Info]
  def exitInfo(inst: syntax.Inst, success: Boolean): Option[Info]

  var instructionInfo: Map[InstLoc, Info] = Map()
  var instructions: Map[InstLoc, syntax.Inst] = Map()

  private def addInstruction(inst: syntax.Inst, loc: InstLoc, v: Info): Unit =
    instructions += loc -> inst
    instructionInfo.get(loc) match
      case None =>
        instructionInfo += loc -> v
      case Some(previousResult) =>
        val joined = Join(previousResult, v).get
        instructionInfo += loc -> joined

  override def enter(dom: FixIn): Unit = dom match
    case FixIn.Eval(inst, loc) => enterInfo(inst) match
      case Some(info) => addInstruction(inst, loc, info)
      case None => // nothing
    case _ => // nothing

  override def exit(dom: FixIn, codom: TrySturdy[FixOut[V]]): Unit = dom match
    case FixIn.Eval(inst, loc) => exitInfo(inst, codom.isSuccess) match
      case Some(info) => addInstruction(inst, loc, info)
      case None => // nothing
    case _ => // nothing


trait InstructionResultLogger[V](stack: DecidableOperandStack[V])(using Top[V], Join[V]) extends InstructionLogger[List[V], V]:
  def boolValue(v: V): V
  def dummyValue: V

  override def enterInfo(inst: Inst): Option[List[V]] =
    if (readsSingleValueFromStack(inst)) {
      val value = stack.peekOrAbort()
      Some(List(value))
    } else if (readsSingleBooleanFromStack(inst)) {
      val v = boolValue(stack.peekOrAbort())
      Some(List(v))
    } else inst match {
      case _: syntax.StoreInst | _: syntax.StoreNInst =>
        val values = stack.peekNOrAbort(2)
        Some(values)
      case _ => None
    }

  override def exitInfo(inst: Inst, success: Boolean): Option[List[V]] =
    if (inst == syntax.Nop || inst == syntax.Unreachable) {
      Some(List(dummyValue))
    } else if (writesSingleValueToStack(inst)) {
      val result = if (success) stack.peekOrAbort() else Top.top[V]
      Some(List(result))
    } else inst match {
      case _: syntax.LoadInst | _: syntax.LoadNInst =>
        val loaded = stack.peekOrAbort()
        val values = List(loaded)
        Some(values)
      case _ => None
    }

  def writesSingleValueToStack(inst: syntax.Inst): Boolean =
    val opcode = inst.opcode
    if (opcode >= OpCode.I32Eqz && opcode <= OpCode.I64Extend32S)
      return true

    inst match {
      case _: syntax.LocalGet | _: syntax.GlobalGet => true
      case _: syntax.MemorySize.type | _: syntax.LoadInst | _: syntax.LoadNInst => true
      case _: syntax.Miscop => true
      case _ => false
    }

  def readsSingleValueFromStack(inst: syntax.Inst): Boolean = inst match
    case _: syntax.LocalSet | _: syntax.GlobalSet | _: syntax.LocalTee => true
    case _: syntax.StoreInst | _: syntax.StoreNInst => true
    case _: syntax.Select.type => true
    case _: syntax.BrTable | _: syntax.CallIndirect => true
    case _ => false

  def readsSingleBooleanFromStack(inst: syntax.Inst): Boolean = inst match
    case _: syntax.If | _: syntax.BrIf => true
    case _: syntax.Select.type => true
    case _ => false
