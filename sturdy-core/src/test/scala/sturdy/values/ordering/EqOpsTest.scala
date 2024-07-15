package sturdy.values.ordering

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.util.GenInterval.{*, given}
import sturdy.util.{Bounded, IsInterval}
import sturdy.values.integer.IntegerOps

trait IntervalEqOps[L,V,B] extends EqOps[V,B]:
  def getBool(b: B): Topped[Boolean]

class EqOpsTest
  [
    L: Integral: Choose: Arbitrary: Ordering: Structural: Bounded,
    V,
    B
  ]
  (
    minValue: L,
    maxValue: L,
    makeEqOps: => IntervalEqOps[L,V, B]
  )
  (using
   ivOps: IsInterval[L, V],
   concreteEqOps: EqOps[L,Boolean]
  )
    extends AnyFunSuite with ScalaCheckPropertyChecks:

  binOpTest(
    testName = "equ",
    testFun = _.equ(_,_),
    expectedFun = concreteEqOps.equ(_,_)
  )

  binOpTest(
    testName = "neq",
    testFun = _.neq(_, _),
    expectedFun = concreteEqOps.neq(_, _)
  )

  def binOpTest(testName: String, testFun: (IntervalEqOps[L,V,B],V,V) => B, expectedFun: (L,L) => Boolean) =
    test(testName + " constant") {
      forAll((Gen.chooseNum[L](Bounded[L].minValue,Bounded[L].maxValue), "x"), (Gen.chooseNum[L](Bounded[L].minValue,Bounded[L].maxValue), "y")) {
        case (x, y) =>
          val eqOps = makeEqOps
          assertResult(IsSound.Sound)(Soundness.isSound(
            expectedFun(x, y),
            eqOps.getBool(testFun(eqOps, ivOps.constant(x), ivOps.constant(y)))
          ))
      }
    }

    test(testName + " interval") {
      forAll((genInterval[L](minValue,maxValue), "x ∈ [x1,x2]"), (genInterval[L](minValue,maxValue), "y ∈ [y1,y2]")) {
        case (Interval(x1, x, x2), Interval(y1, y, y2)) =>
          val eqOps = makeEqOps
          assertResult(IsSound.Sound)(Soundness.isSound(
            expectedFun(x, y),
            eqOps.getBool(testFun(eqOps, ivOps.interval(x1, x2), ivOps.interval(y1, y2)))
          ))
      }
    }

  test("1 == 1") {
    val eqOps = makeEqOps
    eqOps.getBool(eqOps.equ(ivOps.constant(Integral[L].fromInt(1)), ivOps.constant(Integral[L].fromInt(1)))) shouldBe
      Topped.Actual(true)
  }

  test("1 == 2") {
    val eqOps = makeEqOps
    eqOps.getBool(eqOps.equ(ivOps.constant(Integral[L].fromInt(1)), ivOps.constant(Integral[L].fromInt(2)))) shouldBe
      Topped.Actual(false)
  }

  test("1 != 1") {
    val eqOps = makeEqOps
    eqOps.getBool(eqOps.neq(ivOps.constant(Integral[L].fromInt(1)), ivOps.constant(Integral[L].fromInt(1)))) shouldBe
      Topped.Actual(false)
  }

  test("1 != 2") {
    val eqOps = makeEqOps
    eqOps.getBool(eqOps.neq(ivOps.constant(Integral[L].fromInt(1)), ivOps.constant(Integral[L].fromInt(2)))) shouldBe
      Topped.Actual(true)
  }