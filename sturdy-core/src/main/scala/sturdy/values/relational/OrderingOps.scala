package sturdy.values.relational

trait OrderingOps[V, B]:
  def lt(v1: V, v2: V): B
  def le(v1: V, v2: V): B
  def ge(v1: V, v2: V): B
  def gt(v1: V, v2: V): B

object OrderingOps:
  def lt[V, B](v1: V, v2: V)(using ops: OrderingOps[V, B]): B =
    ops.lt(v1, v2)
  def le[V, B](v1: V, v2: V)(using ops: OrderingOps[V, B]): B =
    ops.le(v1, v2)
  def ge[V, B](v1: V, v2: V)(using ops: OrderingOps[V, B]): B =
    ops.ge(v1, v2)
  def gt[V, B](v1: V, v2: V)(using ops: OrderingOps[V, B]): B =
    ops.gt(v1, v2)

given ConcreteOrderingOps[V <: AnyRef] (using ord: Ordering[V]): OrderingOps[V, Boolean] with
  def lt(v1: V, v2: V): Boolean = ord.lt(v1, v2)
  def le(v1: V, v2: V): Boolean = ord.lteq(v1, v2)
  def ge(v1: V, v2: V): Boolean = ord.gteq(v1, v2)
  def gt(v1: V, v2: V): Boolean = ord.gt(v1, v2)

