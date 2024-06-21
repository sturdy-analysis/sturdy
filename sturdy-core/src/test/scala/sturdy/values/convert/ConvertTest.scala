package sturdy.values.convert

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

class ConvertTest
  [
    From: Numeric: Choose: Bounded,
    To,
    VFrom,
    VTo,
    Config <: ConvertConfig[_]: Arbitrary
  ]
  (
    makeConvert: => (TestIntervalOps[From, VFrom], TestIntervalOps[To, VTo], Convert[From, To, VFrom, VTo, Config])
  )
  (using
   concreteConvert: Convert[From, To, From, To, Config]
  )
  extends AnyFunSuite with ScalaCheckPropertyChecks:

  test("convert constant") {
    forAll((Gen.chooseNum[From](Bounded[From].minValue, Bounded[From].maxValue), "x"), (summon[Arbitrary[Config]].arbitrary, "conf")) {
      case (x: From, conf: Config) =>
        val (fromIVOps, toIVOps, convertOps) = makeConvert
        val actual = convertOps(fromIVOps.constant(x), conf)
        val expected = concreteConvert(x, conf)
        toIVOps.shouldContain(actual, expected)
    }
  }

  test("convert interval") {
    forAll((genInterval(Bounded[From].minValue, Bounded[From].maxValue), "x ∈ [x1,x2]"), (summon[Arbitrary[Config]].arbitrary, "conf")) {
      case (Interval(x1, x, x2), conf: Config) =>
        val (fromIVOps, toIVOps, convertOps) = makeConvert
        val actual = convertOps(fromIVOps.interval(x1, x2), conf)
        val expected = concreteConvert(x, conf)
        toIVOps.shouldContain(actual, expected)
    }
  }