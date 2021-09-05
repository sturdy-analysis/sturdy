package sturdy.values.doubles

import sturdy.effect.JoinComputation
import sturdy.effect.failure.Failure
import sturdy.fix.Widening
import sturdy.values.Abstractly
import sturdy.values.JoinValue
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.relational.*

object DoubleInterval:
  val Top = DoubleInterval(Double.NegativeInfinity, Double.PositiveInfinity)
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

given Abstractly[Double, DoubleInterval] with
  override def abstractly(d: Double): DoubleInterval =
    DoubleInterval(d, d)

given PartialOrder[DoubleInterval] with
  override def lteq(x: DoubleInterval, y: DoubleInterval): Boolean = y.l <= x.l && x.h <= y.h

given doubleIntervalJoin: JoinValue[DoubleInterval] with
  override def joinValues(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval =
    DoubleInterval(Math.min(v1.l, v2.l), Math.max(v1.h, v2.h))

given doubleIntervalWiden: Widening[DoubleInterval] with
  override def widen(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval =
    val low =
      if (v1.l <= v2.l)
        v1.l
      else
        Double.NegativeInfinity
    val high =
      if (v1.h >= v2.h)
        v1.h
      else
        Double.PositiveInfinity
    DoubleInterval(low, high)

given IntervalDoubleOps: DoubleOps[DoubleInterval] with
  def doubleLit(d: Double): DoubleInterval = DoubleInterval(d, d)
  def randomDouble(): DoubleInterval = DoubleInterval(0, 1)
  def add(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = v1 + v2
  def sub(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = v1 - v2
  def mul(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = v1 * v2
  def div(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = v1 / v2

  def min(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = ???
  def max(v1: DoubleInterval, v2: DoubleInterval): DoubleInterval = ???

  def absolute(v: DoubleInterval): DoubleInterval = ???
  def negated(v: DoubleInterval): DoubleInterval = ???
  def sqrt(v: DoubleInterval): DoubleInterval = ???
  def ceil(v: DoubleInterval): DoubleInterval = ???
  def floor(v: DoubleInterval): DoubleInterval = ???
  def truncate(v: DoubleInterval): DoubleInterval = ???
  def nearest(v: DoubleInterval): DoubleInterval = ???
  def copysign(v: DoubleInterval, sign: DoubleInterval): DoubleInterval = ???

  def logNatural(v: DoubleInterval): DoubleInterval = ???

given DoubleIntervalCompareOps: CompareOps[DoubleInterval, Topped[Boolean]] with
  def lt(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] =
    if iv1.h < iv2.l then Topped.Actual(true)
    else if iv2.h <= iv1.l then Topped.Actual(false)
    else Topped.Top
  def le(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] =
    if iv1.h <= iv2.l then Topped.Actual(true)
    else if iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  def ge(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] = lt(iv2, iv1)
  def gt(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] = le(iv2, iv1)

given DoubleIntervalEqOps: EqOps[DoubleInterval, Topped[Boolean]] with
  override def equ(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Topped.Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  override def neq(iv1: DoubleInterval, iv2: DoubleInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Topped.Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(true)
    else Topped.Top