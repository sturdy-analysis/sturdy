package sturdy.values.ints


import sturdy.effect.JoinComputation
import sturdy.effect.failure.Failure
import sturdy.fix.Widening
import sturdy.values.Abstractly
import sturdy.values.JoinValue
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.Topped.*
import sturdy.values.relational.*

import scala.collection.immutable.TreeSet

import apron.Abstract0 //default; for domains without environments
import apron.Box

val ourAbstractDomain = apron.Box()

object IntIntervalApron:
  var Top = apron.Interval().setTop()
  def bounded(l: Long, h: Long): IntIntervalApron =
    IntIntervalApron(Math.max(l, Int.MinValue).toInt, Math.min(h, Int.MaxValue).toInt)

case class IntIntervalApron(l: Int, h: Int):
  if (l > h)
    throw new IllegalArgumentException(s"Empty intervals are illegal $this")
  def join(other: IntIntervalApron): IntIntervalApron =
    //problem: die join-Funktion erwartet abstract0 Objekte aber die apron Intervalle sind keine abstract0 Objekte sondern Coeff
    var abstract0Object = Abstract0(ourAbstractDomain, 2, 0)
    abstract0Object.toBox(ourAbstractDomain) //gibt überaproximierendes Intervall-Array um das abstracte objekt herum zurück
    //oder direkt: Abstract0(Manager man, int intdim, int realdim, Interval[] box) - Creates a new abstract element from a box.
    //var intervalArray: Seq[Abstract0] = Array(this, other)
    apron.Abstract0.join(ourAbstractDomain,intervalArray)

     
/* 
object IntInterval:
  val Top = IntInterval(Int.MinValue, Int.MaxValue)
  def bounded(l: Long, h: Long): IntInterval =
    IntInterval(Math.max(l, Int.MinValue).toInt, Math.min(h, Int.MaxValue).toInt)
case class IntInterval(l: Int, h: Int):
  if (l > h)
    throw new IllegalArgumentException(s"Empty intervals are illegal $this")

  def join(other: IntInterval): IntInterval =
    IntInterval(Math.min(l, other.l), Math.max(h, other.h))
  def +(y: IntInterval): IntInterval = IntInterval.bounded(l.toLong + y.l, h.toLong + y.h)
  def -(y: IntInterval): IntInterval = IntInterval.bounded(l.toLong - y.l, h.toLong - y.h)
  def *(y: IntInterval): IntInterval = withBounds2(_*_, y)
  def /(y: IntInterval): IntInterval = withBounds2(_/_, y)
  def withBounds2(f: (Long, Long) => Long, that: IntInterval): IntInterval = {
    val v1 = f(this.l, that.l)
    val v2 = f(this.l, that.h)
    val v3 = f(this.h, that.l)
    val v4 = f(this.h, that.h)
    val low = Math.min(v1, Math.min(v2, Math.min(v3, v4)))
    val high = Math.max(v1, Math.max(v2, Math.max(v3, v4)))
    IntInterval.bounded(low, high)
  }
  override def toString: String = s"[$l,$h]"

given Abstractly[Int, IntInterval] with
  override def abstractly(i: Int): IntInterval =
    IntInterval(i, i)

given PartialOrder[IntInterval] with
  override def lteq(x: IntInterval, y: IntInterval): Boolean = y.l <= x.l && x.h <= y.h

given intIntervalJoin: JoinValue[IntInterval] with
  override def joinValues(v1: IntInterval, v2: IntInterval): IntInterval =
    IntInterval(Math.min(v1.l, v2.l), Math.max(v1.h, v2.h))

given intIntervalWiden(using bounds: => Set[Int]): Widening[IntInterval] with
  private lazy val treeSet: TreeSet[Int] = TreeSet.from(bounds)
  override def widen(v1: IntInterval, v2: IntInterval): IntInterval =
    val low =
      if (v1.l <= v2.l)
        v1.l
      else
        treeSet.maxBefore(v2.l + 1).getOrElse(Int.MinValue)
    val high =
      if (v1.h >= v2.h)
        v1.h
      else
        treeSet.minAfter(v2.h).getOrElse(Int.MaxValue)
    IntInterval(low, high)

given IntervalIntOps(using f: Failure, j: JoinComputation): IntOps[IntInterval] with
  def intLit(i: Int): IntInterval = IntInterval(i, i)
  //def intLit(i: Int): de.poiu.apron.Interval
  def randomInt(): IntInterval = IntInterval.Top
  def add(v1: IntInterval, v2: IntInterval): IntInterval = v1 + v2
  def sub(v1: IntInterval, v2: IntInterval): IntInterval = v1 - v2
  def mul(v1: IntInterval, v2: IntInterval): IntInterval = v1 * v2
  def div(v1: IntInterval, v2: IntInterval): IntInterval = v2 match
    case IntInterval(0, 0) => f.fail(IntDivisionByZero, s"$v1 / $v2")
    case IntInterval(0, h) => j.joinComputations(v1 / IntInterval(1, h))(f.fail(IntDivisionByZero, s"$v1 / $v2"))
    case IntInterval(l, 0) => j.joinComputations(v1 / IntInterval(l, -1))(f.fail(IntDivisionByZero, s"$v1 / $v2"))
    case IntInterval(l, h) =>
      if (l <= 0 && h >= 0)
        j.joinComputations(v1 / v2)(f.fail(IntDivisionByZero, s"$v1 / $v2"))
      else
        v1 / v2

  def max(v1: IntInterval, v2: IntInterval): IntInterval = ???
  def min(v1: IntInterval, v2: IntInterval): IntInterval = ???

  def divUnsigned(v1: IntInterval, v2: IntInterval): IntInterval = ???
  def remainder(v1: IntInterval, v2: IntInterval): IntInterval = ???
  def remainderUnsigned(v1: IntInterval, v2: IntInterval): IntInterval = ???
  def modulo(v1: IntInterval, v2: IntInterval): IntInterval = ???
  def gcd(v1: IntInterval, v2: IntInterval): IntInterval = ???

  def absolute(v: IntInterval): IntInterval = ???
  def bitAnd(v1: IntInterval, v2: IntInterval): IntInterval = ???
  def bitOr(v1: IntInterval, v2: IntInterval): IntInterval = ???
  def bitXor(v1: IntInterval, v2: IntInterval): IntInterval = ???
  def shiftLeft(v: IntInterval, shift: IntInterval): IntInterval = ???
  def shiftRight(v: IntInterval, shift: IntInterval): IntInterval = ???
  def shiftRightUnsigned(v: IntInterval, shift: IntInterval): IntInterval = ???
  def rotateLeft(v: IntInterval, shift: IntInterval): IntInterval = ???
  def rotateRight(v: IntInterval, shift: IntInterval): IntInterval = ???
  def countLeadingZeros(v: IntInterval): IntInterval = ???
  def countTrailinZeros(v: IntInterval): IntInterval = ???
  def nonzeroBitCount(v: IntInterval): IntInterval = ???

given IntIntervalCompareOps: CompareOps[IntInterval, Topped[Boolean]] with
  def lt(iv1: IntInterval, iv2: IntInterval): Topped[Boolean] =
    if iv1.h < iv2.l then Topped.Actual(true)
    else if iv2.h <= iv1.l then Topped.Actual(false)
    else Topped.Top
  def le(iv1: IntInterval, iv2: IntInterval): Topped[Boolean] =
    if iv1.h <= iv2.l then Topped.Actual(true)
    else if iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  def ge(iv1: IntInterval, iv2: IntInterval): Topped[Boolean] = le(iv2, iv1)
  def gt(iv1: IntInterval, iv2: IntInterval): Topped[Boolean] = lt(iv2, iv1)

given IntIntervalEqOps: EqOps[IntInterval, Topped[Boolean]] with
  override def equ(iv1: IntInterval, iv2: IntInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Actual(false)
    else Top
  override def neq(iv1: IntInterval, iv2: IntInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Actual(true)
    else Top */