package stateful.whilelang

import sturdy.common.Labeled

object Syntax {
  sealed trait Expr extends Labeled
  case class Var(s: String) extends Expr
  case class BoolLit(b: Boolean) extends Expr
  case class And(e1: Expr, e2: Expr) extends Expr
  case class Or(e1: Expr, e2: Expr) extends Expr
  case class Not(e: Expr) extends Expr
  case class NumLit(n: Double) extends Expr
  case class RandomNum() extends Expr
  case class Add(e1: Expr, e2: Expr) extends Expr
  case class Sub(e1: Expr, e2: Expr) extends Expr
  case class Mul(e1: Expr, e2: Expr) extends Expr
  case class Div(e1: Expr, e2: Expr) extends Expr
  case class Eq(e1: Expr, e2: Expr) extends Expr

  sealed trait Statement extends Labeled
  case class Assign(s: String, e: Expr) extends Statement
  case class If(cond: Expr, thn: Statement, els: Statement) extends Statement
  case class While(cond: Expr, body: Statement) extends Statement
  case class Block(body: List[Statement]) extends Statement
}
