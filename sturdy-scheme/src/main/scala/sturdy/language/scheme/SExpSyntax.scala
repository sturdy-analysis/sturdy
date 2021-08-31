package sturdy.language.scheme

enum SExp:
  case id(id: String)
  case integer(i: Int)
  case rational(i1: Int, i2: Int)
  case double(d: Double)
  case string(str: String)
  case boolean(b: Boolean)
  case quoted(se: SExp)
  case sexpr(exprs: List[SExp])