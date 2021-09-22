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
import sturdy.fix.Widening
import scala.language.postfixOps
import org.scalatest.freespec.AnyFreeSpec

val ourAbstractManager = apron.Box()
val ourAbstractDomain = apron.Abstract0(ourAbstractManager, 1, 0, Array(apron.Interval()))


// TODO: use lazy vals for test definitons (currently defined in tests)
// TODO: add tests for all use cases/functions
//anyflatspec wird unten benutzt (texte, die den test definieren)
class ApronTest extends AnyFlatSpec, Matchers:
  lazy val add0 = Seq(IntIntervalApron(-1, 1), op2IIA(_+_, 0,1, -1,0))
  lazy val sub0 = Seq(IntIntervalApron(1, 1), op2IIA(_-_, 0,1, -1,0))
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

//binary operation cunstructor for IntIntervalApron using bounded to ensure valid intervals
  def op2IIA(f: (IntIntervalApron, IntIntervalApron) => IntIntervalApron, l0: Int, l1: Int, r0: Int, r1: Int): IntIntervalApron = {
    f(IntIntervalApron.bounded(l0, l1), IntIntervalApron.bounded(r0, r1))
  }

   def op2IItoIIA(f: (IntInterval, IntInterval) => IntInterval, l0: Int, l1: Int, r0: Int, r1: Int): IntIntervalApron = {
    f(IntInterval.bounded(l0, l1), IntInterval.bounded(r0, r1))
  }

  def testDefault(testSequence: Seq[Matchable]) = {
    val (result, rest) = testSequence match { case Seq(result, rest @ _*) => (result, rest) } //muss die sequence noch sortiert werden vor dem match?
    for (member <- rest) result === member //todo: testausgabe hierfür
  }

  "Apron Intervals" must "perform addition" in {

    testDefault(add0)
  }
  it must "perform substraction" in {
    

    testDefault(sub0)
  }
  it must "perform multiplication" in { // example for multiple tests and test cases
    
    for (test <- mult) testDefault(test)
  }
  it must "perform division" in { // TODO
    for (test <- div) testDefault(test)
  }
  it must "join" in {

    testDefault(join0)
  }
  it must "meet" in {
    assert((IntIntervalApron(0,10) meet IntIntervalApron(-10,5)) === IntIntervalApron(0, 5)) 
  }
  it must "widen" in { // TODO
    implicit var bounds: Set[Int] = Set.empty
    assert(IntIntervalApronWiden.widen(IntIntervalApron(0,10), IntIntervalApron(-10,5)) === IntIntervalApron(0, 5))
  }
  it must "compare" in { // TODO
    //assertEquals(IntIntervalApron(0,10) meet IntIntervalApron(-10,5), IntIntervalApron(0, 5))
  }

  "sturdy.IntIntervalApron" must "behave like sturdy.IntInterval" {
    "for addition" in {
      // Add
      //assertEquals(add0, IntIntervalApron(IntInterval(0,1) + IntInterval(-1,0)))
    }
    "for substraction" in {}
    "for multiplication" in {}
    "for division" in {}
    "for join" in {}
    "for meet" in {}
    "for widen" in {}
    "for comparisons" - {
      "greater or equal" in {}
      "greater than" in {}
      "less or equal" in {}
      "less than" in {}
    }
    "for equality" - {
      "equal" in {}
      "not equal" in {}
    }

  }
