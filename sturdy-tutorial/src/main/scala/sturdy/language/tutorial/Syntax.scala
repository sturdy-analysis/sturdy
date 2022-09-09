package sturdy.language.tutorial

import sturdy.util.Labeled

enum Exp extends Labeled:
  case StringLit(s: String)
  case NumLit(n: Int)
  case Var(name: String)
  case Add(e1: Exp, e2: Exp)
  case Sub(e1: Exp, e2: Exp)
  case Mul(e1: Exp, e2: Exp)
  case Div(e1: Exp, e2: Exp)
  case Lt(e1: Exp, e2: Exp)

enum Stm extends Labeled:
  case Assign(name: String, e: Exp)
  case If(cond: Exp, thn: Stm, els: Option[Stm])
  case While(cond: Exp, body: Stm)
  case Block(body: Seq[Stm])