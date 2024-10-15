package sturdy.language.tip.analysis

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.fix.Logger
import sturdy.language.tip.abstractions.TipGradualLogger
import sturdy.language.tip.{FixIn, FixOut, TipGradualOps}
import sturdy.values.integer.IntSign.{Neg, NegOrZero, Pos, TopSign, Zero, ZeroOrPos}
import sturdy.values.integer.{IntSign, IntegerDivisionByZero, IntegerOps}
import sturdy.values.integer.SignIntegerOps
import sturdy.values.PartialOrder
import sturdy.values.integer.given PartialOrder[IntSign]

class UnsafeSignIntegerOps[B, V](using f: Failure, j: EffectStack, base: Integral[B], g: TipGradualOps[IntSign, V]) extends SafeSignIntegerOps[Int]:
  private def addUnsafe(v1: IntSign, v2: IntSign): IntSign =
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
  
  override def add(v1: IntSign, v2: IntSign): IntSign =
    g.withCheck(super.add(v1,v2)){
      addUnsafe(v1, v2)
    }

  override def sub(v1: IntSign, v2: IntSign): IntSign =
    val negV2 = v2 match
      case TopSign => TopSign
      case Neg => Pos
      case Zero => Zero
      case NegOrZero => ZeroOrPos
      case ZeroOrPos => NegOrZero
      case Pos => Neg
    g.withCheck(super.sub(v1, v2)){
      addUnsafe(v1, negV2)
    }

  override def mul(v1: IntSign, v2: IntSign): IntSign =
    g.withCheck(super.mul(v1, v2)){
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
    }

  override def div(v1: IntSign, v2: IntSign): IntSign =
    v2 match
      case Zero => f.fail(IntegerDivisionByZero, s"$v1 / $v2")
      case ZeroOrPos => j.joinWithFailure(v1)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      case NegOrZero => j.joinWithFailure(v1.negated)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      case TopSign => j.joinWithFailure(TopSign)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      case _ => this.mul(v1, v2)


class SafeSignIntegerOps[B](using f: Failure, j: EffectStack, base: Integral[B]) extends IntegerOps[B, IntSign]:
  def integerLit(i: B): IntSign =
    if base.lt(i, base.zero) then Neg
    else if base.gt(i, base.zero) then Pos
    else Zero

  def randomInteger(): IntSign = ZeroOrPos

  def add(v1: IntSign, v2: IntSign): IntSign = (v1, v2) match
    case (_, Zero) => v1
    case (Zero, _) => v2
    case (_, _) => TopSign

  override def sub(v1: IntSign, v2: IntSign): IntSign =
    val negV2 = v2 match
      case TopSign => TopSign
      case Neg => TopSign
      case Zero => Zero
      case NegOrZero => TopSign
      case ZeroOrPos => NegOrZero
      case Pos => Neg
    add(v1, negV2)

  def mul(v1: IntSign, v2: IntSign): IntSign = (v1, v2) match
    case (_, Zero) => Zero
    case (Zero, _) => Zero
    case (_, _) => TopSign

  def max(v1: IntSign, v2: IntSign): IntSign = ???
  def min(v1: IntSign, v2: IntSign): IntSign = ???

  def div(v1: IntSign, v2: IntSign): IntSign = v2 match
    case Zero => f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    case ZeroOrPos => j.joinWithFailure(v1)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case NegOrZero => j.joinWithFailure(v1.negated)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case TopSign => j.joinWithFailure(TopSign)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case _ => mul(v1, v2)

  def divUnsigned(v1: IntSign, v2: IntSign): IntSign = ???
  def remainder(v1: IntSign, v2: IntSign): IntSign = ???
  def remainderUnsigned(v1: IntSign, v2: IntSign): IntSign = ???
  def modulo(v1: IntSign, v2: IntSign): IntSign = ???
  def gcd(v1: IntSign, v2: IntSign): IntSign = ???

  def absolute(v: IntSign): IntSign = ???
  def bitAnd(v1: IntSign, v2: IntSign): IntSign = ???
  def bitOr(v1: IntSign, v2: IntSign): IntSign = ???
  def bitXor(v1: IntSign, v2: IntSign): IntSign = ???
  def shiftLeft(v: IntSign, shift: IntSign): IntSign = ???
  def shiftRight(v: IntSign, shift: IntSign): IntSign = ???
  def shiftRightUnsigned(v: IntSign, shift: IntSign): IntSign = ???
  def rotateLeft(v: IntSign, shift: IntSign): IntSign = ???
  def rotateRight(v: IntSign, shift: IntSign): IntSign = ???
  def countLeadingZeros(v: IntSign): IntSign = ???
  def countTrailingZeros(v: IntSign): IntSign = ???
  def nonzeroBitCount(v: IntSign): IntSign = ???
  def invertBits(v: IntSign): IntSign = ???



