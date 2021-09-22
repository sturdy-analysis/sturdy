package sturdy

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import apron.Abstract0 //default; for domains without environments
import apron.Box
import apron.Interval
import sturdy.values.ints.IntInterval
import sturdy.values.ints.IntIntervalApron
import sturdy.values.ints.IntIntervalApronWiden
import sturdy.values.ints.ApronIntervalIntOps
import sturdy.values.ints.IntervalIntOps
import sturdy.fix.Widening
import sturdy.effect.failure.Failure
import sturdy.effect.JoinComputation
import scala.language.postfixOps
import org.scalatest.freespec.AnyFreeSpec
import sturdy.values.ints.IntOps

val ourAbstractManager = apron.Box()
val ourAbstractDomain = apron.Abstract0(ourAbstractManager, 1, 0, Array(apron.Interval()))


// TODO: use lazy vals for test definitons (currently defined in tests)
// TODO: add tests for all use cases/functions
//anyflatspec wird unten benutzt (texte, die den test definieren)
class ApronTest extends AnyFlatSpec, Matchers:
  
  lazy val add = Seq(IntIntervalApron(-1, 1), op2IIA(_+_, 0,1, -1,0))
  lazy val sub = Seq(IntIntervalApron(1, 1), op2IIA(_-_, 0,1, -1,0))
  lazy val mult = Seq(
      Seq(
        IntIntervalApron(-1, 0), // result
        op2IIA(_*_, 0,1, -1,0),  // case1
        op2IIA(_*_, -1,0, 0,1)   // case2
      ),
      Seq(IntIntervalApron(-25, 15), op2IIA(_*_, -5,3, 5,-3)),
      Seq(IntIntervalApron(-15, 25), op2IIA(_*_, -5,3, -5,-3))
  )
  lazy val div = Seq(
    Seq(IntIntervalApron(1, 1), op2IIA(_/_, 1,1, 1,1))
  )
  lazy val join0 = Seq(IntIntervalApron(-10, 10), op2IIA(_ join _, 0,10, -10,5))
  

  // TODO: for a given trait
  def parityIItoIIA(apron_f: ((IntIntervalApron, IntIntervalApron) => IntIntervalApron), intInterval_f:((IntInterval, IntInterval) => IntInterval),
                      v1_l: Int, v1_h: Int, v2_l: Int, v2_h: Int) =
                        assert(op2IIA(apron_f, v1_l,v1_h, v2_l,v2_h) === op2IItoIIA(intInterval_f, v1_l,v1_h, v2_l,v2_h))

  //binary operation cunstructor for IntIntervalApron using bounded to ensure valid intervals
  def op2IIA(f: (IntIntervalApron, IntIntervalApron) => IntIntervalApron, l0: Int, l1: Int, r0: Int, r1: Int): IntIntervalApron = {
    f(IntIntervalApron.bounded(l0, l1), IntIntervalApron.bounded(r0, r1))
  }

   def op2IItoIIA(f: (IntInterval, IntInterval) => IntInterval, l0: Int, l1: Int, r0: Int, r1: Int): IntIntervalApron = {
    IntIntervalApron(f(IntInterval.bounded(l0, l1), IntInterval.bounded(r0, r1)))
  }

  def testDefault(testSequence: Seq[Matchable]) = {
    val (result, rest) = testSequence match { case Seq(result, rest @ _*) => (result, rest) } //muss die sequence noch sortiert werden vor dem match?
    for (member <- rest) result === member //todo: testausgabe hierfür
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

  "sturdy.IntIntervalApron ADDITION" must "behave like sturdy.IntInterval ADDITION" in{
    
      implicit val f: Failure = fail()
      implicit val j: JoinComputation = new JoinComputation
      parityIItoIIA(ApronIntervalIntOps.add, IntervalIntOps.add, 0,1, -1, 0)
      //assert(add, IntIntervalApron(IntInterval(0,1) + IntInterval(-1,0)))
    }
    // "for substraction" in {}
    // "for multiplication" in {}
    // "for division" in {}
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

  
