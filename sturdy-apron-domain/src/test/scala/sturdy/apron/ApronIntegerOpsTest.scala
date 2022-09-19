package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.{Texpr1Node, Polka, *}
import gmp.*
import sturdy.data.{JOptionC, CombineUnit, noJoin}
import sturdy.effect.{ComputationJoiner, EffectStack, SturdyFailure}
import sturdy.values.integer.{ConcreteIntegerOps, IntegerDivisionByZero, ApronIntegerOps, IntervalIntegerOps, given}
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.failure.{FailureKind, CollectedFailures, ConcreteFailure, Failure, AFallible}
import sturdy.values.Join
import sturdy.values.Widen
import sturdy.values.{Topped, given}
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}
import sturdy.values.utils.given

import scala.language.reflectiveCalls

class ApronIntegerOpsTest extends AnyFunSuite:

  def instantiateIntOps() : (ApronIntegerOps[Int], Apron) =
    implicit val failure: Failure = new ConcreteFailure
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    implicit val effects: EffectStack = new EffectStack(List(failure, apron))
    implicit val intervalOps: IntervalIntegerOps[Int] = new IntervalIntegerOps[Int](50)
    val intOps = new ApronIntegerOps[Int]
    (intOps, apron)


  test("Random Integer"){
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.randomInteger()) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("IntMax"){
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.integerLit(Int.MaxValue)) == Interval(Int.MaxValue, Int.MaxValue))
  }

  test("IntMin") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.integerLit(Int.MinValue)) == Interval(Int.MinValue, Int.MinValue))
  }

  test("Addition"){
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.add(intOps.integerLit(3), intOps.integerLit(5))) == Interval(8,8))
  }

  test("Addition : unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.add(intOps.integerLit(3), intOps.randomInteger())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Subtraction") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.sub(intOps.integerLit(3), intOps.integerLit(4))) == Interval(-1, -1))
  }

  test("Subtraction : unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.sub(intOps.integerLit(3), intOps.randomInteger())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Multiplication") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.mul(intOps.integerLit(3), intOps.integerLit(5))) == Interval(15,15))
  }

  test("Multiplication : negative") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.mul(intOps.integerLit(-3), intOps.integerLit(5))) == Interval(-15, -15))
  }

  test("Multiplication : unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.mul(intOps.integerLit(3), intOps.randomInteger())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Multiplication : unconstrained and zero") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.mul(intOps.randomInteger(), intOps.integerLit(0))) == Interval(0,0))
  }

  test("Negative") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.neg(intOps.integerLit(-6))) == Interval(6,6))
  }

  test("Negative : unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.neg(intOps.randomInteger())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Maximum") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.max(intOps.integerLit(4), intOps.integerLit(2))) == Interval(4,4))
  }

  test("Maximum : unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    val r = apron.getBound(intOps.max(intOps.integerLit(4), intOps.randomInteger()))
    assert(r== Interval(4, Double.PositiveInfinity))
  }

  test("Minimum") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.min(intOps.integerLit(-3), intOps.integerLit(-4))) == Interval(-4, -4))
  }

  test("Minimum : unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.min(intOps.integerLit(4), intOps.randomInteger())) == Interval(Double.NegativeInfinity, 4))
  }

  test("Absolute") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.absolute(intOps.integerLit(-6))) == Interval(6,6))
  }

  test("Absolute : unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.absolute(intOps.randomInteger())) == Interval(0,Double.PositiveInfinity))
  }

  test("Division") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.integerLit(16), intOps.integerLit(4))) == Interval(4,4))
  }

  test("Division : mod != 0 ") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.integerLit(3), intOps.integerLit(2))) == Interval(1, 1))
  }

  test("Division : negative denominator") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.integerLit(2), intOps.integerLit(-2))) == Interval(-1,-1))
  }

  test("Division : negative numerator") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.integerLit(-4), intOps.integerLit(2))) == Interval(-2, -2))
  }

  test("Division : negative numerator and negative denominator") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.integerLit(-8), intOps.integerLit(-4))) == Interval(2, 2))
  }

  test("Division : by zero") {
    val (intOps, apron) = instantiateIntOps()
    try {
      intOps.div(intOps.integerLit(3),intOps.integerLit(0))
      assert(false)
    }
    catch {
      case e : SturdyFailure => assert(true)
      case _ : Throwable => assert(false)
    }
  }

  test("Division : by unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.integerLit(4), intOps.randomInteger())) == Interval(-4, 4))
  }

  test("Division : zero by unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.div(intOps.integerLit(0), intOps.randomInteger())) == Interval(0,0))
  }

  test("Division : unconstrained by zero") {
    val (intOps, apron) = instantiateIntOps()
    try {
      intOps.div(intOps.randomInteger(), intOps.integerLit(0))
      assert(false)
    }
    catch {
      case e: SturdyFailure => assert(true)
      case _: Throwable => assert(false)
    }
  }

  test("Modulo") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.modulo(intOps.integerLit(4), intOps.integerLit(3))) == Interval(1,1))
  }

  test("Modulo : negative") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.modulo(intOps.integerLit(-8), intOps.integerLit(3))) == Interval(1, 1))
  }

  test("Modulo : by negative") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.modulo(intOps.integerLit(19), intOps.integerLit(-4))) == Interval(3, 3))
  }

  test("Modulo : by zero") {
    val (intOps, apron) = instantiateIntOps()
    try {
      intOps.modulo(intOps.integerLit(3), intOps.integerLit(0))
      assert(false)
    } catch {
      case e: SturdyFailure => assert(true)
      case _: Throwable => assert(false)
      }
    }

  test("Modulo : by unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.modulo(intOps.integerLit(5), intOps.randomInteger())) == Interval(0, 5))
  }

  test("Modulo : 0 by unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.modulo(intOps.integerLit(0), intOps.randomInteger())) == Interval(0, 0))
  }

  test("Modulo : unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.modulo(intOps.randomInteger(), intOps.integerLit(7))) == Interval(0, 7))
  }

  test("Modulo : unconstrained by unconstrained"){
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.modulo(intOps.randomInteger(), intOps.randomInteger())) == Interval(0, Double.PositiveInfinity))
  }

  test("Remainder") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.remainder(intOps.integerLit(4), intOps.integerLit(7))) == Interval(4, 4))
  }

  test("Remainder : negative") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.remainder(intOps.integerLit(-18), intOps.integerLit(4))) == Interval(-2, -2))
  }

  test("Remainder : by negative") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.remainder(intOps.integerLit(9), intOps.integerLit(-2))) == Interval(1, 1))
  }

  test("Remainder : by zero") {
    val (intOps, apron) = instantiateIntOps()
    try {
      intOps.remainder(intOps.integerLit(3), intOps.integerLit(0))
      assert(false)
    } catch {
      case e: SturdyFailure => assert(true)
      case _: Throwable => assert(false)
    }
  }

  test("Remainder : by unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.remainder(intOps.integerLit(5), intOps.randomInteger())) == Interval(0, 5))
  }

  test("Remainder : 0 by unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.remainder(intOps.integerLit(0), intOps.randomInteger())) == Interval(0, 0))
  }

  test("Remainder : unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.remainder(intOps.randomInteger(), intOps.integerLit(7))) == Interval(-7, 7))
  }

  test("Remainder : unconstrained by unconstrained") {
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.remainder(intOps.randomInteger(), intOps.randomInteger())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Negate Expr : EQ") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x", ApronAllocationSite.LocalVar("x"))

    val cond = ApronCons.eq(x.expr, ApronExpr.num(0))
    val notCond = cond.negated

    println(cond)
    println(notCond)
    assert(notCond == ApronCons.neq(x.expr, ApronExpr.num(0)))
  }

  test("Negate Expr : DISEQ") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x", ApronAllocationSite.LocalVar("x"))

    val cond = ApronCons.neq(x.expr, ApronExpr.num(0))
    val notCond = cond.negated

    println(cond)
    println(notCond)
    assert(notCond == ApronCons.eq(x.expr, ApronExpr.num(0)))
  }


  test("Negate Expr : SUP") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x", ApronAllocationSite.LocalVar("x"))
    val cond = ApronCons.gt(intOps.sub(x.expr,intOps.integerLit(4)), ApronExpr.num(0))
    val notCond = cond.negated

    println(cond)
    println(notCond)
    apron.assertConstrain(notCond)
    assert(apron.getBound(x) == Interval(Double.NegativeInfinity, 4))
  }

  test("Negate Expr : SUPEQ") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x", ApronAllocationSite.LocalVar("x"))
    val cond = ApronCons.ge(intOps.sub(x.expr,intOps.integerLit(2)), ApronExpr.num(0))
    val notCond = cond.negated

    println(cond)
    println(notCond)
    apron.assertConstrain(notCond)
    assert(apron.getBound(x) == Interval(Double.NegativeInfinity, 1))
  }
