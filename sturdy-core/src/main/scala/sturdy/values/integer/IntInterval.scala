package sturdy.values.integer

import sturdy.effect.Effectful
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.relational.*

import scala.collection.immutable.TreeSet


object IntInterval:
  val Top = IntInterval(Int.MinValue, Int.MaxValue)

case class IntInterval(l: Int, h: Int):
  if (l > h)
    throw new IllegalArgumentException(s"Empty intervals are illegal $this")

  def join(other: IntInterval): IntInterval =
    IntInterval(Math.min(l, other.l), Math.max(h, other.h))
  def +(y: IntInterval): IntInterval =
    try IntInterval(StrictMath.addExact(l, y.l), StrictMath.addExact(h, y.h))
    catch case _: ArithmeticException => IntInterval.Top
  def -(y: IntInterval): IntInterval =
    try IntInterval(StrictMath.subtractExact(l, y.l), StrictMath.subtractExact(h, y.h))
    catch case _: ArithmeticException => IntInterval.Top
  def *(y: IntInterval): IntInterval =
    try withBounds2(StrictMath.multiplyExact, y)
    catch case _: ArithmeticException => IntInterval.Top
  def /(y: IntInterval): IntInterval = withBounds2(_/_, y)
  def withBounds2(f: (Int, Int) => Int, that: IntInterval): IntInterval = {
    val v1 = f(this.l, that.l)
    val v2 = f(this.l, that.h)
    val v3 = f(this.h, that.l)
    val v4 = f(this.h, that.h)
    val low = Math.min(v1, Math.min(v2, Math.min(v3, v4)))
    val high = Math.max(v1, Math.max(v2, Math.max(v3, v4)))
    IntInterval(low, high)
  }
  override def toString: String = s"[$l,$h]"

given Abstractly[Int, IntInterval] with
  override def abstractly(i: Int): IntInterval =
    IntInterval(i, i)

given PartialOrder[IntInterval] with
  override def lteq(x: IntInterval, y: IntInterval): Boolean = y.l <= x.l && x.h <= y.h

given IntIntervalJoin: Join[IntInterval] with
  override def apply(v1: IntInterval, v2: IntInterval): MaybeChanged[IntInterval] =
    MaybeChanged(IntInterval(Math.min(v1.l, v2.l), Math.max(v1.h, v2.h)), v1)

class IntIntervalWiden(bounds: => Set[Int]) extends Widen[IntInterval]:
  private lazy val treeSet: TreeSet[Int] = TreeSet.from(bounds)
  override def apply(v1: IntInterval, v2: IntInterval): MaybeChanged[IntInterval] =
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
    MaybeChanged(IntInterval(low, high), v1)

given IntervalIntegerOps(using f: Failure, j: Effectful): IntegerOps[Int, IntInterval] with
  def integerLit(i: Int): IntInterval = IntInterval(i, i)
  def randomInteger(): IntInterval = IntInterval.Top
  def add(v1: IntInterval, v2: IntInterval): IntInterval = v1 + v2
  def sub(v1: IntInterval, v2: IntInterval): IntInterval = v1 - v2
  def mul(v1: IntInterval, v2: IntInterval): IntInterval = v1 * v2
  def div(v1: IntInterval, v2: IntInterval): IntInterval = v2 match
    case IntInterval(0, 0) => f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    case IntInterval(0, h) => j.joinComputations(v1 / IntInterval(1, h))(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case IntInterval(l, 0) => j.joinComputations(v1 / IntInterval(l, -1))(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case IntInterval(l, h) =>
      if (l <= 0 && h >= 0)
        j.joinComputations(v1 / v2)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
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
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Topped.Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  override def neq(iv1: IntInterval, iv2: IntInterval): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Topped.Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(true)
    else Topped.Top