package sturdy.values.relational

import java.lang.Long as JLong

/** Extra compare ops for integers */
trait UnsignedOrderingOps[V, B]:
  def ltUnsigned(v1: V, v2: V): B
  def leUnsigned(v1: V, v2: V): B
  def geUnsigned(v1: V, v2: V): B = leUnsigned(v2, v1)
  def gtUnsigned(v1: V, v2: V): B = ltUnsigned(v2, v1)

object UnsignedOrderingOps:
  def ltUnsigned[V, B](v1: V, v2: V)(using ops: UnsignedOrderingOps[V, B]): B =
    ops.ltUnsigned(v1, v2)
  def leUnsigned[V, B](v1: V, v2: V)(using ops: UnsignedOrderingOps[V, B]): B =
    ops.leUnsigned(v1, v2)
  def geUnsigned[V, B](v1: V, v2: V)(using ops: UnsignedOrderingOps[V, B]): B =
    ops.geUnsigned(v1, v2)
  def gtUnsigned[V, B](v1: V, v2: V)(using ops: UnsignedOrderingOps[V, B]): B =
    ops.gtUnsigned(v1, v2)
