package sturdy.test

import org.scalatest._
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
import apron.*

val ourAbstractManager = apron.Box()
val ourAbstractDomain =
  apron.Abstract0(ourAbstractManager, 1, 0, Array(apron.Interval()))

// TODO: use lazy vals for test definitons (currently defined in tests)
// TODO: add tests for all use cases/functions
// AnyFreeSpec wird unten benutzt (texte, die den test definieren)
class ApronIntervalsTest extends AnyFreeSpec, Matchers:
  val manager = Box() // global manager
  manager.setPreferedScalarType(
    1
  ) // 2 - mpfr (multi-precision floating real); 1 - mpq (rational); 0 - double

  // Test created interval bounds
  def testApronIntervalBounds(
      lower: Int,
      higher: Int
  ): Assertion = {
    val testApronInterval = apron.Interval(lower, higher)
    assert(testApronInterval.inf.isEqual(lower))
    assert(testApronInterval.sup.isEqual(higher))
  }

  // Test binary node operations on intervals
  def testApronIntervalTexpr0BinNodeOP(
      texpr0BinNodeOperation: Int, // TODO: Confirm function
      expectedResultLower: Int,
      expectedResultHigher: Int,
      leftIntervalLower: Int,
      leftIntervalHigher: Int,
      rightIntervalLower: Int,
      rightIntervalHigher: Int
  ): Assertion = {
    val testInterval0 = apron.Interval(
      leftIntervalLower,
      leftIntervalHigher
    ) // apron Interval initialized with Int uses MpqScalar bounds
    val testInterval1 = apron.Interval(rightIntervalLower, rightIntervalHigher)

    val testAbstractDomain =
      apron.Abstract0(manager, 2, 0, Array(testInterval0, testInterval1))

    val testNode = Texpr0BinNode( // apron binary operation tree expression node
      texpr0BinNodeOperation,
      Texpr0Node.RTYPE_INT,
      Texpr0Node.RDIR_NEAREST,
      Texpr0CstNode(testInterval0),
      Texpr0CstNode(testInterval1)
    )

    val result: Interval = testAbstractDomain.getBound(
      manager,
      Texpr0Intern(testNode)
    )

    assert(result.inf.isEqual(expectedResultLower))
    assert(result.sup.isEqual(expectedResultHigher))
  }

  // sugar-overload
  def testApronIntervalTexpr0BinNodeOP(
      texpr0BinNodeOperation: Int,
      expectedResult: (Int, Int),
      leftInterval: (Int, Int),
      rightInterval: (Int, Int)
  ): Assertion = {
    testApronIntervalTexpr0BinNodeOP(
      texpr0BinNodeOperation,
      expectedResult._1,
      expectedResult._2,
      leftInterval._1,
      leftInterval._2,
      rightInterval._1,
      rightInterval._2
    )
  }

  def testApronIntervalJoin(
      expectedResultLower: Int,
      expectedResultHigher: Int,
      leftIntervalLower: Int,
      leftIntervalHigher: Int,
      rightIntervalLower: Int,
      rightIntervalHigher: Int
  ): Assertion = {
    val expectedResultInterval =
      apron.Interval(expectedResultLower, expectedResultHigher)
    val leftInterval = apron.Interval(leftIntervalLower, leftIntervalHigher)
    val rightInterval = apron.Interval(rightIntervalLower, rightIntervalHigher)

    val abstract0Domain0 = apron.Abstract0(manager, 1, 0, Array(leftInterval))
    val abstract0Domain1 = apron.Abstract0(manager, 1, 0, Array(rightInterval))

    val expectedResultAbstract0Domain =
      apron.Abstract0(manager, 1, 0, Array(expectedResultInterval))

    abstract0Domain0.join(manager, abstract0Domain1)
    assert(abstract0Domain0.isEqual(manager, expectedResultAbstract0Domain))
  }

  def testApronIntervalJoin(
      expectedResult: (Int, Int),
      leftInterval: (Int, Int),
      rightInterval: (Int, Int)
  ): Assertion = {
    testApronIntervalJoin(
      expectedResult._1,
      expectedResult._2,
      leftInterval._1,
      leftInterval._2,
      rightInterval._1,
      rightInterval._2
    )
  }

  def testApronIntervalMeet(
      expectedResultLower: Int,
      expectedResultHigher: Int,
      leftIntervalLower: Int,
      leftIntervalHigher: Int,
      rightIntervalLower: Int,
      rightIntervalHigher: Int
  ): Assertion = {
    val expectedResultInterval =
      apron.Interval(expectedResultLower, expectedResultHigher)
    val leftInterval = apron.Interval(leftIntervalLower, leftIntervalHigher)
    val rightInterval = apron.Interval(rightIntervalLower, rightIntervalHigher)

    val abstract0Domain0 = apron.Abstract0(manager, 1, 0, Array(leftInterval))
    val abstract0Domain1 = apron.Abstract0(manager, 1, 0, Array(rightInterval))

    val expectedResultAbstract0Domain =
      apron.Abstract0(manager, 1, 0, Array(expectedResultInterval))

    abstract0Domain0.meet(manager, abstract0Domain1)
    assert(abstract0Domain0.isEqual(manager, expectedResultAbstract0Domain))
  }

  def testApronIntervalMeet(
      expectedResult: (Int, Int),
      leftInterval: (Int, Int),
      rightInterval: (Int, Int)
  ): Assertion = {
    testApronIntervalMeet(
      expectedResult._1,
      expectedResult._2,
      leftInterval._1,
      leftInterval._2,
      rightInterval._1,
      rightInterval._2
    )
  }

  // generally, testing an external library should be done there
  // and so should documentation be
  // however, here it was part of the assignment
  "Interval bounds must be as defined" - { // TODO: maybe evaluate tests where lower > higher
    "if lower <= higher bounds" in {
      val signSet = Set(
        -1,
        0,
        1
      ) // a set describing different types of bounds (negative, zero, positive)

      signSet.foreach(x =>
        signSet
          .filter(_ >= x)
          .foreach(y => testApronIntervalBounds(x, y))
      )
    }
  }

  "Interval operations must be as expected" - {

    /** Interval operations tests are defined as follows:
      * testApronIntervalTexpr0BinNodeOP(operation, result, leftInterval,
      * rightInterval)
      *
      * Where: operation: Int, operations are defined in apron.Texpr0BinNode
      * result, leftInterval, rightInterval: Tuple2[Int, Int] for each or 6
      * separate Integers
      */
    "for addition" - { // TODO: formulate test for each addition case
      val op = Texpr0BinNode.OP_ADD

      "left interval is (+,+)" in { // TODO: if there's a difference between an empty and non-empty interval, additional cases should be made for empty (+,+) intervals
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (-,-))
      }
      "left interval is (+,0) (empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (-,-))
      }
      "left interval is (+,-) (empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (-,-))
      }
      "left interval is (0,+)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (-,+))
        testApronIntervalTexpr0BinNodeOP(op, (-2, 1), (0, 1), (-2, 0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (-,-))
      }
      "left interval is (0,0)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (-,-))
      }
      "left interval is (0,-) (empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (-,-))
      }
      "left interval is (-,+)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,-))
      }
      "left interval is (-,0)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (-,-))
      }
      "left interval is (-,-)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (-,-))
      }
    }

    "for substraction" - {
      val op = Texpr0BinNode.OP_SUB

      "left interval is (+,+)" in { // TODO: if there's a difference between an empty and non-empty interval, additional cases should be made for empty (+,+) intervals
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (-,+))
        testApronIntervalTexpr0BinNodeOP(op, (1, 4), (1, 2), (-2, 0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (-,-))
      }
      "left interval is (+,0) (empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,0), (-,-))
      }
      "left interval is (+,-) (empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,-), (-,-))
      }
      "left interval is (0,+)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (-,+))
        testApronIntervalTexpr0BinNodeOP(op, (0, 3), (0, 1), (-2, 0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (-,-))
      }
      "left interval is (0,0)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (-,-))
      }
      "left interval is (0,-) (empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,-), (-,-))
      }
      "left interval is (-,+)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,-))
      }
      "left interval is (-,0)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (-,-))
      }
      "left interval is (-,-)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (+,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (+,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (+,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (0,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (0,-))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (-,-))
      }
    }

    "for multiplication" in {
      val op = Texpr0BinNode.OP_MUL

      testApronIntervalTexpr0BinNodeOP(op, (0, 2), (1, 2), (0, 1))
    }

    "for division" in {
      val op = Texpr0BinNode.OP_DIV

      testApronIntervalTexpr0BinNodeOP(op, (0, 2), (0, 2), (1, 1))
    }

    "for join (in Abstract0)" in {
      testApronIntervalJoin((-5, 5), (-5, 0), (0, 5))
    }

    "for meet (in Abstract0)" in {
      testApronIntervalMeet((0, 0), (-5, 0), (0, 5))
      testApronIntervalMeet((-5, 5), (-10, 5), (-5, 10))
    }
  }

/*static public final int FUNID_MEET = 32;
    static public final int FUNID_MEET_ARRAY = 33;
    static public final int FUNID_MEET_LINCONS_ARRAY = 34;
    static public final int FUNID_MEET_TCONS_ARRAY = 35;
    static public final int FUNID_JOIN = 36;
    static public final int FUNID_JOIN_ARRAY = 37;
    static public final int FUNID_EXPAND = 47;
    static public final int FUNID_FOLD = 48;
    static public final int FUNID_WIDENING = 49;
    static public final int FUNID_CLOSURE = 50;*/

// "for join" in {}
// "for meet" in {}
// "for widen" in {}
// "for comparisons" - {
//   "greater or equal" in {}
//   "greater than" in {}
//   "less or equal" in {}
//   "less than" in {}
// }
// "for equality" - {
//   "equal" in {}
//   "not equal" in {}
// }
