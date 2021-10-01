package sturdy

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

val ourAbstractManager = apron.Box()
val ourAbstractDomain =
  apron.Abstract0(ourAbstractManager, 1, 0, Array(apron.Interval()))

// TODO: use lazy vals for test definitons (currently defined in tests)
// TODO: add tests for all use cases/functions
// AnyFreeSpec wird unten benutzt (texte, die den test definieren)
class ApronTest extends AnyFreeSpec, Matchers:

  lazy val add = Seq(IntIntervalApron(-1, 1), op2IIA(_ + _, 0, 1, -1, 0))
  lazy val sub = Seq(IntIntervalApron(1, 1), op2IIA(_ - _, 0, 1, -1, 0))
  lazy val mult = Seq(
    Seq(
      IntIntervalApron(-1, 0), // result
      op2IIA(_ * _, 0, 1, -1, 0), // case1
      op2IIA(_ * _, -1, 0, 0, 1) // case2
    ),
    Seq(IntIntervalApron(-25, 15), op2IIA(_ * _, -5, 3, 5, -3)),
    Seq(IntIntervalApron(-15, 25), op2IIA(_ * _, -5, 3, -5, -3))
  )
  lazy val div = Seq(
    Seq(IntIntervalApron(1, 1), op2IIA(_ / _, 1, 1, 1, 1))
  )
  lazy val join0 =
    Seq(IntIntervalApron(-10, 10), op2IIA(_ joinCopy _, 0, 10, -10, 5))

  // parity test for any binary function (operation)
  def parityIItoIIA(
      apron_f: ((IntIntervalApron, IntIntervalApron) => IntIntervalApron),
      intInterval_f: ((IntInterval, IntInterval) => IntInterval),
      v1_l: Int,
      v1_h: Int,
      v2_l: Int,
      v2_h: Int
  ) =
    assert(
      op2IIA(apron_f, v1_l, v1_h, v2_l, v2_h) === op2IItoIIA(
        intInterval_f,
        v1_l,
        v1_h,
        v2_l,
        v2_h
      )
    )

  //binary operation cunstructor for IntIntervalApron using bounded to ensure valid intervals
  def op2IIA(
      f: (IntIntervalApron, IntIntervalApron) => IntIntervalApron,
      l0: Int,
      l1: Int,
      r0: Int,
      r1: Int
  ): IntIntervalApron = {
    f(IntIntervalApron.bounded(l0, l1), IntIntervalApron.bounded(r0, r1))
  }
  //todo: für join, meet, widen die signaturen in objektfunktionsaufrufe abändern

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

  val manager = Box() // global manager

  @Deprecated
  def testDefault(testSequence: Seq[Matchable]) = {
    val (result, rest) = testSequence match {
      case Seq(result, rest @ _*) => (result, rest)
    } //muss die sequence noch sortiert werden vor dem match?
    for (member <- rest) result === member //todo: testausgabe hierfür
  }

  def testIntervalBounds(
      lower: Int,
      higher: Int
  ) = {
    val testApronInterval = apron.Interval(lower, higher)
    assert(testInterval.inf.isEqual(lower))
    assert(testInterval.sup.isEqual(higher))
  }

  def testApronIntervalTexpr0BinNodeOP(
    texpr0BinNodeOperation: Int,  // TODO: Confirm function
    expectedResultLower: Int,
    expectedResultHigher: Int,
    leftIntervalLower: Int,
    leftIntervalHigher: Int,
    rightIntervalLower: Int,
    rightIntervalHigher: Int
  ) = {
    val testInterval0 = apron.Interval(leftIntervalLower, leftIntervalHigher) // apron Interval initialized with Int uses MpqScalar bounds
    val testInterval1 = apron.Interval(rightIntervalLower, rightIntervalHigher)

    val testAbstractDomain = apron.Abstract0(manager, 2, 0, Array(testInterval0, testInterval1))

    val testNode = Texpr0BinNode(   // apron binary operation tree expression node
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

  def testApronIntervalTexpr0BinNodeOP(
    texpr0BinNodeOperation: Int,
    expectedResult: (Int, Int),
    leftInterval: (Int, Int),
    rightInterval: (Int, Int)
  ) = {
    texpr0BinNodeOperation(
      texpr0BinNodeOperation,
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
      val signSet = Set(-1, 0, 1) // a set describing different types of bounds (negative, zero, positive)

      signSet.foreach(x =>
        signSet.filter(_ >= x)
          .foreach(y =>
            testApronIntervalBounds(x, y)
        )
      )
    }
  } 

  "Interval operations must be as expected" - {
    /**
    *   Interval operations tests are defined as follows:
    *     testApronIntervalTexpr0BinNodeOP(operation, result, leftInterval, rightInterval)
    *
    *   Where:
    *     operation: Int, operations are defined in apron.Texpr0BinNode
    *     result, leftInterval, rightInterval: Tuple2[Int, Int] for each or 6 separate Integers
    *
    **/
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
        testApronIntervalTexpr0BinNodeOP(op, (-2,1), (0,1), (-2,0))
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
        testApronIntervalTexpr0BinNodeOP(op, (1,4), (1,2), (-2,0))
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
        testApronIntervalTexpr0BinNodeOP(op, (0,3), (0,1), (-2,0))
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

      testApronIntervalTexpr0BinNodeOP(op, (0,2), (1,2), (0,1))
    }

    "for division" in {
      val op = Texpr0BinNode.OP_DIV

      testApronIntervalTexpr0BinNodeOP(op, (0,2), (0,2), (1,1))
    }
  }

  "Interval join of intervals (0,2) and (-9,5) must have result (-9,5)" in {
    manager.setPreferedScalarType(1) // 2 sets pref scalar type to mpfr. 1 t0 mpq and 0 to double - double is fastest but least precise
    var ourInterval1 = apron.Interval(0, 2)
    var ourInterval2 = apron.Interval(-9,5)
    val abstractDomain1 = Abstract0(manager, 1, 0, Array(ourInterval1))
    val abstractDomain2 = Abstract0(manager, 1, 0, Array(ourInterval2))
    var result = abstractDomain1.joinCopy(manager, abstractDomain2).getBound(manager, 1) //gibt floats zurück
    var minusNine = apron.MpqScalar(-9)
    var five = apron.MpqScalar(5)
    assert(result.inf.isEqual(minusNine))
    assert(result.sup.isEqual(five))
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
  // "Apron Intervals" must "perform addition" in {

  //   testDefault(add)
  // }
  // it must "perform substraction" in {
    
  //   testDefault(sub)
  // }
  // it must "perform multiplication" in { // example for multiple tests and test cases
    
  //   for (test <- mult) testDefault(test)
  // }
  // it must "perform division" in { // TODO
  //   for (test <- div) testDefault(test)
  // }
  // it must "join" in {

  //   testDefault(join0)
  // }
  // it must "meet" in {
  //   assert((IntIntervalApron(0,10) meet IntIntervalApron(-10,5)) === IntIntervalApron(0, 5)) 
  // }
  // it must "widen" in { // TODO
  //   implicit var bounds: Set[Int] = Set.empty
  //   assert(IntIntervalApronWiden.widen(IntIntervalApron(0,10), IntIntervalApron(-10,5)) === IntIntervalApron(0, 5))
  // }
  // it must "compare" in { // TODO
  //   //assertEquals(IntIntervalApron(0,10) meet IntIntervalApron(-10,5), IntIntervalApron(0, 5))
  // }
/*   implicit val f: Failure = fail()
  implicit val j: JoinComputation = ??? // TODO
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
  } */

//   "sturdy.IntIntervalApron join must behave like sturdy.IntInterval join" in {
//    parityIItoIIA((ApronInterval.joinCopy, (IntInterval.join), 0,1, -1,0) // compare result of the addition of aproninterval (0,1) and aproninterval (-1,0) with the result of intinterval
//   }
// "sturdy.IntIntervalApron meet must behave like sturdy.IntInterval meet" in {
//     parityIItoIIA(ApronIntervalIntOps.meet, IntervalIntOps.meet, 0,1, -1,0)
//   }
//   "sturdy.IntIntervalApron widen must behave like sturdy.IntInterval widen" in {
//     parityIItoIIA(ApronIntervalIntOps.widen, IntervalIntOps.widen, 0,1, -1,0) // compare result of the addition of aproninterval (0,1) and aproninterval (-1,0) with the result of intinterval
//   }

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
