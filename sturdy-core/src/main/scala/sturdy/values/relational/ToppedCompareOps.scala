package sturdy.values.relational

import sturdy.values.Join
import sturdy.values.Topped
import sturdy.values.Topped.*

given ToppedCertainOrderingOps[V, B] (using ops: OrderingOps[V, B]): OrderingOps[Topped[V], Topped[B]] with
  override def lt(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.lt(d1, d2)
  override def le(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.le(d1, d2)
  override def ge(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.ge(d1, d2)
  override def gt(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.gt(d1, d2)

given ToppedUncertainOrderingOps[V, B] (using ops: OrderingOps[V, Topped[B]]): OrderingOps[Topped[V], Topped[B]] with
  override def lt(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.lt(d1, d2)) yield r
  override def le(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.le(d1, d2)) yield r
  override def ge(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.ge(d1, d2)) yield r
  override def gt(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.gt(d1, d2)) yield r
