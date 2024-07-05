package sturdy.values.floating

import sturdy.{IsSound, Soundness}
import sturdy.effect.Effect
import sturdy.effect.failure.Failure
import sturdy.values.Abstractly
import sturdy.values.Join
import sturdy.values.MaybeChanged
import sturdy.values.Widen
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.ordering.*

object FloatInterval:
  val Top = FloatInterval(Float.NegativeInfinity, Float.PositiveInfinity)

case class FloatInterval(l: Float, h: Float):
  if (l > h) throw new IllegalArgumentException(s"Empty intervals are illegal $this")

  def join(other: FloatInterval): FloatInterval =
    FloatInterval(Math.min(l, other.l), Math.max(h, other.h))
  def +(y: FloatInterval): FloatInterval = FloatInterval(l + y.l, h + y.h)
  def -(y: FloatInterval): FloatInterval = FloatInterval(l - y.l, h - y.h)
  def *(y: FloatInterval): FloatInterval = withBounds2(_*_, y)
  def /(y: FloatInterval): FloatInterval = withBounds2(_/_, y)
  def withBounds2(f: (Float, Float) => Float, that: FloatInterval): FloatInterval = {
    val v1 = f(this.l, that.l)
    val v2 = f(this.l, that.h)
    val v3 = f(this.h, that.l)
    val v4 = f(this.h, that.h)
    val low = Math.min(v1, Math.min(v2, Math.min(v3, v4)))
    val high = Math.max(v1, Math.max(v2, Math.max(v3, v4)))
    FloatInterval(low, high)
  }
  override def toString: String = s"[$l,$h]"

given Abstractly[Float, FloatInterval] with
  override def apply(d: Float): FloatInterval =
    FloatInterval(d, d)

given PartialOrder[FloatInterval] with
  override def lteq(x: FloatInterval, y: FloatInterval): Boolean = y.l <= x.l && x.h <= y.h

given JoinFfloatInterval: Join[FloatInterval] with
  override def apply(v1: FloatInterval, v2: FloatInterval): MaybeChanged[FloatInterval] =
    MaybeChanged(FloatInterval(Math.min(v1.l, v2.l), Math.max(v1.h, v2.h)), v1)

given WidenFloatInterval: Widen[FloatInterval] with
  override def apply(v1: FloatInterval, v2: FloatInterval): MaybeChanged[FloatInterval] =
    val low =
      if (v1.l <= v2.l)
        v1.l
      else
        Float.NegativeInfinity
    val high =
      if (v1.h >= v2.h)
        v1.h
      else
        Float.PositiveInfinity
    MaybeChanged(FloatInterval(low, high), v1)

given FloatIntervalFloatOps: FloatOps[Float, FloatInterval] = new IntervalFloatOps {}
trait IntervalFloatOps extends FloatOps[Float, FloatInterval]:
  def floatingLit(f: Float): FloatInterval = FloatInterval(f, f)
  def randomFloat(): FloatInterval = FloatInterval(0, 1)
  def add(v1: FloatInterval, v2: FloatInterval): FloatInterval = v1 + v2
  def sub(v1: FloatInterval, v2: FloatInterval): FloatInterval = v1 - v2
  def mul(v1: FloatInterval, v2: FloatInterval): FloatInterval = v1 * v2
  def div(v1: FloatInterval, v2: FloatInterval): FloatInterval = v1 / v2

  def min(v1: FloatInterval, v2: FloatInterval): FloatInterval = ???
  def max(v1: FloatInterval, v2: FloatInterval): FloatInterval = ???

  def absolute(v: FloatInterval): FloatInterval = ???
  def negated(v: FloatInterval): FloatInterval = ???
  def sqrt(v: FloatInterval): FloatInterval = ???
  def ceil(v: FloatInterval): FloatInterval = ???
  def floor(v: FloatInterval): FloatInterval = ???
  def truncate(v: FloatInterval): FloatInterval = ???
  def nearest(v: FloatInterval): FloatInterval = ???
  def copysign(v: FloatInterval, sign: FloatInterval): FloatInterval = ???

given FloatIntervalOrderingOps: OrderingOps[FloatInterval, Topped[Boolean]] with
  def lt(iv1: FloatInterval, iv2: FloatInterval): Topped[Boolean] =
    if iv1.h < iv2.l then Topped.Actual(true)
    else if iv2.h <= iv1.l then Topped.Actual(false)
    else Topped.Top
  def le(iv1: FloatInterval, iv2: FloatInterval): Topped[Boolean] =
    if iv1.h <= iv2.l then Topped.Actual(true)
    else if iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top

given FloatIntervalEqOps: EqOps[FloatInterval, Topped[Boolean]] with
  override def equ(iv1: FloatInterval, iv2: FloatInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Topped.Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  override def neq(iv1: FloatInterval, iv2: FloatInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Topped.Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(true)
    else Topped.Top

given SoundnessFloatInterval: Soundness[Float, FloatInterval] with
  override def isSound(f: Float, iv: FloatInterval): IsSound =
    if(iv.l <= f && f <= iv.h) {
      IsSound.Sound
    } else {
      IsSound.NotSound(s"Float $f not in interval $iv")
    }