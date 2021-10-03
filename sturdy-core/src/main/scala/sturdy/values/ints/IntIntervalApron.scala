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
import sturdy.values.ints.IntIntervalLikeCompanion

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

  //possibly unneccesary because apron has negative and positive infinity
  def bounded(l: Long, h: Long): IntIntervalApron =
    IntIntervalApron((l max Int.MinValue).toInt, (h min Int.MaxValue).toInt)
  def bounded(l: gmp.Mpq, h: gmp.Mpq): IntIntervalApron =
    IntIntervalApron(
      maxMpq(l, gmp.Mpq(Int.MinValue, 1)),
      minMpq(h, gmp.Mpq(Int.MaxValue, 1))
    )

  //needed if we cannot be sure that we only call constructor with Ints
  def apply(l: Int, h: Int) = new IntIntervalApron(apron.Interval(l, h))
  def apply(inf: apron.MpqScalar, sup: apron.MpqScalar) = new IntIntervalApron(
    apron.Interval(inf, sup)
  )
  def apply(l: gmp.Mpq, h: gmp.Mpq) = new IntIntervalApron(apron.Interval(l, h))
  def apply(l: gmp.Mpq, h: Int) = new IntIntervalApron(
    apron.Interval(l, gmp.Mpq(h))
  )
  def apply(l: Int, h: gmp.Mpq) = new IntIntervalApron(
    apron.Interval(gmp.Mpq(l), h)
  )
  def apply(interval: IntInterval) =
    new IntIntervalApron(interval.l, interval.h)

  def unapply(interval: IntIntervalApron): Some[(Int, Int)] =
    Some(interval.l.doubleValue.toInt, interval.h.doubleValue.toInt)

  def joinCopy(
      one: IntIntervalApron,
      other: IntIntervalApron
  ): IntIntervalApron =
    val abstractDomain = apron.Abstract0(manager, 1, 0, Array(one.interval))
    abstractDomain.join(manager, other.abstractDomain)
    IntIntervalApron(abstractDomain.getBound(manager, 1))

  def meetCopy(
      one: IntIntervalApron,
      other: IntIntervalApron
  ): IntIntervalApron =
    val abstractDomain = apron.Abstract0(manager, 1, 0, Array(one.interval))
    abstractDomain.meet(manager, other.abstractDomain)
    IntIntervalApron(abstractDomain.getBound(manager, 1))

case class IntIntervalApron(val interval: apron.Interval):
  val abstractDomain = Abstract0(manager, 1, 0, Array(this.interval))
  val l: gmp.Mpq = {
    var tmp: gmp.Mpq = gmp.Mpq(0); interval.inf.toMpq(tmp, 0)
    tmp
  }
  val h: gmp.Mpq = {
    var tmp: gmp.Mpq = gmp.Mpq(0); interval.sup.toMpq(tmp, 0)
    tmp
  }

  if ((interval.inf cmp interval.sup) > 0)
    throw new IllegalArgumentException(s"Empty intervals are illegal $this")
  if (
    !(Set(0, 1) contains l.getDen.intValue) ||
    !(Set(0, 1) contains h.getDen.intValue)
  )
    throw new IllegalArgumentException(s"Non-Integer bounds are illegal $this")

  def this(l: Int, h: Int) = this(apron.Interval(l, h))
  def this(inf: apron.MpqScalar, sup: apron.MpqScalar) =
    this(apron.Interval(inf, sup))
  def this(l: gmp.Mpq, h: gmp.Mpq) = this(apron.Interval(l, h))
  def this(l: gmp.Mpq, h: Int) = this(apron.Interval(l, gmp.Mpq(h)))
  def this(l: Int, h: gmp.Mpq) = this(apron.Interval(gmp.Mpq(l), h))

  implicit def toIntInterval()
      : IntInterval = { // implicitly convert IntIntervalApron to IntInterval
    new IntInterval(this.l.doubleValue.toInt, this.h.doubleValue.toInt)
  }

  implicit def toCstNode0(interval: Interval): apron.Texpr0CstNode = {
    Texpr0CstNode(interval)
  }

  private enum UpdateCases:
    case ADN

  private def update(u_case: UpdateCases): Unit =
    u_case match {
      case UpdateCases.ADN => {
        val interval = this.abstractDomain.getBound(manager, 1)
        this.interval.setInf(interval.inf)
        this.interval.setSup(interval.sup)
        this.l.set({
          var tmp: gmp.Mpq = gmp.Mpq(0); interval.inf.toMpq(tmp, 0)
          tmp
        })
        this.h.set({
          var tmp: gmp.Mpq = gmp.Mpq(0); interval.sup.toMpq(tmp, 0)
          tmp
        })
      }
    }

  def joinCopy(other: IntIntervalApron): IntIntervalApron =
    // gibt überaproximierendes Intervall-Array um das abstracte objekt herum zurück
    // oder direkt: Abstract0(Manager man, int intdim, int realdim, Interval[] box) - Creates a new abstract element from a box.
    // es ist nicht möglich, dass diese Implementation schneller ist, als die von IntInterval
    IntIntervalApron(
      abstractDomain
        .joinCopy(manager, other.abstractDomain)
        .getBound(manager, 1)
    )

  def join(other: IntIntervalApron): Unit =
    // gibt überaproximierendes Intervall-Array um das abstracte objekt herum zurück
    // oder direkt: Abstract0(Manager man, int intdim, int realdim, Interval[] box) - Creates a new abstract element from a box.
    // es ist nicht möglich, dass diese Implementation schneller ist, als die von IntInterval
    abstractDomain.join(manager, other.abstractDomain)
    update(UpdateCases.ADN)

  def meetCopy(other: IntIntervalApron): IntIntervalApron =
    IntIntervalApron(
      abstractDomain
        .meetCopy(manager, other.abstractDomain)
        .getBound(manager, 1)
    )

  def meet(other: IntIntervalApron): Unit =
    abstractDomain.meet(manager, other.abstractDomain)
    update(UpdateCases.ADN)

  def +(y: IntIntervalApron): IntIntervalApron =
    var ourNode = Texpr0BinNode(
      Texpr0BinNode.OP_ADD,
      Texpr0Node.RTYPE_INT,
      Texpr0Node.RDIR_NEAREST,
      this.interval,
      y.interval
    )
    IntIntervalApron(
      this.abstractDomain.getBound(manager, Texpr0Intern(ourNode))
    )

  def -(y: IntIntervalApron): IntIntervalApron =
    var ourNode = Texpr0BinNode(
      Texpr0BinNode.OP_SUB,
      Texpr0Node.RTYPE_INT,
      Texpr0Node.RDIR_NEAREST,
      this.interval,
      y.interval
    )
    IntIntervalApron(
      this.abstractDomain.getBound(manager, Texpr0Intern(ourNode))
    )
  def *(y: IntIntervalApron): IntIntervalApron =
    var ourNode = Texpr0BinNode(
      Texpr0BinNode.OP_MUL,
      Texpr0Node.RTYPE_INT,
      Texpr0Node.RDIR_NEAREST,
      this.interval,
      y.interval
    )
    IntIntervalApron(
      this.abstractDomain.getBound(manager, Texpr0Intern(ourNode))
    )
  def /(y: IntIntervalApron): IntIntervalApron =
    var ourNode = Texpr0BinNode(
      Texpr0BinNode.OP_DIV,
      Texpr0Node.RTYPE_INT,
      Texpr0Node.RDIR_NEAREST,
      this.interval,
      y.interval
    )
    IntIntervalApron(
      this.abstractDomain.getBound(manager, Texpr0Intern(ourNode))
    )

  def widen(other: IntIntervalApron): Unit =
    this.abstractDomain.widening(manager, other.abstractDomain)
    update(UpdateCases.ADN)

  override def toString: String = s"[$l,$h]"

given Abstractly[Int, IntIntervalApron] with
  override def abstractly(i: Int): IntIntervalApron =
    IntIntervalApron(i, i)

given PartialOrder[IntIntervalApron] with
  override def lteq(x: IntIntervalApron, y: IntIntervalApron): Boolean =
    y.l <= x.l && x.h <= y.h

given IntIntervalApronJoin: JoinValue[IntIntervalApron] with
  override def joinValues(
      v1: IntIntervalApron,
      v2: IntIntervalApron
  ): IntIntervalApron =
    IntIntervalApron(minMpq(v1.l, v2.l), maxMpq(v1.h, v2.h))

class IntIntervalApronWiden(
    bounds: => Set[Int]
) // TODO: needs to be fixed, no parity with IntInterval
    extends Widening[IntIntervalApron]:
  override def widen(
      v1: IntIntervalApron,
      v2: IntIntervalApron
  ): IntIntervalApron =
    IntIntervalApron(
      v1.abstractDomain
        .widening(manager, v2.abstractDomain)
        .getBound(manager, 0)
    )

given ApronIntervalIntOps(using
    f: Failure,
    j: JoinComputation
): IntOps[IntIntervalApron] with

  def intLit(i: Int): IntIntervalApron = new IntIntervalApron(i, i)
  def randomInt(): IntIntervalApron = IntIntervalApron.Top
  def add(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron =
    v1 + v2
  def sub(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron =
    v1 - v2
  def mul(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron =
    v1 * v2
  def div(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron =
    v2 match
      case IntIntervalApron(0, 0) => f.fail(IntDivisionByZero, s"$v1 / $v2")
      case IntIntervalApron(0, h) =>
        j.joinComputations(v1 / IntIntervalApron(1, h))(
          f.fail(IntDivisionByZero, s"$v1 / $v2")
        )
      case IntIntervalApron(l, 0) =>
        j.joinComputations(v1 / IntIntervalApron(l, -1))(
          f.fail(IntDivisionByZero, s"$v1 / $v2")
        )
      case IntIntervalApron(l, h) =>
        if (l <= 0 && h >= 0)
          j.joinComputations(v1 / v2)(f.fail(IntDivisionByZero, s"$v1 / $v2"))
        else
          v1 / v2

  def max(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def min(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???

  def divUnsigned(
      v1: IntIntervalApron,
      v2: IntIntervalApron
  ): IntIntervalApron = ???
  def remainder(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron =
    ???
  def remainderUnsigned(
      v1: IntIntervalApron,
      v2: IntIntervalApron
  ): IntIntervalApron = ???
  def modulo(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def gcd(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???

  def absolute(v: IntIntervalApron): IntIntervalApron = ???
  def bitAnd(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def bitOr(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def bitXor(v1: IntIntervalApron, v2: IntIntervalApron): IntIntervalApron = ???
  def shiftLeft(
      v: IntIntervalApron,
      shift: IntIntervalApron
  ): IntIntervalApron = ???
  def shiftRight(
      v: IntIntervalApron,
      shift: IntIntervalApron
  ): IntIntervalApron = ???
  def shiftRightUnsigned(
      v: IntIntervalApron,
      shift: IntIntervalApron
  ): IntIntervalApron = ???
  def rotateLeft(
      v: IntIntervalApron,
      shift: IntIntervalApron
  ): IntIntervalApron = ???
  def rotateRight(
      v: IntIntervalApron,
      shift: IntIntervalApron
  ): IntIntervalApron = ???
  def countLeadingZeros(v: IntIntervalApron): IntIntervalApron = ???
  def countTrailinZeros(v: IntIntervalApron): IntIntervalApron = ???
  def nonzeroBitCount(v: IntIntervalApron): IntIntervalApron = ???

given IntIntervalApronCompareOps: CompareOps[IntIntervalApron, Topped[Boolean]]
  with
  def lt(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] =
    if iv1.h < iv2.l then Topped.Actual(true)
    else if iv2.h <= iv1.l then Topped.Actual(false)
    else Topped.Top
  def le(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] =
    if iv1.h <= iv2.l then Topped.Actual(true)
    else if iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  def ge(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] =
    le(iv2, iv1)
  def gt(iv1: IntIntervalApron, iv2: IntIntervalApron): Topped[Boolean] =
    lt(iv2, iv1)

given IntIntervalApronEqOps: EqOps[IntIntervalApron, Topped[Boolean]] with
  override def equ(
      iv1: IntIntervalApron,
      iv2: IntIntervalApron
  ): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Actual(false)
    else Top
  override def neq(
      iv1: IntIntervalApron,
      iv2: IntIntervalApron
  ): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Actual(true)
    else Top
