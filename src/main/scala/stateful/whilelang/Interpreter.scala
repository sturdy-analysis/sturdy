package stateful.whilelang

import stateful.whilelang.ValImpl.Value
import sturdy.lang.whilee.Syntax._

trait Interpreter[V, Addr] {
  type Computation =
    Val[V]
      with Environment[String, Addr]
      with Store[Addr, V]
      with Alloc[Addr]
      with Fail
      with Fix[Statement, Unit]
  val impl: Computation
  import impl._

  implicit val envJoin: impl.EnvironmentJoin[Addr]
  implicit val storeJoin: impl.StoreJoin[V]
  implicit val valJoinUnit: impl.ValJoin[Unit]

  def eval(e: Expr): V = e match {
    case Var(x) =>
      val addr = lookupOrElse(x, fail(s"Unbound variable $x"))
      readOrElse(addr, fail(s"Unbound address $addr for variable $x"))
    case e@BoolLit(b) => boolLit(b, e.label)
    case e@And(e1, e2) => and(eval(e1), eval(e2), e.label)
    case e@Or(e1, e2) => eval(
      Not(
        And(
          Not(e1) <@@ e.label,
          Not(e2) <@@ e.label)
          <@@ e.label)
        <@@ e.label)
    case Not(e) => not(eval(e), e.label)
    case e@NumLit(n) => numLit(n, e.label)
    case e@Add(e1, e2) => add(eval(e1), eval(e2), e.label)
    case e@Sub(e1, e2) => sub(eval(e1), eval(e2), e.label)
    case e@Mul(e1, e2) => mul(eval(e1), eval(e2), e.label)
    case e@Div(e1, e2) => div(eval(e1), eval(e2), e.label)
    case e@Eq(e1, e2) => impl.eq(eval(e1), eval(e2), e.label)
  }

  def run: Statement => Unit = {
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
      case Block(body) => body.foldLeft(())((_,s) => rec(s))
    })
  }
}

object Concrete extends Interpreter[Value, Int] {
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