package sturdy.values.ordering

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.values.{Structural, Topped}
import sturdy.util.GenInterval.{*, given}
import sturdy.values.integer.IntegerOps

trait TestingOrderingOps[L,V,B] extends OrderingOps[V,B]:
  def integerLit(i: L): V
  def interval(low: L, high: L): V
  def getBool(b: B): Topped[Boolean]

class OrderingOpsTest
  [
    L: Integral: Choose: Arbitrary: Ordering: Structural,
    V,
    B
  ]
  (
    minValue: L,
    maxValue: L,
    makeOrderingOps: => TestingOrderingOps[L,V, B]
  )
  (using
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
      forAll((genInterval[L](minValue,maxValue), "x ∈ [x1,x2]"), (genInterval[L](minValue,maxValue), "y ∈ [y1,y2]")) {
        case (Interval(x1, x, x2), Interval(y1, y, y2)) =>
          whenever(precondition(x,y)) {
            val orderingOps = makeOrderingOps
            orderingOps.getBool(testFun(orderingOps, orderingOps.interval(x1, x2), orderingOps.interval(y1, y2))) should contain(expectedFun(x, y))
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