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
import sturdy.values.ints.IntIntervalWiden
import sturdy.fix.Widening
import sturdy.effect.failure.Failure
import sturdy.effect.JoinComputation
import scala.language.postfixOps
import sturdy.values.ints.IntOps
import sturdy.values.ints.given
import sturdy.fix.finiteJoinWidening
import sturdy.values.JoinValue
import sturdy.values.ints.IntIntervalApron
import sturdy.values.Finite

class ParityIntIntervalIsIntIntervalApronTests extends AnyFreeSpec, Matchers:
  def op2IIA(
      f: (IntIntervalApron, IntIntervalApron) => IntIntervalApron,
      l0: Int,
      l1: Int,
      r0: Int,
      r1: Int
  ): IntIntervalApron = {
    f(IntIntervalApron.bounded(l0, l1), IntIntervalApron.bounded(r0, r1))
  }

  def op2II(
      f: (IntInterval, IntInterval) => IntInterval,
      l0: Int,
      l1: Int,
      r0: Int,
      r1: Int
  ): IntInterval = {
    f(IntInterval.bounded(l0, l1), IntInterval.bounded(r0, r1))
  }

  def op2IItoIIA(
      f: (IntInterval, IntInterval) => IntInterval,
      l0: Int,
      l1: Int,
      r0: Int,
      r1: Int
  ): IntIntervalApron = {
    IntIntervalApron(
      op2II(f, l0, l1, r0, r1)
    )
  }

  def parityIItoIIA( // compare results of IntIntervalApron and IntInterval with same initialized values converted to each other
      apronIntInterval_f: (
          (
              IntIntervalApron,
              IntIntervalApron
          ) => IntIntervalApron
      ),
      intInterval_f: ((IntInterval, IntInterval) => IntInterval),
      v1_l: Int,
      v1_h: Int,
      v2_l: Int,
      v2_h: Int
  ) = {
    val testIIA = op2IIA(apronIntInterval_f, v1_l, v1_h, v2_l, v2_h)
    val testII = op2II(intInterval_f, v1_l, v1_h, v2_l, v2_h)
    assert(testIIA === IntIntervalApron(testII))
    assert(testIIA.toIntInterval() === testII)
  }

  implicit val failure: Failure = summon[Failure]
  implicit val joinComputation: JoinComputation = summon[JoinComputation]

  "Functions of IntIntervalApron must behave like those of IntInterval" - {

    "addition" in {
      parityIItoIIA(ApronIntervalIntOps.add, IntervalIntOps.add, 0, 1, -1, 0)

    }
    "substraction" in {
      parityIItoIIA(ApronIntervalIntOps.sub, IntervalIntOps.sub, 0, 1, -1, 0)
    }
    "multiplication" in {
      parityIItoIIA(ApronIntervalIntOps.mul, IntervalIntOps.mul, 0, 1, -1, 0)
    }
    "division" in { // TODO: Fails because failure and joinComputation are null (NPE)
      parityIItoIIA(ApronIntervalIntOps.div, IntervalIntOps.div, 0, 1, -1, 1)
    }
    "joinValues" in {
      parityIItoIIA(
        IntIntervalApronJoin.joinValues,
        IntIntervalJoin.joinValues,
        0,
        1,
        -1,
        0
      )
    }
    // meet not implemented in IntInterval
    // "meet" in {
    //   parityIItoIIA(ApronIntervalIntOps.meet, IntervalIntOps.meet, 0,1, -1,0)
    // }
    "widen" in {
      var explicitBounds: Set[Int] = Set.empty
      parityIItoIIA(
        IntIntervalApronWiden(explicitBounds).widen,
        IntIntervalWiden(explicitBounds).widen,
        0,
        1,
        -1,
        0
      ) // compare result of the addition of aproninterval (0,1) and aproninterval (-1,0) with the result of intinterval
    }
  }
