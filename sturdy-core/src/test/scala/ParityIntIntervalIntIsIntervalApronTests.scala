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
import sturdy.effect.failure.AFailureCollect
import gmp.Mpq

class ParityIntIntervalIsIntIntervalApronTests extends AnyFreeSpec, Matchers:
  def op2IIA(
      f: (IntIntervalApron, IntIntervalApron) => IntIntervalApron,
      l0: Int,
      l1: Int,
      r0: Int,
      r1: Int
  ): IntIntervalApron = {
    f(IntIntervalApron(l0, l1), IntIntervalApron(r0, r1))
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
  ): Assertion = {
    val testIIA = op2IIA(apronIntInterval_f, v1_l, v1_h, v2_l, v2_h)
    val testII = op2II(intInterval_f, v1_l, v1_h, v2_l, v2_h)

    assert(testIIA === IntIntervalApron(testII) && testIIA.toIntInterval() === testII)
  }

  def expectedInfinityParityIItoIIA( // same as above, but only for values where infinity is an expected bound in result for IIA
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
  ): Assertion = {
    def inftyBoundsComparator(boundIIA: apron.Scalar, boundII: Int): Boolean =
      boundIIA.isInfty match {
        case -1 => boundII == Int.MinValue
        case 1 => boundII == Int.MaxValue
        case 0 => boundIIA.cmp(boundII) == 0
      }

    val testIIA: IntIntervalApron = op2IIA(apronIntInterval_f, v1_l, v1_h, v2_l, v2_h)
    val testII: IntInterval = op2II(intInterval_f, v1_l, v1_h, v2_l, v2_h)
    
    val inf = testIIA.interval.inf
    val sup = testIIA.interval.sup
    
    assert(inftyBoundsComparator(inf, testII.l) && inftyBoundsComparator(sup, testII.h))
  }

  // failure: summon[Failure], new AFailureCollect {} or implicitly
  implicit val failure: Failure = implicitly
  // joinComputation cannot be implicit. Why?
  implicit val joinComputation: JoinComputation = new JoinComputation {}

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
    "division" in {
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
      val negativeInfinityAssertion = expectedInfinityParityIItoIIA(
        IntIntervalApronWiden(explicitBounds).widen,
        IntIntervalWiden(explicitBounds).widen,
        0,
        1,
        -1,
        0
      ) // compare result of the addition of aproninterval (0,1) and aproninterval (-1,0) with the result of intinterval
      
    
    }
  }
