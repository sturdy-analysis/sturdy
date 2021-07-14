package sturdy.language.whilelang

import sturdy.util.Labeled

enum Expr extends Labeled:
  case Var(s: String)
  case BoolLit(b: Boolean)
  case And(e1: Expr, e2: Expr)
  case Or(e1: Expr, e2: Expr)
  case Not(e: Expr)
  case NumLit(n: Double)
  case RandomDouble()
  case Add(e1: Expr, e2: Expr)
  case Sub(e1: Expr, e2: Expr)
  case Mul(e1: Expr, e2: Expr)
  case Div(e1: Expr, e2: Expr)
  case Eq(e1: Expr, e2: Expr)
  case Lt(e1: Expr, e2: Expr)

enum Statement extends Labeled:
  case Assign(s: String, e: Expr)
  case If(cond: Expr, thn: Statement, els: Statement)
  case While(cond: Expr, body: Statement)
  case Block(body: List[Statement])
