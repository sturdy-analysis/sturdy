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

// AnyFreeSpec wird unten benutzt (texte, die den test definieren)
class ApronIntervalsTest extends AnyFreeSpec, Matchers:
  val manager = Box() // global manager
  manager.setPreferedScalarType(
    1
  ) // 2 - mpfr (multi-precision float); 1 - mpq (rational number); 0 - double

  // Test created interval bounds
  def testApronIntervalBounds(
      lower: Int,
      higher: Int
  ): Assertion = {
    val testApronInterval = apron.Interval(lower, higher)
    assert(testApronInterval.inf.isEqual(lower))
    assert(testApronInterval.sup.isEqual(higher))
  }

  def texprOPNameAsString(texpr0BinNodeOperation: Int) =
    texpr0BinNodeOperation match {

      case Texpr0BinNode.OP_SUB => "sub"
      case Texpr0BinNode.OP_MUL => "mul"
      case Texpr0BinNode.OP_DIV => "div"
      case Texpr0BinNode.OP_MOD => "mod"
      case Texpr0BinNode.OP_POW => "pow"
      case Texpr0BinNode.OP_ADD => "add"
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

    assert(
      result.inf.isEqual(expectedResultLower) &&
        result.sup.isEqual(expectedResultHigher),
      "with expected result [" + expectedResultLower + "," + expectedResultHigher + "] for term [" + leftIntervalLower + "," + leftIntervalHigher + "] " + texprOPNameAsString(
        texpr0BinNodeOperation
      ) + " [" + rightIntervalLower + "," + rightIntervalHigher + "]. \n The result was " + result + " instead"
    )
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
  "Interval bounds must be as defined" - {
    // a set describing different types of bounds (negative, zero, positive)
    val signSet = Set(-5, -1, 0, 1, 5)
    "if lower <= higher bounds" in {
      signSet.foreach(x =>
        signSet
          .filter(_ >= x)
          .foreach(y => testApronIntervalBounds(x, y))
      )
    }
    "if lower > higher bounds" in {
      signSet.foreach(x =>
        signSet
          .filter(_ < x) // may result in non-canonical empty intervals
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
      *
      * An interval is empty if interval.inf > interval.sup in that case the
      * expected result is the canonical empty interval [1,-1]
      */
    "for addition" - { // TODO: formulate test for each addition case
      val op = Texpr0BinNode.OP_ADD

      "left interval is (+,+) (not empty)" in {
        testApronIntervalTexpr0BinNodeOP(op, (2, 3), (1, 2), (1, 1))

        testApronIntervalTexpr0BinNodeOP(op, (5, 8), (5, 6), (0, 2))
        testApronIntervalTexpr0BinNodeOP(op, (1, 1), (1, 1), (0, 0))

        testApronIntervalTexpr0BinNodeOP(op, (1, 10), (6, 9), (-5, 1))
        testApronIntervalTexpr0BinNodeOP(op, (-11, 3), (1, 3), (-12, 0))
        testApronIntervalTexpr0BinNodeOP(op, (-6, 1), (3, 5), (-9, -4))
      }
      "left interval is (+,+) (empty)" in {
        val result = (1, -1)

        testApronIntervalTexpr0BinNodeOP(op, result, (3, 2), (1, 1))
        testApronIntervalTexpr0BinNodeOP(op, result, (2, 5), (3, 0))
        testApronIntervalTexpr0BinNodeOP(op, result, (5, 1), (2, -3))

        testApronIntervalTexpr0BinNodeOP(op, result, (7, 6), (0, 2))
        testApronIntervalTexpr0BinNodeOP(op, result, (2, 1), (0, 0))
        testApronIntervalTexpr0BinNodeOP(op, result, (4, 1), (0, -2))

        testApronIntervalTexpr0BinNodeOP(op, result, (10, 9), (-5, 1))
        testApronIntervalTexpr0BinNodeOP(op, result, (5, 3), (-12, 0))
        testApronIntervalTexpr0BinNodeOP(op, result, (3, 5), (-4, -9))
      }

      "left interval is (+,0) (empty)" in {
        val result = (1, -1)

        testApronIntervalTexpr0BinNodeOP(op, result, (1, 0), (1, 6))
        testApronIntervalTexpr0BinNodeOP(op, result, (2, 0), (2, 0))
        testApronIntervalTexpr0BinNodeOP(op, result, (3, 0), (3, -7))

        testApronIntervalTexpr0BinNodeOP(op, result, (4, 0), (0, 4))
        testApronIntervalTexpr0BinNodeOP(op, result, (5, 0), (0, 0))
        testApronIntervalTexpr0BinNodeOP(op, result, (6, 0), (0, -8))

        testApronIntervalTexpr0BinNodeOP(op, result, (7, 0), (-9, 5))
        testApronIntervalTexpr0BinNodeOP(op, result, (8, 0), (-10, 0))
        testApronIntervalTexpr0BinNodeOP(op, result, (9, 0), (-11, -12))
      }
      "left interval is (+,-) (empty)" in {
        val result = (1, -1)

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
      "left interval is (0,+) (not empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (+,+))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,0))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (-,+))
        testApronIntervalTexpr0BinNodeOP(op, (-2, 1), (0, 1), (-2, 0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (-,-))
      }
      "left interval is (0,0) (not empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (+,+))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,0))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (-,-))
      }
      "left interval is (0,-) (empty)" in {
        val result = (1, -1)

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
      "left interval is (-,+) (not empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (+,+))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,0))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,-))
      }
      "left interval is (-,0) (not empty)" in {
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
      "left interval is (-,-) (not empty)" in {
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
      "left interval is (-,-) (empty)" in {
        val result = (1, -1)

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

      "left interval is (+,+) (not empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (+,+))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (+,+), (0,0))

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
      "left interval is (0,+) (not empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (+,+))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (0,0))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (-,+))
        testApronIntervalTexpr0BinNodeOP(op, (0, 3), (0, 1), (-2, 0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,+), (-,-))
      }
      "left interval is (0,0) (not empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (+,+))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (0,0), (0,0))

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
      "left interval is (-,+) (not empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (+,+))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (0,0))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,+), (-,-))
      }
      "left interval is (-,0) (not empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (+,+))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (0,0))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (-,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (-,0))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,0), (-,-))
      }
      "left interval is (-,-) (not empty)" in {
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (+,+))

        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (0,+))
        // testApronIntervalTexpr0BinNodeOP(op, (result), (-,-), (0,0))

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
