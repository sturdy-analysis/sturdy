package sturdy.language.simplestack

enum Inst:
  case Unknown
  case Const(i: Int)
  case Dup
  case Add
  case Mul
  case Gt
  case JumpIf
