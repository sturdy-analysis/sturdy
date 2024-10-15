package sturdy.gradual.values.integer

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.gradual.GradualOps
import sturdy.values.PartialOrder
import sturdy.values.integer.{IntSign, IntegerDivisionByZero, IntegerOps}

given OverflowOptimisticIntSignIntegerOps[B](using base: Numeric[B], f: Failure, j: EffectStack): IntegerOps[B, IntSign] with
  import IntSign.*

  override def integerLit(i: B): IntSign =
    if base.lt(i, base.zero) then Neg
    else if base.gt(i, base.zero) then Pos
    else Zero

  def randomInteger(): IntSign = ZeroOrPos

  override def add(v1: IntSign, v2: IntSign): IntSign =
    (v1, v2) match
      case (TopSign, _) => TopSign
      case (_, TopSign) => TopSign
      case (_, Zero) => v1
      case (Zero, _) => v2

      case (Neg, Neg) => Neg
      case (Neg, NegOrZero) => Neg
      case (Neg, ZeroOrPos) => TopSign
      case (Neg, Pos) => TopSign

      case (NegOrZero, Neg) => Neg
      case (NegOrZero, NegOrZero) => NegOrZero
      case (NegOrZero, ZeroOrPos) => TopSign
      case (NegOrZero, Pos) => TopSign

      case (ZeroOrPos, Neg) => TopSign
      case (ZeroOrPos, NegOrZero) => TopSign
      case (ZeroOrPos, ZeroOrPos) => ZeroOrPos
      case (ZeroOrPos, Pos) => Pos

      case (Pos, Neg) => TopSign
      case (Pos, NegOrZero) => TopSign
      case (Pos, ZeroOrPos) => Pos
      case (Pos, Pos) => Pos

  override def sub(v1: IntSign, v2: IntSign): IntSign =
    val negV2 = v2 match
      case TopSign => TopSign
      case Neg => Pos
      case Zero => Zero
      case NegOrZero => ZeroOrPos
      case ZeroOrPos => NegOrZero
      case Pos => Neg
    add(v1, negV2)

  override def mul(v1: IntSign, v2: IntSign): IntSign =
    (v1, v2) match
      case (_, Zero) => Zero
      case (Zero, _) => Zero
      case (TopSign, _) => TopSign
      case (_, TopSign) => TopSign

      case (Neg, Neg) => Pos
      case (Neg, NegOrZero) => ZeroOrPos
      case (Neg, ZeroOrPos) => NegOrZero
      case (Neg, Pos) => Neg

      case (NegOrZero, Neg) => ZeroOrPos
      case (NegOrZero, NegOrZero) => ZeroOrPos
      case (NegOrZero, ZeroOrPos) => NegOrZero
      case (NegOrZero, Pos) => NegOrZero

      case (ZeroOrPos, Neg) => NegOrZero
      case (ZeroOrPos, NegOrZero) => NegOrZero
      case (ZeroOrPos, ZeroOrPos) => ZeroOrPos
      case (ZeroOrPos, Pos) => ZeroOrPos

      case (Pos, Neg) => Neg
      case (Pos, NegOrZero) => NegOrZero
      case (Pos, ZeroOrPos) => ZeroOrPos
      case (Pos, Pos) => Pos

  override def div(v1: IntSign, v2: IntSign): IntSign =
    v2 match
      case Zero => f.fail(IntegerDivisionByZero, s"$v1 / $v2")
      case ZeroOrPos => j.joinWithFailure(v1)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      case NegOrZero => j.joinWithFailure(v1.negated)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      case TopSign => j.joinWithFailure(TopSign)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      case _ => this.mul(v1, v2)

  override def max(v1: IntSign, v2: IntSign): IntSign = ???
  override def min(v1: IntSign, v2: IntSign): IntSign = ???
  override def absolute(v: IntSign): IntSign = ???

  override def divUnsigned(v1: IntSign, v2: IntSign): IntSign = ???
  override def remainder(v1: IntSign, v2: IntSign): IntSign = ???
  override def remainderUnsigned(v1: IntSign, v2: IntSign): IntSign = ???
  override def modulo(v1: IntSign, v2: IntSign): IntSign = ???
  override def gcd(v1: IntSign, v2: IntSign): IntSign = ???

  override def bitAnd(v1: IntSign, v2: IntSign): IntSign = ???
  override def bitOr(v1: IntSign, v2: IntSign): IntSign = ???
  override def bitXor(v1: IntSign, v2: IntSign): IntSign = ???
  override def shiftLeft(v: IntSign, shift: IntSign): IntSign = ???
  override def shiftRight(v: IntSign, shift: IntSign): IntSign = ???
  override def shiftRightUnsigned(v: IntSign, shift: IntSign): IntSign = ???
  override def rotateLeft(v: IntSign, shift: IntSign): IntSign = ???
  override def rotateRight(v: IntSign, shift: IntSign): IntSign = ???
  override def countLeadingZeros(v: IntSign): IntSign = ???
  override def countTrailingZeros(v: IntSign): IntSign = ???
  override def nonzeroBitCount(v: IntSign): IntSign = ???
  override def invertBits(v: IntSign): IntSign = ???


given OverflowIntSingGradualization[B]: OverflowGradualization[B, IntSign] with {
  type Safe = SafeSignIntegerOps[B]
  type Unsafe = OverflowOptimisticIntSignIntegerOps[B]
}
