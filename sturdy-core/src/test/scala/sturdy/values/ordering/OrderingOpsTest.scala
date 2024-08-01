package sturdy.values.ordering

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.util.{Bounded, IsInterval}
import sturdy.values.{Structural, Topped}
import sturdy.util.GenInterval.{*, given}
import sturdy.values.integer.IntegerOps

trait TestingOrderingOps[L,V,B] extends OrderingOps[V,B]:
  def getBool(b: B): Topped[Boolean]

class OrderingOpsTest
  [
    L: Numeric: Choose: Arbitrary: Structural: Bounded,
    V,
    B
  ]
  (
    makeOrderingOps: => TestingOrderingOps[L,V, B]
  )
  (using
     ivOps: IsInterval[L,V],
     concreteOrderingOps: OrderingOps[L,Boolean]
  )
    extends AnyFunSuite with ScalaCheckPropertyChecks:

  binOpTest(
    testName = "lt",
    precondition = (_,_) => true,
    testFun = _.lt(_,_),
    expectedFun = concreteOrderingOps.lt(_,_)
  )

  binOpTest(
    testName = "le",
    precondition = (_, _) => true,
    testFun = _.le(_, _),
    expectedFun = concreteOrderingOps.le(_, _)
  )

  binOpTest(
    testName = "gt",
    precondition = (_, _) => true,
    testFun = _.gt(_, _),
    expectedFun = concreteOrderingOps.gt(_, _)
  )

  binOpTest(
    testName = "ge",
    precondition = (_, _) => true,
    testFun = _.ge(_, _),
    expectedFun = concreteOrderingOps.ge(_, _)
  )

  def binOpTest(testName: String, precondition: (L,L) => Boolean, testFun: (TestingOrderingOps[L,V,B],V,V) => B, expectedFun: (L,L) => Boolean) =
    test(testName) {
      forAll((genInterval[L](Bounded[L].minValue,Bounded[L].maxValue), "x ∈ [x1,x2]"), (genInterval[L](Bounded[L].minValue,Bounded[L].maxValue), "y ∈ [y1,y2]")) {
        case (Interval(x1, x, x2, xSpecials), Interval(y1, y, y2, ySpecials)) =>
          whenever(precondition(x,y)) {
            val orderingOps = makeOrderingOps
            orderingOps.getBool(testFun(orderingOps, ivOps.interval(x1, x2, xSpecials), ivOps.interval(y1, y2, ySpecials))) should contain(expectedFun(x, y))
          }
      }
    }

  def contain(expected: Boolean): Matcher[Topped[Boolean]] =
    (actual: Topped[Boolean]) =>
      MatchResult(
        actual == Topped.Top || actual == Topped.Actual(expected),
        s"interval $actual does not contain ${expected}",
        s"interval $actual contains ${expected}"
      )