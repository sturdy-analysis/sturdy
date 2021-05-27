package stateful.whilelang

import stateful.Join
import sturdy.common.Label

trait Val[V] {
  type ValJoin[A]

  def boolLit(b: Boolean, l: Label): V
  def and(e1: V, e2: V, l: Label): V
  def not(e: V, l: Label): V
  def numLit(n: Double, l: Label): V
  def add(e1: V, e2: V, l: Label): V
  def sub(e1: V, e2: V, l: Label): V
  def mul(e1: V, e2: V, l: Label): V
  def div(e1: V, e2: V, l: Label): V
  def eq(e1: V, e2: V, l: Label): V
  def if_[A](v: V, thn: => A, els: => A)(implicit j: ValJoin[A]): A
}

object ValImpl {
  sealed trait Value {
    def asBool: Boolean
    def asDouble(): Double
  }
  case class BoolValue(b: Boolean) extends Value {
    override def asBool: Boolean = b
    override def asDouble(): Double = throw new IllegalArgumentException(s"Expected Int but got $this")
  }
  case class DoubleValue(i: Double) extends Value {
    override def asBool: Boolean = throw new IllegalArgumentException(s"Expected Bool but got $this")
    override def asDouble(): Double = i
  }
}
trait ValImpl extends Val[ValImpl.Value] {
  import ValImpl._
  override type ValJoin[_] = Unit
  override def boolLit(b: Boolean, l: Label): Value = BoolValue(b)
  override def and(e1: Value, e2: Value, l: Label): Value = BoolValue(e1.asBool && e2.asBool)
  override def not(e: Value, l: Label): Value = BoolValue(!e.asBool)
  override def numLit(n: Double, l: Label): Value = DoubleValue(n)
  override def add(e1: Value, e2: Value, l: Label): Value = DoubleValue(e1.asDouble() + e2.asDouble())
  override def sub(e1: Value, e2: Value, l: Label): Value = DoubleValue(e1.asDouble() - e2.asDouble())
  override def mul(e1: Value, e2: Value, l: Label): Value = DoubleValue(e1.asDouble() * e2.asDouble())
  override def div(e1: Value, e2: Value, l: Label): Value = DoubleValue(e1.asDouble() / e2.asDouble())
  override def eq(e1: Value, e2: Value, l: Label): Value = BoolValue(e1.asDouble() == e2.asDouble())
  override def if_[A](v: Value, thn: => A, els: => A)(implicit j: ValJoin[A]): A =
    if (v.asBool)
      thn
    else
      els
}

object ValAbs {
  sealed trait Value
  case object TopValue extends Value
  case class BoolValue(v: Option[Boolean]) extends Value {
    def &&(other: BoolValue): BoolValue = BoolValue(for (b1 <- v; b2 <- other.v) yield b1 && b2)
    def ||(other: BoolValue): BoolValue = BoolValue(for (b1 <- v; b2 <- other.v) yield b1 || b2)
    def ! : BoolValue = BoolValue(for (b1 <- v) yield !b1)
    def same(other: BoolValue): BoolValue = BoolValue(for (b1 <- v; b2 <- other.v) yield b1 == b2)
  }
  case class Interval(l: Double, h: Double) extends Value {
    def isEmpty: Boolean = l > h
    def +(y: Interval): Interval = Interval(l + y.l, h + y.h)
    def -(y: Interval): Interval = Interval(l - y.l, h - y.h)
    def *(y: Interval): Interval = withBounds2(_*_, y)
    def /(y: Interval): Interval = withBounds2(_/_, y)
    def withBounds2(f: (Double, Double) => Double, that: Interval): Interval = {
      val v1 = f(this.l, that.l)
      val v2 = f(this.l, that.h)
      val v3 = f(this.h, that.l)
      val v4 = f(this.h, that.h)
      val low = Math.min(v1, Math.min(v2, Math.min(v3, v4)))
      val high = Math.max(v1, Math.max(v2, Math.max(v3, v4)))
      Interval(low, high)
    }
  }
}
trait ValAbs extends Val[ValAbs.Value] {
  import ValAbs._
  override type ValJoin[A] = Join[A]
  override def boolLit(b: Boolean, l: Label): Value = BoolValue(Some(b))
  override def and(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (b1: BoolValue, b2: BoolValue) => b1 && b2
    case _ => throw new IllegalArgumentException(s"Expected two Bool but got ($e1, $e2)")
  }
  override def not(e: Value, l: Label): Value = e match {
    case TopValue => TopValue
    case b: BoolValue => b.!
    case _ => throw new IllegalArgumentException(s"Expected Bool but got $e")
  }
  override def numLit(n: Double, l: Label): Value = Interval(n, n)
  override def add(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (iv1: Interval, iv2: Interval) => iv1 + iv2
    case _ => throw new IllegalArgumentException(s"Expected two Interval but got ($e1, $e2)")
  }
  override def sub(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (iv1: Interval, iv2: Interval) => iv1 - iv2
    case _ => throw new IllegalArgumentException(s"Expected two Interval but got ($e1, $e2)")
  }
  override def mul(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (iv1: Interval, iv2: Interval) => iv1 * iv2
    case _ => throw new IllegalArgumentException(s"Expected two Interval but got ($e1, $e2)")
  }
  override def div(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (iv1: Interval, iv2: Interval) => iv1 / iv2
    case _ => throw new IllegalArgumentException(s"Expected two Interval but got ($e1, $e2)")
  }
  override def eq(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (iv1: Interval, iv2: Interval) => BoolValue(Some(iv1.isEmpty && iv2.isEmpty || iv1.l == iv2.l && iv1.h == iv2.h))
    case (b1: BoolValue, b2: BoolValue) => b1 same b2
    case _ => throw new IllegalArgumentException(s"Expected two values of equal type but got ($e1, $e2)")
  }
  override def if_[A](v: Value, thn: => A, els: => A)(implicit j: ValJoin[A]): A = v match {
    case BoolValue(Some(true)) => thn
    case BoolValue(Some(false)) => els
    case BoolValue(None) => j.join(thn, els)
    case TopValue => j.join(thn, els)
    case _ => throw new IllegalArgumentException(s"Expected Bool but got $v")
  }
}
