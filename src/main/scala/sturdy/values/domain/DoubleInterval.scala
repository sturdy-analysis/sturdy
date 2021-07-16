package sturdy.values.domain

import sturdy.effect.JoinComputation
import sturdy.effect.failure.Failure
import sturdy.values.{JoinValue, Topped}
import sturdy.values.Topped.*
import sturdy.values.doubles.DoubleOps
import sturdy.values.relational.*


case class DoubleInterval(l: Double, h: Double):
  if (l > h) throw new IllegalArgumentException(s"Empty intervals are illegal $this")

  def join(other: DoubleInterval): DoubleInterval =
    DoubleInterval(Math.min(l, other.l), Math.max(h, other.h))
  def +(y: DoubleInterval): DoubleInterval = DoubleInterval(l + y.l, h + y.h)
  def -(y: DoubleInterval): DoubleInterval = DoubleInterval(l - y.l, h - y.h)
  def *(y: DoubleInterval): DoubleInterval = withBounds2(_*_, y)
  def /(y: DoubleInterval): DoubleInterval = withBounds2(_/_, y)
  def withBounds2(f: (Double, Double) => Double, that: DoubleInterval): DoubleInterval = {
    val v1 = f(this.l, that.l)
    val v2 = f(this.l, that.h)
    val v3 = f(this.h, that.l)
    val v4 = f(this.h, that.h)
    val low = Math.min(v1, Math.min(v2, Math.min(v3, v4)))
    val high = Math.max(v1, Math.max(v2, Math.max(v3, v4)))
    DoubleInterval(low, high)
  }
  override def toString: String = s"[$l,$h]"


given DoubleIntervalJoin: JoinValue[DoubleInterval] with
  override def joinValues(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval =
    DoubleInterval(Math.min(v1.l, v2.l), Math.max(v1.h, v2.h))

given IntervalDoubleOps: DoubleOps[DoubleInterval] with
  def numLit(d: Double): DoubleInterval = DoubleInterval(d, d)
  def randomDouble(): DoubleInterval = DoubleInterval(0, 1)
  def add(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = v1 + v2
  def sub(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = v1 - v2
  def mul(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = v1 * v2
  def div(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = v1 / v2

given IntervalCompareOps: CompareOps[DoubleInterval, Topped[Boolean]] with
  def lt(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] =
    if iv1.h < iv2.l then Actual(true)
    else if iv2.h <= iv1.l then Actual(false)
    else Top
  def le(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] =
    if iv1.h <= iv2.l then Actual(true)
    else if iv2.h < iv1.l then Actual(false)
    else Top
  def ge(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] = lt(iv2, iv1)
  def gt(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] = le(iv2, iv1)

given IntervalEqOps: EqOps[DoubleInterval, Topped[Boolean]] with
  override def equ(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Actual(false)
    else Top
  override def neq(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Actual(true)
    else Top