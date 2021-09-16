package sturdy.values.ints

import java.lang.Long as JLong

/** Extra compare ops for integers */
trait IntegerCompareOps[V, B]:
  def ltUnsigned(v1: V, v2: V): B = ???
  def leUnsigned(v1: V, v2: V): B = ???
  def geUnsigned(v1: V, v2: V): B = ???
  def gtUnsigned(v1: V, v2: V): B = ???

object IntegerCompareOps:
  def ltUnsigned[V, B](v1: V, v2: V)(using ops: IntegerCompareOps[V, B]): B =
    ops.ltUnsigned(v1, v2)
  def leUnsigned[V, B](v1: V, v2: V)(using ops: IntegerCompareOps[V, B]): B =
    ops.leUnsigned(v1, v2)
  def geUnsigned[V, B](v1: V, v2: V)(using ops: IntegerCompareOps[V, B]): B =
    ops.geUnsigned(v1, v2)
  def gtUnsigned[V, B](v1: V, v2: V)(using ops: IntegerCompareOps[V, B]): B =
    ops.gtUnsigned(v1, v2)

given ConcreteIntIntegerCompareOps: IntegerCompareOps[Int, Boolean] with
  override def ltUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) < 0
  override def leUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) <= 0
  override def geUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) >= 0
  override def gtUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) > 0

given ConcreteLongIntegerCompareOps: IntegerCompareOps[Long, Boolean] with
  override def ltUnsigned(v1: Long, v2: Long): Boolean = JLong.compareUnsigned(v1, v2) < 0
  override def leUnsigned(v1: Long, v2: Long): Boolean = JLong.compareUnsigned(v1, v2) <= 0
  override def geUnsigned(v1: Long, v2: Long): Boolean = JLong.compareUnsigned(v1, v2) >= 0
  override def gtUnsigned(v1: Long, v2: Long): Boolean = JLong.compareUnsigned(v1, v2) > 0
