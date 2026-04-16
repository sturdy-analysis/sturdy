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
  case Add(e1: Exp, e2: Exp)
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
    case Add(e1, e2) => s"(${e1} + ${e2})"
    case Sub(e1, e2) => s"(${e1} - ${e2})"
    case Mul(e1, e2) => s"(${e1} * ${e2})"
    case Let(name, value, body) => s"(let ${name} = ${value} in ${body})"
    case If(cond, thenBranch, elseBranch) => s"(if ${cond} then ${thenBranch} else ${elseBranch})"
    case Call(fun, args @ _*) => s"${fun}(${args.mkString(", ")})"

object Exp:
  def Call(fun: String, arg: Exp, args: Exp*): Exp = Call(fun, arg +: args)

import Exp.*

def _interp(exp: Exp, env: Map[String, Int], fenv: Map[String, Def]): Int = exp match
  case Var(name) => env.getOrElse(name, throw new Exception(s"Unbound variable: $name"))
  case Num(n) => n
  case Eq(e1, e2) => if _interp(e1, env, fenv) == _interp(e2, env, fenv) then 1 else 0
  case Lt(e1, e2) => if _interp(e1, env, fenv) < _interp(e2, env, fenv) then 1 else 0
  case Le(e1, e2) => if _interp(e1, env, fenv) <= _interp(e2, env, fenv) then 1 else 0
  case Add(e1, e2) => _interp(e1, env, fenv) + _interp(e2, env, fenv)
  case Sub(e1, e2) => _interp(e1, env, fenv) - _interp(e2, env, fenv)
  case Mul(e1, e2) => _interp(e1, env, fenv) * _interp(e2, env, fenv)
  case Let(name, value, body) =>
    val v = _interp(value, env, fenv)
    _interp(body, env + (name -> v), fenv)

  case If(cond, thenBranch, elseBranch) =>
    if _interp(cond, env, fenv) != 0 then
      _interp(thenBranch, env, fenv)
    else
      _interp(elseBranch, env, fenv)

  case Call(funName, args) =>
    val definition = fenv.getOrElse(funName, throw new Exception(s"Unbound function: $funName"))
    val argValues = args.map(a => _interp(a, env, fenv))
    if (args.size != definition.params.size)
      throw new Exception(s"Argument arity mismatch for $funName with $argValues")
    val newEnv = definition.params.zip(argValues).toMap
    _interp(definition.body, newEnv, fenv)

def interp(funs: List[Def], args: Seq[Int]) : Int =
  val emptyEnv = Map.empty[String, Int]
  val fenv = funs.map(f => f.name -> f).toMap
  _interp(Call("main", args.map(Num.apply)), emptyEnv, fenv)


val natdiff = Def("diff", List("x", "y"),
  If(Eq(Var("x"),Var("y")),
    Num(0),
    If(Lt(Var("x"), Var("y")),
      Sub(Var("y"), Var("x")),
      Sub(Var("x"), Var("y")))))

val fac = Def("fac", List("n", "acc"), If(Le(Var("n"), Num(0)), Var("acc"), Call("fac", Sub(Var("n"), Num(1)), Mul(Var("acc"), Var("n")))))
val fac_main = Def("main", List("n"), Call("fac", Var("n"), Num(1)))

val fib = Def("fib", List("n", "a", "b"), If(Le(Var("n"), Num(0)), Var("a"), Call("fib", Sub(Var("n"), Num(1)), Var("b"), Add(Var("a"), Var("b")))))
val fib_main = Def("main", List("n"), Call("fib", Var("n"), Num(0), Num(1)))

// Nested recursion
val mul = Def("mul", List("a", "b", "r"), If(Le(Var("b"), Num(0)), Var("r"), Call("mul", Var("a"), Sub(Var("b"), Num(1)), Add(Var("r"), Var("a")))))
val fac_mul = Def("fac_mul", List("n", "acc"), If(Le(Var("n"), Num(0)), Var("acc"), Call("fac_mul", Sub(Var("n"), Num(1)), Call("mul", Var("acc"), Var("n"), Num(0)))))
val fac_mul_main = Def("main", List("n"), Call("fac_mul", Var("n"), Num(1)))

object Run extends App:
  println(natdiff)
  println(fac)
  println(fac_main)
  println((for i <- Range(0,10) yield interp(List(fac, fac_main), List(i))).mkString(", "))

  println(fib)
  println(fib_main)
  println((for i <- Range(0, 10) yield interp(List(fib, fib_main), List(i))).mkString(", "))

  println(mul)
  println(fac_mul)
  println((for i <- Range(0, 10) yield interp(List(mul, fac_mul, fac_mul_main), List(i))).mkString(", "))
