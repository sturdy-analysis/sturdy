package sturdy.language.tip.backward.values

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.language.tip.backward.Meet
import sturdy.language.tip.backward.TipBackFailure.BackwardsUnreachable
import sturdy.values.integer.IntSign
import sturdy.values.integer.IntSign.*
import sturdy.values.integer.Interval
import sturdy.values.integer.Interval.*
import sturdy.values.integer.CombineIntSign
import sturdy.values.integer.SignIntegerOps
import sturdy.language.tip.backward.TipBackFailure.BackwardsUnreachable
import sturdy.values.integer.CombineIntSign





/** 
 * 
 * e1 + e2
 * 
 * Necessary properties
 * - e1 and e2 must be evaluated exactly once
 * - e2 must be evaluated before e1
 * 
 */
trait BackIntegerOps[B, V]:
  def integerLit(i: B): V

  def toValue(is: List[B]): V

  def randomInteger(): V

  def add(v1: V => V, v2: V => V, r: V): V
  def sub(v1: V => V, v2: V => V, r: V): V
  def mul(v1: V => V, v2: V => V, r: V): V
  def div(v1: V => V, v2: V => V, r: V): V

class LiftedBackIntegerOps[B,V,I](extract: V => I, inject: I => V)(using ops: BackIntegerOps[B,I]) extends BackIntegerOps[B,V]:
  import scala.language.implicitConversions
  private implicit def ex(v: V): I = extract(v)
  private implicit def in(i: I): V = inject(i)
  private implicit def wrap(f: V => V): I => I = i => extract(f(inject(i)))

  override def integerLit(i: B): V = ops.integerLit(i)
  override def toValue(is: List[B]): V = ops.toValue(is)
  override def randomInteger(): V = ops.randomInteger()
  override def add(v1: V => V, v2: V => V, r: V): V = ops.add(v1, v2, r)
  override def sub(v1: V => V, v2: V => V, r: V): V = ops.sub(v1, v2, r)
  override def mul(v1: V => V, v2: V => V, r: V): V = ops.mul(v1, v2, r)
  override def div(v1: V => V, v2: V => V, r: V): V = ops.div(v1, v2, r)


given SignBackIntegerOps[B](using failure: Failure, j: EffectStack, base: Integral[B]): BackIntegerOps[B, IntSign] with
  override def integerLit(i: B): IntSign =
    if base.lt(i, base.zero) then Neg
    else if base.gt(i, base.zero) then Pos
    else Zero

  override def toValue(is: List[B]): IntSign = is match
    case Nil => throw new IllegalArgumentException("List is empty, cannot extract head element.")
    case (i :: _) => integerLit(i)
  override def randomInteger(): IntSign = TopSign

  //  { x = Pos, y = Pos } 
  // x * y = Pos 
  //  { x = Pos, y = Top }
  
  // evalBack(x * y, Pos)
  //   evalBack(y, Top) = Top
  //   evalBack(x, Top) = Pos
  // = Pos
  // missed: y needs to be Pos

  // evalBack(x * (y - 56), Pos)
  //   evalBack(y - 56, Top) = Top
  //     evalBack(y, Top) = Top
  //   evalBack(x, Top) = Pos
  // = Pos
  // missed: y needs to be Pos

  // evalBack(y * x, Pos)
  //   evalBack(x, Top) = Pos
  //   evalBack(y, Pos) = Pos
  // = Pos
  // found: y is Pos

  override def add(v1: IntSign => IntSign, v2: IntSign => IntSign, r: IntSign): IntSign = r match
    case TopSign =>
      val a2 = v2(TopSign)
      val a1 = v1(TopSign)
      SignIntegerOps.add(a1, a2)
    case Neg => v2(TopSign) match
      case Zero | Pos | ZeroOrPos => v1(Neg)
      case Neg | NegOrZero | TopSign => v1(TopSign)
      Neg
    case NegOrZero => v2(TopSign) match
      case Pos => v1(Neg)
      case Zero | ZeroOrPos => v1(NegOrZero)
      case Neg | NegOrZero | TopSign => v1(TopSign)
      NegOrZero
    case Zero =>
      val a2 = v2(TopSign)
      v1(a2.negated)
      Zero
    case ZeroOrPos => v2(TopSign) match
      case Neg => v1(Pos)
      case Zero | NegOrZero => v1(ZeroOrPos)
      case Pos | ZeroOrPos | TopSign => v1(TopSign)
      ZeroOrPos
    case Pos => v2(TopSign) match
      case Zero | NegOrZero | Neg => v1(Pos)
      case Pos | ZeroOrPos | TopSign => v1(TopSign)
      Pos
  
  override def sub(v1: IntSign => IntSign, v2: IntSign => IntSign, r: IntSign): IntSign = r match
    case TopSign =>
      val a2 = v2(TopSign)
      val a1 = v1(TopSign)
      SignIntegerOps.sub(a1, a2)
    case Neg => v2(TopSign) match
      case Zero | Neg | NegOrZero => v1(Neg)
      case Pos | ZeroOrPos | TopSign => v1(TopSign)
      Neg
    case NegOrZero => v2(TopSign) match
      case Neg => v1(Neg)
      case Zero | NegOrZero => v1(NegOrZero)
      case Pos | ZeroOrPos | TopSign => v1(TopSign)
      NegOrZero
    case Zero => v1(v2(TopSign)); Zero
    case ZeroOrPos => v2(TopSign) match
      case Pos => v1(Pos)
      case ZeroOrPos | Zero => v1(ZeroOrPos)
      case Neg | NegOrZero | TopSign => v1(TopSign)
      ZeroOrPos
    case Pos => v2(TopSign) match
      case Pos | ZeroOrPos | Zero => v1(Pos)
      case Neg | NegOrZero | TopSign => v1(TopSign)
      Pos


  override def mul(v1: IntSign => IntSign, v2: IntSign => IntSign, r: IntSign): IntSign = r match
    case TopSign =>
      val a2 = v2(TopSign)
      val a1 = v1(TopSign)
      SignIntegerOps.mul(a1, a2)
    case Neg => v2(TopSign) match
      case Neg | NegOrZero => v1(Pos)
      case Pos | ZeroOrPos => v1(Neg)
      case Zero => failure(BackwardsUnreachable, "impossible: ? * Zero = Neg")
      case TopSign => j.joinComputations(v1(Neg))(v1(Pos))
      Neg
    case NegOrZero => v2(TopSign) match
      case Neg => v1(ZeroOrPos)
      case Pos => v1(NegOrZero)
      case NegOrZero | ZeroOrPos | Zero | TopSign => v1(TopSign)
      NegOrZero
    case Zero => v2(TopSign) match
      case Neg | Pos => v1(Zero)
      case NegOrZero | ZeroOrPos | Zero | TopSign => v1(TopSign)
      Zero
    case ZeroOrPos => v2(TopSign) match
      case Neg => v1(NegOrZero)
      case Pos => v1(ZeroOrPos)
      case NegOrZero | ZeroOrPos | Zero | TopSign => v1(TopSign)
      ZeroOrPos
    case Pos => v2(TopSign) match
      case Neg | NegOrZero => v1(Neg)
      case Pos | ZeroOrPos => v1(Pos)
      case Zero => failure(BackwardsUnreachable, "impossible: ? * Zero = Pos")
      case TopSign => j.joinComputations(v1(Neg))(v1(Pos))
      Pos

  override def div(v1: IntSign => IntSign, v2: IntSign => IntSign, r: IntSign): IntSign =
    def splitV2(f: (Neg.type | Pos.type ) => IntSign) =
      j.joinComputations{v2(Neg);f(Neg)}{v2(Pos);f(Pos)}
    r match
      case TopSign =>
        splitV2(a2 => SignIntegerOps.div(v1(TopSign), a2))
      case Neg => splitV2 {
        case Neg => v1(Pos)
        case Pos => v1(Neg)
      }; Neg
      case NegOrZero => splitV2 {
        case Neg => v1(ZeroOrPos)
        case Pos => v1(NegOrZero)
      }; NegOrZero
      case Zero => splitV2 {
        case Neg | Pos => v1(Zero)
      }; Zero
      case ZeroOrPos => splitV2 {
        case Neg => v1(NegOrZero)
        case Pos => v1(ZeroOrPos)
      }; ZeroOrPos
      case Pos => splitV2 {
        case Neg => v1(Neg)
        case Pos => v1(Pos)
      }; Pos



given Meet[IntSign] with
  override def meet(v1: IntSign, v2: IntSign): Option[IntSign] = (v1, v2) match
    case (TopSign, _) => Some(v2)
    case (_, TopSign) => Some(v1)
    case (NegOrZero, Neg | Zero) => Some(v2)
    case (Neg | Zero, NegOrZero) => Some(v1)
    case (ZeroOrPos, Zero | Pos) => Some(v2)
    case (Zero | Pos, ZeroOrPos) => Some(v1)
    case _ if v1 == v2 => Some(v1)
    case _ => None



given IntervalBackIntegerOps[B](using failure: Failure, j: EffectStack, base: Integral[B]): BackIntegerOps[B, Interval] with

  override def integerLit(i: B): Interval = I(base.toInt(i), base.toInt(i))

  override def toValue(is: List[B]): Interval = is match
    case i1 :: i2 :: Nil =>
      val l = base.toInt(i1)
      val h = base.toInt(i2)
      I(l,h)
    case _ =>
      throw new IllegalArgumentException("List must contain exactly two elements.")


  override def randomInteger(): Interval = ITop

  override def add(v1: Interval => Interval, v2: Interval => Interval, r: Interval): Interval = r match
    case ITop => Interval.ITop
    case I(l, h) =>
      val I(l2, h2) = v2(ITop)
      val I(l1, h1) = v1(ITop)
      I(l1 + l2, h1 + h2)

  override def sub(v1: Interval => Interval, v2: Interval => Interval, r: Interval): Interval = r match
    case ITop => ITop
    case I(l, h) =>
      val I(l2, h2) = v2(ITop)
      val I(l1, h1) = v1(ITop)
      I(l1 - h2, h1 - l2)

  override def mul(v1: Interval => Interval, v2: Interval => Interval, r: Interval): Interval = ???
  override def div(v1: Interval => Interval, v2: Interval => Interval, r: Interval): Interval = ???


given Meet[Interval] with
  override def meet(v1: Interval, v2: Interval): Option[Interval] = (v1, v2) match
    case (ITop, _) => Some(v2)
    case (_, ITop) => Some(v1)
    case (I(low1, high1), I(low2, high2)) =>
      val newLow = Math.max(low1, low2)
      val newHigh = Math.min(high1, high2)
      if newLow <= newHigh then Some(I(newLow, newHigh)) else None