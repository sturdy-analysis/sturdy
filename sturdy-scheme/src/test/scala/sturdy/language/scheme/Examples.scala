package sturdy.language.scheme

import sturdy.language.scheme.Literal.*
import sturdy.language.scheme.Expr.*
import sturdy.util.Labeled

object Examples:
  
  val ex1 = {
    List(Lit(BoolLit(true)))
  }

  val ex2 = {
    List(Lit(BoolLit(true)), Lit(IntLit(1)))
  }

