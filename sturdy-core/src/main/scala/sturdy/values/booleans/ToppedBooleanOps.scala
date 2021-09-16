package sturdy.values.booleans

import sturdy.values.Topped
import sturdy.values.Topped.*

given ToppedBooleanOps[V](using ops: BooleanOps[V]): BooleanOps[Topped[V]] with
  def boolLit(b: Boolean): Topped[V] = Actual(ops.boolLit(b))
  def and(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (b1 <- v1; b2 <- v2) yield ops.and(b1, b2)
  def not(v: Topped[V]): Topped[V] =
    for (b <- v) yield ops.not(b)
  def or(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (b1 <- v1; b2 <- v2) yield ops.or(b1, b2)
