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

  @Deprecated
  def testDefault(testSequence: Seq[Matchable]) = {
    val (result, rest) = testSequence match {
      case Seq(result, rest @ _*) => (result, rest)
    } //muss die sequence noch sortiert werden vor dem match?
    for (member <- rest) result === member //todo: testausgabe hierfür
  }

  // generally, testing an external library should be done there 
  // and so should documentation be
  // however, here it was part of the assignment
  // TODO: factory function to test different bounds, magic numbers shouldn't be use
  "Interval must have lower bound 0 and upper bound 1" in {
    var ourInterval = apron.Interval(0, 1)
    //val abstractDomain = Abstract0(manager, 1, 0, Array(ourInterval))
    assert(ourInterval.inf.isEqual(0))
    assert(ourInterval.sup.isEqual(1))
  }

  // TODO: test too specific, factory function for different bounds
  "Interval addition of intervals (0,1) and (-2,0) must have result (-2,1)" in {
    val manager = Box() // TODO: used in all tests, should be declared in outer scope
    var ourInterval1 = Interval(0, 1) // TODO: should be parametrized, factory function
    var ourInterval2 = Interval(-2, 0)  
    val abstractDomain =  // TODO: factory function
      Abstract0(manager, 2, 0, Array(ourInterval1, ourInterval2))

    var ourNode = apron.Texpr0BinNode(  // TODO: factory function
      apron.Texpr0BinNode.OP_ADD,
      apron.Texpr0Node.RTYPE_INT,
      apron.Texpr0Node.RDIR_NEAREST,
      apron.Texpr0CstNode(ourInterval1),
      apron.Texpr0CstNode(ourInterval2)
    )
    var result: Interval = abstractDomain.getBound(
      manager,
      apron.Texpr0Intern(ourNode)
    ) // gibt floats zurück
    Console.printf( // TODO: there should never be prints in tests (and unless making a console program, never be a print in code in general)
      "Resulting interval should be (-2,1) and is (%s, %s) \n",
      result.inf,
      result.sup
    )
    var minusTwo = apron.Scalar.create() // TODO: apron.MpqScalar should be used for Int and also has a constructor
    var one = apron.Scalar.create()      // TODO: see @ line 133
    minusTwo.set(-2)                     // TODO: see @ line 133
    one.set(1)                           // TODO: see @ line 133
    assert(result.inf.isEqual(minusTwo))
    assert(result.sup.isEqual(one))
  }

  // TODO: see comments in test beginning @ line 109
  "Interval subtraction of intervals (1,2) and (-2,0) must have result (1,4)" in {
    val manager = apron.Box()
    var ourInterval1 = apron.Interval(1, 2)
    var ourInterval2 = apron.Interval(-2, 0)
    val abstractDomain =
      Abstract0(manager, 2, 0, Array(ourInterval1, ourInterval2))

    var ourNode = apron.Texpr0BinNode(
      apron.Texpr0BinNode.OP_SUB,
      apron.Texpr0Node.RTYPE_INT,
      apron.Texpr0Node.RDIR_NEAREST,
      apron.Texpr0CstNode(ourInterval1),
      apron.Texpr0CstNode(ourInterval2)
    )
    var result = abstractDomain.getBound(
      manager,
      apron.Texpr0Intern(ourNode)
    ) //gibt floats zurück
    Console.printf(
      "Resulting interval should be (1,4) and is (%s, %s) \n",
      result.inf,
      result.sup
    )
    var four = apron.Scalar.create()
    var one = apron.Scalar.create()
    four.set(4)
    one.set(1)
    assert(result.inf.isEqual(one))
    assert(result.sup.isEqual(four))
  }

  // TODO: see comments in test beginning @ line 109
  "Interval multiplication of intervals (1,2) and (0,1) must have result (0,2)" in {
    val manager = apron.Box()
    var ourInterval1 = apron.Interval(1, 2)
    var ourInterval2 = apron.Interval(0, 1)
    val abstractDomain =
      Abstract0(manager, 2, 0, Array(ourInterval1, ourInterval2))

    var ourNode = apron.Texpr0BinNode(
      apron.Texpr0BinNode.OP_MUL,
      apron.Texpr0Node.RTYPE_INT,
      apron.Texpr0Node.RDIR_NEAREST,
      apron.Texpr0CstNode(ourInterval1),
      apron.Texpr0CstNode(ourInterval2)
    )
    var result = abstractDomain.getBound(
      manager,
      apron.Texpr0Intern(ourNode)
    ) //gibt floats zurück
    Console.printf(
      "Resulting interval should be (0,2) and is (%s, %s) \n",
      result.inf,
      result.sup
    )
    var zero = apron.Scalar.create()
    var two = apron.Scalar.create()
    zero.set(0)
    two.set(2)
    assert(result.inf.isEqual(zero))
    assert(result.sup.isEqual(two))
  }

  // TODO: see comments in test beginning @ line 109
  "Interval division of intervals (0,2) and (1,1) must have result (0,2)" in {
    val manager = apron.Box()
    var ourInterval1 = apron.Interval(0, 2)
    var ourInterval2 = apron.Interval(1, 1)
    val abstractDomain =
      Abstract0(manager, 2, 0, Array(ourInterval1, ourInterval2))

    var ourNode = apron.Texpr0BinNode(
      apron.Texpr0BinNode.OP_DIV,
      apron.Texpr0Node.RTYPE_INT,
      apron.Texpr0Node.RDIR_NEAREST,
      apron.Texpr0CstNode(ourInterval1),
      apron.Texpr0CstNode(ourInterval2)
    )
    var result = abstractDomain.getBound(
      manager,
      apron.Texpr0Intern(ourNode)
    ) //gibt floats zurück
    Console.printf(
      "Resulting interval should be (0,2) and is (%s, %s) \n",
      result.inf,
      result.sup
    )
    var zero = apron.Scalar.create()
    var two = apron.Scalar.create()
    zero.set(0)
    two.set(2)
    assert(result.inf.isEqual(zero))
    assert(result.sup.isEqual(two))
  }

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
