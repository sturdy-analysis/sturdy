package sturdy

import org.scalatest._
import apron.Abstract0 //default; for domains without environments
import apron.Box
import apron.Interval
import sturdy.IntInterval
import sturdy.IntIntervalApron

val ourAbstractManager = apron.Box()
val ourAbstractDomain = apron.Abstract0(ourAbstractManager, 1, 0, Array(apron.Interval()))


// TODO: use lazy vals for test definitons (currently defined in tests)
// TODO: add tests for all use cases/functions
class ApronTest extends AnyFlatSpec, AnyFreeSpec, Matchers:
  lazy val add0 = Seq(IntIntervalApron(-1, 1), op2IIA(_+_, 0,1, -1,0))
  lazy val sub0 = Seq(IntIntervalApron(1, 1), op2IIA(_-_, 0,1, -1,0))
  lazy val mult = Seq(
      Seq(
        IntIntervalApron(-1, 0), // result
        op2IIA(_*_, 0,1, -1,0)   // case1
        op2IIA(_*_, -1,0, 0,1)   // case2
      ),
      Seq(IntIntervalApron(-25, 15), op2IIA(_*_, -5,3, 5,-3)),
      Seq(IntIntervalApron(-15, 25), op2IIA(_*_, -5,3, -5,-3))
  )

  def op2IIA(f: (IntIntervalApron, IntIntervalApron) => IntIntervalApron, l0: Int, l1: Int, r0: Int, r1: Int): IntIntervalApron = {
    IntIntervalApron.bounded(l0, l1) f IntIntervalApron.bounded(r0, r1)
  }
  def op2II(f: (IntInterval, IntInterval) => IntInterval, l0: Int, l1: Int, r0: Int, r1: Int): IntInterval = {
    IntInterval.bounded(l0, l0) f IntInterval.bounded(r0, r1)
  }
  def testDefault(testSequence: Seq[Matchable]) = {
    val split = testSequence match { case SortedSeq(result, rest @ _*) => (result, rest) }
    for (member <- split[1]) split[0] should === member
  }

  "Apron Intervals" must "be able to perform addition" in {

    testDefault(add0)
  }
  it must "be able to perform substraction" in {
    

    testDefault(sub0)
  }
  it must "be able to perform multiplication" in { // example for multiple tests and test cases
    
    for (test <- mult) testDefault(test)
  }
  it must "be able to perform division" in { // TODO
    testDefault(???)
  }
  it must "be able to join" in {
    val join0 = Seq(IntIntervalApron(-10, 10), op2IIA(_join_, 0,10, -10,5))

    testDefault(join0)
  }
  it must "be able to meet" in {
    assertEquals(IntIntervalApron(0,10) meet IntIntervalApron(-10,5), IntIntervalApron(0, 5))
  }
  it must "be able to widen" in { // TODO
    assertEquals(IntIntervalApron(0,10) meet IntIntervalApron(-10,5), IntIntervalApron(0, 5))
  }
  it must "be able to compare" in { // TODO
    //assertEquals(IntIntervalApron(0,10) meet IntIntervalApron(-10,5), IntIntervalApron(0, 5))
  }

  "Apron intervals must behave like sturdy.IntInterval" - {
    "for addition" in {
      // Add
      //assertEquals(add0, IntIntervalApron(IntInterval(0,1) + IntInterval(-1,0)))
    }
    "for substraction" in {}
    "for multiplication" in {}
    "for division" in {}
  }
