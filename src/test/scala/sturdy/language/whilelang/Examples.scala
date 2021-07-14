package sturdy.language.whilelang

import Expr._
import Statement._

object Examples:
  
  val ex1 = Block(List(
    Assign("x", RandomDouble()),
    Assign("y", RandomDouble()),
    If(Lt(Var("x"), NumLit(0.5)),
      Block(List(
        Assign("y", NumLit(1))
      )),
      Block(List(
        Assign("y", NumLit(2))
      )))
  ))

  val ex2 = Block(List(
    Assign("x", RandomDouble()),
    If(Lt(Var("x"), NumLit(0.5)),
      Block(List(
        Assign("y", NumLit(1))
      )),
      Block(List(
        Assign("y", NumLit(2))
      )))
  ))

  val ex3 = Block(List(
    Assign("x", NumLit(2.0)),
    If(Lt(Var("x"), NumLit(2.5)),
      Block(List(
        Assign("y", Mul(NumLit(1), Var("x")))
      )),
      Block(List(
        Assign("y", NumLit(2))
      )))
  ))

