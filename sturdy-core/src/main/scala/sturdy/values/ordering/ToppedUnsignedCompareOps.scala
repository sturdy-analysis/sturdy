package sturdy.values.ordering

import sturdy.values.Join
import sturdy.values.Topped
import sturdy.values.Topped.*

given ToppedCertainUnsignedOrderingOps[V, B] (using ops: UnsignedOrderingOps[V, B]): UnsignedOrderingOps[Topped[V], Topped[B]] with
  override def ltUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.ltUnsigned(d1, d2)
  override def leUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.leUnsigned(d1, d2)

given ToppedUncertainUnsignedOrderingOps[V, B] (using ops: UnsignedOrderingOps[V, Topped[B]]): UnsignedOrderingOps[Topped[V], Topped[B]] with
  override def ltUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.ltUnsigned(d1, d2)) yield r
  override def leUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.leUnsigned(d1, d2)) yield r
