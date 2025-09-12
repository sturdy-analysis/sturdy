package sturdy.language.wasm.abstractions

import sturdy.data.{*, given}
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.OperandStack
import sturdy.fix
import sturdy.language.wasm.generic.{FixIn, FixOut}
import sturdy.language.wasm.generic.InstLoc
import sturdy.values.*
import swam.OpCode
import swam.syntax
import swam.syntax.Inst

trait InstructionLogger[Info, V](using Join[Info]) extends fix.Logger[FixIn, FixOut[V]]:

  def enterInfo(inst: syntax.Inst, loc: InstLoc): Option[Info]
  def exitInfo(inst: syntax.Inst, loc: InstLoc, success: Boolean): Option[Info]

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
    case FixIn.Eval(inst, loc) => enterInfo(inst, loc) match
      case Some(info) => addInstruction(inst, loc, info)
      case None => // nothing
    case _ => // nothing

  override def exit(dom: FixIn, codom: TrySturdy[FixOut[V]]): Unit = dom match
    case FixIn.Eval(inst, loc) => exitInfo(inst, loc, codom.isSuccess) match
      case Some(info) => addInstruction(inst, loc, info)
      case None => // nothing
    case _ => // nothing


trait InstructionResultLogger[Info, V](stack: OperandStack[V, NoJoin])(using Top[V], Join[V]) extends InstructionLogger[List[Info], V]:
  def boolValue(v: V): V
  def getInfo(v: V): Info

  override def enterInfo(inst: Inst, loc: InstLoc): Option[List[Info]] =
    infoArity(inst, loc) match
      case InfoArity.Input(n, boolean) =>
        val values = stack.peekNOrAbort(n)
        val converted = if(boolean) values.map(boolValue) else values
        Some(converted.map(getInfo))
      case _: InfoArity.Output | _: InfoArity.NoInfo.type =>
        None

  override def exitInfo(inst: Inst, loc: InstLoc, success: Boolean): Option[List[Info]] =
    infoArity(inst, loc) match
      case InfoArity.Output(n, boolean) if(success) =>
        val values = stack.peekNOrAbort(n)
        val converted = if(boolean) values.map(boolValue) else values
        Some(converted.map(getInfo))
      case _: InfoArity.Output | _: InfoArity.Input | _: InfoArity.NoInfo.type =>
        None

  enum InfoArity:
    case Input(pop: Int, boolean: Boolean = false)
    case Output(push: Int, boolean: Boolean = false)
    case NoInfo

  private def infoArity(inst: syntax.Inst, loc: InstLoc): InfoArity = {
    import InfoArity.*
    import syntax.*
    inst match
      case _: Nop.type | _: Unreachable.type |
           _: Drop.type | _: ElemDrop | _: DataDrop |
           _: Return.type | _: Br
        => NoInfo
      case _: If | _: BrIf
        => Input(1, boolean = true)
      case _: Relop | _: VectorRelop |
           _: Testop | _: VectorTestop | _: VVectorTestop
        => Output(1, boolean = true)
      case _: BrTable |
           _: LocalSet | _: LocalTee | _: GlobalSet | _: TableSet |
           _: MemoryGrow.type | _: TableGrow
        => Input(1)
      case _: StoreInst | _: StoreNInst | _: VectorStoreInst
        => Input(2)
      case _: MemoryInit | _: MemoryFill.type | _: MemoryCopy.type |
           _: TableInit | _: TableFill | _: TableCopy
        => Input(3)
      case _: AConst | _: v128.Const |
           _: Unop | _: VectorUnop | _: VVectorUnop |
           _: Binop | _: VectorBinop | _: VVectorBinop |
           _: VectorTernop |
           _: Select.type | _: SelectReturns |
           _: Convertop | _: SatConvertop | _: VectorConvertop |
           _: ReferenceInst |
           _: LocalGet | _: GlobalGet | _: TableGet |
           _: LoadInst | _: LoadNInst | _: VectorLoadInst |
           _: i8x16.Swizzle.type | _:i8x16.Shuffle | _: VectorDot | _: VectorSplat |
           _: VectorExtractLane | _: VectorReplaceLane |
           _: VectorBitmask | _: VectorShiftop | _: VectorExtmul | _: VectorExtadd |
           _: MemorySize.type | _: TableSize
        => Output(1)
      case block: Block
        => Output(block.tpe.arity(loc.module.functionTypes))
      case loop: Loop
        => Output(loop.tpe.arity(loc.module.functionTypes))
      case syntax.Call(idx) =>
        val function = loc.module.functions.lift(idx).getOrElse(throw Error(s"Function index $idx not in module ${loc.module}"))
        Output(function.funcType.t.size)
      case syntax.CallIndirect(_, typeIdx) =>
        val funType = loc.module.functionTypes.lift(typeIdx).getOrElse(throw Error(s"Type index $typeIdx not in module ${loc.module}"))
        Output(funType.t.size)
  }
