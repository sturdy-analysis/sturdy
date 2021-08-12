package sturdy.values.relational

import sturdy.values.JoinValue
import sturdy.values.Topped
import sturdy.values.Topped._

given ToppedCertainCompareOps[V, B](using ops: CompareOps[V, B]): CompareOps[Topped[V], Topped[B]] with
  override def equiv(v1: Topped[V], v2: Topped[V]): Topped[B] = ???
  override def lt(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.lt(d1, d2)
  override def le(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.le(d1, d2)
  override def ge(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.ge(d1, d2)
  override def gt(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2) yield ops.gt(d1, d2)
  def isZero(v1: Topped[V]): Topped[B] = ???
  def isPositive(v1: Topped[V]): Topped[B] = ???
  def isNegative(v1: Topped[V]): Topped[B] = ???
  def isOdd(v1: Topped[V]): Topped[B] = ???
  def isEven(v1: Topped[V]): Topped[B] = ???

given ToppedUncertainCompareOps[V, B](using ops: CompareOps[V, Topped[B]]): CompareOps[Topped[V], Topped[B]] with
  override def equiv(v1: Topped[V], v2: Topped[V]): Topped[B] = ???
  override def lt(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.lt(d1, d2)) yield r
  override def le(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.le(d1, d2)) yield r
  override def ge(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.ge(d1, d2)) yield r
  override def gt(v1: Topped[V], v2: Topped[V]): Topped[B] =
    for (d1 <- v1; d2 <- v2; r <- ops.gt(d1, d2)) yield r
  def isZero(v1: Topped[V]): Topped[B] = ???
  def isPositive(v1: Topped[V]): Topped[B] = ???
  def isNegative(v1: Topped[V]): Topped[B] = ???
  def isOdd(v1: Topped[V]): Topped[B] = ???
  def isEven(v1: Topped[V]): Topped[B] = ???

