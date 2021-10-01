package sturdy.test

import org.scalatest._
import apron.Abstract0 //default; for domains without environments
import apron.Box
import apron.Interval
import sturdy.values.ints.IntInterval
import sturdy.values.ints.IntIntervalApron
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import sturdy.values.ints.IntIntervalApronWiden
import sturdy.values.ints.ApronIntervalIntOps
import sturdy.values.ints.IntervalIntOps
import sturdy.fix.Widening
import sturdy.effect.failure.Failure
import sturdy.effect.JoinComputation
import scala.language.postfixOps
import sturdy.values.ints.IntOps

import sturdy.test.IntIntervalApronTests

class ParityIntIntervalIsIntIntervalApronTest extends AnyFreeSpec, Matchers:

  def parityIItoIIA(
      apron_f: ((IntIntervalApron, IntIntervalApron) => IntIntervalApron),
      intInterval_f: ((IntInterval, IntInterval) => IntInterval),
      v1_l: Int,
      v1_h: Int,
      v2_l: Int,
      v2_h: Int
  ) =
    assert(
      IntIntervalApronTests.op2IIA(apron_f, v1_l, v1_h, v2_l, v2_h) === op2IItoIIA(
        intInterval_f,
        v1_l,
        v1_h,
        v2_l,
        v2_h
      )
    )

  def op2IItoIIA(
      f: (IntInterval, IntInterval) => IntInterval,
      l0: Int,
      l1: Int,
      r0: Int,
      r1: Int
  ): IntIntervalApron = {
    IntIntervalApron(
      f(IntInterval.bounded(l0, l1), IntInterval.bounded(r0, r1))
    )
  }

  "Functions of IntIntervalApron must behave like those of IntInterval" - {
    "addition" in {
      parityIItoIIA(ApronIntervalIntOps.add, IntervalIntOps.add, 0,1, -1,0) // compare result of the addition of aproninterval (0,1) and aproninterval (-1,0) with the result of intinterval
    }
    "substraction" in {
      parityIItoIIA(ApronIntervalIntOps.sub, IntervalIntOps.sub, 0,1, -1,0)
    }
    "multiplication" in {
      parityIItoIIA(ApronIntervalIntOps.mul, IntervalIntOps.mul, 0,1, -1,0) // compare result of the addition of aproninterval (0,1) and aproninterval (-1,0) with the result of intinterval
    }
    "division" in {
      parityIItoIIA(ApronIntervalIntOps.div, IntervalIntOps.div, 0,1, -1,0)
    }
    "join" in {
      parityIItoIIA((ApronInterval.joinCopy, (IntInterval.join), 0,1, -1,0) // compare result of the addition of aproninterval (0,1) and aproninterval (-1,0) with the result of intinterval
    }
    "meet" in {
      parityIItoIIA(ApronIntervalIntOps.meet, IntervalIntOps.meet, 0,1, -1,0)
    }
    "widen" in {
      parityIItoIIA(ApronIntervalIntOps.widen, IntervalIntOps.widen, 0,1, -1,0) // compare result of the addition of aproninterval (0,1) and aproninterval (-1,0) with the result of intinterval
    }
  }

   /*
  implicit val f: Failure = fail() // TODO: new AFailure {}
  implicit val j: JoinComputation = new JoinComputation {} 
  */
