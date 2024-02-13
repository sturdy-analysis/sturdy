package sturdy.values.ordering

import org.scalacheck.{Gen, Shrink}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.values.Topped
import sturdy.utils.GenInterval.{*, given}
import sturdy.values.integer.IntegerOps

trait IntervalOrderingOps[V,B] extends OrderingOps[V,B]:
  def integerLit(i: Int): V
  def interval(low: Int, high: Int): V
  def getBool(b: B): Topped[Boolean]

class OrderingOpsTest[V,B](size: Int,
                           makeOrderingOps: => IntervalOrderingOps[V, B])
    extends AnyFunSuite with ScalaCheckPropertyChecks:

  binOpTest(
    testName = "lt",
    precondition = (_,_) => true,
    testFun = _.lt(_,_),
    expectedFun = ConcreteOrderingOps.lt(_,_)
  )

  binOpTest(
    testName = "le",
    precondition = (_, _) => true,
    testFun = _.le(_, _),
    expectedFun = ConcreteOrderingOps.le(_, _)
  )

  binOpTest(
    testName = "gt",
    precondition = (_, _) => true,
    testFun = _.gt(_, _),
    expectedFun = ConcreteOrderingOps.gt(_, _)
  )

  binOpTest(
    testName = "ge",
    precondition = (_, _) => true,
    testFun = _.ge(_, _),
    expectedFun = ConcreteOrderingOps.ge(_, _)
  )

  def binOpTest(testName: String, precondition: (Int,Int) => Boolean, testFun: (OrderingOps[V,B],V,V) => B, expectedFun: (Int,Int) => Boolean) =
    test(testName) {
      forAll((genInterval(size), "x ∈ [x1,x2]"), (genInterval(size), "y ∈ [y1,y2]")) {
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