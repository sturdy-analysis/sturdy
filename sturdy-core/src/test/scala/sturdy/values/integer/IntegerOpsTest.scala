package sturdy.values.integer

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.utils.GenInterval.{*, given}
import sturdy.utils.{Bounded, TestIntervalOps}

import math.Ordering.Implicits.infixOrderingOps
import scala.util.Try

class IntegerOpsTest
  [
    L: Ordering: Arbitrary: Choose: Bounded,
    N
  ]
  (
    makeIntegerOps: => (TestIntervalOps[L, N], IntegerOps[L, N])
  )
  (using
   integral: Integral[L],
   concreteIntegerOps: IntegerOps[L, L])
  extends AnyFunSuite with ScalaCheckPropertyChecks:

  test("integer literal") {
    forAll("n") { (n: L) =>
      val (ivOps, integerOps) = makeIntegerOps
      ivOps.shouldContain(integerOps.integerLit(n), n)
    }
  }

  binOpTest(
    testName = "add",
    precondition = (_, _) => true,
    testFun = _.add(_, _),
    expectedFun = concreteIntegerOps.add
  )

  binOpTest(
    testName = "sub",
    precondition = (_, _) => true,
    testFun = _.sub(_, _),
    expectedFun = concreteIntegerOps.sub
  )

  binOpTest(
    testName = "mul",
    precondition = (_, _) => true,
    testFun = _.mul(_, _),
    expectedFun = concreteIntegerOps.mul
  )

  binOpTest(
    testName = "min",
    precondition = (_, _) => true,
    testFun = _.min(_, _),
    expectedFun = concreteIntegerOps.min
  )

  binOpTest(
    testName = "max",
    precondition = (_, _) => true,
    testFun = _.max(_, _),
    expectedFun = concreteIntegerOps.max
  )

  unOpTest(
    testName = "absolute",
    precondition = _ => true,
    testFun = _.absolute(_),
    expectedFun = concreteIntegerOps.absolute
  )

  binOpTest(
    testName = "div",
    precondition = (_, y) => y != 0,
    testFun = _.div(_, _),
    expectedFun = concreteIntegerOps.div
  )

  test("div([1,1],[-1,1])") {
    val (ivOps,integerOps) = makeIntegerOps
    ivOps.shouldEqual(
      integerOps.div(
        ivOps.interval(integral.fromInt(1), integral.fromInt(1)),
        ivOps.interval(integral.fromInt(-1), integral.fromInt(1))),
      concreteIntegerOps.div(integral.fromInt(1),integral.fromInt(-1)),
      concreteIntegerOps.div(integral.fromInt(1),integral.fromInt(1))
    )
  }

  test("div([-1,1],[-1,-1])") {
    val (ivOps,integerOps) = makeIntegerOps
    ivOps.shouldEqual(
      integerOps.div(
        ivOps.interval(integral.fromInt(-1), integral.fromInt(1)),
        ivOps.interval(integral.fromInt(-1), integral.fromInt(-1))),
      integral.fromInt(-1),integral.fromInt(1)
    )
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

  test("shiftLeft([1,1], [-1,-1])") {
    val (ivOps,integerOps) = makeIntegerOps
    val result = concreteIntegerOps.shiftLeft(integral.fromInt(1),integral.fromInt(-1))
    ivOps.shouldEqual(
      integerOps.shiftLeft(
        ivOps.constant(integral.fromInt(1)),
        ivOps.constant(integral.fromInt(-1))),
      result, result
    )
  }


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
    val (ivOps,integerOps) = makeIntegerOps
    ivOps.shouldEqual(
      integerOps.countLeadingZeros(
        ivOps.interval(integral.fromInt(2), integral.fromInt(4))),
      concreteIntegerOps.countLeadingZeros(integral.fromInt(4)),
      concreteIntegerOps.countLeadingZeros(integral.fromInt(2))
    )
  }

  def binOpTest(testName: String, precondition: (L,L) => Boolean, testFun: (IntegerOps[L,N],N,N) => N, expectedFun: (L,L) => L) = {
    test(testName + " constant") {
      forAll(
        (Gen.chooseNum[L](Bounded[L].minValue, Bounded[L].maxValue), "x"),
        (Gen.chooseNum[L](Bounded[L].minValue, Bounded[L].maxValue), "y")
      ) {
        case (x, y) =>
          whenever(precondition(x, y)) {
            val (ivOps,integerOps) = makeIntegerOps
            ivOps.shouldContain(
              testFun(integerOps, ivOps.constant(x), ivOps.constant(y)),
              expectedFun(x, y)
            )
          }
      }
    }

    test(testName + " intervals") {
      forAll((genInterval[L](Bounded[L].minValue,Bounded[L].maxValue), "x ∈ [x1,x2]"), (genInterval[L](Bounded[L].minValue,Bounded[L].maxValue), "y ∈ [y1,y2]")) {
        case (Interval(x1, x, x2), Interval(y1, y, y2)) =>
          whenever(precondition(x,y)) {
            val (ivOps,integerOps) = makeIntegerOps
            ivOps.shouldContain(
              testFun(integerOps, ivOps.interval(x1, x2), ivOps.interval(y1, y2)),
              expectedFun(x, y)
            )
          }
      }
    }
  }

  def unOpTest(testName: String, precondition: L => Boolean, testFun: (IntegerOps[L,N],N) => N, expectedFun: L => L) = {
    test(testName + " constant") {
      forAll((Gen.chooseNum[L](Bounded[L].minValue,Bounded[L].maxValue), "x")) {
        case x =>
          whenever(precondition(x)) {
            val (ivOps,integerOps) = makeIntegerOps
            val expected = expectedFun(x)
            ivOps.shouldEqual(
              testFun(integerOps, ivOps.constant(x)),
              expected, expected
            )
          }
      }
    }

    test(testName + " interval") {
      forAll((genInterval(Bounded[L].minValue,Bounded[L].maxValue), "x ∈ [x1,x2]")) {
        case Interval(x1, x, x2) =>
          whenever(precondition(x)) {
            val (ivOps,integerOps) = makeIntegerOps
            ivOps.shouldContain(
              testFun(integerOps, ivOps.interval(x1, x2)),
              expectedFun(x)
            )
          }
      }
    }
  }
