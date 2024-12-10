package sturdy.gradual.values.integer

import sturdy.data.{JOptionC, NoJoin}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.gradual.GradualOps
import sturdy.values.{PartialOrder, Top}
import sturdy.values.integer.NumericInterval.constant
import sturdy.values.integer.{IntegerOps, NumericInterval, NumericIntervalIntegerOps, StrictIntegerOps}

import scala.math.Ordering.Implicits.infixOrderingOps

given OverflowOptimisticNumericIntervalOps[I]
(using strict: StrictIntegerOps[I, I, NoJoin], num: Numeric[I], t: Top[NumericInterval[I]], safeOps: NumericIntervalIntegerOps[I])
(using f: Failure, j: EffectStack): IntegerOps[I, NumericInterval[I]] with

  import NumericInterval.constant
  private def interval(low: I, high: I) = NumericInterval.safe(low, high)
  private val top = t.top
  private val zero = num.fromInt(0)
  private val one = num.fromInt(1)

  override def integerLit(i: I): NumericInterval[I] = constant(i)

  override def randomInteger(): NumericInterval[I] = top

  override def add(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    val lowResult = strict.addStrict(v1.low, v2.low)
    val highResult = strict.addStrict(v1.high, v2.high)
    (lowResult, highResult) match {
      case (JOptionC.Some(low), JOptionC.Some(high)) => interval(low, high)
      case (JOptionC.None(), JOptionC.Some(high)) => interval(top.low, high)
      case (JOptionC.Some(low), JOptionC.None()) => interval(low, top.high)
      case (JOptionC.None(), JOptionC.None()) =>
        val low =
          if v1.low < zero && v2.low < zero then top.low
          else if v1.high > zero && v2.high > zero then top.high
          else sys.error("A sum should only overflow if both operands are the same sign...")
        val high =
          if v1.high < zero && v2.high < zero then top.low
          else if v1.high > zero && v2.high > zero then top.high
          else sys.error("A sum should only overflow if both operands are the same sign...")
        interval(low, high)
      case _ => sys.error("I thought StrictIntegerOps would only return Some and None...")
    }

  override def sub(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    val lowResult = strict.subStrict(v1.low, v2.high)
    val highResult = strict.subStrict(v1.high, v2.low)
    (lowResult, highResult) match {
      case (JOptionC.Some(low), JOptionC.Some(high)) => interval(low, high)
      case (JOptionC.None(), JOptionC.Some(high)) => interval(top.low, high)
      case (JOptionC.Some(low), JOptionC.None()) => interval(low, top.high)
      case (JOptionC.None(), JOptionC.None()) =>
        val low =
          if v1.low < zero && v2.high > zero then top.low
          else if v1.low > zero && v2.high < zero then top.high
          else sys.error("A subtraction should only overflow if both operands have different sign...")
        val high =
          if v1.high < zero && v2.low > zero then top.low
          else if v1.high > zero && v2.low < zero then top.high
          else sys.error("A subtraction should only overflow if both operands have different sign...")
        interval(low, high)
      case _ => sys.error("I thought StrictIntegerOps would only return Some and None...")
    }

  override def mul(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    def limitOf(n1: I, n2: I): I =
      strict.mulStrict(n1, n2) match {
        case JOptionC.Some(v) => v
        case JOptionC.None() =>
          if (n1 > zero && n2 > zero) || (n1 < zero && n2 < zero) then top.high
          else if (n1 > zero && n2 < zero) || (n1 < zero && n2 > zero) then top.low
          else sys.error("A multiplication should only overflow if both operands are not zero...")
        case _ => sys.error("I thought StrictIntegerOps would only return Some and None...")
      }

    val limits = List(
      limitOf(v1.high, v2.high),
      limitOf(v1.high, v2.low),
      limitOf(v1.low, v2.high),
      limitOf(v1.low, v2.low)
    )

    NumericInterval.safe(limits.min, limits.max)

  override def div(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    safeOps.div(v1, v2)

  override def max(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def min(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def absolute(v: NumericInterval[I]): NumericInterval[I] = ???

  override def divUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def remainder(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def remainderUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def modulo(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def gcd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def bitAnd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def bitOr(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def bitXor(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  override def shiftLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???

  override def shiftRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???

  override def shiftRightUnsigned(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???

  override def rotateLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???

  override def rotateRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???

  override def countLeadingZeros(v: NumericInterval[I]): NumericInterval[I] = ???

  override def countTrailingZeros(v: NumericInterval[I]): NumericInterval[I] = ???

  override def nonzeroBitCount(v: NumericInterval[I]): NumericInterval[I] = ???

  override def invertBits(v: NumericInterval[I]): NumericInterval[I] = ???

given OverflowNumericIntervalGradualization[I]: OverflowGradualization[I, NumericInterval[I]] with {
  type Safe = NumericIntervalIntegerOps[I]
  type Unsafe = OverflowOptimisticNumericIntervalOps[I]
}
