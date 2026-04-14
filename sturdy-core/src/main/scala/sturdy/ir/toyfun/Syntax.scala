package sturdy.ir.toyfun


/*
def fac(n, acc) :=
  if n = zero
  then acc
	else fac(n - 1, acc * n)

def main(n) := fac(n, 1)

 */

case class Def(name: String, params: List[String], body: Exp):
  override def toString: String = s"def ${name}(${params.mkString(", ")}) =\n  ${body}"

enum Exp:
  case Var(name: String)
  case Num(n: Int)
  case Eq(e1: Exp, e2: Exp)
  case Lt(e1: Exp, e2: Exp)
  case Le(e1: Exp, e2: Exp)
  case Sub(e1: Exp, e2: Exp)
  case Mul(e1: Exp, e2: Exp)
  case Let(name: String, value: Exp, body: Exp)
  case If(cond: Exp, thenBranch: Exp, elseBranch: Exp)
  case Call(fun: String, args: Seq[Exp])

  override def toString: String = this match
    case Var(name) => name
    case Num(n) => n.toString
    case Eq(e1, e2) => s"(${e1} == ${e2})"
    case Lt(e1, e2) => s"(${e1} < ${e2})"
    case Le(e1: Exp, e2: Exp) => s"(${e1} <= ${e2})"
    case Sub(e1, e2) => s"(${e1} - ${e2})"
    case Mul(e1, e2) => s"(${e1} * ${e2})"
    case Let(name, value, body) => s"(let ${name} = ${value} in ${body})"
    case If(cond, thenBranch, elseBranch) => s"(if ${cond} then ${thenBranch} else ${elseBranch})"
    case Call(fun, args @ _*) => s"${fun}(${args.mkString(", ")})"

object Exp:
  def Call(fun: String, arg: Exp, args: Exp*): Exp = Call(fun, arg +: args)

import Exp.*

val natdiff = Def("diff", List("x", "y"),
  If(Eq(Var("x"),Var("y")),
    Num(0),
    If(Lt(Var("x"), Var("y")),
      Sub(Var("y"), Var("x")),
      Sub(Var("x"), Var("y")))))

val fac = Def("fac", List("n", "acc"), If(Le(Var("n"), Num(0)), Var("acc"), Call("fac", Sub(Var("n"), Num(1)), Mul(Var("acc"), Var("n")))))
val fac_main = Def("main", List("n"), Call("fac", Var("n"), Num(1)))

object Run extends App:
  println(natdiff)
  println(fac)
  println(sturdy.ir.toyfun.fac_main)