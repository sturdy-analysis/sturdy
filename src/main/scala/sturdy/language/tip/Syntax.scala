package sturdy.language.tip

import sturdy.util.Labeled

enum Expr extends Labeled:
  case Var(s: String)
  case NumLit(n: Double)
  case RandomDouble()
  case Add(e1: Expr, e2: Expr)
  case Sub(e1: Expr, e2: Expr)
  case Mul(e1: Expr, e2: Expr)
  case Div(e1: Expr, e2: Expr)
  case Gt(e1: Expr, e2: Expr)
  case Eq(e1: Expr, e2: Expr)

enum Statement extends Labeled:
  case Assign(s: String, e: Expr)
  case If(cond: Expr, thn: Statement, els: Statement)
  case While(cond: Expr, body: Statement)
  case Block(body: List[Statement])
