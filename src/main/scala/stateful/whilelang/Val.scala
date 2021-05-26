package stateful.whilelang

import stateful.whilelang.ValImpl.{BoolValue, Value, DoubleValue}
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
class ValImpl extends Val[Value] {
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
  override def if_[A](v: Value, thn: => A, els: => A)(implicit j: Unit): A =
    if (v.asBool)
      thn
    else
      els
}