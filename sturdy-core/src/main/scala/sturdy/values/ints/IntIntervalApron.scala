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
import scala.language.implicitConversions
import scala.math.Ordered.orderingToOrdered

import scala.collection.immutable.TreeSet

import apron.Abstract0 // default; for domains without environments
import apron.Box
import apron.*
import scala.compiletime.ops.int

val manager = apron.Box()
def maxMpq(one: gmp.Mpq, other: gmp.Mpq) = {
  if ((one cmp other) > 0)
    one
  else 
    other
}
def minMpq(one: gmp.Mpq, other: gmp.Mpq) = {
  if ((one cmp other) < 0)
    one
  else 
    other
}

object IntIntervalApron:
  val Top = IntIntervalApron(gmp.Mpq(-1, 0), gmp.Mpq(1, 0))
  
  def bounded(l: Long, h: Long): IntIntervalApron =
    IntIntervalApron((l max Int.MinValue).toInt, (h min Int.MaxValue).toInt)
  def bounded(l: gmp.Mpq, h: gmp.Mpq): IntIntervalApron =
    IntIntervalApron(maxMpq(l, gmp.Mpq(Int.MaxValue, 1)), maxMpq(h, gmp.Mpq(Int.MinValue, 1)))

  def apply(l: Int, h: Int) = new IntIntervalApron(apron.Interval(l, h))
  def apply(inf: apron.MpqScalar, sup: apron.MpqScalar) = new IntIntervalApron(apron.Interval(inf, sup))
  def apply(l: gmp.Mpq, h: gmp.Mpq) = new IntIntervalApron(apron.Interval(l, h))
  def apply(l: gmp.Mpq, h: Int) = new IntIntervalApron(apron.Interval(l, gmp.Mpq(h)))
  def apply(l: Int, h: gmp.Mpq) = new IntIntervalApron(apron.Interval(gmp.Mpq(l), h))
  def apply(interval: IntInterval) = new IntIntervalApron(interval.l, interval.h)

  def unapply(interval: IntIntervalApron): Some[(Int, Int)] =
    Some(interval.l.doubleValue.toInt, interval.h.doubleValue.toInt)

  def joinCopy(one: IntIntervalApron, other: IntIntervalApron): IntIntervalApron =
    val abstractDomain = apron.Abstract0(manager, 1, 0, Array(one.interval))
    abstractDomain.join(manager, other.abstractDomain)
    IntIntervalApron(abstractDomain.getBound(manager, 1))
  
  

case class IntIntervalApron(val interval: apron.Interval):
  val abstractDomain = Abstract0(manager, 1, 0, Array(this.interval))
  val l: gmp.Mpq = {var tmp: gmp.Mpq = null; interval.inf.toMpq(tmp, 0); tmp.canonicalize; tmp}
  val h: gmp.Mpq = {var tmp: gmp.Mpq = null; interval.sup.toMpq(tmp, 0); tmp.canonicalize; tmp}
  
  if ((interval.inf cmp interval.sup) < 0)
    throw new IllegalArgumentException(s"Empty intervals are illegal $this")
  if (!(Set(0,1) contains l.getDen.intValue) || !(Set(0,1) contains h.getDen.intValue))
    throw new IllegalArgumentException(s"Non-Integer bounds are illegal $this")
  
  def this(l: Int, h: Int) = this(apron.Interval(l, h))
  def this(inf: apron.MpqScalar, sup: apron.MpqScalar) = this(apron.Interval(inf, sup))
  def this(l: gmp.Mpq, h: gmp.Mpq) = this(apron.Interval(l, h))
  def this(l: gmp.Mpq, h: Int) = this(apron.Interval(l, gmp.Mpq(h)))
  def this(l: Int, h: gmp.Mpq) = this(apron.Interval(gmp.Mpq(l), h))

  implicit def toIntInterval(): IntInterval = {  // implicitly convert IntIntervalApron to IntInterval
    new IntInterval(this.l.doubleValue.toInt, this.h.doubleValue.toInt)
  }

  private enum UpdateCases:
    case ADN

  private def update(u_case: UpdateCases): Unit =
    u_case match {
      case UpdateCases.ADN => {
        val interval = this.abstractDomain.getBound(manager, 1)
        this.interval.setInf(interval.inf)
        this.interval.setSup(interval.sup)
        this.l.set({var tmp: gmp.Mpq = null; interval.inf.toMpq(tmp, 0); tmp.canonicalize; tmp})
        this.h.set({var tmp: gmp.Mpq = null; interval.sup.toMpq(tmp, 0); tmp.canonicalize; tmp})
      } 
    }


//todo
  implicit def toLinExpr0(): Linexpr0 = { // implicitly convert IntIntervalApron to Linexpr0
    var coeffArray = new Array[Coeff](1)
    coeffArray(0) = this.interval

    Linexpr0(coeffArray, this.interval) 
      
  }

  def joinCopy(other: IntIntervalApron): IntIntervalApron =
    // gibt überaproximierendes Intervall-Array um das abstracte objekt herum zurück
    // oder direkt: Abstract0(Manager man, int intdim, int realdim, Interval[] box) - Creates a new abstract element from a box.
    // es ist nicht möglich, dass diese Implementation schneller ist, als die von IntInterval
    IntIntervalApron(abstractDomain.joinCopy(manager, other.abstractDomain).getBound(manager, 1))

  def join(other: IntIntervalApron): Unit =
    // gibt überaproximierendes Intervall-Array um das abstracte objekt herum zurück
    // oder direkt: Abstract0(Manager man, int intdim, int realdim, Interval[] box) - Creates a new abstract element from a box.
    // es ist nicht möglich, dass diese Implementation schneller ist, als die von IntInterval
    abstractDomain.join(manager, other.abstractDomain)
    update(UpdateCases.ADN)
  
  def meet(other: IntIntervalApron): IntIntervalApron = 
    IntIntervalApron(abstractDomain.meetCopy(manager, other.abstractDomain).getBound(manager, 1))
 
  def +(y: IntIntervalApron): IntIntervalApron =
    IntIntervalApron.bounded({
      val tmp = l.clone
      tmp add y.l
      tmp
    }, {
      val tmp = h.clone
      tmp add y.h
      tmp
    })
  def -(y: IntIntervalApron): IntIntervalApron =
    IntIntervalApron.bounded({
      val tmp = l.clone
      tmp sub y.l
      tmp
    }, {
      val tmp = h.clone
      tmp sub y.h
      tmp
    })
  def *(y: IntIntervalApron): IntIntervalApron = withBounds2(_ mul _, y)
  def /(y: IntIntervalApron): IntIntervalApron = withBounds2(_ div _, y)
  def withBounds2(f: (gmp.Mpq, gmp.Mpq) => Unit, that: IntIntervalApron): IntIntervalApron = {    
    val v1 = gmp.Mpq(0); v1.add(l); f(v1, that.l)
    val v2 = gmp.Mpq(0); v2.add(l); f(v2, that.h)
    val v3 = gmp.Mpq(0); v3.add(h); f(v3, that.l)
    val v4 = gmp.Mpq(0); v4.add(h); f(v4, that.h)
    
    val low = minMpq(v1, minMpq(v2, minMpq(v3, v4)))
    val high = maxMpq(v1, maxMpq(v2, maxMpq(v3, v4)))
    IntIntervalApron.bounded(low, high)
  }

  def widen(other: IntIntervalApron): Unit =
    this.abstractDomain.widening(manager, other.abstractDomain)
    update(UpdateCases.ADN)

  override def toString: String = s"[$l,$h]"

given Abstractly[Int, IntIntervalApron] with
  override def abstractly(i: Int): IntIntervalApron =
    IntIntervalApron(i, i)

given PartialOrder[IntIntervalApron] with
  override def lteq(x: IntIntervalApron, y: IntIntervalApron): Boolean = y.l <= x.l && x.h <= y.h

given IntIntervalApronJoin: JoinValue[IntIntervalApron] with
  override def joinValues(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron =
    IntIntervalApron(minMpq(v1.l, v2.l), maxMpq(v1.h, v2.h))

given IntIntervalApronWiden(using bounds: => Set[Int]): Widening[IntIntervalApron] with
  override def widen(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron =
    IntIntervalApron(v1.abstractDomain.widening(manager, v2.abstractDomain).getBound(manager, 1))

given ApronIntervalIntOps(using f: Failure, j: JoinComputation): IntOps[IntIntervalApron] with

  def intLit(i: Int): IntIntervalApron = new IntIntervalApron(i, i)
    def randomInt(): IntIntervalApron = IntIntervalApron.Top
  def add(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = v1 + v2
  def sub(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = v1 - v2
  def mul(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = v1 * v2
  def div(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = v2 match
    case IntIntervalApron(0, 0) => f.fail(IntDivisionByZero, s"$v1 / $v2")
    case IntIntervalApron(0, h) => j.joinComputations(v1 / IntIntervalApron(1, h))(f.fail(IntDivisionByZero, s"$v1 / $v2"))
    case IntIntervalApron(l, 0) => j.joinComputations(v1 / IntIntervalApron(l, -1))(f.fail(IntDivisionByZero, s"$v1 / $v2"))
    case IntIntervalApron(l, h) =>
      if (l <= 0 && h >= 0)
        j.joinComputations(v1 / v2)(f.fail(IntDivisionByZero, s"$v1 / $v2"))
      else
        v1 / v2

  def max(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def min(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???

  def divUnsigned(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def remainder(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def remainderUnsigned(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def modulo(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def gcd(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???

  def absolute(v: IntIntervalApron): IntIntervalApron = ???
  def bitAnd(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def bitOr(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def bitXor(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def shiftLeft(v: IntIntervalApron, shift: IntIntervalApron): IntIntervalApron = ???
  def shiftRight(v: IntIntervalApron, shift: IntIntervalApron): IntIntervalApron = ???
  def shiftRightUnsigned(v: IntIntervalApron, shift: IntIntervalApron): IntIntervalApron = ???
  def rotateLeft(v: IntIntervalApron, shift: IntIntervalApron): IntIntervalApron = ???
  def rotateRight(v: IntIntervalApron, shift: IntIntervalApron): IntIntervalApron = ???
  def countLeadingZeros(v: IntIntervalApron): IntIntervalApron = ???
  def countTrailinZeros(v: IntIntervalApron): IntIntervalApron = ???
  def nonzeroBitCount(v: IntIntervalApron): IntIntervalApron = ???

given IntIntervalApronCompareOps: CompareOps[IntIntervalApron, Topped[Boolean]] with
  def lt(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] =
    if iv1.h < iv2.l then Topped.Actual(true)
    else if iv2.h <= iv1.l then Topped.Actual(false)
    else Topped.Top
  def le(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] =
    if iv1.h <= iv2.l then Topped.Actual(true)
    else if iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  def ge(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] = le(iv2, iv1)
  def gt(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] = lt(iv2, iv1)

given IntIntervalApronEqOps: EqOps[IntIntervalApron, Topped[Boolean]] with
  override def equ(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Actual(false)
    else Top
  override def neq(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Actual(true)
    else Top