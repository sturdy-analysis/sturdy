package sturdy.values.integer

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.utils.GenInterval.{*, given}

import math.Ordering.Implicits.infixOrderingOps


trait IntervalIntegerOps[L,N] extends IntegerOps[L,N]:
  def integerLit(i: L): N
  def interval(low: L, high: L): N
  def getBounds(n:N): (L,L)

class IntegerOpsTest
  [
    L: Ordering: Arbitrary: Choose,
    N
  ]
  (
    minValue: L,
    maxValue: L,
    makeIntegerOps: => IntervalIntegerOps[L, N]
  )
  (using
   integral: Integral[L],
   concreteIntegerOps: IntegerOps[L, L])
  extends AnyFunSuite with ScalaCheckPropertyChecks:


  test("integer literal") {
    forAll("n") { (n: L) =>
      val integerOps = makeIntegerOps
      integerOps.getBounds(integerOps.integerLit(n)) shouldBe (n,n)
    }

  }

  binOpTest(
    testName = "add",
    precondition = (_, _) => true,
    testFun = _.add(_, _),
    expectedFun = concreteIntegerOps.add(_, _)
  )

  binOpTest(
    testName = "sub",
    precondition = (_, _) => true,
    testFun = _.sub(_, _),
    expectedFun = concreteIntegerOps.sub(_, _)
  )

  binOpTest(
    testName = "mul",
    precondition = (_, _) => true,
    testFun = _.mul(_, _),
    expectedFun = concreteIntegerOps.mul(_, _)
  )

  binOpTest(
    testName = "min",
    precondition = (_, _) => true,
    testFun = _.min(_, _),
    expectedFun = concreteIntegerOps.min(_, _)
  )

  test("min([0,0],[-1,0])") {
      val integerOps = makeIntegerOps
      integerOps.getBounds(
        integerOps.min(
          integerOps.interval(integral.fromInt(0), integral.fromInt(0)),
          integerOps.interval(integral.fromInt(-1), integral.fromInt(0)))) shouldBe
        (-1,0)
  }

  binOpTest(
    testName = "max",
    precondition = (_, _) => true,
    testFun = _.max(_, _),
    expectedFun = concreteIntegerOps.max(_, _)
  )

  unOpTest(
    testName = "absolute",
    precondition = _ => true,
    testFun = _.absolute(_),
    expectedFun = concreteIntegerOps.absolute(_)
  )

  binOpTest(
    testName = "div",
    precondition = (_, y) => y != 0,
    testFun = _.div(_, _),
    expectedFun = concreteIntegerOps.div(_, _)
  )

  test("divide by zero") {
    val integerOps = makeIntegerOps
    integerOps.getBounds(
      integerOps.div(
        integerOps.interval(integral.fromInt(1), integral.fromInt(1)),
        integerOps.interval(integral.fromInt(-1), integral.fromInt(1)))) should
      contain(
        concreteIntegerOps.div(integral.fromInt(1),integral.fromInt(-1)),
        concreteIntegerOps.div(integral.fromInt(1),integral.fromInt(1)))
  }

  test("divide [-1,1] / [-1,-1]") {
    val integerOps = makeIntegerOps
    integerOps.getBounds(
      integerOps.div(
        integerOps.interval(integral.fromInt(-1), integral.fromInt(1)),
        integerOps.interval(integral.fromInt(-1), integral.fromInt(-1)))) shouldBe
      (integral.fromInt(-1),integral.fromInt(1))
  }

  binOpTest(
    testName = "divUnsigned",
    precondition = (_, y) => y != 0,
    testFun = _.divUnsigned(_, _),
    expectedFun = concreteIntegerOps.divUnsigned(_, _)
  )

  binOpTest(
    testName = "remainder",
    precondition = (_, y) => y != 0,
    testFun = _.remainder(_, _),
    expectedFun = concreteIntegerOps.remainder(_, _)
  )

  binOpTest(
    testName = "remainderUnsigned",
    precondition = (_, y) => y != 0,
    testFun = _.remainderUnsigned(_, _),
    expectedFun = concreteIntegerOps.remainderUnsigned(_, _)
  )

  binOpTest(
    testName = "modulo",
    precondition = (_, y) => y != 0,
    testFun = _.modulo(_, _),
    expectedFun = concreteIntegerOps.modulo(_, _)
  )

  binOpTest(
    testName = "shiftLeft",
    precondition = (_, _) => true,
    testFun = _.shiftLeft(_, _),
    expectedFun = concreteIntegerOps.shiftLeft(_, _)
  )

  binOpTest(
    testName = "shiftRight",
    precondition = (_, _) => true,
    testFun = _.shiftRight(_, _),
    expectedFun = concreteIntegerOps.shiftRight(_, _)
  )

  binOpTest(
    testName = "shiftRightUnsigned",
    precondition = (_, _) => true,
    testFun = _.shiftRightUnsigned(_,_),
    expectedFun = concreteIntegerOps.shiftRightUnsigned(_,_)
  )

  unOpTest(
    testName = "countLeadingZeros",
    precondition = _ => true,
    testFun = _.countLeadingZeros(_),
    expectedFun = concreteIntegerOps.countLeadingZeros(_)
  )

  test("countLeadingZeros([1,4])") {
    val integerOps = makeIntegerOps
    integerOps.getBounds(
      integerOps.countLeadingZeros(
        integerOps.interval(integral.fromInt(2), integral.fromInt(4)))) shouldBe
      (concreteIntegerOps.countLeadingZeros(integral.fromInt(4)), concreteIntegerOps.countLeadingZeros(integral.fromInt(2)))
  }

  def binOpTest(testName: String, precondition: (L,L) => Boolean, testFun: (IntegerOps[L,N],N,N) => N, expectedFun: (L,L) => L) =
    test(testName) {
      forAll((genInterval[L](minValue,maxValue), "x ∈ [x1,x2]"), (genInterval[L](minValue,maxValue), "y ∈ [y1,y2]")) {
        case (Interval(x1, x, x2), Interval(y1, y, y2)) =>
          whenever(precondition(x,y)) {
            val integerOps = makeIntegerOps
            integerOps.getBounds(testFun(integerOps, integerOps.interval(x1, x2), integerOps.interval(y1, y2))) should contain(expectedFun(x, y))
          }
      }
    }

  def unOpTest(testName: String, precondition: L => Boolean, testFun: (IntegerOps[L,N],N) => N, expectedFun: L => L) =
    test(testName) {
      forAll((genInterval(minValue,maxValue), "x ∈ [x1,x2]")) {
        case Interval(x1, x, x2) =>
          whenever(precondition(x)) {
            val integerOps = makeIntegerOps
            integerOps.getBounds(testFun(integerOps, integerOps.interval(x1, x2))) should contain(expectedFun(x))
          }
      }
    }
  def contain(expected: L): Matcher[(L,L)] =
    contain(expected,expected)

  def contain(expected_low: L, expected_high: L): Matcher[(L, L)] =
    (actual: (L, L)) =>
      MatchResult(
        actual._1 <= expected_low && expected_high <= actual._2,
        s"interval $actual does not contain ${(expected_low,expected_high)}",
        s"interval $actual contains ${(expected_low,expected_high)}"
      )