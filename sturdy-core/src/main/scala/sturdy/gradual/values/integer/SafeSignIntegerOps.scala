package sturdy.gradual.values.integer

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.integer.{IntSign, IntegerDivisionByZero, IntegerOps}

given SafeSignIntegerOps[B](using f: Failure, j: EffectStack, base: Integral[B]): IntegerOps[B, IntSign] with
  import IntSign.*
  
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
