package sturdy.language.schemelang

import sturdy.language.schemelang.Literal.*
import sturdy.language.schemelang.Expr.*
import sturdy.util.Labeled

object Examples:
  
  val ex1 = {
    List(Lit(BoolLit(true)))
  }

  val ex2 = {
    List(Lit(BoolLit(true)), Lit(IntLit(1)))
  }

