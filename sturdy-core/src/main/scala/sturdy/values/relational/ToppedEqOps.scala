package sturdy.values.relational

import sturdy.values.JoinValue
import sturdy.values.Topped
import sturdy.values.Topped.*

given ToppedCertainEqOps[V, B](using ops: EqOps[V, B]): EqOps[Topped[V], Topped[B]] with
  override def equ(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (x1 <- v1; x2 <- v2) yield ops.equ(x1, x2)
  override def neq(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (x1 <- v1; x2 <- v2) yield ops.neq(x1, x2)

given ToppedUncertainEqOps[V, B](using ops: EqOps[V, Topped[B]]): EqOps[Topped[V], Topped[B]] with
  override def equ(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (x1 <- v1; x2 <- v2; r <- ops.equ(x1, x2)) yield r
  override def neq(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (x1 <- v1; x2 <- v2; r <- ops.neq(x1, x2)) yield r
