package sturdy.values.integer

import sturdy.data.{JOptionA, JOptionC, JOptionPowerset, NoJoin, SomeJOption, joinComputations, joinWithFailure, noJoin, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.convert.*
import sturdy.values.relational.*

import java.nio.{ByteBuffer, ByteOrder}
import scala.collection.immutable.{AbstractSeq, LinearSeq, TreeSet}
import Ordering.Implicits.infixOrderingOps
import Numeric.Implicits.infixNumericOps
import Integral.Implicits.infixIntegralOps
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.Breaks.{break, breakable}


object NumericInterval:
  def constant[I](i: I): NumericInterval[I] = NumericInterval(i, i)

case class NumericInterval[I](low: I, high: I)://, overflow: Topped[Boolean])
  import NumericInterval.*

  def containsNum(n: I)(using Ordering[I]): Boolean = low <= n && n <= high

  def toBoolean(using ord: Ordering[I], num: Numeric[I]): Topped[Boolean] =
    val zero = num.fromInt(0)
    if (low == zero && low == high)
      Topped.Actual(false)
    else if (containsNum(zero))
      Topped.Top
    else
      Topped.Actual(true)

  def countOfNumsInInterval(using Numeric[I]): BigInt = BigInt(high.toLong) - BigInt(low.toLong) + 1

  def toUnsigned(using num: Numeric[I], ordUnsigned: UnsignedOrderingOps[I, Boolean]): NumericInterval[I] = {
    if (ordUnsigned.gtUnsigned(low, high)) {
      NumericInterval(num.zero, low)
    } else {
      this
    }
  }

  def getDecomposition(using ord: Ordering[I], num: Numeric[I]): Decomposition[I] =
    val zero = num.fromInt(0)
    val one = num.fromInt(1)
    Decomposition(
      lessZero = if (low <= -one) Some(NumericInterval(low, high.min(-one))) else None,
      leqZero = if (low <= zero) Some(NumericInterval(low, high.min(zero))) else None,
      hasZero = containsNum(zero),
      geqZero = if (high >= zero) Some(NumericInterval(low.max(zero), high)) else None,
      greaterZero = if (high >= one) Some(NumericInterval(low.max(one), high)) else None
    )

  def map[J](f: I => J): NumericInterval[J] =
      NumericInterval(f(low), f(high))

  def isConstant: Boolean = low == high

  def isTop(using Top[NumericInterval[I]]): Boolean =
    this == summon[Top[NumericInterval[I]]].top

  def combineCross[J, K](other: NumericInterval[J], f: (I, J) => K)(using Ordering[K]): NumericInterval[K] =
    val x1y1 = f(low, other.low)
    val x1y2 = f(low, other.high)
    val x2y1 = f(high, other.low)
    val x2y2 = f(high, other.high)
    NumericInterval(x1y1.min(x1y2).min(x2y1).min(x2y2), x1y1.max(x1y2).max(x2y1).max(x2y2))

case class Decomposition[I](lessZero: Option[NumericInterval[I]], leqZero: Option[NumericInterval[I]],
                            hasZero: Boolean,
                            geqZero: Option[NumericInterval[I]], greaterZero: Option[NumericInterval[I]])



given StandardIntervalIntegerOps[I](using Ordering[I], IntegerOps[I, I], StrictIntegerOps[I, I, NoJoin], Numeric[I], Top[NumericInterval[I]])
  (using Failure, EffectStack): IntervalIntegerOps[I] =
  new IntervalIntegerOps(20)


class IntervalIntegerOps[I]
  (val feasibleNumberOfOps: Int)
  (using ordering: Ordering[I], ops: IntegerOps[I, I], strict: StrictIntegerOps[I, I, NoJoin], num: Numeric[I], t: Top[NumericInterval[I]])(using f: Failure, j: EffectStack) extends IntegerOps[I, NumericInterval[I]]:
  import NumericInterval.*

  private val zero = num.fromInt(0)
  private val one = num.fromInt(1)

  private val (numWithHighestBitSetToOne, signBit, minValue, maxValue, numBits) = {
    var i = one
    var highestValue = one
    var bitCounter = zero
    while (i > zero) {
      i = ops.shiftLeft(i, one)
      highestValue = ops.bitOr(highestValue, i)
      bitCounter += one
    }
    // i is now 0b10000000... = minValue (1 is sign bit)
    (ops.shiftRightUnsigned(i, one), i, i, ops.bitXor(highestValue, i), bitCounter + one)
  }

  val top = NumericInterval(minValue, maxValue)

  def toBigInt(v: NumericInterval[I]): Topped[BigInt] = {
    if (v.low == v.high)
      Topped.Actual(BigInt(v.low.toLong))
    else
      Topped.Top
  }

  def deleteNumFromInterval(i: NumericInterval[I], num: I): List[NumericInterval[I]] = {
    if (num > i.high || num < i.low) {
      return List(i)
    }
    var result = List[NumericInterval[I]]()
    if (num == minValue && i.low == minValue && i.high != minValue)
      result = NumericInterval(minValue + one, i.high) :: result
    else if (num != minValue && num - one >= i.low)   // for num = minValue there would be an overflow in this condition
      result = NumericInterval(i.low, num - one) :: result
    if (num == maxValue && i.high == maxValue && i.low != maxValue)
      result = NumericInterval(i.low, maxValue - one) :: result
    else if (num != maxValue && num + one <= i.high) // for num = maxValue there would be an overflow in this condition
      result = NumericInterval(num + one, i.high) :: result
    result
  }

  def integerLit(i: I): NumericInterval[I] = NumericInterval(i, i)
  def randomInteger(): NumericInterval[I] = top

  def intervalToRange(x: NumericInterval[I]): ArrayBuffer[I] = {
    var i = x.low
    val result = ArrayBuffer[I]()
    while (true) {
      result.append(i)
      if (i == x.high) {
        return result
      }
      i += one
    }

    throw IllegalStateException()
  }

  def toUnsigned(i: I): BigInt = {
    val signBitSetToOneAsUnsigned = BigInt(maxValue.toLong) + 1
    if (i == minValue) {
      return signBitSetToOneAsUnsigned
    }

    if (i < zero) {
      signBitSetToOneAsUnsigned * 2 - i.toLong
    } else {
      i.toLong
    }
  }

  def joinMultipleIntervals(intervals: Iterable[NumericInterval[I]]): NumericInterval[I] =
    assert(intervals.nonEmpty)
    intervals.tail.foldLeft(intervals.head) {
      (joinedInterval, currentInterval) => NumericIntervalJoin(joinedInterval, currentInterval).get
    }

  def computeOpBruteForceIfFeasibleElseTake(unOp: I => I, v: NumericInterval[I], soundResult: NumericInterval[I]): NumericInterval[I] = {
    val count = v.countOfNumsInInterval
    if (count > feasibleNumberOfOps)
      return soundResult

    var lb = maxValue
    var ub = minValue

    val worstLb = soundResult.low
    val worstUb = soundResult.high

    var x = v.low
    while (true) {    // quasi x <= v.high, but this does not detect the overflow if v.high == maxValue
      val result = unOp(x)
      lb = lb.min(result)
      ub = ub.max(result)
      if (lb <= worstLb && ub >= worstUb) {
        assert(lb == worstLb && ub == worstUb)
        return soundResult
      }
      if (x == v.high) {
        assert(NumericIntervalOrdering.lteq(NumericInterval(lb, ub), soundResult))
        return NumericInterval(lb, ub)
      }

      x = ops.add(x, one)
    }
    throw IllegalStateException()
  }

  def computeOpBruteForceIfFeasibleElseTake(op: (I, I) => I,
                                            v1: NumericInterval[I],
                                            v2: NumericInterval[I],
                                            soundResult: NumericInterval[I]
                                           ): NumericInterval[I] = {

    if (v1.countOfNumsInInterval * v2.countOfNumsInInterval > feasibleNumberOfOps)
      return soundResult

    var lb = maxValue
    var ub = minValue
    val worstLb = soundResult.low
    val worstUb = soundResult.high
    var x = v1.low
    while (true) {       // quasi "while(x <= v1.high)", but this does not detect the overflow if v1.high == maxValue
      var y = v2.low
      breakable{while (true) {      // quasi "while(y <= v2.high)", but this does not detect the overflow if v2.high == maxValue
        val result = op(x, y)
        lb = lb.min(result)
        ub = ub.max(result)
        if (lb <= worstLb && ub >= worstUb) {
          assert(lb == worstLb && ub == worstUb)
          return soundResult
        }

        if (x == v1.high && y == v2.high) {
          assert(NumericIntervalOrdering.lteq(NumericInterval(lb, ub), soundResult))
          return NumericInterval(lb, ub)
        }
        if (y == v2.high) break
        y = ops.add(y, one)
      }}
      x = ops.add(x, one)
    }

    throw IllegalStateException()
  }

  def computeOpBruteForceIfFeasibleElseTakeWithOneOperandFixed(op: I => NumericInterval[I], v: NumericInterval[I],
                                                               soundResult: NumericInterval[I]
                                                              ): NumericInterval[I] = {
    val count = v.countOfNumsInInterval
    if (count > feasibleNumberOfOps)
      return soundResult

    var lb = maxValue
    var ub = minValue

    val worstLb = soundResult.low
    val worstUb = soundResult.high

    var x = v.low
    while (true) {    // quasi x <= v.high, but this does not detect the overflow if v.high == maxValue
      val result = op(x)
      lb = lb.min(result.low)
      ub = ub.max(result.high)
      if (lb <= worstLb && ub >= worstUb) {
        assert(lb == worstLb && ub == worstUb)
        return soundResult
      }
      if (x == v.high) {
        assert(NumericIntervalOrdering.lteq(NumericInterval(lb, ub), soundResult))
        return NumericInterval(lb, ub)
      }
      x = ops.add(x, one)
    }
    throw IllegalStateException()
  }

  def add(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    /*
    // see Hacker's delight by Henry S. Warren, Jr., Chapter 4.2 (https://doc.lagout.org/security/Hackers%20Delight.pdf)

    Fig. 4.2:
    s = a + c;
    t = v + d;
    u = a & c & ~s & ~(v & d &~t);
    v = ((a ^ c) | ~(a ^ s)) & (~v & ~d & t);
    if ((u | v) < 0) {
      s = 0x80000000;
      t = 0x7FFFFFFF;}
    */

    inline def not(i: I): I = ops.invertBits(i)
    inline def and3(i1: I, i2: I, i3: I): I = ops.bitAnd(ops.bitAnd(i1, i2), i3)
    inline def and4(i1: I, i2: I, i3: I, i4: I): I = ops.bitAnd(ops.bitAnd(ops.bitAnd(i1, i2), i3), i4)
    inline def or(i1: I, i2: I): I = ops.bitOr(i1, i2)
    inline def xor(i1: I, i2: I): I = ops.bitXor(i1, i2)

    val NumericInterval(a, b) = v1
    val NumericInterval(c, d) = v2

    var s = ops.add(a, c)
    var t = ops.add(b, d)

    val u = and4(
      a,
      c,
      not(s),
      not(and3(b, d, not(t)))
    )
    val v = ops.bitAnd(
      or(xor(a, c), not(xor(a, s))),
      and3(not(b), not(d), t)
    )

    if (or(u, v) < zero) {
      s = minValue
      t = maxValue
    }

    NumericInterval(s, t)
  }

  def sub(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    // We want to calculate v1 + (-1) * v2, but (-1) * minValue = minValue, so we have to manage this case by hand.
    if (v2.low == minValue && v2.high == minValue) {
      val lowResult = strict.subStrict(v1.low, minValue)
      val highResult = strict.subStrict(v1.high, minValue)
      (lowResult, highResult) match {
        case (JOptionC.None(), h) =>
          // every result will overflow in the positive direction
          assert(h == JOptionC.None())
          NumericInterval(ops.sub(v1.low, minValue), ops.sub(v1.high, minValue))
        case (JOptionC.Some(_), JOptionC.None()) =>
          // only v1.high - minValue overflows in the positive direction
          top
        case (JOptionC.Some(l), JOptionC.Some(h)) =>
          // no result will overflow
          NumericInterval(l, h)
        case _ =>
          sys.error("I thought StrictIntegerOps would only return Some and None...")
      }
    } else if (v2.low == minValue) {
      joinComputations(
        sub(v1, NumericInterval(minValue, minValue))
      )(
        sub(v1, NumericInterval(minValue + one, v2.high))
      )
    } else {
      add(v1, mul(v2, NumericInterval(-one, -one)))
    }
  }

  def mul(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    val mulStrict = (i1: I, i2: I) => strict.mulStrict(i1, i2)
      .getOrElse(return top) // imprecise! For I = Int: [Int.MaxValue, Int.MaxValue] * [2, 3] = [-2, Int.MaxValue - 2] != Top

    // the following use of combineCross can be seen in Table 6 of "Complete Interval Arithmetic and its Implementation on the Computer" by Ulrich W. Kulisch
    // https://www.math.kit.edu/ianm2/~kulisch/media/arjpkx.pdf


    val resultImpreciseWhenOverflowing = v1.combineCross(v2, mulStrict)   // see Table 6 in https://www.math.kit.edu/ianm2/~kulisch/media/arjpkx.pdf
    if (resultImpreciseWhenOverflowing.isTop)
      computeOpBruteForceIfFeasibleElseTake(ops.mul, v1, v2, top)
    else
      resultImpreciseWhenOverflowing
  }

  def max(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    NumericInterval(ops.max(v1.low, v2.low), ops.max(v1.high, v2.high))

  def min(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    NumericInterval(ops.min(v1.low, v2.low), ops.min(v1.high, v2.high))


  def absolute(v: NumericInterval[I]): NumericInterval[I] =
    if (v.low == minValue) { // abs(minValue) = minValue * (-1) = minValue in Scala, C++, Java
      return NumericInterval(minValue, v.high.abs)
    }
    if (v.low < zero) {
      if (v.high < zero) {
        // neg, neg
        NumericInterval(v.high.abs, v.low.abs)
      } else {
        // neg, pos
        NumericInterval(zero, v.low.abs.max(v.high))
      }
    } else {
      // pos, pos
      v
    }

  private inline def divByZero(v1: NumericInterval[I], v2: NumericInterval[I]) = f.fail(IntegerDivisionByZero, s"$v1 / $v2")

  private def divWith(f: (I, I) => I, v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    def divWithDivisorNotContainingZero(b1: NumericInterval[I], b2: NumericInterval[I]): NumericInterval[I] = {
      // the use of combineCross in divWithoutOverflowsAndDivisorNeqZero is legitimated by Table 7 of "Complete Interval Arithmetic and its Implementation on the Computer" by Ulrich W. Kulisch
      // https://www.math.kit.edu/ianm2/~kulisch/media/arjpkx.pdf

      // minValue / (-1) = minValue, therefore combineCross does not work if (-1 is an interval limit of v2 && minValue is an interval limit of v1)
      // In this case we join the results of combineCross( { v1\{minValue}, minValue } x { v2\{-1}, -1} )
      if (b1.low == minValue && b2.high == -one) {
        val newB1 = NumericInterval(minValue, minValue) :: deleteNumFromInterval(b1, minValue)
        val newB2 = NumericInterval(-one, -one) :: deleteNumFromInterval(b2, -one)
        joinMultipleIntervals(
          newB1.flatMap(x => newB2.map(y => x.combineCross(y, f)))
        )
      } else {
        b1.combineCross(b2, f)
      }
    }

    if (v2.low == zero && v2.high == zero)
      return divByZero(v1, v2)

    if (!v2.containsNum(zero)) {
      divWithDivisorNotContainingZero(v1, v2)
    } else{
      val v2sWithoutZero = deleteNumFromInterval(v2, zero)

      joinWithFailure(
        joinMultipleIntervals(
          v2sWithoutZero.map(currentV2 =>
            divWithDivisorNotContainingZero(v1, currentV2)
          )
        )
      )(divByZero(v1, v2))
    }

  def div(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.div, v1, v2)

  def divUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    if (v2.low == zero && v2.high == zero) {
      return divByZero(v1, v2)
    }

    val possibleResults = ArrayBuffer[NumericInterval[I]]()   // elements will be joined in the end
    if (v2.containsNum(one)) {
      // v1 / 1 = v1
      possibleResults.append(v1)
    }

    val decomp1 = v1.getDecomposition
    val decomp2 = v2.getDecomposition
    // let v := numBits
    if (decomp2.lessZero.nonEmpty) {
      // let y < 0 as signed (=> y >= 2^(v-1) as unsigned)
      // x / y = 1 if x >= y as unsigned (<=> y <= x < 0 as signed)
      //       = 0 if x < y as unsigned (<=> x >= 0 || 0 > x < y as signed)
      val y_lt0 = decomp2.lessZero.get
      val x_lt0 = decomp1.lessZero
      if (x_lt0.nonEmpty && x_lt0.get.high >= y_lt0.low) {
        possibleResults.append(NumericInterval(one, one))
      }
      val x_geq0 = decomp1.geqZero
      if (x_geq0.nonEmpty || (x_lt0.nonEmpty && x_lt0.get.low < y_lt0.high)) {
        possibleResults.append(NumericInterval(zero, zero))
      }
    }
    val y_geq2Option = decomp2.greaterZero.flatMap(gt0 =>   // get interval of all y \in v2 with y >= 2
      deleteNumFromInterval(gt0, one) match
        case Nil => None
        case List(geq2) =>
          Some(geq2)
        case _ => throw IllegalStateException(s"$gt0 - {1} with $gt0 >= 1 should result in one interval")
    )

    if (y_geq2Option.nonEmpty) {
      // let y >= 2 as signed
      // then 0 <= x / y < 2^(v-1)

      val y_geq2 = y_geq2Option.get
      if (decomp1.hasZero) {
        // 0 / y = 0
        possibleResults.append(NumericInterval(zero, zero))
      }
      if (decomp1.lessZero.nonEmpty) {
        // let x < 0 as signed (<=> x >= 2^(v-1) as unsigned)

        // x1 <= x2 < 0 as signed <=> 2^(v-1) <= x1 <= x2 as unsigned.
        // Thus _/_ is monotonically increasing in its first argument
        // and monotonically decreasing in its second argument for the current domain.
        // Hence, the result is the following:
        val x_lt0 = decomp1.lessZero.get
        possibleResults.append(NumericInterval(
          ops.divUnsigned(x_lt0.low, y_geq2.high),
          ops.divUnsigned(x_lt0.high, y_geq2.low),
        ))
      }
      if (decomp1.greaterZero.nonEmpty) {
        // let x > 0 as signed (<=> 0 < x < 2^(v-1) as unsigned)

        // 0 < x1 <= x2 as signed <=> 0 < x1 <= x2 < 2^(v-1) as unsigned.
        // Thus _/_ is monotonically increasing in its first argument
        // and monotonically decreasing in its second argument for the current domain.
        // Hence, the result is the following:
        val x_gt0 = decomp1.greaterZero.get
        possibleResults.append(NumericInterval(
          ops.divUnsigned(x_gt0.low, y_geq2.high),
          ops.divUnsigned(x_gt0.high, y_geq2.low),
        ))
      }
    }

    assert(possibleResults.nonEmpty, s"$v1 $v2       $y_geq2Option")
    val result = joinMultipleIntervals(possibleResults)
    if (v2.containsNum(zero)) {
      joinWithFailure(result)(divByZero(v1, v2))
    } else {
      result
    }
  }

  def modulo(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    // a mod n has result in [0, |n| - 1]. Thus most imprecise result is NumericInterval(zero, maxValue)

    // if [a1, a2] \subseteq [0, maxValue]: result = a rem n
    // else if [-1, 0] \in [a1, a2]: result = [0, |n| - 1]   // mind n1 = minValue when calculating |n|. Also maybe divByZero if 0 \in n
    // else
    //    => [a1, a2] \subseteq [minValue, -2]
    //    let [r1, r2] := [a1, a2] rem [n1, n2]
    //    sound bc:
    //    result = [0, min(r2 + |n|, |n| - 1)]   // imprecise but sound!
    //      for v < 0: v mod m = (v rem m) + m <= r2 + |n|
    //       otherwise 0 mod m = 0
    //    "<=" not tight in [-3, -3] mod [2, 5]:
    //    [-3, -3] rem [2, 5] = [-3, 0]
    //    [-3, -3] mod [2, 5] = [0, 2] = [-3 mod 3, -3 mod 5]
    //    min(r2 + |n|, |n| - 1) = 4 in this example

    val NumericInterval(a1, a2) = v1
    val NumericInterval(n1, n2) = v2

    if (a1 >= zero)
      return remainder(v1, v2)

    val nAbsMinusOne = if (n1 == minValue) maxValue else n1.abs.max(n2.abs)
    val nContainsZero = v2.containsNum(zero)
    if (a1 <= -one && a2 >= zero) {
      val result = NumericInterval(zero, nAbsMinusOne)
      return
        if (nContainsZero)
          joinWithFailure(result)(divByZero(v1, v2))
        else
          result
    }
    val remRes = remainder(v1, v2)

    assert(remRes.high <= zero && remRes.low > minValue)
    val high = if (remRes == zero) nAbsMinusOne else (remRes.high + nAbsMinusOne) + one    // nAbsMinusOne + one might be maxValue + 1
    val result = computeOpBruteForceIfFeasibleElseTake(ops.modulo, v1, v2, NumericInterval(zero, high))
    if (nContainsZero)
      joinWithFailure(result)(divByZero(v1, v2))
    else
      result

  def remainderUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.remainderUnsigned, v1, v2)

  private def negativeAbsolute(v: NumericInterval[I]): NumericInterval[I] = {
    if (v.high >= zero) {
      if (v.low >= zero) {
        // > 0, > 0
        NumericInterval(ops.mul(v.high, -one), ops.mul(v.low, -one))
      } else {
        // < 0, >= 0
        NumericInterval(v.low.min(ops.mul(v.high, -one)), zero)
      }
    } else {
      // <= 0, <= 0
      v
    }
  }

  def remainderWithConstDivisor(dividend: NumericInterval[I], divisor: I): NumericInterval[I] = {
    assert(dividend.high <= zero && dividend.low <= dividend.high)
    val negativeDivisor = if (divisor < zero) divisor else (-one) * divisor
    val positiveDivisor = ops.toBigInt(divisor).head * (-1)
    if (dividend.countOfNumsInInterval >= positiveDivisor) {
      NumericInterval(negativeDivisor + one, zero)
    } else {
      val remOfLow = ops.remainder(dividend.low, divisor)
      val remOfHigh = ops.remainder(dividend.high, divisor)
      if (remOfLow <= remOfHigh) {
        NumericInterval(remOfLow, remOfHigh)
      } else {
        NumericInterval(negativeDivisor + one, zero)
      }
    }
  }

  def remainder(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    // computes a % n with the following properties:
    // i) a % n >= 0 if a >= 0; a % n <= 0 if a < 0.
    // ii) a % n = a % (-n). This also holds for minValue, although -minValue is not an element of I (actually -minValue = minValue = minValue.abs)
    // iii) |a % n| <= a
    // iv) |a % n| <= n-1


    // We reduce the calculation to a % n with a \subseteq [minValue, 0] and n \subseteq [minValue, -1].
    // Of course it would be nicer to reduce it to positive values, but the problem is that our representation of I
    // assumes that Top = [minValue, maxValue] = [-maxValue - 1, maxValue]
    // Thus abs(minValue) = minValue and there is no positive representation for this number.

    def remainderNegativeDividendAndDivisor(dividend: NumericInterval[I], divisor: NumericInterval[I]): NumericInterval[I] = {
      inline def isEven(n: I): Boolean = ops.modulo(n, num.fromInt(2)) == zero

      val NumericInterval(b1, b2) = dividend
      val NumericInterval(m1, m2) = divisor
      assert(b2 <= zero && b1 <= b2)
      assert(m2 < zero && m1 <= m2)

      // The proofs in the comments are for [a1, a2] % [n1, n2] with [a1, a2] = [-v2, -v1] = |[v1, v2]| and [n1, n2] = [-m2, -m1] = |[m1, m2]|
      // so that we only have to deal with non negative numbers in the proofs.

      // [a1, a2] % [n1, n2] \subseteq [0, maxValue] bc of asserts

      // if divisorContainedMinValue, then the first three cases handle situation correctly
      if (m2 < b1)
      // a2 < n1
      // a % n = a for all a \in [a1, a2], n \in [n1, n2]
      // => result = NumericInterval(a1, a2)
        NumericInterval(b1, b2)
      else if (m1 < b1) // && m2 >= v1
      // a2 < n2 && n1 <= a2
      // result = [0, a2] = [a2 % a2, a2 % n2]. result cant be bigger than a2 bc of iii)
        NumericInterval(b1, zero)
      else if (b1 == b2 && b1 == m1 && isEven(m1))
      // a1 == a2 && a2 == m2 && isEven(n2)
      // result = [0, min(n2-n1, n2/2 - 1)]
      // n2 % (n2 - i) = i for i \in {0, 1, ..., n2/2 - 1},
      // bc n2 = 1*(n2 - i) + i and n2 - i >= n2/2 + 1 > i.
      // n2 % (n2 - i) <= n2/2 - 1 for i >= n2/2, bc. n2 - i <= n2/2 and iv)
        NumericInterval(ops.max(m1 - m2, ops.div(m1, num.fromInt(2)) + one), zero)
      else if (b1 == b2 && b1 == m1) // && isOdd(m1)
      // a1 == a2 && a2 == n2 && isOdd(n2)
      // result = [0, min(n2-n1, floor(n2/2))]
      // n2 % (n2 - i) = i for i \in {0, 1, ..., floor(n2/2)},
      // bc n2 = 1*(n2 - i) + i and n2 - i >= ceil(n2/2) > i.
      // n2 % (n2 - i) <= floor(n2/2) for i > ceil(n2/2), bc. n2 - i <= floor(n2/2) and iv)
        NumericInterval(ops.max(m1 - m2, ops.div(m1, num.fromInt(2))), zero)
      else
      // returned result is [0, n2-1] in this case. In the following cases this is precise:
      // if (a2 - a1 + 1 >= n2) (#[a1, a2] >= n2 - 1  => All values are of Z/(n2)Z are taken)
      // if (a2 == n2 && a1 != a2)
      //    result = [0, a2-1] = [a2 % n2, (a2-1) % n2]. result cant be bigger than a2-1 = n2-1 bc of iv)
      // if (a1 < n2 < a2)
      //    result = [0, n2 - 1] = [n2 % n2, (n2-1) % n2]. result cant be bigger than n2-1 bc of iv)
      // The only imprecise case is n2 <= a1 && a2-a1+1 < n2
        if (m1 >= b2 && ops.mul(b2 - b1 + one, -one) > m1)
          computeOpBruteForceIfFeasibleElseTakeWithOneOperandFixed(remainderWithConstDivisor(dividend, _), divisor, NumericInterval(m1 + one, zero))
        else
          NumericInterval(m1 + one, zero)
    }

    def remainderPositiveDividendAndNegativeDivisor(dividend: NumericInterval[I], divisor: NumericInterval[I]): NumericInterval[I] = {
      assert(dividend.low >= one && dividend.low <= dividend.high)
      assert(divisor.high <= -one && divisor.low <= divisor.high)

      val negativeDividend: NumericInterval[I] = NumericInterval(ops.mul(dividend.high, -one), ops.mul(dividend.low, -one))
      // using ii):
      mul(NumericInterval.constant(num.fromInt(-1)), remainderNegativeDividendAndDivisor(negativeDividend, divisor))
    }

    def remainderNegativeDivisor(dividend: NumericInterval[I], divisor: NumericInterval[I]): NumericInterval[I] = {
      assert(divisor.high < zero && divisor.low <= divisor.high)

      val decomp = dividend.getDecomposition
      val possibleResults = ArrayBuffer[NumericInterval[I]]()

      if (decomp.leqZero.nonEmpty)
        possibleResults.append(remainderNegativeDividendAndDivisor(decomp.leqZero.get, divisor))

      if (decomp.greaterZero.nonEmpty)
        possibleResults.append(remainderPositiveDividendAndNegativeDivisor(decomp.greaterZero.get, divisor))

      joinMultipleIntervals(possibleResults)
    }
    val dividend = v1
    val divisorAbs = negativeAbsolute(v2) // see ii)

    val decompDivisor = divisorAbs.getDecomposition
    (decompDivisor.lessZero, decompDivisor.hasZero) match {
      case (None, true) => divByZero(v1, v2)
      case (None, false) => throw IllegalStateException("negative abs of divisor cannot be > 0")
      case (Some(negativeDivisor), hasZero) =>
        val result = remainderNegativeDivisor(dividend, negativeDivisor)
        if (hasZero)
          joinWithFailure(result)(divByZero(v1, v2))
        else
          result
    }
  }

  def gcd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    computeOpBruteForceIfFeasibleElseTake(ops.gcd, v1, v2,
      top // the current implementation of ConcreteIntOps yields gcd(minValue, minValue) = minValue
    )
  }

  // see Hacker's delight by Henry S. Warren, Jr., Chapter 4.3 (https://doc.lagout.org/security/Hackers%20Delight.pdf)
  def bitAnd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    // use of DeMorgan-Rules as explained in Hacker's Delight
    invertBits(bitOr(NumericInterval(
      ops.invertBits(v1.high),
      ops.invertBits(v1.low)), NumericInterval(
      ops.invertBits(v2.high),
      ops.invertBits(v2.low)
    )))

  private def bitIsOne(num: I, numWithOneNonZeroBit: I): Boolean = ops.bitAnd(num, numWithOneNonZeroBit) != zero

  // see Hacker's delight by Henry S. Warren, Jr., Chapter 4.3 (https://doc.lagout.org/security/Hackers%20Delight.pdf)
  def bitOr(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    def minOr(a: I, b: I, c: I, d: I): I = {
      // see Fig. 4.3
      var m = numWithHighestBitSetToOne
      while (m != zero) {
        if (!bitIsOne(a, m) && bitIsOne(c, m)) {
          val temp = ops.bitAnd(ops.bitOr(a, m), -m)
          if (temp <= b)
            return ops.bitOr(temp, c)
        } else if (bitIsOne(a, m) && !bitIsOne(c, m)) {
          val temp = ops.bitAnd(ops.bitOr(c, m), -m)
          if (temp <= d)
            return ops.bitOr(a, temp)
        }
        m = ops.shiftRightUnsigned(m, one)
      }
      ops.bitOr(a, c)
    }

    def maxOr(a: I, b: I, c: I, d: I): I = {
      // see Fig. 4.4
      var m = numWithHighestBitSetToOne
      while (m != zero) {
        if (bitIsOne(b, m) && bitIsOne(d, m)) {
          var temp = ops.bitOr(b - m, m - one)
          if (temp >= a)
            return ops.bitOr(temp, d)

          temp = ops.bitOr(d - m, m - one)
          if (temp >= c)
            return ops.bitOr(b, temp)
        }
        m = ops.shiftRightUnsigned(m, one)
      }
      ops.bitOr(b, d)
    }

    val a = v1.low
    val b = v1.high
    val c = v2.low
    val d = v2.high

    // following match of indicatorForSignsOfABCD corresponds to Table 4.1
    var indicatorForSignsOfABCD = 0
    if (d >= zero) indicatorForSignsOfABCD |= 1
    if (c >= zero) indicatorForSignsOfABCD |= 2
    if (b >= zero) indicatorForSignsOfABCD |= 4
    if (a >= zero) indicatorForSignsOfABCD |= 8

    indicatorForSignsOfABCD match
      case 0 | 3 | 12 | 15 => NumericInterval(minOr(a, b, c, d), maxOr(a, b, c, d))
      case 1 => NumericInterval(a                , -one)
      case 4 => NumericInterval(c                , -one)
      case 5 => NumericInterval(ops.min(a, c), maxOr(zero, b, zero, d))
      case 7 => NumericInterval(minOr(a, -one, c, d), maxOr(zero, b, c, d))
      case 13 => NumericInterval(minOr(a, b, c, -one), maxOr(a, b, zero, d))

  def bitXor(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    // see Hacker's delight by Henry S. Warren, Jr., Chapter 4.3 (https://doc.lagout.org/security/Hackers%20Delight.pdf)

    // for minXorValueForPosInterval and maxXorValueForPosInterval see the text and code image right above the section "Signed Bounds".
    // minXorValueForPosInterval and maxXorValueForPosInterval only work for positive numbers as pointed out in the section "Signed Bounds".
    def minXorValueForPosInterval(b1: NumericInterval[I], b2: NumericInterval[I]): I = {
      var a = b1.low
      val b = b1.high
      var c = b2.low
      val d = b2.high
      var m = numWithHighestBitSetToOne
      while (m != zero) {
        if (!bitIsOne(a, m) && bitIsOne(c, m)) {
          val temp = ops.bitAnd(ops.bitOr(a, m), -m)
          if (temp <= b)
            a = temp
        } else if (bitIsOne(a, m) && !bitIsOne(c, m)) {
          val temp = ops.bitAnd(ops.bitOr(c, m), -m)
          if (temp <= d)
            c = temp
        }
        m = ops.shiftRightUnsigned(m, one)
      }
      ops.bitXor(a, c)
    }

    def maxXorValueForPosInterval(b1: NumericInterval[I], b2: NumericInterval[I]): I = {
      val a = b1.low
      var b = b1.high
      val c = b2.low
      var d = b2.high
      var m = numWithHighestBitSetToOne
      while (m != zero) {
        if (bitIsOne(b, m) && bitIsOne(d, m)) {
          var temp = ops.bitOr(b - m, m - one)
          if (temp >= a) {
            b = temp
          } else {
            temp = ops.bitOr(d - m, m - one)
            if (temp >= c) d = temp
          }
        }
        m = ops.shiftRightUnsigned(m, one)
      }
      ops.bitXor(b, d)
    }

    val decomp1 = v1.getDecomposition
    val decomp2 = v2.getDecomposition

    val geq1 = decomp1.geqZero
    val geq2 = decomp2.geqZero

    val flipSignBit: NumericInterval[I] => NumericInterval[I] = _.map(ops.bitXor(_, signBit))
    val lt1WithoutSign: Option[NumericInterval[I]] = decomp1.lessZero.map(i => flipSignBit(i))
    val lt2WithoutSign: Option[NumericInterval[I]] = decomp2.lessZero.map(i => flipSignBit(i))

    val results: ArrayBuffer[NumericInterval[I]] = ArrayBuffer()

    val xorForPosIntervals = (b1: NumericInterval[I], b2: NumericInterval[I]) => NumericInterval(minXorValueForPosInterval(b1, b2), maxXorValueForPosInterval(b1, b2))

    if (geq1.nonEmpty && geq2.nonEmpty)
      results.append(xorForPosIntervals(geq1.get, geq2.get))
    if (lt1WithoutSign.nonEmpty && lt2WithoutSign.nonEmpty)
      results.append(xorForPosIntervals(lt1WithoutSign.get, lt2WithoutSign.get))
    if (lt1WithoutSign.nonEmpty && geq2.nonEmpty)
      results.append(
        flipSignBit(
          xorForPosIntervals(lt1WithoutSign.get, geq2.get)
        )
      )
    if (geq1.nonEmpty && lt2WithoutSign.nonEmpty)
      results.append(
        flipSignBit(
          xorForPosIntervals(geq1.get, lt2WithoutSign.get)
        )
      )
    joinMultipleIntervals(results)
  }

  private inline def mapBounded[A](v: NumericInterval[I], mapFun: I => I): NumericInterval[I] = NumericInterval(mapFun(v.low), mapFun(v.high))

  def shiftLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = {
    // it holds shiftLeft(v, s) = shiftLeft(v, s') if s mod numBits == s' mod numBits

    def shiftLeftWithPreprocessedShift(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = {
      // shift is preprocessed like this:
      assert(shift.low > -numBits && shift.high < numBits && shift.countOfNumsInInterval <= numBits.toLong)
      val vCount = v.countOfNumsInInterval

      def resultForConstantShift(shift: I): NumericInterval[I] = {
        assert(shift >= zero && shift < numBits)
        if (shift == zero)
          return v

        val firstBitShiftedOut = ops.shiftLeft(one, numBits - shift)    // rightmost bit which will overflow by the shift
        val allBitsThatWontVanishSetToOne = firstBitShiftedOut - one
        val intervalsOfNotVanishedBits: List[NumericInterval[I]] = // all values that can appear when setting the bits to zero that overflow by the shift.
          if (vCount >= toUnsigned(firstBitShiftedOut)) {
            // all values are possible
            List(NumericInterval(zero, allBitsThatWontVanishSetToOne))
          } else {
            val lowNotVanishingBits = ops.bitAnd(v.low, allBitsThatWontVanishSetToOne)
            val highNotVanishingBits = ops.bitAnd(v.high, allBitsThatWontVanishSetToOne)
            if (lowNotVanishingBits <= highNotVanishingBits) {
              List(NumericInterval(lowNotVanishingBits, highNotVanishingBits))
            } else {
              // e.g. v = [0110, 1010] and firstBitShiftedOut = 1000. Then we must return
              // [110, 111] for the numbers < 1000 and [000, 010] for the numbers > 1000
              List(NumericInterval(lowNotVanishingBits, allBitsThatWontVanishSetToOne), NumericInterval(zero, highNotVanishingBits))
            }
          }

        val shiftByShift = ops.shiftLeft(_, shift)
        val bitThatWillBeSignBit = ops.shiftRightUnsigned(firstBitShiftedOut, one)
        // when we shift each interval [l, h] of intervalsOfNotVanishedBits, we get the correct result, if the numbers were unsigned.
        // So we (only) have to consider the case when the sign bit of shift(h) is 1 and the sign bit of shift(l) is 0.
        joinMultipleIntervals(
          intervalsOfNotVanishedBits.map { v =>
            if ((v.low >= bitThatWillBeSignBit) == (v.high >= bitThatWillBeSignBit)) {
              NumericInterval(shiftByShift(v.low), shiftByShift(v.high))
            } else {
              // now shift(v.low) >= 0 and shift(v.high) < 0.
              // Thus 10000...0 = minValue is a possible result and also
              // 01...10...0 is also in the interval where the number of ones is (#bits that don't vanish - 1). This is
              // the max value that can be achieved by a shiftLeft(_, shift). We compute it like this:
              // shift(allBitsThatWontVanishSetToOne) = 11...10...0, so we only have to remove the sign bit.
              NumericInterval(minValue, ops.bitAnd(maxValue, shiftByShift(allBitsThatWontVanishSetToOne)))
            }
          }
        )
      }

      joinMultipleIntervals(intervalToRange(shift).map { s =>
        val sAsPositive = if (s < zero) s + numBits else s
        val result = resultForConstantShift(sAsPositive)
        result
      })
    }

    val countOfOriginalShift = shift.countOfNumsInInterval

    val sh: NumericInterval[I] =    // sh \subseteq [-numBits + 1, numBits - 1]
      if (countOfOriginalShift < numBits.toLong) {
        val remOfLow = ops.modulo(shift.low, numBits)
        val remOfHigh = ops.modulo(shift.high, numBits)
        if (remOfLow <= remOfHigh)
          NumericInterval(remOfLow, remOfHigh)
        else
          NumericInterval(remOfLow - numBits, remOfHigh)
      } else {
        bitAnd(shift, NumericInterval(numBits - one, numBits - one))   // shift % numBits
      }
    shiftLeftWithPreprocessedShift(v, sh)
  }

  private inline def shiftRightOfPositiveNumberAndShiftBetween0AndNumBits(v: NumericInterval[I], shiftLtNumBits: NumericInterval[I]): NumericInterval[I] = {
    assert(v.low >= zero && shiftLtNumBits.low >= zero && shiftLtNumBits.high < numBits)
    // for v >= 0 and 0 <= s < numBits: v >>> s is monotonically increasing in v and monotonically decreasing in s
    NumericInterval(
      ops.shiftRight(v.low, shiftLtNumBits.high),
      ops.shiftRight(v.high, shiftLtNumBits.low)
    )
  }

  def shiftRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = {
    val decomp = v.getDecomposition
    val possibleResults = ArrayBuffer[NumericInterval[I]]() // elements will be joined in the end
    val sh = bitAnd(shift, NumericInterval(numBits - one, numBits - one)) // = shift % [numBits, numBits]
    assert(sh.low >= zero && sh.high < numBits)

    if (decomp.lessZero.nonEmpty) {
      val lt0 = decomp.lessZero.get
      // let x < 0, 0 <= s < numBits. Then shiftRight(x, s) is monotonically increasing in both arguments.
      possibleResults.append(NumericInterval(
        ops.shiftRight(lt0.low, sh.low),
        ops.shiftRight(lt0.high, sh.high)
      ))
    }

    if (decomp.geqZero.nonEmpty) {
      // let x < 0, 0 <= s < numBits. Then shiftRight(x, s) is monotonically increasing in x and monot. descreasing in s.
      possibleResults.append(shiftRightOfPositiveNumberAndShiftBetween0AndNumBits(decomp.geqZero.get, sh))
    }

    joinMultipleIntervals(possibleResults)
  }

  def shiftRightUnsigned(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = {
    inline def modNumBits(interval: NumericInterval[I]) = bitAnd(interval, NumericInterval(numBits - one, numBits - one))
    def getShiftMinusOne(shiftBetween0AndNumBitsMinusOne: NumericInterval[I]): NumericInterval[I] = {
      val countOfInterval = shift.countOfNumsInInterval
      if (countOfInterval >= 2 && countOfInterval < numBits.toLong && ops.bitAnd(shift.high, numBits - one) == zero) {
        val shiftWithoutMod0 = modNumBits(NumericInterval(shift.low, shift.high - one))
        assert(shiftWithoutMod0.low > zero)
        NumericInterval(
          shiftWithoutMod0.low - one,
          shiftWithoutMod0.high - one
        )
      } else {
        NumericInterval(ops.max(zero, shiftBetween0AndNumBitsMinusOne.low - one), shiftBetween0AndNumBitsMinusOne.high - one)
      }
    }

    val decomp = v.getDecomposition
    val possibleResults = ArrayBuffer[NumericInterval[I]]() // elements will be joined in the end
    val sh = modNumBits(shift)    // (i) later we have to consider that sh contains too many numbers, e.g. when shift = [-1, 0]. Then sh = [0, numBits].
    assert(sh.low >= zero && sh.high < numBits)

    if (decomp.geqZero.nonEmpty) {
      possibleResults.append(shiftRightOfPositiveNumberAndShiftBetween0AndNumBits(decomp.geqZero.get, sh))
    }

    if (decomp.lessZero.nonEmpty && sh.low == zero) {
      possibleResults.append(decomp.lessZero.get)
    }

    if (decomp.lessZero.nonEmpty && sh.high != zero) {  // sh.high != zero <=> sh != [zero, zero]
      val lt0 = decomp.lessZero.get
      // for l <= h < 0 it holds [l, h] >>> 1 = [l >>> 1, h >>> 1] >= 0
      // Thus [l, h] >>> sh = [l >>> 1, h >>> 1] >>> (sh - 1).
      val intervalShiftedByOne: NumericInterval[I] = NumericInterval(ops.shiftRightUnsigned(lt0.low, one), ops.shiftRightUnsigned(lt0.high, one))
      val shiftMinusOne: NumericInterval[I] = getShiftMinusOne(sh)    // getShiftMinusOne will consider (i) and the returned bound contains the correct min and max value
      // We only need to know the min and maxValue of sh because of the monotonicity argument in
      // shiftRightOfPositiveNumberAndShiftBetween0AndNumBits, which we will use now:
      possibleResults.append(shiftRightOfPositiveNumberAndShiftBetween0AndNumBits(intervalShiftedByOne, shiftMinusOne))
    }

    joinMultipleIntervals(possibleResults)
  }

  def rotateLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = computeOpBruteForceIfFeasibleElseTake(ops.rotateLeft, v, shift, top)

  def rotateRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = computeOpBruteForceIfFeasibleElseTake(ops.rotateRight, v, shift, top)

  def countLeadingZeros(v: NumericInterval[I]): NumericInterval[I] =
    val decomp = v.getDecomposition
    val results = ArrayBuffer[NumericInterval[I]]()
    if (decomp.lessZero.nonEmpty)
    // bitSign is set to one
      results.append(NumericInterval(zero, zero))
    if (decomp.geqZero.nonEmpty)
      // if y >= x and x,y >= 0, then clz(y) <= clz(x)
      val geq0 = decomp.geqZero.get
      results.append(NumericInterval(ops.countLeadingZeros(geq0.high), ops.countLeadingZeros(geq0.low)))
    joinMultipleIntervals(results)

  def countTrailingZeros(v: NumericInterval[I]): NumericInterval[I] = {

    def findPositionOfHighestOneFollowedByZeros(b: NumericInterval[I]): I = {
      assert(b.high < zero || b.low > zero)
      // find max. n s.t. the rightmost n bits of v.high can be set to zero and the resulting num is in v.

      // Call this num (that maximizes n) x. Suppose c2 = ctz(y) > ctz(x) and y \in v.
      // Then y has a 1 at the c2-th bit (we always count from the right and the least sign. bit is the zeroth bit).
      // y must have a 0 where v.high has a 1 at a position j > c2, or otherwise either y > v or we would have found y with our method.
      // Now take the leftmost bits of v.high until (incl.) position j, append j 0's to the right and call this number z.
      // It holds v.high >= z > y >= v.low and ctz(z) > ctz(y), which is a contradiction.

      var ub = numBits
      var i = signBit
      var testNum = zero
      while (i != one) {
        if (bitIsOne(b.high, i)) {
          testNum = ops.bitXor(testNum, i)
          if (b.containsNum(testNum)) {
            return ops.countTrailingZeros(testNum)
          }
        }
        i = ops.shiftRightUnsigned(i, one)
      }
      // if here, then v = [1, 1] or v = [-1, -1]
      zero
    }

    if (v.low == v.high)
      return NumericInterval.constant(ops.countTrailingZeros(v.low))
    // now there will always be an odd number in the interval, so result.low = zero

    if (v.containsNum(zero))
      return NumericInterval(zero, numBits)

    NumericInterval(zero, findPositionOfHighestOneFollowedByZeros(v))
  }

  def nonzeroBitCount(v: NumericInterval[I]): NumericInterval[I] = {
    def nonzeroBitCountForIntervalNotChangingSign(v: NumericInterval[I]): NumericInterval[I] = {
      assert(v.high < zero || v.low >= zero)
      if (v.low == v.high)
        NumericInterval.constant(ops.nonzeroBitCount(v.low))
      else
        NumericInterval(minimizeOnes(v), numBits - minimizeOnes(invertBits(v)))
    }

    def minimizeOnes(b: NumericInterval[I]): I = {
      // max zeros:
      // go from left (excl. sign bit) to right and compare v.low v.high:
      //    if n-bit (counting from the right) is different:
      //      n-th bit of v.low must be zero
      //      if all <n-bits of v.low are 0:
      //        return nzbc(v.low)
      //      else
      //        nzbc(v.low) + 1 (set n-th bit to 1, <n-bits to 0, and >n like all numbers in v

      val (l, h) = (b.low, b.high)
      var i = signBit
      var copyingBoundsFromLeftToRight = zero
      while (i != zero) {
        (bitIsOne(l, i), bitIsOne(h, i)) match {
          case (true, true) => copyingBoundsFromLeftToRight = ops.bitXor(copyingBoundsFromLeftToRight, i)
          case (false, false) => // do nothing
          case (true, false) => throw IllegalStateException(s"it should be $l <= $h")
          case (false, true) =>
            if (copyingBoundsFromLeftToRight == l)
              return ops.nonzeroBitCount(l)
            else
              return ops.nonzeroBitCount(ops.bitXor(copyingBoundsFromLeftToRight, i))
        }
        i = ops.shiftRightUnsigned(i, one)
      }
      // if here, then l == h
      throw IllegalStateException("l != h should hold when calling this function")
    }

    val decomp = v.getDecomposition

    joinMultipleIntervals(List(
      decomp.geqZero.map(nonzeroBitCountForIntervalNotChangingSign),
      decomp.lessZero.map(nonzeroBitCountForIntervalNotChangingSign)
    ).filter(_.nonEmpty).map(_.get)
    )
  }

  def invertBits(v: NumericInterval[I]): NumericInterval[I] =
    /*
    Let l <= x <= h.
    Then ~h <= ~x <= ~l, because:

    -x = twosCompl(x) = ~x + 1
    => ~x = -x - 1
    => ~h = -h - 1 <= -x - 1 = ~x = -x - 1 <= -l - 1 = ~l
       and of course these bounds are tight for x=l and x=h
    */
    NumericInterval(ops.invertBits(v.high), ops.invertBits(v.low))

given TopNumericIntervalInt: Top[NumericInterval[Int]] with
  def top: NumericInterval[Int] = NumericInterval(Integer.MIN_VALUE, Integer.MAX_VALUE)
given TopNumericIntervalLong: Top[NumericInterval[Long]] with
  def top: NumericInterval[Long] = NumericInterval(Long.MinValue, Long.MaxValue)
given TopNumericIntervalByte: Top[NumericInterval[Byte]] with
  def top: NumericInterval[Byte] = NumericInterval(Byte.MinValue, Byte.MaxValue)

given NumericIntervalAbstractly[I](using Ordering[I]): Abstractly[I, NumericInterval[I]] with
  override def apply(i: I): NumericInterval[I] =
    NumericInterval(i, i)

given NumericIntervalOrdering[I](using Ordering[I]): PartialOrder[NumericInterval[I]] with
  override def lteq(x: NumericInterval[I], y: NumericInterval[I]): Boolean =
    y.low <= x.low && x.high <= y.high

given NumericIntervalJoin[I](using Ordering[I]): Join[NumericInterval[I]] with
  import NumericInterval.*
  override def apply(v1: NumericInterval[I], v2: NumericInterval[I]): MaybeChanged[NumericInterval[I]] =
    MaybeChanged(NumericInterval(v1.low.min(v2.low), v1.high.max(v2.high)), v1)

class NumericIntervalWiden[I](bounds: => Set[I], minValue: I, maxValue: I)(using Numeric[I]) extends Widen[NumericInterval[I]]:
  private lazy val treeSet: TreeSet[I] = TreeSet.from(bounds)
  override def apply(v1: NumericInterval[I], v2: NumericInterval[I]): MaybeChanged[NumericInterval[I]] =
    val low =
      if (v1.low <= v2.low) v1.low
      else treeSet.maxBefore(v2.low + summon[Numeric[I]].fromInt(1)).getOrElse(minValue)
    val high =
      if (v1.high >= v2.high) v1.high
      else treeSet.minAfter(v2.high).getOrElse(maxValue)
    MaybeChanged(NumericInterval(low, high), v1)

given NumericIntervalOrderingOps[I](using Ordering[I]): OrderingOps[NumericInterval[I], Topped[Boolean]] with
  def lt(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] =
    if (iv1.high < iv2.low) Topped.Actual(true)
    else if (iv2.high <= iv1.low) Topped.Actual(false)
    else Topped.Top
  def le(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] =
    if (iv1.high <= iv2.low) Topped.Actual(true)
    else if (iv2.high < iv1.low) Topped.Actual(false)
    else Topped.Top

given NumericIntervalUnsignedOrderingOps[I](using ops: UnsignedOrderingOps[I, Boolean], num: Numeric[I]): UnsignedOrderingOps[NumericInterval[I], Topped[Boolean]] with
  def ltUnsigned(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] =
    val u1 = iv1.toUnsigned
    val u2 = iv2.toUnsigned
    if (ops.ltUnsigned(u1.high, u2.low)) Topped.Actual(true)
    else if (ops.leUnsigned(u2.high, u1.low)) Topped.Actual(false)
    else Topped.Top
  def leUnsigned(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] =
    val u1 = iv1.toUnsigned
    val u2 = iv2.toUnsigned
    if (ops.leUnsigned(u1.high, u2.low)) Topped.Actual(true)
    else if (ops.ltUnsigned(u2.high, u1.low)) Topped.Actual(false)
    else Topped.Top

given NumericIntervalEqOps[I](using Ordering[I]): EqOps[NumericInterval[I], Topped[Boolean]] with
  override def equ(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] =
    if (iv1.low == iv1.high && iv1.high == iv2.low && iv2.low == iv2.high) Topped.Actual(true)
    else if (iv1.high < iv2.low || iv2.high < iv1.low) Topped.Actual(false)
    else Topped.Top
  override def neq(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] =
    if (iv1.low == iv1.high && iv1.high == iv2.low && iv2.low == iv2.high) Topped.Actual(false)
    else if (iv1.high < iv2.low || iv2.high < iv1.low) Topped.Actual(true)
    else Topped.Top

given ConvertNumericIntervals[From, To, I1, I2, Config <: ConvertConfig[_]]
(using convert: Convert[From, To, I1, I2, Config])(using Failure, EffectStack)
: Convert[From, To, NumericInterval[I1], NumericInterval[I2], Config] with

  def apply(i: NumericInterval[I1], conf: Config): NumericInterval[I2] =
    NumericInterval(convert(i.low, conf), convert(i.high, conf))

given ConvertNumericIntervalToConstant[From, To, I]: Convert[From, To, NumericInterval[I], Topped[I], NilCC.type] with
  def apply(i: NumericInterval[I], conf: NilCC.type): Topped[I] =
    if (i.isConstant) Topped.Actual(i.low) else Topped.Top

given ConvertConstantToNumericInterval[From, To, I](using Top[NumericInterval[I]]): Convert[From, To, Topped[I], NumericInterval[I], NilCC.type] with
  def apply(i: Topped[I], conf: NilCC.type): NumericInterval[I] = i match
    case Topped.Top => summon[Top[NumericInterval[I]]].top
    case Topped.Actual(l) => NumericInterval(l, l)

given ConvertNumericIntervalToBytes[From, To, I, B]
(using convert: Convert[From, To, I, Seq[B], config.BytesSize && SomeCC[ByteOrder]], topB: Top[NumericInterval[B]])
(using Failure, EffectStack, Ordering[B])
: Convert[From, To, NumericInterval[I], Seq[NumericInterval[B]], config.BytesSize && SomeCC[ByteOrder]] with
  def apply(i: NumericInterval[I], conf: config.BytesSize && SomeCC[ByteOrder]): Seq[NumericInterval[B]] =
    val bigEndianConf = conf.c1 && SomeCC(ByteOrder.BIG_ENDIAN, false)
      val lowBytes = convert(i.low, bigEndianConf)
      val highBytes = convert(i.high, bigEndianConf)

      val byteIntervals = new ListBuffer[NumericInterval[B]]
      var equalPrefix = true
      lowBytes.zip(highBytes).foreach { case (lowByte, highByte) =>
        if (equalPrefix) {
          if (lowByte == highByte)
            byteIntervals += NumericInterval.constant(lowByte)
          else {
            equalPrefix = false
            if (lowByte <= highByte)
              byteIntervals += NumericInterval(lowByte, highByte)
            else
              throw new IllegalStateException(s"lowByte ($lowByte) > highByte ($highByte), but should have been smaller")
          }
        } else {
          byteIntervals += topB.top
        }
      }
      if (conf.c2.t == ByteOrder.BIG_ENDIAN)
        byteIntervals.toSeq
      else
        byteIntervals.view.reverse.toSeq


given ConvertBytesToNumericInterval[From, To, I]
(using convert: Convert[From, To, Seq[Byte], I, config.BytesSize && SomeCC[ByteOrder] && config.Bits], top: Top[NumericInterval[I]])
(using Failure, EffectStack, Ordering[I])
: Convert[From, To, Seq[NumericInterval[Byte]], NumericInterval[I], config.BytesSize && SomeCC[ByteOrder] && config.Bits] with
  def apply(bytes: Seq[NumericInterval[Byte]], conf: config.BytesSize && SomeCC[ByteOrder] && config.Bits): NumericInterval[I] =
    val lowBytes = new ListBuffer[Byte]
    val highBytes = new ListBuffer[Byte]

    val byteIntervals = if (conf.c1.c2.t == ByteOrder.BIG_ENDIAN) bytes else bytes.reverse
    if (byteIntervals.exists(_.isTop))
      return summon[Top[NumericInterval[I]]].top
    byteIntervals.foreach { i =>
      lowBytes += i.low
      highBytes += i.high
    }

    val bigEndianConf = conf.c1.c1 && SomeCC(ByteOrder.BIG_ENDIAN, false) && conf.c2
    val low = convert(lowBytes.toSeq, bigEndianConf)
    val high = convert(highBytes.toSeq, bigEndianConf)
    NumericInterval(low, high)
