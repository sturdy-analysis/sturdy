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
  def top[I]: NumericInterval[I] = NumericInterval.Top()
  def apply[I](l: I, h: I)(using ord: Ordering[I]): NumericInterval[I] =
    if (ord.lteq(l, h))
      NumericInterval.Bounded(l, h)
    else
      NumericInterval.Top()
  def fromBounds[I](b1: I, b2: I)(using ord: Ordering[I]): NumericInterval[I] =
    if (ord.lteq(b1, b2))
      NumericInterval.Bounded(b1, b2)
    else
      NumericInterval.Bounded(b2, b1)
  def constant[I](i: I): NumericInterval.Bounded[I] = NumericInterval.Bounded(i, i)

  case class Decomposition[I](lessZero: Option[Bounded[I]], leqZero: Option[Bounded[I]],
                              hasZero: Boolean,
                              geqZero: Option[Bounded[I]], greaterZero: Option[Bounded[I]])

  enum IsZero[I]:
    case Zero()
    case NotZero()
    case MaybeZeroTop()
    case BorderingZero(nonZeroLow: I, nonZeroHigh: I)
    case IncludesZero(belowZero: Bounded[I], aboveZero: Bounded[I])

    def toBoolean: Topped[Boolean] = this match
      case IsZero.Zero() => Topped.Actual(false)
      case IsZero.NotZero() => Topped.Actual(true)
      case IsZero.MaybeZeroTop() | IsZero.BorderingZero(_, _) | IsZero.IncludesZero(_, _) => Topped.Top

  private inline def fromInt[I](i: Int)(using Numeric[I]): I = summon[Numeric[I]].fromInt(i)

enum NumericInterval[I]:
  case Top()
  case Bounded(low: I, high: I)

  import NumericInterval.*

  def map[J](f: I => J)(using Ordering[J]): NumericInterval[J] = this match
    case Top() => Top()
    case Bounded(l, h) =>
      NumericInterval.fromBounds(f(l), f(h))

  inline def mapConstant[J](f: I => J)(using ord: Ordering[I]): NumericInterval[J] = this match
    case Top() => Top()
    case Bounded(l, h) =>
      if (ord.equiv(l, h))
        NumericInterval.constant(f(l))
      else
        NumericInterval.Top()

  inline def combine[J, K](other: NumericInterval[J], fl: (I, J) => K, fh: (I, J) => K)(using Ordering[K]): NumericInterval[K] = (this, other) match
    case (Top(), _) => Top()
    case (_, Top()) => Top()
    case (Bounded(l1, h1), Bounded(l2, h2)) =>
      NumericInterval.fromBounds(fl(l1, l2), fh(h1, h2))

  inline def combineCross[J, K](other: NumericInterval[J], f: (I, J) => K)(using Ordering[K]): NumericInterval[K] = (this, other) match
    case (Top(), _) => Top()
    case (_, Top()) => Top()
    case (Bounded(x1, x2), Bounded(y1, y2)) =>
      val x1y1 = f(x1, y1)
      val x1y2 = f(x1, y2)
      val x2y1 = f(x2, y1)
      val x2y2 = f(x2, y2)
      val result = NumericInterval.fromBounds(x1y1.min(x1y2).min(x2y1).min(x2y2), x1y1.max(x1y2).max(x2y1).max(x2y2))
      result

  def isTop: Boolean = this match
    case Top() => true
    case _ => false

  def isZero(using num: Numeric[I], ord: Ordering[I]): IsZero[I] = this match
    case Top() => IsZero.MaybeZeroTop()
    case Bounded(l, h) =>
      val zero = num.fromInt(0)
      if (l == zero && h == zero)
        IsZero.Zero()
      else if (l == zero)
        IsZero.BorderingZero(num.fromInt(1), h)
      else if (h == zero)
        IsZero.BorderingZero(l, num.fromInt(-1))
      else if (l < zero && h > zero)
        IsZero.IncludesZero(Bounded(l, num.fromInt(-1)), Bounded(num.fromInt(1), h))
      else
        IsZero.NotZero()

  def isConstant(using Ordering[I]): Boolean = this match
    case Top() => false
    case Bounded(l, h) => l == h


given IntervalIntegerOps[I](using ordering: Ordering[I], ops: IntegerOps[I, I], strict: StrictIntegerOps[I, I, NoJoin], num: Numeric[I])(using f: Failure, j: EffectStack): IntegerOps[I, NumericInterval[I]] with
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

  def deleteNumFromInterval(interval: NumericInterval[I], num: I): List[Bounded[I]] = {
    val b = asBounded(interval)

    if (num > b.high || num < b.low) {
      return List(b)
    }

    var result = List[Bounded[I]]()
    if (num == minValue && b.low == minValue && b.high != minValue)
      result = Bounded(minValue + one, b.high) :: result
    else if (num != minValue && num - one >= b.low)   // for num = minValue there would be an overflow in this condition
      result = Bounded(b.low, num - one) :: result
    if (num == maxValue && b.high == maxValue && b.low != maxValue)
      result = Bounded(b.low, maxValue - one) :: result
    else if (num != maxValue && num + one <= b.high) // for num = maxValue there would be an overflow in this condition
      result = Bounded(num + one, b.high) :: result
    result
  }

  def asBounded(v: NumericInterval[I]): Bounded[I] = v match
    case b@Bounded(low, high) => b
    case Top() => Bounded(minValue, maxValue)

  def intervalContainsNum(v: NumericInterval[I], n: I): Boolean = v match {
    case NumericInterval.Top() => true
    case NumericInterval.Bounded(low, high) =>
      low <= n && n <= high
  }

  def integerLit(i: I): NumericInterval[I] = NumericInterval(i, i)
  def randomInteger(): NumericInterval[I] = NumericInterval.top


  def getDecomposition(x: NumericInterval[I]): Decomposition[I] =
    val bounded = asBounded(x)
    bounded.isZero match
      case IsZero.Zero() => Decomposition(None, Some(Bounded(zero, zero)), true, Some(Bounded(zero, zero)), None)
      case IsZero.MaybeZeroTop() => throw IllegalStateException()
      case IsZero.NotZero() =>
        if (bounded.high < zero)
          Decomposition(Some(bounded), Some(bounded), false, None, None)
        else
          Decomposition(None, None, false, Some(bounded), Some(bounded))
      case IsZero.BorderingZero(nonZeroLow, nonZeroHigh) =>
        if (nonZeroLow < zero)
          Decomposition(Some(Bounded(nonZeroLow, nonZeroHigh)), Some(Bounded(nonZeroLow, zero)), true, Some(Bounded(zero, zero)), None)
        else
          Decomposition(None, Some(Bounded(zero, zero)), true, Some(Bounded(zero, nonZeroHigh)), Some(Bounded(nonZeroLow, nonZeroHigh)))
      case IsZero.IncludesZero(belowZero, aboveZero) => Decomposition(Some(belowZero), Some(Bounded(belowZero.low, zero)), true, Some(Bounded(zero, aboveZero.high)), Some(aboveZero))

  def intervalToRange(x: Bounded[I]): ArrayBuffer[I] = {
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

  def countOfNumsInInterval(v: Bounded[I]): BigInt = BigInt(v.high.toLong) - BigInt(v.low.toLong) + 1

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

  val feasibleNumberOfOps = 20
  def computeOpBruteForceIfFeasibleElseTake(unOp: I => I, v: NumericInterval[I], soundResult: NumericInterval[I]): NumericInterval[I] = {
    val b = asBounded(v)
    val count = countOfNumsInInterval(b)
    if (count > feasibleNumberOfOps)
      return soundResult

    var lb = maxValue
    var ub = minValue

    val soundResultBounded = asBounded(soundResult)
    val worstLb = soundResultBounded.low
    val worstUb = soundResultBounded.high

    var x = b.low
    while (true) {    // quasi x <= b.high, but this does not detect the overflow if b.high == maxValue
      val result = unOp(x)
      lb = lb.min(result)
      ub = ub.max(result)
      if (lb <= worstLb && ub >= worstUb) {
        assert(lb == worstLb && ub == worstUb)
        return soundResult
      }
      if (x == b.high) {
        assert(NumericIntervalOrdering.lteq(Bounded(lb, ub), soundResult))
        return Bounded(lb, ub)
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
    val b1 = asBounded(v1)
    val b2 = asBounded(v2)

    if (countOfNumsInInterval(b1) * countOfNumsInInterval(b2) > feasibleNumberOfOps)
      return soundResult

    var lb = maxValue
    var ub = minValue
    val soundResultBounded = asBounded(soundResult)
    val worstLb = soundResultBounded.low
    val worstUb = soundResultBounded.high
    var x = b1.low
    while (true) {       // quasi x <= b1.high, but this does not detect the overflow if b1.high == maxValue
      var y = b2.low
      while (true) {      // quasi y <= b2.high, but this does not detect the overflow if b2.high == maxValue
        val result = op(x, y)
        lb = lb.min(result)
        ub = ub.max(result)
        if (lb <= worstLb && ub >= worstUb) {
          assert(lb == worstLb && ub == worstUb)
          return soundResult
        }

        if (y == b2.high) {
          assert(NumericIntervalOrdering.lteq(Bounded(lb, ub), soundResult))
          return Bounded(lb, ub)
        }
        y = ops.add(y, one)
      }

      if (x == b1.high) {
        assert(NumericIntervalOrdering.lteq(Bounded(lb, ub), soundResult))
        return Bounded(lb, ub)
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
    t = b + d;
    u = a & c & ~s & ~(b & d &~t);
    v = ((a ^ c) | ~(a ^ s)) & (~b & ~d & t);
    if ((u | v) < 0) {
      s = 0x80000000;
      t = 0x7FFFFFFF;}
    */

    inline def not(i: I): I = ops.invertBits(i)
    inline def and3(i1: I, i2: I, i3: I): I = ops.bitAnd(ops.bitAnd(i1, i2), i3)
    inline def and4(i1: I, i2: I, i3: I, i4: I): I = ops.bitAnd(ops.bitAnd(ops.bitAnd(i1, i2), i3), i4)
    inline def or(i1: I, i2: I): I = ops.bitOr(i1, i2)
    inline def xor(i1: I, i2: I): I = ops.bitXor(i1, i2)

    val Bounded(a, b) = asBounded(v1)
    val Bounded(c, d) = asBounded(v2)

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

    Bounded(s, t)
  }

  def sub(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    val b1 = asBounded(v1)
    val b2 = asBounded(v2)
    // We want to calculate v1 + (-1) * v2, but (-1) * minValue = minValue, so we have to manage this case by hand.
    if (b2.low == minValue && b2.high == minValue) {
      val lowResult = strict.subStrict(b1.low, minValue)
      val highResult = strict.subStrict(b1.high, minValue)
      (lowResult, highResult) match {
        case (JOptionC.None(), h) =>
          // every result will overflow in the positive direction
          assert(h == JOptionC.None())
          Bounded(ops.sub(b1.low, minValue), ops.sub(b1.high, minValue))
        case (JOptionC.Some(_), JOptionC.None()) =>
          // only b1.high - minValue overflows in the positive direction
          Top()
        case (JOptionC.Some(l), JOptionC.Some(h)) =>
          // no result will overflow
          Bounded(l, h)
        case _ =>
          sys.error("I thought StrictIntegerOps would only return Some and None...")
      }
    } else if (b2.low == minValue) {
      joinComputations(
        sub(v1, Bounded(minValue, minValue))
      )(
        sub(v1, Bounded(minValue + one, b2.high))
      )
    } else {
      add(v1, mul(v2, Bounded(-one, -one)))
    }
  }

  def mul(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    val mulStrict = (i1: I, i2: I) => strict.mulStrict(i1, i2)
      .getOrElse(return NumericInterval.top) // imprecise! For I = Int: [Int.MaxValue, Int.MaxValue] * [2, 3] = [-2, Int.MaxValue - 2] != Top

    // the following use of combineCross can be seen in Table 6 of "Complete Interval Arithmetic and its Implementation on the Computer" by Ulrich W. Kulisch
    // https://www.math.kit.edu/ianm2/~kulisch/media/arjpkx.pdf


    val resultImpreciseWhenOverflowing = v1.combineCross(v2, mulStrict)   // see Table 6 in https://www.math.kit.edu/ianm2/~kulisch/media/arjpkx.pdf
    if (resultImpreciseWhenOverflowing.isTop)
      computeOpBruteForceIfFeasibleElseTake(ops.mul, v1, v2, Top())
    else
      resultImpreciseWhenOverflowing
  }

  def max(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = (v1, v2) match
    case (Top(), _) => Top()
    case (_, Top()) => Top()
    case (Bounded(l1, h1), Bounded(l2, h2)) => NumericInterval(ops.max(l1, l2), ops.max(h1, h2))

  def min(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = (v1, v2) match
    case (Top(), _) => Top()
    case (_, Top()) => Top()
    case (Bounded(l1, h1), Bounded(l2, h2)) => NumericInterval(ops.min(l1, l2), ops.min(h1, h2))

  def absolute(v: NumericInterval[I]): NumericInterval[I] = v match
    // mind that abs(minValue) = minValue in Scala, C++, Java
    case Top() => Top()
    case Bounded(l, h) if l == minValue =>
      Bounded(minValue, h.abs)
    case Bounded(l, h) =>
      if (l < zero) {
        if (h < zero) {
          // neg, neg
          NumericInterval.Bounded(h.abs, l.abs)
        } else {
          // neg, pos
          NumericInterval.Bounded(zero, l.abs.max(h))
        }
      } else {
        // pos, pos
        v
      }

  private inline def divByZero(v1: NumericInterval[I], v2: NumericInterval[I]) = f.fail(IntegerDivisionByZero, s"$v1 / $v2")

  private def divWith(f: (I, I) => I, v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    def divWithDivisorNotContainingZero(b1: Bounded[I], b2: Bounded[I]): NumericInterval[I] = {
      // the use of combineCross in divWithoutOverflowsAndDivisorNeqZero is legitimated by Table 7 of "Complete Interval Arithmetic and its Implementation on the Computer" by Ulrich W. Kulisch
      // https://www.math.kit.edu/ianm2/~kulisch/media/arjpkx.pdf

      // minValue / (-1) = minValue, therefore combineCross does not work if (-1 is an interval limit of b2 && minValue is an interval limit of b1)
      // In this case we join the results of combineCross( { b1\{minValue}, minValue } x { b2\{-1}, -1} )
      if (b1.low == minValue && b2.high == -one) {
        val newB1 = Bounded(minValue, minValue) :: deleteNumFromInterval(b1, minValue)
        val newB2 = Bounded(-one, -one) :: deleteNumFromInterval(b2, -one)
        joinMultipleIntervals(
          newB1.flatMap(x => newB2.map(y => asBounded(x).combineCross(asBounded(y), f)))
        )
      } else {
        b1.combineCross(b2, f)
      }
    }

    val b1 = asBounded(v1)
    val b2 = asBounded(v2)
    if (b2.low == zero && b2.high == zero)
      return divByZero(v1, v2)

    if (!intervalContainsNum(v2, zero)) {
      divWithDivisorNotContainingZero(b1, b2)
    } else{
      val v2sWithoutZero = deleteNumFromInterval(v2, zero)

      joinWithFailure(
        joinMultipleIntervals(
          v2sWithoutZero.map(currentV2 =>
            divWithDivisorNotContainingZero(b1, currentV2)
          )
        )
      )(divByZero(v1, v2))
    }

  def div(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.div, v1, v2)

  def divUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    val b1 = asBounded(v1)
    val b2 = asBounded(v2)

    if (b2.low == zero && b2.high == zero) {
      return divByZero(v1, v2)
    }

    val possibleResults = ArrayBuffer[NumericInterval[I]]()   // elements will be joined in the end
    if (intervalContainsNum(v2, one)) {
      // v1 / 1 = v1
      possibleResults.append(v1)
    }

    val decomp1 = getDecomposition(v1)
    val decomp2 = getDecomposition(v2)
    // let b := numBits
    if (decomp2.lessZero.nonEmpty) {
      // let y < 0 as signed (=> y >= 2^(b-1) as unsigned)
      // x / y = 1 if x >= y as unsigned (<=> y <= x < 0 as signed)
      //       = 0 if x < y as unsigned (<=> x >= 0 || 0 > x < y as signed)
      val y_lt0 = decomp2.lessZero.get
      val x_lt0 = decomp1.lessZero
      if (x_lt0.nonEmpty && x_lt0.get.high >= y_lt0.low) {
        possibleResults.append(Bounded(one, one))
      }
      val x_geq0 = decomp1.geqZero
      if (x_geq0.nonEmpty || (x_lt0.nonEmpty && x_lt0.get.low < y_lt0.high)) {
        possibleResults.append(Bounded(zero, zero))
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
      // then 0 <= x / y < 2^(b-1)

      val y_geq2 = y_geq2Option.get
      if (decomp1.hasZero) {
        // 0 / y = 0
        possibleResults.append(Bounded(zero, zero))
      }
      if (decomp1.lessZero.nonEmpty) {
        // let x < 0 as signed (<=> x >= 2^(b-1) as unsigned)

        // x1 <= x2 < 0 as signed <=> 2^(b-1) <= x1 <= x2 as unsigned.
        // Thus _/_ is monotonically increasing in its first argument
        // and monotonically decreasing in its second argument for the current domain.
        // Hence, the result is the following:
        val x_lt0 = decomp1.lessZero.get
        possibleResults.append(Bounded(
          ops.divUnsigned(x_lt0.low, y_geq2.high),
          ops.divUnsigned(x_lt0.high, y_geq2.low),
        ))
      }
      if (decomp1.greaterZero.nonEmpty) {
        // let x > 0 as signed (<=> 0 < x < 2^(b-1) as unsigned)

        // 0 < x1 <= x2 as signed <=> 0 < x1 <= x2 < 2^(b-1) as unsigned.
        // Thus _/_ is monotonically increasing in its first argument
        // and monotonically decreasing in its second argument for the current domain.
        // Hence, the result is the following:
        val x_gt0 = decomp1.greaterZero.get
        possibleResults.append(Bounded(
          ops.divUnsigned(x_gt0.low, y_geq2.high),
          ops.divUnsigned(x_gt0.high, y_geq2.low),
        ))
      }
    }

    assert(possibleResults.nonEmpty, s"$v1 $v2       $y_geq2Option")
    val result = joinMultipleIntervals(possibleResults)
    if (intervalContainsNum(b2, zero)) {
      joinWithFailure(result)(divByZero(v1, v2))
    } else {
      result
    }
  }

  def modulo(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    // a mod n has result in [0, |n| - 1]. Thus most imprecise result is Bounded(zero, maxValue)

    // if [a1, a2] \subseteq [0, maxValue]: result = a rem n
    // else if [-1, 0] \in [a1, a2]: result = [0, |n| - 1]   // mind n1 = minValue when calculating |n|. Also maybe divByZero if 0 \in n
    // else
    //    => [a1, a2] \subseteq [minValue, -2]
    //    let [r1, r2] := [a1, a2] rem [n1, n2]
    //    sound bc:
    //    result = [0, min(r2 + |n|, |n| - 1)]   // imprecise but sound!
    //      for b < 0: b mod m = (b rem m) + m <= r2 + |n|
    //       otherwise 0 mod m = 0
    //    "<=" not tight in [-3, -3] mod [2, 5]:
    //    [-3, -3] rem [2, 5] = [-3, 0]
    //    [-3, -3] mod [2, 5] = [0, 2] = [-3 mod 3, -3 mod 5]
    //    min(r2 + |n|, |n| - 1) = 4 in this example

    val Bounded(a1, a2) = asBounded(v1)
    val Bounded(n1, n2) = asBounded(v2)

    if (a1 >= zero)
      return remainder(v1, v2)

    val nAbsMinusOne = if (n1 == minValue) maxValue else n1.abs.max(n2.abs)
    val nContainsZero = intervalContainsNum(v2, zero)
    if (a1 <= -one && a2 >= zero) {
      val result = Bounded(zero, nAbsMinusOne)
      return
        if (nContainsZero)
          joinWithFailure(result)(divByZero(v1, v2))
        else
          result
    }
    val remRes = asBounded(remainder(v1, v2))

    assert(remRes.high <= zero && remRes.low > minValue)
    val high = if (remRes == zero) nAbsMinusOne else (remRes.high + nAbsMinusOne) + one    // nAbsMinusOne + one might be maxValue + 1
    val result = computeOpBruteForceIfFeasibleElseTake(ops.modulo, v1, v2, Bounded(zero, high))
    if (nContainsZero)
      joinWithFailure(result)(divByZero(v1, v2))
    else
      result

  def remainderUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.remainderUnsigned, v1, v2)

  private def negativeAbsolute(v: NumericInterval[I]): Bounded[I] = v match {
    case Top() => Bounded(minValue, zero)
    case Bounded(l, h) =>
      if (h >= zero) {
        if (l >= zero) {
          // > 0, > 0
          NumericInterval.Bounded(ops.mul(h, -one), ops.mul(l, -one))
        } else {
          // < 0, >= 0
          NumericInterval.Bounded(l.min(ops.mul(h, -one)), zero)
        }
      } else {
        // <= 0, <= 0
        asBounded(v)
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

    def remainderNegativeDividendAndDivisor(dividend: Bounded[I], divisor: Bounded[I]): NumericInterval[I] =
      inline def isEven(n: I): Boolean = ops.modulo(n, num.fromInt(2)) == zero
      val Bounded(b1, b2) = dividend
      val Bounded(m1, m2) = divisor
      assert(b2 <= zero && b1 <= b2)
      assert(m2 < zero && m1 <= m2)

      // The proofs in the comments are for [a1, a2] % [n1, n2] with [a1, a2] = [-b2, -b1] = |[b1, b2]| and [n1, n2] = [-m2, -m1] = |[m1, m2]|
      // so that we only have to deal with non negative numbers in the proofs.

      // [a1, a2] % [n1, n2] \subseteq [0, maxValue] bc of asserts

      // if divisorContainedMinValue, then the first three cases handle situation correctly
      if (m2 < b1)
      // a2 < n1
      // a % n = a for all a \in [a1, a2], n \in [n1, n2]
      // => result = Bounded(a1, a2)
        Bounded(b1, b2)
      else if (m1 < b1) // && m2 >= b1
      // a2 < n2 && n1 <= a2
      // result = [0, a2] = [a2 % a2, a2 % n2]. result cant be bigger than a2 bc of iii)
        Bounded(b1, zero)
      else if (b1 == b2 && b1 == m1 && isEven(m1))
      // a1 == a2 && a2 == m2 && isEven(n2)
      // result = [0, min(n2-n1, n2/2 - 1)]
      // n2 % (n2 - i) = i for i \in {0, 1, ..., n2/2 - 1},
      // bc n2 = 1*(n2 - i) + i and n2 - i >= n2/2 + 1 > i.
      // n2 % (n2 - i) <= n2/2 - 1 for i >= n2/2, bc. n2 - i <= n2/2 and iv)
        Bounded(ops.max(m1 - m2, ops.div(m1, num.fromInt(2)) + one), zero)
      else if (b1 == b2 && b1 == m1) // && isOdd(m1)
      // a1 == a2 && a2 == n2 && isOdd(n2)
      // result = [0, min(n2-n1, floor(n2/2))]
      // n2 % (n2 - i) = i for i \in {0, 1, ..., floor(n2/2)},
      // bc n2 = 1*(n2 - i) + i and n2 - i >= ceil(n2/2) > i.
      // n2 % (n2 - i) <= floor(n2/2) for i > ceil(n2/2), bc. n2 - i <= floor(n2/2) and iv)
        Bounded(ops.max(m1 - m2, ops.div(m1, num.fromInt(2))), zero)
      else
      // returned result is [0, n2-1] in this case. In the following cases this is precise:
      // if (a2 - a1 + 1 >= n2) (#[a1, a2] >= n2 - 1  => All values are of Z/(n2)Z are taken)
      // if (a2 == n2 && a1 != a2)
      //    result = [0, a2-1] = [a2 % n2, (a2-1) % n2]. result cant be bigger than a2-1 = n2-1 bc of iv)
      // if (a1 < n2 < a2)
      //    result = [0, n2 - 1] = [n2 % n2, (n2-1) % n2]. result cant be bigger than n2-1 bc of iv)
      // The only imprecise case is n2 <= a1 && a2-a1+1 < n2
        if (m1 >= b2 && ops.mul(b2-b1 + one, -one) > m1)
          computeOpBruteForceIfFeasibleElseTake(ops.remainder, dividend, divisor, Bounded(m1 + one, zero))
        else
          Bounded(m1 + one, zero)

    def remainderPositiveDividendAndNegativeDivisor(dividend: Bounded[I], divisor: Bounded[I]): NumericInterval[I] =
      assert(dividend.low >= one && dividend.low <= dividend.high)
      assert(divisor.high <= -one && divisor.low <= divisor.high)

      val negativeDividend: Bounded[I] = Bounded(ops.mul(dividend.high, -one), ops.mul(dividend.low, -one))
      // using ii):
      mul(NumericInterval.constant(num.fromInt(-1)), remainderNegativeDividendAndDivisor(negativeDividend, divisor))

    def remainderNegativeDivisor(dividend: Bounded[I], divisor: Bounded[I]): NumericInterval[I] =
      assert(divisor.high < zero && divisor.low <= divisor.high)
      dividend.isZero match {
        case IsZero.Zero() => NumericInterval.constant(zero)
        case IsZero.NotZero() =>
          if (dividend.high < zero)
            remainderNegativeDividendAndDivisor(dividend, divisor)
          else
            remainderPositiveDividendAndNegativeDivisor(dividend, divisor)
        case IsZero.BorderingZero(a1, a2) =>
          if (a2 < zero)
            NumericIntervalJoin(NumericInterval.constant(zero), remainderNegativeDividendAndDivisor(Bounded(a1, a2), divisor)).get
          else
          // 0 < a1 <= a2
            NumericIntervalJoin(NumericInterval.constant(zero), remainderPositiveDividendAndNegativeDivisor(Bounded(a1, a2), divisor)).get

        case IsZero.IncludesZero(belowZero, aboveZero) =>
          joinComputations(joinComputations(
            NumericInterval.constant(zero))(
            remainderNegativeDividendAndDivisor(belowZero, divisor)))(
            remainderPositiveDividendAndNegativeDivisor(aboveZero, divisor)
          )

        case IsZero.MaybeZeroTop() => throw IllegalStateException()
      }

    val dividend = asBounded(v1)
    val divisorAbs = negativeAbsolute(v2) // see ii)
    divisorAbs.isZero match {
      case IsZero.Zero() => divByZero(v1, v2)
      case IsZero.NotZero() =>
        remainderNegativeDivisor(dividend, divisorAbs) // divisorAbs \subseteq [minValue, -1]
      case IsZero.BorderingZero(n1, n2) =>
        // [n1, n2] \subseteq [minValue, -1]
        joinWithFailure(
          remainderNegativeDivisor(dividend, Bounded(n1, n2))
        )(
          divByZero(v1, v2)
        )
      case IsZero.IncludesZero(_, _) | IsZero.MaybeZeroTop() => throw IllegalStateException()
    }
  }

  def gcd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    computeOpBruteForceIfFeasibleElseTake(ops.gcd, v1, v2,
      Top() // the current implementation of ConcreteIntOps yields gcd(minValue, minValue) = minValue
    )

  // see Hacker's delight by Henry S. Warren, Jr., Chapter 4.3 (https://doc.lagout.org/security/Hackers%20Delight.pdf)
  def bitAnd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    if (v1.isTop || v2.isTop)
      return Top()

    val bounded1 = asBounded(v1)
    val bounded2 = asBounded(v2)
    // use of DeMorgan-Rules as explained in Hacker's Delight
    val needsInversion = asBounded(bitOr(Bounded(
      ops.invertBits(bounded1.high),
      ops.invertBits(bounded1.low)), Bounded(
      ops.invertBits(bounded2.high),
      ops.invertBits(bounded2.low)
    )))

    Bounded(
      ops.invertBits(needsInversion.high),
      ops.invertBits(needsInversion.low)
    )

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

    if (v1.isTop || v2.isTop)
      return Top()

    val bounded1 = asBounded(v1)
    val bounded2 = asBounded(v2)
    val a = bounded1.low
    val b = bounded1.high
    val c = bounded2.low
    val d = bounded2.high

    // following match of indicatorForSignsOfABCD corresponds to Table 4.1
    var indicatorForSignsOfABCD = 0
    if (d >= zero) indicatorForSignsOfABCD |= 1
    if (c >= zero) indicatorForSignsOfABCD |= 2
    if (b >= zero) indicatorForSignsOfABCD |= 4
    if (a >= zero) indicatorForSignsOfABCD |= 8

    indicatorForSignsOfABCD match
      case 0 | 3 | 12 | 15 => Bounded(minOr(a, b, c, d), maxOr(a, b, c, d))
      case 1 => Bounded(a                , -one)
      case 4 => Bounded(c                , -one)
      case 5 => Bounded(ops.min(a, c), maxOr(zero, b, zero, d))
      case 7 => Bounded(minOr(a, -one, c, d), maxOr(zero, b, c, d))
      case 13 => Bounded(minOr(a, b, c, -one), maxOr(a, b, zero, d))

  def bitXor(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = {
    // see Hacker's delight by Henry S. Warren, Jr., Chapter 4.3 (https://doc.lagout.org/security/Hackers%20Delight.pdf)

    // for minXorValueForPosInterval and maxXorValueForPosInterval see the text and code image right above the section "Signed Bounds".
    // minXorValueForPosInterval and maxXorValueForPosInterval only work for positive numbers as pointed out in the section "Signed Bounds".
    def minXorValueForPosInterval(b1: Bounded[I], b2: Bounded[I]): I = {
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

    def maxXorValueForPosInterval(b1: Bounded[I], b2: Bounded[I]): I = {
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

    if (v1.isTop || v2.isTop)   // just for efficiency
      return Top()

    val decomp1 = getDecomposition(v1)
    val decomp2 = getDecomposition(v2)

    val geq1 = decomp1.geqZero
    val geq2 = decomp2.geqZero

    val flipSignBit: NumericInterval[I] => NumericInterval[I] = _.map(ops.bitXor(_, signBit))
    val lt1WithoutSign: Option[Bounded[I]] = decomp1.lessZero.map(i => asBounded(flipSignBit(i)))
    val lt2WithoutSign: Option[Bounded[I]] = decomp2.lessZero.map(i => asBounded(flipSignBit(i)))

    val results: ArrayBuffer[NumericInterval[I]] = ArrayBuffer()

    val xorForPosIntervals = (b1: Bounded[I], b2: Bounded[I]) => Bounded(minXorValueForPosInterval(b1, b2), maxXorValueForPosInterval(b1, b2))

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

  private inline def mapBounded[A](v: Bounded[I], mapFun: I => I): Bounded[I] = Bounded(mapFun(v.low), mapFun(v.high))

  def shiftLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = {
    // it holds shiftLeft(v, s) = shiftLeft(v, s') if s mod numBits == s' mod numBits

    def shiftLeftWithPreprocessedShift(v: Bounded[I], shift: Bounded[I]): NumericInterval[I] = {
      // shift is preprocessed like this:
      assert(shift.low > -numBits && shift.high < numBits && countOfNumsInInterval(shift) <= numBits.toLong)
      val vCount = countOfNumsInInterval(v)

      def resultForConstantShift(shift: I): NumericInterval[I] = {
        assert(shift >= zero && shift < numBits)
        if (shift == zero)
          return v

        val firstBitShiftedOut = ops.shiftLeft(one, numBits - shift)    // rightmost bit which will overflow by the shift
        val allBitsThatWontVanishSetToOne = firstBitShiftedOut - one
        val intervalsOfNotVanishedBits: List[Bounded[I]] = // all values that can appear when setting the bits to zero that overflow by the shift.
          if (vCount >= toUnsigned(firstBitShiftedOut)) {
            // all values are possible
            List(Bounded(zero, allBitsThatWontVanishSetToOne))
          } else {
            val lowNotVanishingBits = ops.bitAnd(v.low, allBitsThatWontVanishSetToOne)
            val highNotVanishingBits = ops.bitAnd(v.high, allBitsThatWontVanishSetToOne)
            if (lowNotVanishingBits <= highNotVanishingBits) {
              List(Bounded(lowNotVanishingBits, highNotVanishingBits))
            } else {
              // e.g. v = [0110, 1010] and firstBitShiftedOut = 1000. Then we must return
              // [110, 111] for the numbers < 1000 and [000, 010] for the numbers > 1000
              List(Bounded(lowNotVanishingBits, allBitsThatWontVanishSetToOne), Bounded(zero, highNotVanishingBits))
            }
          }

        val shiftByShift = ops.shiftLeft(_, shift)
        val bitThatWillBeSignBit = ops.shiftRightUnsigned(firstBitShiftedOut, one)
        // when we shift each interval [l, h] of intervalsOfNotVanishedBits, we get the correct result, if the numbers were unsigned.
        // So we (only) have to consider the case when the sign bit of shift(h) is 1 and the sign bit of shift(l) is 0.
        joinMultipleIntervals(
          intervalsOfNotVanishedBits.map { v =>
            if ((v.low >= bitThatWillBeSignBit) == (v.high >= bitThatWillBeSignBit)) {
              Bounded(shiftByShift(v.low), shiftByShift(v.high))
            } else {
              // now shift(v.low) >= 0 and shift(v.high) < 0.
              // Thus 10000...0 = minValue is a possible result and also
              // 01...10...0 is also in the interval where the number of ones is (#bits that don't vanish - 1). This is
              // the max value that can be achieved by a shiftLeft(_, shift). We compute it like this:
              // shift(allBitsThatWontVanishSetToOne) = 11...10...0, so we only have to remove the sign bit.
              Bounded(minValue, ops.bitAnd(maxValue, shiftByShift(allBitsThatWontVanishSetToOne)))
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

    val shiftBounded = asBounded(shift)
    val countOfOriginalShift = countOfNumsInInterval(shiftBounded)

    val sh: Bounded[I] =    // sh \subseteq [-numBits + 1, numBits - 1]
      if (countOfOriginalShift < numBits.toLong) {
        val remOfLow = ops.modulo(shiftBounded.low, numBits)
        val remOfHigh = ops.modulo(shiftBounded.high, numBits)
        if (remOfLow <= remOfHigh)
          Bounded(remOfLow, remOfHigh)
        else
          Bounded(remOfLow - numBits, remOfHigh)
      } else {
        asBounded(bitAnd(shift, Bounded(numBits - one, numBits - one)))   // shift % numBits
      }
    shiftLeftWithPreprocessedShift(asBounded(v), sh)
  }

  private inline def shiftRightOfPositiveNumberAndShiftBetween0AndNumBits(v: Bounded[I], shiftLtNumBits: Bounded[I]): Bounded[I] = {
    assert(v.low >= zero && shiftLtNumBits.low >= zero && shiftLtNumBits.high < numBits)
    // for v >= 0 and 0 <= s < numBits: v >>> s is monotonically increasing in v and monotonically decreasing in s
    Bounded(
      ops.shiftRight(v.low, shiftLtNumBits.high),
      ops.shiftRight(v.high, shiftLtNumBits.low)
    )
  }

  def shiftRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = {
    val decomp = getDecomposition(v)
    val possibleResults = ArrayBuffer[NumericInterval[I]]() // elements will be joined in the end
    val sh = asBounded(bitAnd(shift, Bounded(numBits - one, numBits - one))) // = shift % [numBits, numBits]
    assert(sh.low >= zero && sh.high < numBits)

    if (decomp.lessZero.nonEmpty) {
      val lt0 = decomp.lessZero.get
      // let x < 0, 0 <= s < numBits. Then shiftRight(x, s) is monotonically increasing in both arguments.
      possibleResults.append(Bounded(
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
    inline def modNumBits(interval: NumericInterval[I]) = asBounded(bitAnd(interval, Bounded(numBits - one, numBits - one)))
    def getShiftMinusOne(shiftBetween0And32: Bounded[I]): Bounded[I] = {
      val shiftBounded = asBounded(shift)
      val countOfInterval = countOfNumsInInterval(shiftBounded)
      if (countOfInterval >= 2 && countOfInterval < numBits.toLong && ops.bitAnd(shiftBounded.high, numBits - one) == zero) {
        val shiftWithoutMod0 = modNumBits(Bounded(shiftBounded.low, shiftBounded.high - one))
        assert(shiftWithoutMod0.low > zero)
        Bounded(
          shiftWithoutMod0.low - one,
          shiftWithoutMod0.high - one
        )
      } else {
        Bounded(ops.max(zero, shiftBetween0And32.low - one), shiftBetween0And32.high - one)
      }
    }

    val decomp = getDecomposition(v)
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
      val intervalShiftedByOne: Bounded[I] = Bounded(ops.shiftRightUnsigned(lt0.low, one), ops.shiftRightUnsigned(lt0.high, one))
      val shiftMinusOne: Bounded[I] = getShiftMinusOne(sh)    // getShiftMinusOne will consider (i) and the returned bound contains the correct min and max value
      // We only need to know the min and maxValue of sh because of the monotonicity argument in
      // shiftRightOfPositiveNumberAndShiftBetween0AndNumBits, which we will use now:
      possibleResults.append(shiftRightOfPositiveNumberAndShiftBetween0AndNumBits(intervalShiftedByOne, shiftMinusOne))
    }

    joinMultipleIntervals(possibleResults)
  }

  def rotateLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = computeOpBruteForceIfFeasibleElseTake(ops.rotateLeft, v, shift, Top())

  def rotateRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = computeOpBruteForceIfFeasibleElseTake(ops.rotateRight, v, shift, Top())

  def countLeadingZeros(v: NumericInterval[I]): NumericInterval[I] =
    val b = asBounded(v)
    val decomp = getDecomposition(v)
    val results = ArrayBuffer[NumericInterval[I]]()
    if (decomp.lessZero.nonEmpty)
    // bitSign is set to one
      results.append(Bounded(zero, zero))
    if (decomp.geqZero.nonEmpty)
      // if y >= x and x,y >= 0, then clz(y) <= clz(x)
      val geq0 = decomp.geqZero.get
      results.append(Bounded(ops.countLeadingZeros(geq0.high), ops.countLeadingZeros(geq0.low)))
    joinMultipleIntervals(results)

  def countTrailingZeros(v: NumericInterval[I]): NumericInterval[I] = {

    def findPositionOfHighestOneFollowedByZeros(b: Bounded[I]): I = {
      assert(b.high < zero || b.low > zero)
      // find max. n s.t. the rightmost n bits of b.high can be set to zero and the resulting num is in b.

      // Call this num (that maximizes n) x. Suppose c2 = ctz(y) > ctz(x) and y \in b.
      // Then y has a 1 at the c2-th bit (we always count from the right and the least sign. bit is the zeroth bit).
      // y must have a 0 where b.high has a 1 at a position j > c2, or otherwise either y > b or we would have found y with our method.
      // Now take the leftmost bits of b.high until (incl.) position j, append j 0's to the right and call this number z.
      // It holds b.high >= z > y >= b.low and ctz(z) > ctz(y), which is a contradiction.

      var ub = numBits
      var i = signBit
      var testNum = zero
      while (i != one) {
        if (bitIsOne(b.high, i)) {
          testNum = ops.bitXor(testNum, i)
          if (intervalContainsNum(b, testNum)) {
            return ops.countTrailingZeros(testNum)
          }
        }
        i = ops.shiftRightUnsigned(i, one)
      }
      // if here, then v = [1, 1] or v = [-1, -1]
      zero
    }

    if (v.isTop) // just for efficiency
      return Bounded(zero, numBits)
    val b = asBounded(v)
    if (b.low == b.high)
      return NumericInterval.constant(ops.countTrailingZeros(b.low))
    // now there will always be an odd number in the interval, so result.low = zero

    if (intervalContainsNum(b, zero))
      return Bounded(zero, numBits)

    Bounded(zero, findPositionOfHighestOneFollowedByZeros(b))
  }

  def nonzeroBitCount(v: NumericInterval[I]): NumericInterval[I] = {
    def nonzeroBitCountForIntervalNotChangingSign(b: Bounded[I]): Bounded[I] = {
      assert(b.high < zero || b.low >= zero)
      if (b.low == b.high)
        NumericInterval.constant(ops.nonzeroBitCount(b.low))
      else
        Bounded(minimizeOnes(b), numBits - minimizeOnes(asBounded(invertBits(b))))
    }

    def minimizeOnes(b: Bounded[I]): I = {
      // max zeros:
      // go from left (excl. sign bit) to right and compare b.low b.high:
      //    if n-bit (counting from the right) is different:
      //      n-th bit of b.low must be zero
      //      if all <n-bits of b.low are 0:
      //        return nzbc(b.low)
      //      else
      //        nzbc(b.low) + 1 (set n-th bit to 1, <n-bits to 0, and >n like all numbers in b

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

    val decomp = getDecomposition(asBounded(v))

    joinMultipleIntervals(List(
      decomp.geqZero.map(nonzeroBitCountForIntervalNotChangingSign),
      decomp.lessZero.map(nonzeroBitCountForIntervalNotChangingSign)
    ).filter(_.nonEmpty).map(_.get)
    )
  }

  def invertBits(v: NumericInterval[I]): NumericInterval[I] =
    val b = asBounded(v)
    /*
    Let l <= x <= h.
    Then ~h <= ~x <= ~l, because:

    -x = twosCompl(x) = ~x + 1
    => ~x = -x - 1
    => ~h = -h - 1 <= -x - 1 = ~x = -x - 1 <= -l - 1 = ~l
       and of course these bounds are tight for x=l and x=h
    */
    Bounded(ops.invertBits(b.high), ops.invertBits(b.low))

given TopNumericInterval[I]: Top[NumericInterval[I]] with
  override def top: NumericInterval[I] = NumericInterval.top

given NumericIntervalAbstractly[I](using Ordering[I]): Abstractly[I, NumericInterval[I]] with
  override def apply(i: I): NumericInterval[I] =
    NumericInterval.Bounded(i, i)

given NumericIntervalOrdering[I](using Ordering[I]): PartialOrder[NumericInterval[I]] with
  override def lteq(x: NumericInterval[I], y: NumericInterval[I]): Boolean = (x, y) match
    case (NumericInterval.Top(), _) => false
    case (_, NumericInterval.Top()) => true
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      l2 <= l1 && h1 <= h2

given NumericIntervalJoin[I](using Ordering[I]): Join[NumericInterval[I]] with
  import NumericInterval.*
  override def apply(v1: NumericInterval[I], v2: NumericInterval[I]): MaybeChanged[NumericInterval[I]] = (v1, v2) match
    case (Top(), _) => MaybeChanged.Unchanged(v1)
    case (_, Top()) => MaybeChanged.Changed(v2)
    case (Bounded(l1, h1), Bounded(l2, h2)) => MaybeChanged(Bounded(l1.min(l2), h1.max(h2)), v1)

class NumericIntervalWiden[I](bounds: => Set[I], minValue: I, maxValue: I)(using Numeric[I]) extends Widen[NumericInterval[I]]:
  private lazy val treeSet: TreeSet[I] = TreeSet.from(bounds)
  override def apply(v1: NumericInterval[I], v2: NumericInterval[I]): MaybeChanged[NumericInterval[I]] = (v1, v2) match
    case (NumericInterval.Top(), _) => MaybeChanged.Unchanged(v1)
    case (_, NumericInterval.Top()) => MaybeChanged.Changed(v2)
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      val low =
        if (l1 <= l2) l1
        else treeSet.maxBefore(l2 + summon[Numeric[I]].fromInt(1)).getOrElse(minValue)
      val high =
        if (h1 >= h2) h1
        else treeSet.minAfter(h2).getOrElse(maxValue)
      MaybeChanged(NumericInterval.Bounded(low, high), v1)

given NumericIntervalOrderingOps[I](using Ordering[I]): OrderingOps[NumericInterval[I], Topped[Boolean]] with
  def lt(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (h1 < l2) Topped.Actual(true)
      else if (h2 <= l1) Topped.Actual(false)
      else Topped.Top
  def le(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (h1 <= l2) Topped.Actual(true)
      else if (h2 < l1) Topped.Actual(false)
      else Topped.Top

given NumericIntervalUnsignedOrderingOps[I](using ops: UnsignedOrderingOps[I, Boolean]): UnsignedOrderingOps[NumericInterval[I], Topped[Boolean]] with
  def ltUnsigned(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (ops.ltUnsigned(h1, l2)) Topped.Actual(true)
      else if (ops.leUnsigned(h2, l1)) Topped.Actual(false)
      else Topped.Top
  def leUnsigned(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (ops.leUnsigned(h1, l2)) Topped.Actual(true)
      else if (ops.ltUnsigned(h2, l1)) Topped.Actual(false)
      else Topped.Top

given NumericIntervalEqOps[I](using Ordering[I]): EqOps[NumericInterval[I], Topped[Boolean]] with
  override def equ(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (l1 == h1 && h1 == l2 && l2 == h2) Topped.Actual(true)
      else if (h1 < l2 || h2 < l1) Topped.Actual(false)
      else Topped.Top
  override def neq(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (l1 == h1 && h1 == l2 && l2 == h2) Topped.Actual(false)
      else if (h1 < l2 || h2 < l1) Topped.Actual(true)
      else Topped.Top

given ConvertNumericIntervals[From, To, I1, I2, Config <: ConvertConfig[_]]
(using convert: Convert[From, To, I1, I2, Config])(using Failure, EffectStack)
: Convert[From, To, NumericInterval[I1], NumericInterval[I2], Config] with

  def apply(i: NumericInterval[I1], conf: Config): NumericInterval[I2] = i match
    case NumericInterval.Top() => safeConversion(conf, NumericInterval.Top())
    case NumericInterval.Bounded(l, h) => NumericInterval.Bounded(convert(l, conf), convert(h, conf))

given ConvertNumericIntervalToConstant[From, To, I]: Convert[From, To, NumericInterval[I], Topped[I], NilCC.type] with
  def apply(i: NumericInterval[I], conf: NilCC.type): Topped[I] = i match
    case NumericInterval.Top() => Topped.Top
    case NumericInterval.Bounded(l, h) => if (l == h) Topped.Actual(l) else Topped.Top

given ConvertConstantToNumericInterval[From, To, I]: Convert[From, To, Topped[I], NumericInterval[I], NilCC.type] with
  def apply(i: Topped[I], conf: NilCC.type): NumericInterval[I] = i match
    case Topped.Top => NumericInterval.Top()
    case Topped.Actual(l) => NumericInterval.Bounded(l, l)

given ConvertNumericIntervalToBytes[From, To, I, B]
(using convert: Convert[From, To, I, Seq[B], config.BytesSize && SomeCC[ByteOrder]])
(using Failure, EffectStack, Ordering[B])
: Convert[From, To, NumericInterval[I], Seq[NumericInterval[B]], config.BytesSize && SomeCC[ByteOrder]] with
  def apply(i: NumericInterval[I], conf: config.BytesSize && SomeCC[ByteOrder]): Seq[NumericInterval[B]] = i match
    case NumericInterval.Top() =>
      val bytes = Seq.fill(conf._1.bytes)(NumericInterval.Top[B]())
      safeConversion(conf, bytes)
    case NumericInterval.Bounded(l, h) =>
      val bigEndianConf = conf.c1 && SomeCC(ByteOrder.BIG_ENDIAN, false)
      val lowBytes = convert(l, bigEndianConf)
      val highBytes = convert(h, bigEndianConf)

      val byteIntervals = new ListBuffer[NumericInterval[B]]
      var equalPrefix = true
      lowBytes.zip(highBytes).foreach { case (lowByte, highByte) =>
        if (equalPrefix) {
          if (lowByte == highByte)
            byteIntervals += NumericInterval.constant(lowByte)
          else {
            equalPrefix = false
            if (lowByte <= highByte)
              byteIntervals += NumericInterval.Bounded(lowByte, highByte)
            else
              throw new IllegalStateException(s"lowByte ($lowByte) > highByte ($highByte), but should have been smaller")
          }
        } else {
          byteIntervals += NumericInterval.Top()
        }
      }
      if (conf.c2.t == ByteOrder.BIG_ENDIAN)
        byteIntervals.toSeq
      else
        byteIntervals.view.reverse.toSeq


given ConvertBytesToNumericInterval[From, To, I]
(using convert: Convert[From, To, Seq[Byte], I, config.BytesSize && SomeCC[ByteOrder] && config.Bits])
(using Failure, EffectStack, Ordering[I])
: Convert[From, To, Seq[NumericInterval[Byte]], NumericInterval[I], config.BytesSize && SomeCC[ByteOrder] && config.Bits] with
  def apply(bytes: Seq[NumericInterval[Byte]], conf: config.BytesSize && SomeCC[ByteOrder] && config.Bits): NumericInterval[I] =

    val lowBytes = new ListBuffer[Byte]
    val highBytes = new ListBuffer[Byte]

    val byteIntervals = if (conf.c1.c2.t == ByteOrder.BIG_ENDIAN) bytes else bytes.reverse
    byteIntervals.foreach {
      case NumericInterval.Bounded(lowByte, highByte) =>
        lowBytes += lowByte
        highBytes += highByte
      case NumericInterval.Top() =>
        return NumericInterval.Top()
    }

    val bigEndianConf = conf.c1.c1 && SomeCC(ByteOrder.BIG_ENDIAN, false) && conf.c2
    val low = convert(lowBytes.toSeq, bigEndianConf)
    val high = convert(highBytes.toSeq, bigEndianConf)
    NumericInterval(low, high)
