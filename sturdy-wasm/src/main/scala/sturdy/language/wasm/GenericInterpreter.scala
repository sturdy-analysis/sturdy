package sturdy.language.wasm

import swam.syntax.*

trait GenericInterpreter[V]:

  def eval(inst: Inst): V = inst match
    case _: AConst => ???
//    case _: Unop =>
//    case _: Binop =>
//    case _: Testop =>
//    case _: Relop =>
//    case _: Convertop =>
//    case _: Miscop =>
//    case _: MemoryInst =>
//    case _: VarInst =>
//    case _: Drop.type =>
//    case _: Select.type =>
//    case _: MemorySize.type =>
//    case _: MemoryGrow.type =>
//    case _: Nop.type =>
//    case _: Unreachable.type =>
//    case _: Block =>
//    case _: Loop =>
//    case _: If =>
//    case _: Br =>
//    case _: BrIf =>
//    case _: BrTable =>
//    case _: Return.type =>
//    case _: Call =>
//    case _: CallIndirect =>
