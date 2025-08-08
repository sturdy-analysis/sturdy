package sturdy.values.floating

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalactic.Equality
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.{IsSound, Soundness}
import sturdy.util.GenInterval.{*, given}
import sturdy.util.{*, given}

import math.Ordering.Implicits.infixOrderingOps


class FloatOpsTest
  [
    L: Arbitrary: Choose: Bounded,
    N
  ]
  (
    specials: Seq[L],
    makeFloatOps: => (IsInterval[L,N], FloatOps[L, N], Soundness[L, N])
  )
  (using
     ord: Ordering[L],
     fractional: Fractional[L],
     concreteFloatOps: FloatOps[L, L]
  )
  extends AnyFunSuite with ScalaCheckPropertyChecks:

  val minValue = Bounded[L].minValue
  val maxValue = Bounded[L].maxValue

  test("Float literal") {
    forAll("n") { (n: L) =>
      implicit val (ivOps, floatOps, soundness) = makeFloatOps
      assertResult(IsSound.Sound)(soundness.isSound(concreteFloatOps.floatingLit(n), floatOps.floatingLit(n)))
    }
  }

  binOpTest(
    testName = "add",
    testFun = _.add(_, _),
    expectedFun = concreteFloatOps.add
  )

  binOpTest(
    testName = "sub",
    testFun = _.sub(_, _),
    expectedFun = concreteFloatOps.sub
  )

  binOpTest(
    testName = "mul",
    testFun = _.mul(_, _),
    expectedFun = concreteFloatOps.mul
  )

  binOpTest(
    testName = "div",
    testFun = _.div(_, _),
    expectedFun = concreteFloatOps.div
  )

  binOpTest(
    testName = "min",
    testFun = _.min(_, _),
    expectedFun = concreteFloatOps.min
  )

  binOpTest(
    testName = "max",
    testFun = _.max(_, _),
    expectedFun = concreteFloatOps.max
  )

  unOpTest(
    testName = "absolute",
    testFun = _.absolute(_),
    expectedFun = concreteFloatOps.absolute
  )

  unOpTest(
    testName = "negated",
    testFun = _.negated(_),
    expectedFun = concreteFloatOps.negated
  )

  unOpTest(
    testName = "sqrt",
    testFun = _.sqrt(_),
    expectedFun = concreteFloatOps.sqrt
  )


  unOpTest(
    testName = "ceil",
    testFun = _.ceil(_),
    expectedFun = concreteFloatOps.ceil
  )

  unOpTest(
    testName = "floor",
    testFun = _.floor(_),
    expectedFun = concreteFloatOps.floor
  )

  unOpTest(
    testName = "truncate",
    testFun = _.truncate(_),
    expectedFun = concreteFloatOps.truncate
  )

  unOpTest(
    testName = "nearest",
    testFun = _.nearest(_),
    expectedFun = concreteFloatOps.nearest
  )

  binOpTest(
    testName = "copysign",
    testFun = _.copysign(_,_),
    expectedFun = concreteFloatOps.copysign
  )

  def binOpTest(testName: String, testFun: (FloatOps[L,N],N,N) => N, expectedFun: (L,L) => L): Unit =
    test(testName + " constant") {
      forAll((genConstant[L](minValue, maxValue, specials*), "x"), (genConstant[L](minValue, maxValue, specials*), "y")) {
        case (x, y) =>
          implicit val (ivOps, floatOps,soundness) = makeFloatOps
          val expected = expectedFun(x, y)
          val actual = testFun(floatOps, ivOps.constant(x), ivOps.constant(y))
          assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
      }
    }

    test(testName + " intervals") {
      forAll((genInterval[L](minValue,maxValue, specials*), "x"), (genInterval[L](minValue,maxValue, specials*), "y")) {
        case (Interval(x1, x, x2, xSpecials), Interval(y1, y, y2, ySpecials)) =>
          implicit val (ivOps, floatOps,soundness) = makeFloatOps
          val expected = expectedFun(x, y)
          val actual = testFun(floatOps, ivOps.interval(x1, x2, xSpecials), ivOps.interval(y1, y2, ySpecials))
          assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
      }
    }

  def unOpTest(testName: String, testFun: (FloatOps[L,N],N) => N, expectedFun: L => L) =
    test(testName + " constant") {
      forAll((genConstant[L](minValue,maxValue,specials*), "x")) {
        case x =>
          implicit val (ivOps, floatOps,soundness) = makeFloatOps
          val expected = expectedFun(x)
          val actual = testFun(floatOps, ivOps.constant(x))
          assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
      }
    }

    test(testName + " interval") {
      forAll((genInterval(minValue, maxValue,specials*), "x")) {
        case Interval(x1, x, x2, xSpecials) =>
          implicit val (ivOps, floatOps,soundness) = makeFloatOps
          val expected = expectedFun(x)
          val actual = testFun(floatOps, ivOps.interval(x1, x2, xSpecials))
          assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
      }
    }
