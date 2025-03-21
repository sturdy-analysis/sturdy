package sturdy.values.booleans

import sturdy.values.Topped
import sturdy.values.Topped.*

given GenToppedBooleanOps[V](using ops: BooleanOps[V]): BooleanOps[Topped[V]] with
  def boolLit(b: Boolean): Topped[V] = Actual(ops.boolLit(b))
  def and(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (b1 <- v1; b2 <- v2) yield ops.and(b1, b2)
  def not(v: Topped[V]): Topped[V] =
    for (b <- v) yield ops.not(b)
  def or(v1: Topped[V], v2: Topped[V]): Topped[V] =
    for (b1 <- v1; b2 <- v2) yield ops.or(b1, b2)

given ToppedBooleanOps: BooleanOps[Topped[Boolean]] with
  def boolLit(b: Boolean): Topped[Boolean] = Actual(b)
  def and(v1: Topped[Boolean], v2: Topped[Boolean]): Topped[Boolean] = (v1, v2) match
    case (Actual(false),_) | (_,Actual(false)) => Actual(false)
    case (Actual(b1), Actual(b2)) => Actual(b1 && b2)
    case _ => Top
  def not(v: Topped[Boolean]): Topped[Boolean] =
    for (b <- v) yield !b
  def or(v1: Topped[Boolean], v2: Topped[Boolean]): Topped[Boolean] = (v1, v2) match
    case (Actual(true),_) | (_,Actual(true)) => Actual(true)
    case (Actual(b1), Actual(b2)) => Actual(b1 || b2)
    case _ => Top

