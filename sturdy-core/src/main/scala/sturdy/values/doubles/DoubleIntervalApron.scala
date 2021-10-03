package sturdy.values.doubles

import sturdy.effect.JoinComputation
import sturdy.effect.failure.Failure
import sturdy.fix.Widening
import sturdy.values.Abstractly
import sturdy.values.JoinValue
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.relational.*
import scala.language.postfixOps

import apron.Abstract0 // default; for domains without environments
import apron.Box
import apron.*

val manager = apron.Box()

object DoubleIntervalApron:
  val Top =
    DoubleIntervalApron(Double.NegativeInfinity, Double.PositiveInfinity)

// bounded definition may be required to ensure a legal interval

  def apply(interval: apron.Interval): DoubleIntervalApron = {
    var tmp: apron.DoubleScalar = interval.inf.asInstanceOf[apron.DoubleScalar]
    val l = tmp.`val`
    tmp = interval.sup.asInstanceOf[apron.DoubleScalar]
    val h = tmp.`val`
    new DoubleIntervalApron(l, h)
  }

case class DoubleIntervalApron(var l: Double, var h: Double):
  lazy val interval = apron.Interval(l, h)
  val abstractDomain = Abstract0(manager, 1, 0, Array(apron.Interval(l, h)))
  if (l > h)
    throw new IllegalArgumentException(s"Empty intervals are illegal $this")

  private enum UpdateCases:
    case ADN

  private def update(u_case: UpdateCases): Unit =
    u_case match {
      case UpdateCases.ADN => {
        val interval = this.abstractDomain.getBound(manager, 0)
        var tmp: apron.DoubleScalar =
          interval.inf.asInstanceOf[apron.DoubleScalar]
        this.l = tmp.`val`
        tmp = interval.sup.asInstanceOf[apron.DoubleScalar]
        this.h = tmp.`val`
      }
    }
  def join(other: DoubleIntervalApron): Unit =
    abstractDomain.join(manager, other.abstractDomain)
    update(UpdateCases.ADN)

  def +(y: DoubleIntervalApron): DoubleIntervalApron =
    var ourNode = Texpr0BinNode(
      Texpr0BinNode.OP_ADD,
      Texpr0Node.RTYPE_DOUBLE,
      Texpr0Node.RDIR_NEAREST,
      Texpr0CstNode(this.interval),
      Texpr0CstNode(y.interval)
    )
    DoubleIntervalApron(
      this.abstractDomain.getBound(manager, Texpr0Intern(ourNode))
    )
  def -(y: DoubleIntervalApron): DoubleIntervalApron =
    var ourNode = Texpr0BinNode(
      Texpr0BinNode.OP_SUB,
      Texpr0Node.RTYPE_DOUBLE,
      Texpr0Node.RDIR_NEAREST,
      Texpr0CstNode(this.interval),
      Texpr0CstNode(y.interval)
    )
    DoubleIntervalApron(
      this.abstractDomain.getBound(manager, Texpr0Intern(ourNode))
    )
  def *(y: DoubleIntervalApron): DoubleIntervalApron =
    var ourNode = Texpr0BinNode(
      Texpr0BinNode.OP_MUL,
      Texpr0Node.RTYPE_DOUBLE,
      Texpr0Node.RDIR_NEAREST,
      Texpr0CstNode(this.interval),
      Texpr0CstNode(y.interval)
    )
    DoubleIntervalApron(
      this.abstractDomain.getBound(manager, Texpr0Intern(ourNode))
    )
  def /(y: DoubleIntervalApron): DoubleIntervalApron =
    var ourNode = Texpr0BinNode(
      Texpr0BinNode.OP_DIV,
      Texpr0Node.RTYPE_DOUBLE,
      Texpr0Node.RDIR_NEAREST,
      Texpr0CstNode(this.interval),
      Texpr0CstNode(y.interval)
    )
    DoubleIntervalApron(
      this.abstractDomain.getBound(manager, Texpr0Intern(ourNode))
    )
  override def toString: String = s"[$l,$h]"

given Abstractly[Double, DoubleIntervalApron] with
  override def abstractly(d: Double): DoubleIntervalApron =
    DoubleIntervalApron(d, d)

given PartialOrder[DoubleIntervalApron] with
  override def lteq(x: DoubleIntervalApron, y: DoubleIntervalApron): Boolean =
    y.l <= x.l && x.h <= y.h

given DoubleIntervalApronJoin: JoinValue[DoubleIntervalApron] with
  override def joinValues(
      v1: DoubleIntervalApron,
      v2: DoubleIntervalApron
  ): DoubleIntervalApron =
    DoubleIntervalApron(v1.l min v2.l, v1.h max v2.h)

given DoubleIntervalApronWiden: Widening[DoubleIntervalApron] with
  override def widen(
      v1: DoubleIntervalApron,
      v2: DoubleIntervalApron
  ): DoubleIntervalApron =
    DoubleIntervalApron(
      v1.abstractDomain
        .widening(manager, v2.abstractDomain)
        .getBound(manager, 0)
    )

given ApronIntervalDoubleOps: DoubleOps[DoubleIntervalApron] with
  def doubleLit(d: Double): DoubleIntervalApron = DoubleIntervalApron(d, d)
  def randomDouble(): DoubleIntervalApron = DoubleIntervalApron(0, 1)
  def add(
      v1: DoubleIntervalApron,
      v2: DoubleIntervalApron
  ): DoubleIntervalApron = v1 + v2
  def sub(
      v1: DoubleIntervalApron,
      v2: DoubleIntervalApron
  ): DoubleIntervalApron = v1 - v2
  def mul(
      v1: DoubleIntervalApron,
      v2: DoubleIntervalApron
  ): DoubleIntervalApron = v1 * v2
  def div(
      v1: DoubleIntervalApron,
      v2: DoubleIntervalApron
  ): DoubleIntervalApron = v1 / v2

  def min(
      v1: DoubleIntervalApron,
      v2: DoubleIntervalApron
  ): DoubleIntervalApron = ???
  def max(
      v1: DoubleIntervalApron,
      v2: DoubleIntervalApron
  ): DoubleIntervalApron = ???

  def absolute(v: DoubleIntervalApron): DoubleIntervalApron = ???
  def negated(v: DoubleIntervalApron): DoubleIntervalApron = ???
  def sqrt(v: DoubleIntervalApron): DoubleIntervalApron = ???
  def ceil(v: DoubleIntervalApron): DoubleIntervalApron = ???
  def floor(v: DoubleIntervalApron): DoubleIntervalApron = ???
  def truncate(v: DoubleIntervalApron): DoubleIntervalApron = ???
  def nearest(v: DoubleIntervalApron): DoubleIntervalApron = ???
  def copysign(
      v: DoubleIntervalApron,
      sign: DoubleIntervalApron
  ): DoubleIntervalApron = ???

  def logNatural(v: DoubleIntervalApron): DoubleIntervalApron = ???

given DoubleIntervalApronCompareOps
    : CompareOps[DoubleIntervalApron, Topped[Boolean]] with
  def lt(iv1: DoubleIntervalApron, iv2: DoubleIntervalApron): Topped[Boolean] =
    if iv1.h < iv2.l then Topped.Actual(true)
    else if iv2.h <= iv1.l then Topped.Actual(false)
    else Topped.Top
  def le(iv1: DoubleIntervalApron, iv2: DoubleIntervalApron): Topped[Boolean] =
    if iv1.h <= iv2.l then Topped.Actual(true)
    else if iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  def ge(iv1: DoubleIntervalApron, iv2: DoubleIntervalApron): Topped[Boolean] =
    lt(iv2, iv1)
  def gt(iv1: DoubleIntervalApron, iv2: DoubleIntervalApron): Topped[Boolean] =
    le(iv2, iv1)

given DoubleIntervalApronEqOps: EqOps[DoubleIntervalApron, Topped[Boolean]] with
  override def equ(
      iv1: DoubleIntervalApron,
      iv2: DoubleIntervalApron
  ): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then
      Topped.Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  override def neq(
      iv1: DoubleIntervalApron,
      iv2: DoubleIntervalApron
  ): Topped[Boolean] =
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then
      Topped.Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(true)
    else Topped.Top
