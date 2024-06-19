package sturdy.values.floating

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.utils.GenInterval.{*, given}

import math.Ordering.Implicits.infixOrderingOps


trait TestingFloatOps[L,N] extends FloatOps[L,N]:
  def floatLit(i: L): N
  def interval(low: L, high: L): N
  def shouldContain(n:N, m:L): Assertion
  def shouldEqual(n:N, l:L, u:L): Assertion

class FloatOpsTest
  [
    L: Arbitrary: Choose,
    N
  ]
  (
    minValue: L,
    maxValue: L,
    makeFloatOps: => TestingFloatOps[L, N]
  )
  (using
   ord: Ordering[L],
   fractional: Fractional[L],
   concreteFloatOps: FloatOps[L, L])
  extends AnyFunSuite with ScalaCheckPropertyChecks:

  def newFloatOps: TestingFloatOps[L, N] = makeFloatOps

  test("Float literal") {
    forAll("n") { (n: L) =>
      val floatOps = makeFloatOps
      floatOps.shouldEqual(floatOps.floatLit(n), n, n)
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

  unOpTest(
    testName = "sqrt",
    precondition = _ => true,
    testFun = _.sqrt(_),
    expectedFun = concreteFloatOps.sqrt
  )

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
            val floatOps = makeFloatOps
            floatOps.shouldContain(
              testFun(floatOps, floatOps.floatLit(x), floatOps.floatLit(y)),
              expectedFun(x, y)
            )
          }
      }
    }

    test(testName + " intervals") {
      forAll((genInterval[L](minValue,maxValue), "x ∈ [x1,x2]"), (genInterval[L](minValue,maxValue), "y ∈ [y1,y2]")) {
        case (Interval(x1, x, x2), Interval(y1, y, y2)) =>
          whenever(precondition(x,y)) {
            val floatOps = makeFloatOps
            floatOps.shouldContain(
              testFun(floatOps, floatOps.interval(x1, x2), floatOps.interval(y1, y2)),
              expectedFun(x, y)
            )
          }
      }
    }

  def unOpTest(testName: String, precondition: L => Boolean, testFun: (FloatOps[L,N],N) => N, expectedFun: L => L) =
    test(testName + " constant") {
      forAll((Gen.chooseNum[L](minValue,maxValue), "x")) {
        case x =>
          whenever(precondition(x)) {
            val floatOps = makeFloatOps
            val expected = expectedFun(x)
            floatOps.shouldContain(
              testFun(floatOps, floatOps.floatLit(x)),
              expected
            )
          }
      }
    }

    test(testName + " interval") {
      forAll((genInterval(minValue, maxValue), "x ∈ [x1,x2]")) {
        case Interval(x1, x, x2) =>
          whenever(precondition(x)) {
            val floatOps = makeFloatOps
            floatOps.shouldContain(
              testFun(floatOps, floatOps.interval(x1, x2)),
              expectedFun(x)
            )
          }
      }
    }

  def contain(expected: L): Matcher[(L,L)] =
    contain(expected,expected)

  def contain(expected_low: L, expected_high: L): Matcher[(L, L)] =
    (actual: (L, L)) =>
      MatchResult(
        ord.lteq(actual._1, expected_low) && ord.lteq(expected_high, actual._2),
        s"interval $actual does not contain ${(expected_low,expected_high)}",
        s"interval $actual contains ${(expected_low,expected_high)}"
      )
