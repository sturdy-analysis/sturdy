package sturdy.language.tip.backward.values

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.integer.IntSign
import sturdy.values.integer.IntSign.*
import sturdy.values.integer.SignIntegerOps


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

  override def add(v1: IntSign => IntSign, v2: IntSign => IntSign, r: IntSign): IntSign = r match
    case TopSign =>
      val a2 = v2(TopSign)
      val a1 = v1(TopSign)
      SignIntegerOps.add(a1, a2)
    case Neg => v2(TopSign) match
      case Zero | Pos | ZeroOrPos => v1(Neg); Neg
      case Neg | NegOrZero | TopSign => v1(TopSign); Neg
    case NegOrZero => v2(TopSign) match
      case Pos => v1(Neg); NegOrZero
      case Zero | ZeroOrPos => v1(NegOrZero); NegOrZero
      case Neg | NegOrZero | TopSign => v1(TopSign); NegOrZero
    case Zero =>
      val a2 = v2(TopSign)
      v1(a2.negated)
      Zero
    case ZeroOrPos => v2(TopSign) match
      case Neg => v1(Pos); ZeroOrPos
      case Zero | NegOrZero => v1(ZeroOrPos); ZeroOrPos
      case Pos | ZeroOrPos | TopSign => v1(TopSign); ZeroOrPos
    case Pos => v2(TopSign) match
      case Zero | NegOrZero | Neg => v1(Pos); Pos
      case Pos | ZeroOrPos | TopSign => v1(TopSign); Pos

  override def sub(v1: IntSign => IntSign, v2: IntSign => IntSign, r: IntSign): IntSign = ???

  override def mul(v1: IntSign => IntSign, v2: IntSign => IntSign, r: IntSign): IntSign = ???

  override def div(v1: IntSign => IntSign, v2: IntSign => IntSign, r: IntSign): IntSign = ???
