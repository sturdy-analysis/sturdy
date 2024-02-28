package sturdy.values.integer

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.relational.*

import scala.collection.immutable.TreeSet

enum Interval:
  case ITop
  case I (low: Double, high: Double)

  def <(s2: Interval): Boolean = s2 == ITop || ((this,s2) match
    case (I(l1,h1),I(l2,h2)) => h1 < l2
    case (_,_) => false)


  def negated: Interval = this match
    case ITop => ITop
    case I(low, high) => I(-high, -low)



import sturdy.values.integer.Interval.*

given Abstractly[Int, Interval] with
  override def apply(i: Int): Interval = I(i,i)


given PartialOrder[Interval] with
  override def lteq(x: Interval, y: Interval): Boolean = (x, y) match
    case (Interval.I(low1, high1), Interval.I(low2, high2)) => low2 <= low1 && high1 <= high2



given CombineInterval[W <: Widening]: Combine[Interval, W] with
  override def apply(v1: Interval, v2: Interval): MaybeChanged[Interval] =
    if v1 == v2 then Unchanged(v1)
    else (v1, v2) match
      case (I(l1, h1), I(l2, h2)) => Changed(I(math.min(l1, l2), math.max(h1, h2)))
      case _ => Changed(ITop)



class IntervalWiden(bounds: => Set[Double], minValue: Double, maxValue: Double)(using Numeric[Double]) extends Widen[Interval]:
  private lazy val treeSet: TreeSet[Double] = TreeSet.from(bounds)

  override def apply(v1: Interval, v2: Interval): MaybeChanged[Interval] =
    if v1 == v2 then Unchanged(v1)
    else (v1, v2) match
      case (I(l1, h1), I(l2, h2)) =>
        val low =
          if (l1 <= l2) l1
          else treeSet.maxBefore(l2 + summon[Numeric[Double]].fromInt(1)).getOrElse(minValue)
        val high =
          if (h1 >= h2) h1
          else treeSet.minAfter(h2).getOrElse(maxValue)
        MaybeChanged(Interval.I(low, high), v1)

      case _ => Changed(ITop)




given MyIntervalIntegerOps[B](using f: Failure, j: EffectStack, base: Integral[B]): IntegerOps[B, Interval] with
  def integerLit(i: B): Interval =
    val iInt = base.toInt(i)
    I(iInt, iInt)

  def randomInteger(): Interval = ITop

  def add(v1: Interval, v2: Interval): Interval = (v1, v2) match
    case (I(l1, h1), I(l2, h2)) => I(l1 + l2, h1 + h2)
    case (_, _) => ITop


  def sub(v1: Interval, v2: Interval): Interval = (v1, v2) match
    case (I(l1, h1), I(l2, h2)) => I(l1 - h2, h1 - l2)
    case (_, _) => ITop

  def neg(v: Interval): Interval = v.negated


  def mul(v1: Interval, v2: Interval): Interval = (v1, v2) match
    case (ITop, _) | (_, ITop) => ITop
    case (I(l1, h1), I(l2, h2)) =>
      val products = List(l1 * l2, l1 * h2, h1 * l2, h1 * h2)
      I(products.min, products.max)

  def div(v1: Interval, v2: Interval): Interval = (v1, v2) match
    case (_, ITop) | (ITop, _) => ITop
    case (_, I(l2, h2)) if l2 <= 0 && h2 >= 0 => ITop
    case (I(l1, h1), I(l2, h2)) =>
      val divs = List(l1 / l2, l1 / h2, h1 / l2, h1 / h2)
      I(divs.min, divs.max)


  def max(v1: Interval, v2: Interval): Interval = (v1, v2) match
    case (ITop, _) | (_, ITop) => ITop
    case (I(l1, h1), I(l2, h2)) => I(math.max(l1, l2), math.max(h1, h2))


  def min(v1: Interval, v2: Interval): Interval = (v1, v2) match
    case (ITop, i) => i
    case (i, ITop) => i
    case (I(l1, h1), I(l2, h2)) => I(math.min(l1, l2), math.min(h1, h2))

  def divUnsigned(v1: Interval, v2: Interval): Interval = ???
  def remainder(v1: Interval, v2: Interval): Interval = ???
  def remainderUnsigned(v1: Interval, v2: Interval): Interval = ???
  def modulo(v1: Interval, v2: Interval): Interval = ???
  def gcd(v1: Interval, v2: Interval): Interval = ???

  def absolute(v: Interval): Interval = ???
  def bitAnd(v1: Interval, v2: Interval): Interval = ???
  def bitOr(v1: Interval, v2: Interval): Interval = ???
  def bitXor(v1: Interval, v2: Interval): Interval = ???
  def shiftLeft(v: Interval, shift: Interval): Interval = ???
  def shiftRight(v: Interval, shift: Interval): Interval = ???
  def shiftRightUnsigned(v: Interval, shift: Interval): Interval = ???
  def rotateLeft(v: Interval, shift: Interval): Interval = ???
  def rotateRight(v: Interval, shift: Interval): Interval = ???
  def countLeadingZeros(v: Interval): Interval = ???
  def countTrailingZeros(v: Interval): Interval = ???
  def nonzeroBitCount(v: Interval): Interval = ???
  def invertBits(v: Interval): Interval = ???

given IntervalOrderingOps: OrderingOps[Interval, Topped[Boolean]] with
  def lt(v1: Interval, v2: Interval): Topped[Boolean] = (v1, v2) match
    case (I(l1, h1), I(l2, h2)) =>
      if (h1 < l2) then Topped.Actual(true)
      else if (h2 <= l1) then Topped.Actual(false)
      else Topped.Top
    case _ => Topped.Top

  def le(v1: Interval, v2: Interval): Topped[Boolean] = (v1, v2) match
    case (I(l1, h1), I(l2, h2)) =>
      if (h1 <= l2) then Topped.Actual(true)
      else if (h2 < l1) then Topped.Actual(false)
      else Topped.Top
    case _ => Topped.Top



given IntervalEqOps: EqOps[Interval, Topped[Boolean]] with
  def equ(v1: Interval, v2: Interval): Topped[Boolean] = (v1, v2) match
    case (I(l1, h1), I(l2, h2)) =>
      if (l1 == h1 && h1 == l2 && l2 == h2) Topped.Actual(true)
      else if (h1 < l2 || h2 < l1) Topped.Actual(false)
      else Topped.Top
    case _ => Topped.Top

  def neq(v1: Interval, v2: Interval): Topped[Boolean] = equ(v1, v2).map(!_)

given FiniteInterval: Finite[Interval] with {}