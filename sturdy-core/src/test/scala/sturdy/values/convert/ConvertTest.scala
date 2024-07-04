package sturdy.values.convert

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalactic.Fail
import org.scalatest.Assertion
import org.scalatest.enablers.Containing
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.{IsSound, Soundness}
import sturdy.effect.failure.{*, given}
import sturdy.utils.GenInterval.{*, given}
import sturdy.utils.{*, given}
import sturdy.values.Finite
import sturdy.values.config.{AllConfigs, Bits, Overflow, UnsupportedConfiguration}

import scala.util.{Success, Try}


class ConvertTest
  [
    From: Numeric: Choose: Bounded,
    To,
    VFrom,
    VTo,
    Config <: ConvertConfig[_]: AllConfigs
  ]
  (
    makeConvert: => (TestIntervalOps[From, VFrom], TestIntervalOps[To, VTo], Convert[From, To, VFrom, VTo, Config], Soundness[CFallible[To], AFallible[VTo]], CollectedFailures[FailureKind])
  )
  (using
   concreteConvert: Convert[From, To, From, To, Config],
  )
  extends AnyFunSuite with ScalaCheckPropertyChecks:

  val cfailure = ConcreteFailure()

  for(conf <- AllConfigs[Config]) {
    test(s"convert[$conf] constant") {
      forAll((Gen.chooseNum[From](Bounded[From].minValue, Bounded[From].maxValue), "x")) {
        case (x: From) =>
          implicit val (fromIVOps, toIVOps, convertOps, soundness, afailure) = makeConvert
          val actual = afailure.fallible(convertOps(fromIVOps.constant(x), conf))
          val expected = cfailure.fallible(concreteConvert(x, conf))
          assertResult(IsSound.Sound, s"$actual does not overapproximate $expected")(soundness.isSound(expected, actual))
      }
    }

    test(s"convert[$conf] interval") {
      forAll((genInterval(Bounded[From].minValue, Bounded[From].maxValue), "x ∈ [x1,x2]")) {
        case Interval(x1, x, x2) =>
          implicit val (fromIVOps, toIVOps, convertOps, soundness, afailure) = makeConvert
          val actual = afailure.fallible(convertOps(fromIVOps.interval(x1, x2), conf))
          val expected = cfailure.fallible(concreteConvert(x, conf))
          assertResult(IsSound.Sound, s"$actual does not overapproximate $expected")(soundness.isSound(expected, actual))
      }
    }

  }