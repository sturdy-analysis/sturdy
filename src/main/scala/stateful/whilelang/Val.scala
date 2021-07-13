package stateful.whilelang

import stateful.{Join, JoinComputation}
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
  def lt(e1: V, e2: V, l: Label): V
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
    override def toString: String = b.toString
  }
  case class DoubleValue(d: Double) extends Value {
    override def asBool: Boolean = throw new IllegalArgumentException(s"Expected Bool but got $this")
    override def asDouble(): Double = d
    override def toString: String = d.toString
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
  override def lt(e1: Value, e2: Value, l: Label): Value = BoolValue(e1.asDouble() < e2.asDouble())
  override def if_[A](v: Value, thn: => A, els: => A)(implicit j: ValJoin[A]): A =
    if (v.asBool)
      thn
    else
      els
}

object ValAbs {
  sealed trait Value
  case object TopValue extends Value {
    override def toString: String = "Top"
  }
  case class BoolValue(v: Topped[Boolean]) extends Value {
    def join(other: BoolValue): BoolValue =
      BoolValue(for (b1 <- v; b2 <- other.v if b1 == b2) yield b1)
    def &&(other: BoolValue): BoolValue = BoolValue(for (b1 <- v; b2 <- other.v) yield b1 && b2)
    def ||(other: BoolValue): BoolValue = BoolValue(for (b1 <- v; b2 <- other.v) yield b1 || b2)
    def ! : BoolValue = BoolValue(for (b1 <- v) yield !b1)
    override def toString: String = v.toString("Bool")
  }
  case class NumValue(v: Topped[Interval]) extends Value {
    def join(other: NumValue): NumValue =
      NumValue(for (iv1 <- v; iv2 <- other.v) yield iv1.join(iv2))
    override def toString: String = v.toString("Num")
  }

  case class Interval(l: Double, h: Double) {
    if (l > h) throw new IllegalArgumentException(s"Empty intervals are illegal $this")

    def join(other: Interval): Interval =
      Interval(Math.min(l, other.l), Math.max(h, other.h))
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
    def <(that: Interval): Topped[Boolean] =
      if (this.h < that.l)
        Actual(true)
      else if (that.h <= this.l)
        Actual(false)
      else
        Top
    def ===(that: Interval): Topped[Boolean] = {
      if (this.l == this.h && this.l == that.l && this.h == that.h)
        Actual(true)
      else if (this.h < that.l || that.h < this.l)
        Actual(false)
      else
        Top
    }

    override def toString: String = s"[$l,$h]"
  }

  object Join extends Join[Value] {
    override def apply(v1: Value, v2: Value): Value = (v1, v2) match {
      case (b1: BoolValue, b2: BoolValue) => b1.join(b2)
      case (n1: NumValue, n2: NumValue) => n1.join(n2)
      case _ => TopValue
    }
  }
}
trait ValAbs extends Val[ValAbs.Value] with JoinComputation {
  import ValAbs._
  override type ValJoin[A] = Join[A]
  override def boolLit(b: Boolean, l: Label): Value = BoolValue(Actual(b))
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
  override def numLit(n: Double, l: Label): Value = NumValue(Actual(Interval(n, n)))
  override def add(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (n1: NumValue, n2: NumValue) =>
      NumValue(for (iv1 <- n1.v; iv2 <- n2.v) yield iv1 + iv2)
    case _ => throw new IllegalArgumentException(s"Expected two Interval but got ($e1, $e2)")
  }
  override def sub(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (n1: NumValue, n2: NumValue) =>
      NumValue(for (iv1 <- n1.v; iv2 <- n2.v) yield iv1 - iv2)
    case _ => throw new IllegalArgumentException(s"Expected two Interval but got ($e1, $e2)")
  }
  override def mul(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (n1: NumValue, n2: NumValue) =>
      NumValue(for (iv1 <- n1.v; iv2 <- n2.v) yield iv1 * iv2)
    case _ => throw new IllegalArgumentException(s"Expected two Interval but got ($e1, $e2)")
  }
  override def div(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (n1: NumValue, n2: NumValue) =>
      NumValue(for (iv1 <- n1.v; iv2 <- n2.v) yield iv1 / iv2)
    case _ => throw new IllegalArgumentException(s"Expected two Interval but got ($e1, $e2)")
  }
  override def eq(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (n1: NumValue, n2: NumValue) =>
      BoolValue(for (iv1 <- n1.v; iv2 <- n2.v; b <- iv1 === iv2) yield b)
    case (b1: BoolValue, b2: BoolValue) =>
      BoolValue(for (v1 <- b1.v; v2 <- b2.v) yield v1 == v2)
    case _ => throw new IllegalArgumentException(s"Expected two values of equal type but got ($e1, $e2)")
  }
  override def lt(e1: Value, e2: Value, l: Label): Value = (e1, e2) match {
    case (TopValue, _) => TopValue
    case (_, TopValue) => TopValue
    case (n1: NumValue, n2: NumValue) =>
      BoolValue(for (iv1 <- n1.v; iv2 <- n2.v; b <- iv1 < iv2) yield b)
    case _ => throw new IllegalArgumentException(s"Expected two Interval but got ($e1, $e2)")
  }
  override def if_[A](v: Value, thn: => A, els: => A)(implicit j: ValJoin[A]): A = v match {
    case BoolValue(Actual(true)) => thn
    case BoolValue(Actual(false)) => els
    case BoolValue(Top) => join(thn, els)
    case TopValue => join(thn, els)
    case _ => throw new IllegalArgumentException(s"Expected Bool but got $v")
  }
}

sealed trait Topped[+V] {
  def foreach[A](f: V => A): Unit = this match {
    case Top => // nothing
    case Actual(v) => f(v)
  }
  def filter(f: V => Boolean): Topped[V] = this match {
    case Top => Top
    case Actual(v) => if (f(v)) this else Top
  }
  def withFilter(f: V => Boolean): Topped[V] = filter(f)
  final def toString(suffix: String): String = this match {
    case Top => s"Top$suffix"
    case Actual(v) => v.toString
  }
  final def map[A](f: V => A): Topped[A] = this match {
    case Top => Top
    case Actual(v) => Actual(f(v))
  }
  final def flatMap[A](f: V => Topped[A]): Topped[A] = this match {
    case Top => Top
    case Actual(v) => f(v)
  }
}
case object Top extends Topped[Nothing]
case class Actual[V](v: V) extends Topped[V]
