package sturdy.language.tip.oldBackward.values

import sturdy.values.Topped
import sturdy.values.Topped.*
import sturdy.values.integer.IntSign
import sturdy.values.integer.IntSign.*

import sturdy.values.integer.Interval
import sturdy.values.integer.Interval.*

import sturdy.values.integer.SignOrderingOps
import sturdy.values.integer.IntervalOrderingOps


trait BackOrderingOps[V, B]:
  def lt(v1: V => V, v2: V => V, r: B): B
  def le(v1: V => V, v2: V => V, r: B): B
  def ge(v1: V => V, v2: V => V, r: B): B
  def gt(v1: V => V, v2: V => V, r: B): B

class LiftedBackOrderingOps[V,B,UV,UB](extract: V => UV, inject: UV => V, extractB: B => UB, injectB: UB => B)(using ops: BackOrderingOps[UV,UB]) extends BackOrderingOps[V,B]:
  import scala.language.implicitConversions
  private implicit def exB(v: B): UB = extractB(v)
  private implicit def inB(i: UB): B = injectB(i)
  private implicit def wrap(f: V => V): UV => UV = i => extract(f(inject(i)))

  override def lt(v1: V => V, v2: V => V, r: B): B = ops.lt(v1, v2, r)
  override def le(v1: V => V, v2: V => V, r: B): B = ops.le(v1, v2, r)
  override def ge(v1: V => V, v2: V => V, r: B): B = ops.ge(v1, v2, r)
  override def gt(v1: V => V, v2: V => V, r: B): B = ops.gt(v1, v2, r)

given SignBackOrderingOps: BackOrderingOps[IntSign, Topped[Boolean]] with
  override def lt(v1: IntSign => IntSign, v2: IntSign => IntSign, r: Topped[Boolean]): Topped[Boolean] = r match
    case Topped.Top =>
      val a2 = v2(TopSign)
      val a1 = v1(TopSign)
      SignOrderingOps.lt(a1, a2)
    case Topped.Actual(true) => v2(TopSign) match
      case Neg | NegOrZero | Zero => v1(Neg); r
      case ZeroOrPos | Pos | TopSign => v1(TopSign); r
    case Topped.Actual(false) => v2(TopSign) match
      case Zero | ZeroOrPos => v1(ZeroOrPos); r
      case Pos => v1(Pos); r
      case Neg | NegOrZero | TopSign => v1(TopSign); r

    override def le(v1: IntSign => IntSign, v2: IntSign => IntSign, r: Topped[Boolean]): Topped[Boolean] = r match
      case Topped.Top =>
        val a2 = v2(TopSign)
        val a1 = v1(TopSign)
        SignOrderingOps.le(a1, a2)
      case Topped.Actual(true) => v2(TopSign) match
        case NegOrZero | Zero => v1(NegOrZero); r
        case Neg => v1(Neg); r
        case ZeroOrPos | Pos | TopSign => v1(TopSign); r
      case Topped.Actual(false) => v2(TopSign) match
        case Zero | ZeroOrPos | Pos => v1(Pos); r
        case Neg | NegOrZero | TopSign => v1(TopSign); r

  override def ge(v1: IntSign => IntSign, v2: IntSign => IntSign, r: Topped[Boolean]): Topped[Boolean] = r match
    case Topped.Top =>
      val a2 = v2(TopSign)
      val a1 = v1(TopSign)
      SignOrderingOps.lt(a1, a2)
    case Topped.Actual(true) => v2(TopSign) match
      case Pos => v1(Pos); r
      case ZeroOrPos | Zero => v1(ZeroOrPos); r
      case Neg | NegOrZero | TopSign => v1(TopSign); r
    case Topped.Actual(false) => v2(TopSign) match
      case Zero | NegOrZero | Neg => v1(Neg); r
      case Pos | ZeroOrPos | TopSign => v1(TopSign); r

    override def gt(v1: IntSign => IntSign, v2: IntSign => IntSign, r: Topped[Boolean]): Topped[Boolean] = r match
      case Topped.Top =>
        val a2 = v2(TopSign)
        val a1 = v1(TopSign)
        SignOrderingOps.gt(a1, a2)
      case Topped.Actual(true) => v2(TopSign) match
        case Pos | ZeroOrPos | Zero => v1(Pos); r
        case Neg | NegOrZero | TopSign => v1(TopSign); r
      case Topped.Actual(false) => v2(TopSign) match
        case Zero | NegOrZero => v1(NegOrZero); r
        case Neg => v1(Neg); r
        case Pos | ZeroOrPos | TopSign => v1(TopSign); r


given IntervalBackOrderingOps: BackOrderingOps[Interval, Topped[Boolean]] with
  override def lt(v1: Interval => Interval, v2: Interval => Interval, r: Topped[Boolean]): Topped[Boolean] = r match
    case Topped.Top =>
      val a1 = v2(ITop)
      val a2 = v1(ITop)
      IntervalOrderingOps.lt(a1, a2)
    case Topped.Actual(true) => v2(ITop) match
      case I(l,h) => v1(I(Int.MinValue,l-1)); r
      case ITop   => v1(ITop); r
    case Topped.Actual(false) => v2(ITop) match
      case I(l, h) => v1(I(h,Int.MaxValue)); r
      case ITop => v1(ITop); r

  override def le(v1: Interval => Interval, v2: Interval => Interval, r: Topped[Boolean]): Topped[Boolean] = r match
    case Topped.Top =>
      val a1 = v1(ITop)
      val a2 = v2(ITop)
      IntervalOrderingOps.le(a1, a2)
    case Topped.Actual(true) => v2(ITop) match
      case I(l, h) =>
        v1(I(Int.MinValue, l)); r  // Inclusive comparison, so we include 'l'
      case ITop =>
        v1(ITop); r
    case Topped.Actual(false) => v2(ITop) match
      case I(l, h) =>
        v1(I(h + 1, Int.MaxValue)); r // Exclude 'h' since it's not less than or equal
      case ITop =>
        v1(ITop); r

  override def ge(v1: Interval => Interval, v2: Interval => Interval, r: Topped[Boolean]): Topped[Boolean] = r match
    case Topped.Top =>
      val a1 = v1(ITop)
      val a2 = v2(ITop)
      IntervalOrderingOps.ge(a1, a2)
    case Topped.Actual(true) => v2(ITop) match
      case I(l, h) =>
        v1(I(h, Int.MaxValue)); r  // Inclusive comparison, include 'h'
      case ITop =>
        v1(ITop); r
    case Topped.Actual(false) => v2(ITop) match
      case I(l, h) =>
        v1(I(Int.MinValue, l - 1)); r // Exclude 'l' as it's not greater than or equal
      case ITop =>
        v1(ITop); r

  override def gt(v1: Interval => Interval, v2: Interval => Interval, r: Topped[Boolean]): Topped[Boolean] = r match
    case Topped.Top =>
      val a1 = v1(ITop)
      val a2 = v2(ITop)
      //println(s"Got here: ${a1} and ${a2} with result ${IntervalOrderingOps.gt(a1, a2)}")
      IntervalOrderingOps.gt(a1, a2)
    case Topped.Actual(true) => v2(ITop) match
      case I(l, h) =>
        v1(I(h + 1, Int.MaxValue)); r  // Exclude 'h' for strict inequality
      case ITop =>
        v1(ITop); r
    case Topped.Actual(false) => v2(ITop) match
      case I(l, h) =>
        v1(I(Int.MinValue, h)); r  // Include 'h' as it's not strictly greater
      case ITop =>
        v1(ITop); r
