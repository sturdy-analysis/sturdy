package sturdy.language.scheme

import sturdy.language.scheme.Literal.*
import sturdy.language.scheme.Exp.*
import sturdy.util.Labled

object Examples:
  
  /*
  (define (fac n)
    (if (= n 1)
        1
        (* n (fac (- n 1)))))

  (fac 10)
  */
  val factorial = Program(List(
    Form.Definition(Define("fac",
      Lam(List("n"), Body(List(), List(
        If(Op2(Op2Kinds.Eqv, Var("n"), Lit(Literal.IntLit(1))),
          Lit(Literal.IntLit(1)),
          OpVar(OpVarKinds.Mul, List(
            Var("n"),
            Apply(Var("fac"), List(
              OpVar(OpVarKinds.Sub, List(
                Var("n"),
                Lit(Literal.IntLit(1))
              ))
            ))
          ))
        )
      )))
    )),
    Form.Expression(Apply(Var("fac"), List(Lit(Literal.IntLit(10)))))
  ))

