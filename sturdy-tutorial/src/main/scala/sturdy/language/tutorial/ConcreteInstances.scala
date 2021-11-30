package sturdy.language.tutorial

import sturdy.data.JOptionC
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.{Stateless, SturdyFailure}
import sturdy.effect.failure.FailureKind

/* The numeric operations of the language. */
class CNumericOps(using f: Failure) extends NumericOps[Int]:
  override def lit(i: Int): Int = i
  override def add(v1: Int, v2: Int): Int = v1+v2
  override def sub(v1: Int, v2: Int): Int = v1+v2
  override def mul(v1: Int, v2: Int): Int = v1*v2
  override def div(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(DivisionByZero, "division by zero")
    else
      v1 / v2
  override def lt(v1: Int, v2: Int): Int =
    if (v1 < v2) 1 else 0

/*
 * The concrete branching implmentation. If the condition is 0 we evaluate the thn continuation, otherwise
 * we evaluate the els continuation. Since we can always decide the condition in the concrete case, the concrete
 * branching implmentation does not involve any joining.
 */
class CBranching[R] extends Branching[Int, R]:
  override def branch(v: Int, thn: => R, els: => R): R =
    if (v != 0) thn else els

/*
 * The concrete store is very similar to the store in the reference interpreter, namely a map from names to values.
 * Since the store is an effect component it additionally needs to provide methods to read and write the internal
 * state (in our case the map).
 */
class CStore(_init: Map[String,Int] = Map()) extends Store[Int, NoJoin]:
  protected var store: Map[String,Int] = _init
  override def read(name: String): JOptionC[Int] = JOptionC(store.get(name))
  override def write(name: String, v: Int): Unit = store += (name -> v)

  override type State = Map[String,Int]
  override def getState: Map[String, Int] = store
  override def setState(s: Map[String, Int]): Unit = store = s

/* The concrete failure implementation. We simply throw an exception in case of a failure. */
class CFailure extends Failure, Stateless:
  override def fail(kind: FailureKind, msg: String): Nothing =
    throw CFailureException(kind, msg)

case class CFailureException(kind: FailureKind, msg: String) extends SturdyFailure:
  override def toString: String = s"Failure $kind: $msg"