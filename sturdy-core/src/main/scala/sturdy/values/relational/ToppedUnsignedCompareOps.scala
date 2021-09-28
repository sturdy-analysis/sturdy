package sturdy.values.relational

import sturdy.values.Join
import sturdy.values.Topped
import sturdy.values.Topped.*

given ToppedCertainUnsignedCompareOps[V, B](using ops: UnsignedCompareOps[V, B]): UnsignedCompareOps[Topped[V], Topped[B]] with
  override def ltUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.ltUnsigned(d1, d2)
  override def leUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.leUnsigned(d1, d2)
  override def geUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.geUnsigned(d1, d2)
  override def gtUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.gtUnsigned(d1, d2)

given ToppedUncertainUnsignedCompareOps[V, B](using ops: UnsignedCompareOps[V, Topped[B]]): UnsignedCompareOps[Topped[V], Topped[B]] with
  override def ltUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.ltUnsigned(d1, d2)) yield r
  override def leUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.leUnsigned(d1, d2)) yield r
  override def geUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.geUnsigned(d1, d2)) yield r
  override def gtUnsigned(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.gtUnsigned(d1, d2)) yield r
