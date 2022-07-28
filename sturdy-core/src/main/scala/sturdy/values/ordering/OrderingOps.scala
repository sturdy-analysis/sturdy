package sturdy.values.ordering

trait OrderingOps[V, B]:
  def lt(v1: V, v2: V): B
  def le(v1: V, v2: V): B

  def ge(v1: V, v2: V): B = le(v2, v1)
  def gt(v1: V, v2: V): B = lt(v2, v1)

object OrderingOps:
  def lt[V, B](v1: V, v2: V)(using ops: OrderingOps[V, B]): B =
    ops.lt(v1, v2)
  def le[V, B](v1: V, v2: V)(using ops: OrderingOps[V, B]): B =
    ops.le(v1, v2)
  def ge[V, B](v1: V, v2: V)(using ops: OrderingOps[V, B]): B =
    ops.ge(v1, v2)
  def gt[V, B](v1: V, v2: V)(using ops: OrderingOps[V, B]): B =
    ops.gt(v1, v2)

given ConcreteOrderingOps[V] (using ord: Ordering[V]): OrderingOps[V, Boolean] with
  def lt(v1: V, v2: V): Boolean = ord.lt(v1, v2)
  def le(v1: V, v2: V): Boolean = ord.lteq(v1, v2)
