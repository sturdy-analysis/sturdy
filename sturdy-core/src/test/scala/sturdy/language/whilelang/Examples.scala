package sturdy.language.whilelang

import Expr.*
import Statement.*
import sturdy.util.Labeled

object Examples:
  
  val ex1 = {
    Labeled.reset()
    Block(List(
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
  }

  val ex2 = {
    Labeled.reset()
    Block(List(
      Assign("x", RandomDouble()),
      If(Lt(Var("x"), NumLit(0.5)),
        Block(List(
          Assign("y", NumLit(1))
        )),
        Block(List(
          Assign("y", NumLit(2))
        )))
    ))
  }

  val ex3 = {
    Labeled.reset()
    Block(List(
      Assign("x", NumLit(2.0)),
      If(Lt(Var("x"), NumLit(2.5)),
        Block(List(
          Assign("y", Mul(NumLit(1), Var("x")))
        )),
        Block(List(
          Assign("y", NumLit(2))
        )))
    ))
  }

  val ex4 = {
    Labeled.reset()
    Block(List(
      Assign("x", NumLit(0.0)),
      Assign("y", Div(NumLit(5), Var("x")))
    ))
  }

  val ex5 = {
    Labeled.reset()
    Block(List(
      Assign("x", RandomDouble()),
      Assign("y", Div(NumLit(5), Var("x")))
    ))
  }
