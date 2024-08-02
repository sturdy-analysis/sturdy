package sturdy.values.convert

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalactic.Fail
import org.scalatest.Assertion
import org.scalatest.enablers.Containing
import org.scalatest.events.TestFailed
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.{IsSound, Soundness}
import sturdy.effect.failure.{*, given}
import sturdy.util.{Bounded, IsInterval, *, given}
import sturdy.util.GenInterval.{*, given}
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
    specials: Seq[From],
    makeConvert: => (IsInterval[From, VFrom], IsInterval[To, VTo], Convert[From, To, VFrom, VTo, Config], Soundness[CFallible[To], AFallible[VTo]], CollectedFailures[FailureKind])
  )
  (using
    concreteConvert: Convert[From, To, From, To, Config],
  )
  extends AnyFunSuite with ScalaCheckPropertyChecks:

  val cfailure = ConcreteFailure()

  def _makeConvert: (IsInterval[From, VFrom], IsInterval[To, VTo], Convert[From, To, VFrom, VTo, Config], Soundness[CFallible[To], AFallible[VTo]], CollectedFailures[FailureKind]) = makeConvert

  for(conf <- AllConfigs[Config]) {
    test(s"convert[$conf] constant") {
      forAll((genConstant[From](Bounded[From].minValue, Bounded[From].maxValue, specials*), "x")) {
        case (x: From) =>
          implicit val (fromIvOps, toIvOps, convertOps, soundness, afailure) = makeConvert
          val actual = afailure.fallible(convertOps(fromIvOps.constant(x), conf))
          val expected = cfailure.fallible(concreteConvert(x, conf))
          assertResult(IsSound.Sound, s"$actual does not overapproximate $expected")(soundness.isSound(expected, actual))
      }
    }

    test(s"convert[$conf] interval") {
      forAll((genInterval(Bounded[From].minValue, Bounded[From].maxValue, specials*), "x ∈ [x1,x2]")) {
        case Interval(x1, x, x2, xSpecials) =>
          implicit val (fromIvOps, toIvOps, convertOps, soundness, afailure) = makeConvert
          val actual = afailure.fallible(convertOps(fromIvOps.interval(x1, x2, xSpecials), conf))
          val expected = cfailure.fallible(concreteConvert(x, conf))
          assertResult(IsSound.Sound, s"$actual does not overapproximate $expected")(soundness.isSound(expected, actual))
      }
    }

  }