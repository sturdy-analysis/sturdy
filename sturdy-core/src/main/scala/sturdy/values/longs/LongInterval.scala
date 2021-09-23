package sturdy.values.longs

import sturdy.effect.JoinComputation
import sturdy.effect.failure.Failure
import sturdy.fix.Widening
import sturdy.values.Abstractly
import sturdy.values.JoinValue
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.relational.*

import scala.collection.immutable.TreeSet


object LongInterval:
  val Top = LongInterval(Long.MinValue, Long.MaxValue)
  def bounded(l: Long, h: Long): LongInterval =
    LongInterval(Math.max(l, Long.MinValue).toLong, Math.min(h, Long.MaxValue).toLong)

case class LongInterval(l: Long, h: Long):
  if (l > h)
    throw new IllegalArgumentException(s"Empty intervals are illegal $this")

  def join(other: LongInterval): LongInterval =
    LongInterval(Math.min(l, other.l), Math.max(h, other.h))
  def +(y: LongInterval): LongInterval = LongInterval.bounded(l.toLong + y.l, h.toLong + y.h)
  def -(y: LongInterval): LongInterval = LongInterval.bounded(l.toLong - y.l, h.toLong - y.h)
  def *(y: LongInterval): LongInterval = withBounds2(_*_, y)
  def /(y: LongInterval): LongInterval = withBounds2(_/_, y)
  def withBounds2(f: (Long, Long) => Long, that: LongInterval): LongInterval = {
    val v1 = f(this.l, that.l)
    val v2 = f(this.l, that.h)
    val v3 = f(this.h, that.l)
    val v4 = f(this.h, that.h)
    val low = Math.min(v1, Math.min(v2, Math.min(v3, v4)))
    val high = Math.max(v1, Math.max(v2, Math.max(v3, v4)))
    LongInterval.bounded(low, high)
  }
  override def toString: String = s"[$l,$h]"

given Abstractly[Long, LongInterval] with
  override def abstractly(i: Long): LongInterval =
    LongInterval(i, i)

given PartialOrder[LongInterval] with
  override def lteq(x: LongInterval, y: LongInterval): Boolean = y.l <= x.l && x.h <= y.h

given LongIntervalJoin: JoinValue[LongInterval] with
  override def joinValues(v1: LongInterval, v2: LongInterval): LongInterval =
    LongInterval(Math.min(v1.l, v2.l), Math.max(v1.h, v2.h))

given LongIntervalWiden(using bounds: => Set[Long]): Widening[LongInterval] with
  private lazy val treeSet: TreeSet[Long] = TreeSet.from(bounds)
  override def widen(v1: LongInterval, v2: LongInterval): LongInterval =
    val low =
      if (v1.l <= v2.l)
        v1.l
      else
        treeSet.maxBefore(v2.l + 1).getOrElse(Long.MinValue)
    val high =
      if (v1.h >= v2.h)
        v1.h
      else
        treeSet.minAfter(v2.h).getOrElse(Long.MaxValue)
    LongInterval(low, high)

given IntervalLongOps(using f: Failure, j: JoinComputation): LongOps[LongInterval] with
  def longLit(l: Long): LongInterval = LongInterval(l, l)
  def randomLong(): LongInterval = LongInterval.Top
  def add(v1: LongInterval, v2: LongInterval): LongInterval = v1 + v2
  def sub(v1: LongInterval, v2: LongInterval): LongInterval = v1 - v2
  def mul(v1: LongInterval, v2: LongInterval): LongInterval = v1 * v2
  def div(v1: LongInterval, v2: LongInterval): LongInterval = v2 match
    case LongInterval(0, 0) => f.fail(LongDivisionByZero, s"$v1 / $v2")
    case LongInterval(0, h) => j.joinComputations(v1 / LongInterval(1, h))(f.fail(LongDivisionByZero, s"$v1 / $v2"))
    case LongInterval(l, 0) => j.joinComputations(v1 / LongInterval(l, -1))(f.fail(LongDivisionByZero, s"$v1 / $v2"))
    case LongInterval(l, h) =>
      if (l <= 0 && h >= 0)
        j.joinComputations(v1 / v2)(f.fail(LongDivisionByZero, s"$v1 / $v2"))
      else
        v1 / v2
  
  def divUnsigned(v1: LongInterval, v2: LongInterval): LongInterval = ???
  def remainder(v1: LongInterval, v2: LongInterval): LongInterval = ???
  def remainderUnsigned(v1: LongInterval, v2: LongInterval): LongInterval = ???

  def bitAnd(v1: LongInterval, v2: LongInterval): LongInterval = ???
  def bitOr(v1: LongInterval, v2: LongInterval): LongInterval = ???
  def bitXor(v1: LongInterval, v2: LongInterval): LongInterval = ???
  def shiftLeft(v: LongInterval, shift: LongInterval): LongInterval = ???
  def shiftRight(v: LongInterval, shift: LongInterval): LongInterval = ???
  def shiftRightUnsigned(v: LongInterval, shift: LongInterval): LongInterval = ???
  def rotateLeft(v: LongInterval, shift: LongInterval): LongInterval = ???
  def rotateRight(v: LongInterval, shift: LongInterval): LongInterval = ???
  def countLeadingZeros(v: LongInterval): LongInterval = ???
  def countTrailinZeros(v: LongInterval): LongInterval = ???
  def nonzeroBitCount(v: LongInterval): LongInterval = ???

given LongIntervalCompareOps: CompareOps[LongInterval, Topped[Boolean]] with
  def lt(iv1: LongInterval, iv2: LongInterval): Topped[Boolean] =
    if iv1.h < iv2.l then Topped.Actual(true)
    else if iv2.h <= iv1.l then Topped.Actual(false)
    else Topped.Top
  def le(iv1: LongInterval, iv2: LongInterval): Topped[Boolean] =
    if iv1.h <= iv2.l then Topped.Actual(true)
    else if iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  def ge(iv1: LongInterval, iv2: LongInterval): Topped[Boolean] = le(iv2, iv1)
  def gt(iv1: LongInterval, iv2: LongInterval): Topped[Boolean] = lt(iv2, iv1)

given LongIntervalEqOps: EqOps[LongInterval, Topped[Boolean]] with
  override def equ(iv1: LongInterval, iv2: LongInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Topped.Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  override def neq(iv1: LongInterval, iv2: LongInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Topped.Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(true)
    else Topped.Top