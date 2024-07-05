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
    makeFloatOps: => (TestIntervalOps[L,N], FloatOps[L, N], Soundness[L, N])
  )
  (using
   ord: Ordering[L],
   fractional: Fractional[L],
   concreteFloatOps: FloatOps[L, L])
  extends AnyFunSuite with ScalaCheckPropertyChecks:

  val minValue = Bounded[L].minValue
  val maxValue = Bounded[L].maxValue

  test("Float literal") {
    forAll("n") { (n: L) =>
      implicit val (ivOps, floatOps, soundness) = makeFloatOps
//      given Equality[N] = implicitly
      floatOps.floatingLit(n) should equal((n,n))
    }

  }

  binOpTest(
    testName = "add",
    precondition = (_, _) => true,
    testFun = _.add(_, _),
    expectedFun = concreteFloatOps.add
  )

  binOpTest(
    testName = "sub",
    precondition = (_, _) => true,
    testFun = _.sub(_, _),
    expectedFun = concreteFloatOps.sub
  )

  binOpTest(
    testName = "mul",
    precondition = (_, _) => true,
    testFun = _.mul(_, _),
    expectedFun = concreteFloatOps.mul
  )

  binOpTest(
    testName = "div",
    precondition = (_, y) => y != 0,
    testFun = _.div(_, _),
    expectedFun = concreteFloatOps.div
  )

  binOpTest(
    testName = "min",
    precondition = (_, _) => true,
    testFun = _.min(_, _),
    expectedFun = concreteFloatOps.min
  )

  binOpTest(
    testName = "max",
    precondition = (_, _) => true,
    testFun = _.max(_, _),
    expectedFun = concreteFloatOps.max
  )

  unOpTest(
    testName = "absolute",
    precondition = _ => true,
    testFun = _.absolute(_),
    expectedFun = concreteFloatOps.absolute
  )

  unOpTest(
    testName = "negated",
    precondition = _ => true,
    testFun = _.negated(_),
    expectedFun = concreteFloatOps.negated
  )

  ignore("sqrt crashes JVM") {
    unOpTest(
      testName = "sqrt",
      precondition = _ => true,
      testFun = _.sqrt(_),
      expectedFun = concreteFloatOps.sqrt
    )
  }

  unOpTest(
    testName = "ceil",
    precondition = _ => true,
    testFun = _.ceil(_),
    expectedFun = concreteFloatOps.ceil
  )

  unOpTest(
    testName = "floor",
    precondition = _ => true,
    testFun = _.floor(_),
    expectedFun = concreteFloatOps.floor
  )

  unOpTest(
    testName = "truncate",
    precondition = _ => true,
    testFun = _.truncate(_),
    expectedFun = concreteFloatOps.truncate
  )

  unOpTest(
    testName = "nearest",
    precondition = _ => true,
    testFun = _.nearest(_),
    expectedFun = concreteFloatOps.nearest
  )

  binOpTest(
    testName = "copysign",
    precondition = (_,_) => true,
    testFun = _.copysign(_,_),
    expectedFun = concreteFloatOps.copysign
  )

  def binOpTest(testName: String, precondition: (L,L) => Boolean, testFun: (FloatOps[L,N],N,N) => N, expectedFun: (L,L) => L): Unit =
    test(testName + " constant") {
      forAll((Gen.chooseNum[L](minValue, maxValue), "x"), (Gen.chooseNum[L](minValue, maxValue), "y")) {
        case (x, y) =>
          whenever(precondition(x, y)) {
            implicit val (ivOps,floatOps,soundness) = makeFloatOps
            val expected = expectedFun(x, y)
            val actual = testFun(floatOps, ivOps.constant(x), ivOps.constant(y))
            assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
          }
      }
    }

    test(testName + " intervals") {
      forAll((genInterval[L](minValue,maxValue), "x ∈ [x1,x2]"), (genInterval[L](minValue,maxValue), "y ∈ [y1,y2]")) {
        case (Interval(x1, x, x2), Interval(y1, y, y2)) =>
          whenever(precondition(x,y)) {
            implicit val (ivOps,floatOps,soundness) = makeFloatOps
            val expected = expectedFun(x, y)
            val actual = testFun(floatOps, ivOps.interval(x1, x2), ivOps.interval(y1, y2))
            assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
          }
      }
    }

  def unOpTest(testName: String, precondition: L => Boolean, testFun: (FloatOps[L,N],N) => N, expectedFun: L => L) =
    test(testName + " constant") {
      forAll((Gen.chooseNum[L](minValue,maxValue), "x")) {
        case x =>
          whenever(precondition(x)) {
            implicit val (ivOps,floatOps,soundness) = makeFloatOps
            val expected = expectedFun(x)
            val actual = testFun(floatOps, ivOps.constant(x))
            assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
          }
      }
    }

    test(testName + " interval") {
      forAll((genInterval(minValue, maxValue), "x ∈ [x1,x2]")) {
        case Interval(x1, x, x2) =>
          whenever(precondition(x)) {
            implicit val (ivOps,floatOps,soundness) = makeFloatOps
            val expected = expectedFun(x)
            val actual = testFun(floatOps, ivOps.interval(x1, x2))
            assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
          }
      }
    }
