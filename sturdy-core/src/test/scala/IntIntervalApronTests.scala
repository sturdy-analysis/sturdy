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

class IntIntervalApronTests extends AnyFreeSpec, Matchers:

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

  def testDefault(testSequence: Seq[Matchable]) = {
    val (result, rest) = testSequence match {
      case Seq(result, rest @ _*) => (result, rest)
    } //muss die sequence noch sortiert werden vor dem match?
    for (member <- rest) result === member //todo: testausgabe hierfür
  }
/*
  "Apron Intervals" must "perform addition" in {

    testDefault(add)
  }
  it must "perform substraction" in {

    testDefault(sub)
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

 */
