package sturdy.values.boolean

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.util.GenBoolean.{*,given}
import sturdy.values.{Structural, Topped}
import sturdy.values.booleans.{BooleanBranching, BooleanOps}
import sturdy.values.integer.IntegerOps

trait TestingBooleanOps[B] extends BooleanOps[B]:
  def toBool(b: Topped[Boolean]): B
  def fromBool(b: B): Topped[Boolean]

class BooleanOpsTest
  [
    B: Integral: Choose: Arbitrary: Ordering,
  ]
  (
    makeTestingBoolOps: => TestingBooleanOps[B]
  )
  (using
   concreteBooleanOps: BooleanOps[Boolean]
  )
    extends AnyFunSuite with ScalaCheckPropertyChecks:

  binOpTest(
    testName = "and",
    testFun = _.and(_,_),
    expectedFun = concreteBooleanOps.and(_,_)
  )

  binOpTest(
    testName = "or",
    testFun = _.or(_, _),
    expectedFun = concreteBooleanOps.or(_, _)
  )

  def binOpTest(testName: String, testFun: (TestingBooleanOps[B],B,B) => B, expectedFun: (Boolean,Boolean) => Boolean) =
    test(testName) {
      forAll((genBoolean, "x ∈ x^"), (genBoolean, "y ∈ y^")) {
        case (BooleanVal(xCon, xAbs), BooleanVal(yCon, yAbs)) =>
          val booleanOps = makeTestingBoolOps
          booleanOps.fromBool(testFun(booleanOps, booleanOps.toBool(xAbs), booleanOps.toBool(yAbs))) should
            contain(expectedFun(xCon,yCon))
      }
    }

  def contain(expected: Boolean): Matcher[Topped[Boolean]] =
    (actual: Topped[Boolean]) =>
      MatchResult(
        actual == Topped.Top || actual == Topped.Actual(expected),
        s"interval $actual does not contain ${expected}",
        s"interval $actual contains ${expected}"
      )