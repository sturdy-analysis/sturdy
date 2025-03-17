package sturdy.language.simplestack

enum Inst:
  case Unknown
  case Const(i: Int)
  case Add
  case Mul
  case Gt
  case JumpIf(pc: Int)
