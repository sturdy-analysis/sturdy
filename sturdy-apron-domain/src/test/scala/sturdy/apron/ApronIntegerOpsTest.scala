package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.*
import gmp.*
import sturdy.data.{CombineUnit, JOptionC, noJoin}
import sturdy.apron.JoinTexpr1Node
import sturdy.effect.{ComputationJoiner, EffectStack}
import sturdy.values.integer.{ApronIntegerOps, IntervalIntegerOps, given}
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.failure.{AFallible, CollectedFailures, ConcreteFailure, Failure, FailureKind}
import sturdy.values.Join
import sturdy.values.Widen
import sturdy.values.{Topped, given}
import sturdy.values.integer.ConcreteIntegerOps

class ApronIntegerOpsTest extends AnyFunSuite:

  class IntApronCallFrame[Data, Var](apron: Apron, initData: Data, initVars: Iterable[(Var, Texpr1Node)] = Iterable.empty)(using Join[Texpr1Node], Widen[Texpr1Node])
    extends ApronCallFrame[Data, Var, Texpr1Node](apron, initData, v => Some(v), _ => None, identity, identity, initVars)

  def instantiateIntOps() : (ApronIntegerOps[Int], Apron) =
    implicit val failure: Failure = new ConcreteFailure
    val manager = new Polka(false)
    implicit val apron: Apron = new Apron(manager)
    var callFrame: IntApronCallFrame[String, String] = null
    implicit val effects: EffectStack = new EffectStack(List(callFrame))
    callFrame = new IntApronCallFrame(apron, "initial call frame")
    implicit val intervalOps: IntervalIntegerOps[Int] = new IntervalIntegerOps[Int](50)
    val intOps = new ApronIntegerOps[Int]
    (intOps, apron)


  test("Random Integer"){
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.randomInteger()) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Addition"){
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.add(intOps.integerLit(3), intOps.integerLit(5))) == Interval(8,8))
  }

  test("Addition with unbounded") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.add(intOps.integerLit(3), intOps.randomInteger())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Subtraction") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.sub(intOps.integerLit(3), intOps.integerLit(4))) == Interval(-1, -1))
  }

  test("Subtraction with unbounded") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.sub(intOps.integerLit(3), intOps.randomInteger())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Multiplication") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.mul(intOps.integerLit(3), intOps.integerLit(5))) == Interval(15,15))
  }

  test("Multiplication with negative") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.mul(intOps.integerLit(-3), intOps.integerLit(5))) == Interval(-15, -15))
  }

  test("Multiplication with unbounded") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.mul(intOps.integerLit(3), intOps.randomInteger())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Multiplication with unbounded and zero") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.mul(intOps.randomInteger(), intOps.integerLit(0))) == Interval(0,0))
  }

  test("Neg") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.neg(intOps.integerLit(-6))) == Interval(6,6))
  }

  test("Neg with unbounded") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.neg(intOps.randomInteger())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Max") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.max(intOps.integerLit(4), intOps.integerLit(-4))) == Interval(4,4))
  }

  test("Max with unbounded") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.max(intOps.integerLit(4), intOps.randomInteger())) == Interval(4, Double.PositiveInfinity))
  }

  test("Min") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.min(intOps.integerLit(4), intOps.integerLit(-4))) == Interval(-4, -4))
  }

  test("Min with unbounded") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.min(intOps.integerLit(4), intOps.randomInteger())) == Interval(Double.NegativeInfinity, 4))
  }

  test("Absolute") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.absolute(intOps.integerLit(-6))) == Interval(6,6))
  }

  test("Absolute with unbounded") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.absolute(intOps.randomInteger())) == Interval(0,Double.PositiveInfinity))
  }

  test("Division") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.integerLit(1), intOps.integerLit(0))) == Interval(0, Double.PositiveInfinity))
  }

  test("Division by zero") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.randomInteger(),intOps.randomInteger())) == Interval(0, Double.PositiveInfinity))
  }

  test("Division by unbounded") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.randomInteger(), intOps.randomInteger())) == Interval(0, Double.PositiveInfinity))
  }

  test("Negate EQ") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x")
    val cond = apron.makeConstraint(x, Tcons1.EQ)
    val notCond = apron.negateExpr(cond)

    println(cond)
    println(notCond)
    assert(notCond == apron.makeConstraint(x, Tcons1.DISEQ))
  }

  test("Negate DISEQ") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x")
    val cond = apron.makeConstraint(x, Tcons1.DISEQ)
    val notCond = apron.negateExpr(cond)

    println(cond)
    println(notCond)
    assert(notCond == apron.makeConstraint(x, Tcons1.EQ))
  }


  test("Negate SUP") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x")
    val cond = apron.makeConstraint(intOps.sub(x,intOps.integerLit(4)), Tcons1.SUP)
    val notCond = apron.negateExpr(cond)

    println(cond)
    println(notCond)
    apron.constrain(notCond)
    assert(apron.getBound(x) == Interval(Double.NegativeInfinity, 4))
  }

  test("Negate SUPEQ") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x")
    val cond = apron.makeConstraint(intOps.sub(x,intOps.integerLit(2)), Tcons1.SUPEQ)
    val notCond = apron.negateExpr(cond)

    println(cond)
    println(notCond)
    apron.constrain(notCond)
    assert(apron.getBound(x) == Interval(Double.NegativeInfinity, 1))
  }
