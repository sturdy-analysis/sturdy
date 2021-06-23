package stateful.whilelang

import stateful.{JoinUnit, Join}
import sturdy.common.Label
import sturdy.lang.whilee.Syntax._

trait Interpreter[V, Addr] {
  val impl: Val[V]
    with Random[V]
    with Environment[String, Addr]
    with Store[Addr, V]
    with Alloc[Addr]
    with Fail
    with Fix[Statement, Unit]
  import impl._

  implicit val envJoinV: impl.EnvironmentJoin[V]
  implicit val envJoinUnit: impl.EnvironmentJoin[Unit]
  implicit val storeJoin: impl.StoreJoin[V]
  implicit val valJoinUnit: impl.ValJoin[Unit]

  def eval(e: Expr): V = e match {
    case Var(x) =>
      lookupAndThen(x, fail(s"Unbound variable $x")) { addr =>
        readOrElse(addr, fail(s"Unbound address $addr for variable $x"))
      }

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
    case Lt(e1, e2) => lt(eval(e1), eval(e2), e.label)
    case RandomNum() => randomNum()
  }

  lazy val run: Statement => Unit = {
    fix(rec => {
      case s@Assign(x, e) =>
        lookupAndThen(x, {
          val addr = alloc(s.label)
          bind(x, addr)
          addr
        }) { addr => {
          val v = eval(e)
          write(addr, v)
        }
        }
      case If(cond, thn, els) => if_(eval(cond), rec(thn), rec(els))
      case s@While(cond, body) => rec(
        If(cond,
          Block(List(body, s)) <@@ s.label,
          Block(Nil) <@@ s.label)
        <@@ s.label)
      case Block(body) => {
        body.foldLeft(())((_,s) => rec(s))
      }
    })
  }
}

class Concrete extends Interpreter[ValImpl.Value, Int] {
  override val impl =
    new ValImpl
      with RandomImpl
      with EnvironmentImpl[String, Int]
      with StoreImpl[Int, ValImpl.Value]
      with AllocImpl
      with FailImpl
      with FixImpl[Statement, Unit]
  override implicit val envJoinV: impl.EnvironmentJoin[Int] = ()
  override implicit val envJoinUnit: impl.EnvironmentJoin[Unit] = ()
  override implicit val storeJoin: impl.StoreJoin[ValImpl.Value] = ()
  override implicit val valJoinUnit: impl.ValJoin[Unit] = ()
}

class Interval extends Interpreter[ValAbs.Value, Label] {
  override val impl =
    new ValAbs
      with RandomAbs
      with EnvironmentAbs[String, Label]
      with StoreAbs[Label, ValAbs.Value]
      with AllocAbs
      with FailAbs
      with FixImpl[Statement, Unit] {
      override val storeJoinVal: Join[ValAbs.Value] = ValAbs.Join
    }
  override implicit val envJoinV: impl.EnvironmentJoin[ValAbs.Value] = ValAbs.Join
  override implicit val envJoinUnit: impl.EnvironmentJoin[Unit] = JoinUnit
  override implicit val storeJoin: impl.StoreJoin[ValAbs.Value] = ValAbs.Join
  override implicit val valJoinUnit: impl.ValJoin[Unit] = JoinUnit
}

object ex1 extends App {
  val p = Block(List(
    Assign("x", RandomNum()),
    Assign("y", RandomNum()),
    If(Lt(Var("x"), NumLit(0.5)),
      Block(List(
        Assign("y", NumLit(1))
      )),
      Block(List(
        Assign("y", NumLit(2))
      )))
  ))

  val interpreter = new Concrete()
  interpreter.run(p)
  println(interpreter.impl.env)
  println(interpreter.impl.store)

  val analysis = new Interval()
  analysis.run(p)
  println(analysis.impl.env)
  println(analysis.impl.store)
}

object ex2 extends App {
  val p = Block(List(
    Assign("x", RandomNum()),
    If(Lt(Var("x"), NumLit(0.5)),
      Block(List(
        Assign("y", NumLit(1))
      )),
      Block(List(
        Assign("y", NumLit(2))
      )))
  ))

  val interpreter = new Concrete()
  interpreter.run(p)
  println(interpreter.impl.env)
  println(interpreter.impl.store)

  val analysis = new Interval()
  analysis.run(p)
  println(analysis.impl.env)
  println(analysis.impl.store)
}

