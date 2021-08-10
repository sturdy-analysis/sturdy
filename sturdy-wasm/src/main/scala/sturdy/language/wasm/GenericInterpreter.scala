package sturdy.language.wasm

import swam.syntax.*

trait GenericInterpreter[V]:

  def eval(inst: Inst): V = inst match
    case i32.Const(v) => ???
    case i64.Const(v) => ???
    case f32.Const(v) => ???
    case f64.Const(v) => ???
    case op : IUnop =>
      val v = ??? // stack.pop()
      evalIUnop(op, v)
    case op : FUnop =>
      val v = ??? // stack.pop()
      evalFUnop(op, v)
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

  def evalIUnop(op: IUnop, v: V): V = op match
    case i32.Clz => ???
//    case i32.Ctz =>
//    case i32.Popcnt =>
//    case i32.Extend8S =>
//    case i32.Extend16S =>
//    case i64.Clz =>
//    case i64.Ctz =>
//    case i64.Popcnt =>
//    case i64.Extend8S =>
//    case i64.Extend16S =>
//    case i64.Extend32S =>

  def evalFUnop(op: FUnop, v: V): V = op match
    case f32.Abs => ???
//    case f32.Neg =>
//    case f32.Sqrt =>
//    case f32.Ceil =>
//    case f32.Floor =>
//    case f32.Trunc =>
//    case f32.Nearest =>
//    case f64.Abs =>
//    case f64.Neg =>
//    case f64.Sqrt =>
//    case f64.Ceil =>
//    case f64.Floor =>
//    case f64.Trunc =>
//    case f64.Nearest =>
