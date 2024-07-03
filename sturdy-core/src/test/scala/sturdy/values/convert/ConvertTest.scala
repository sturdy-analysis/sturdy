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
import sturdy.values.config.{AllConfigs, UnsupportedConfiguration}

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
    makeConvert: => (TestIntervalOps[From, VFrom], TestIntervalOps[To, VTo], Convert[From, To, VFrom, VTo, Config], Soundness[CFallible[To], AFallible[VTo]])
  )
  (using
   concreteConvert: Convert[From, To, From, To, Config],
  )
  extends AnyFunSuite with ScalaCheckPropertyChecks:

  val cfailure = new ConcreteFailure
  given Finite[FailureKind] with {}

  def catchExceptions[A](failure: Failure, f: => A) =
    try {
      failure.fallible(convertOps(fromIVOps.constant(x), conf))
    } catch {
      case exc: UnsupportedConfiguration[Config]
    }

  for(conf <- AllConfigs[Config]) {
    test(s"convert[$conf] constant") {
      forAll((Gen.chooseNum[From](Bounded[From].minValue, Bounded[From].maxValue), "x")) {
        case (x: From) =>
          implicit val (fromIVOps, toIVOps, convertOps,soundness) = makeConvert
          val afailure = new CollectedFailures[FailureKind]()
          val actual = try { afailure.fallible(convertOps(fromIVOps.constant(x), conf)) } catch { case (exc: UnsupportedConfiguration[Config]) => afailure.fallible(afailure.fail(ConversionFailure, exc.msg)) }
          val expected = try { cfailure.fallible(concreteConvert(x, conf)) } catch { case (exc: UnsupportedConfiguration[Config]) => cfailure.fallible(cfailure.fail(ConversionFailure, exc.msg)) }
          assertResult(IsSound.Sound, s"$actual does not overapproximate $expected")(soundness.isSound(expected, actual))
      }
    }

    test(s"convert[$conf] interval") {
      forAll((genInterval(Bounded[From].minValue, Bounded[From].maxValue), "x ∈ [x1,x2]")) {
        case Interval(x1, x, x2) =>
          implicit val (fromIVOps, toIVOps, convertOps, soundness) = makeConvert
          val afailure = new CollectedFailures[FailureKind]()
          val actual = Try(afailure.fallible(convertOps(fromIVOps.interval(x1, x2), conf)))
          val expected = Try(cfailure.fallible(concreteConvert(x, conf)))
          (actual,expected) match
            case (scala.util.Success(act), scala.util.Success(exp)) => assertResult(IsSound.Sound, s"$actual does not overapproximate $expected")(soundness.isSound(exp, act))
            case (scala.util.Failure(actException), scala.util.Failure(expException)) => assert(actException.getClass == expException.getClass)
            case (_, _) => fail(s"Excpected $expected, but got $actual")
      }
    }
  }