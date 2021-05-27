package stateful.whilelang

import stateful.whilelang.ValImpl.Value
import sturdy.lang.whilee.Syntax._

trait Interpreter[V, Addr] {
  val impl: Val[V]
    with Environment[String, Addr]
    with Store[Addr, V]
    with Alloc[Addr]
    with Fail
    with Fix[Statement, Unit]
  import impl._

  implicit val envJoin: impl.EnvironmentJoin[Addr]
  implicit val storeJoin: impl.StoreJoin[V]
  implicit val valJoinUnit: impl.ValJoin[Unit]

  def eval(e: Expr): V = e match {
    case Var(x) =>
      val addr = lookupOrElse(x, fail(s"Unbound variable $x"))
      readOrElse(addr, fail(s"Unbound address $addr for variable $x"))
    case BoolLit(b) => boolLit(b, e.label)
    case And(e1, e2) => and(eval(e1), eval(e2), e.label)
    case Or(e1, e2) => eval(
      Not(
        And(
          Not(e1) <@@ e.label,
          Not(e2) <@@ e.label)
          <@@ e.label)
        <@@ e.label)
    case Not(e) => not(eval(e), e.label)
    case NumLit(n) => numLit(n, e.label)
    case Add(e1, e2) => add(eval(e1), eval(e2), e.label)
    case Sub(e1, e2) => sub(eval(e1), eval(e2), e.label)
    case Mul(e1, e2) => mul(eval(e1), eval(e2), e.label)
    case Div(e1, e2) => div(eval(e1), eval(e2), e.label)
    case Eq(e1, e2) => impl.eq(eval(e1), eval(e2), e.label)
  }

  lazy val run: Statement => Unit = {
    fix(rec => {
      case Assign(x, e) =>
        val addr = lookupOrElse(x, {
          val a = alloc
          bind(x, a)
          a
        })
        val v = eval(e)
        write(addr, v)
      case If(cond, thn, els) => if_(eval(cond), rec(thn), rec(els))
      case s@While(cond, body) => rec(
        If(cond,
          Block(List(body, s)) <@@ s.label,
          Block(Nil) <@@ s.label)
        <@@ s.label)
      case Block(body) => scoped {
        body.foldLeft(())((_,s) => rec(s))
      }
    })
  }
}

class Concrete extends Interpreter[Value, Int] {
  override val impl =
    new ValImpl
      with EnvironmentImpl[String, Int]
      with StoreImpl[Int, Value]
      with AllocImpl
      with FailImpl
      with FixImpl[Statement, Unit]
  override implicit val envJoin: impl.EnvironmentJoin[Int] = ()
  override implicit val storeJoin: impl.StoreJoin[ValImpl.Value] = ()
  override implicit val valJoinUnit: impl.ValJoin[Unit] = ()
}

object ex extends App {
  val p = Block(List(
    Assign("x", NumLit(5)),
    Assign("y", NumLit(1)),
    While(Not(Eq(Var("x"), NumLit(0))), Block(List(
      Assign("x", Sub(Var("x"), NumLit(1))),
      Assign("y", Mul(Var("y"), NumLit(2)))
    )))
  ))

  val interpreter = new Concrete()
  interpreter.run(p)
  println(interpreter.impl.env)
  println(interpreter.impl.store)
}
