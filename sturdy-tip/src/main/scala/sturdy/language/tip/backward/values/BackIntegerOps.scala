package sturdy.language.tip.backward.values

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.language.tip.backward.Meet
import sturdy.language.tip.backward.TipBackFailure.BackwardsUnreachable
import sturdy.values.integer.IntSign
import sturdy.values.integer.IntSign.*
import sturdy.values.integer.CombineIntSign
import sturdy.values.integer.SignIntegerOps


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
