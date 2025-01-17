package sturdy.language.wasm.abstractions

import sturdy.data.{noJoin, CombineEquiList}
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.fix
import sturdy.language.wasm.generic.{FixIn, FixOut, InstLoc, ModuleInstance}
import sturdy.values.*
import swam.{BlockType, OpCode, syntax}
import swam.syntax.{AConst, Binop, Block, Br, BrIf, BrTable, Call, CallIndirect, Convertop, Drop, GlobalGet, GlobalSet, If, Inst, LoadInst, LoadNInst, LocalGet, LocalSet, LocalTee, Loop, MemoryGrow, MemoryInst, MemorySize, Miscop, Nop, Relop, Return, Select, StoreInst, StoreNInst, Testop, Unop, Unreachable, VarInst, f32, f64, i32, i64}

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

trait InstructionResultLoggerFix[V](stack: DecidableOperandStack[V])(module : swam.syntax.Module)(using Top[V], Join[V]) extends InstructionLogger[List[V], V]:

  override def enterInfo(inst: Inst): Option[List[V]] = None

  override def exitInfo(inst: Inst, success: Boolean): Option[List[V]] =
    val n = writeToStack(inst)
    if(n > 0 && success) Some(stack.peekNOrAbort(n)) else None

  def writeToStack(inst: syntax.Inst): Int =
    inst match
      case _: AConst => 1
      case _: Unop => 1
      case _: Binop => 1
      case _: Testop => 1
      case _: Relop => 1
      case _: Convertop => 1
      case _: Miscop => 1
      case inst: MemoryInst => inst match
        case _: LoadInst => 1
        case _: LoadNInst => 1
        case _: StoreInst => 0
        case _: StoreNInst => 0
      case inst: VarInst => inst match
        case LocalGet(_) => 1
        case LocalSet(_) => 0
        case LocalTee(_) => 1
        case GlobalGet(_) => 1
        case GlobalSet(_) => 0
      case Drop => 0
      case Select => 1
      case MemorySize => 1
      case MemoryGrow => 1
      case Nop => 0
      case Unreachable => 0
      case Block(tpe, _) => tpe match
        case BlockType.FunctionType(tpe) => module.types(tpe).t.length
        case _ => 0
      case Loop(tpe, _) => tpe match
        case BlockType.FunctionType(tpe) => module.types(tpe).t.length
        case _ => 0
      case If(tpe, _, _) => tpe match
        case BlockType.FunctionType(tpe) => module.types(tpe).t.length
        case _ => 0
      case Br(_) => 0
      case BrIf(_) => 0
      case BrTable(_, _) => 0
      case Return => 0
      case Call(funcidx) => module.types(module.funcs(funcidx).tpe).t.length
      case CallIndirect(typeidx) => module.types(typeidx).t.length
