package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.{Polka, Texpr1Node, *}
import gmp.*
import sturdy.data.{CombineUnit, JOptionC, noJoin}
import sturdy.apron.JoinTexpr1Node
import sturdy.effect.{ComputationJoiner, EffectStack, SturdyFailure}
import sturdy.values.integer.{ApronIntegerOps, ConcreteIntegerOps, IntegerDivisionByZero, IntegerOps, IntervalIntegerOps, given}
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.failure.{AFallible, CollectedFailures, ConcreteFailure, Failure, FailureKind}
import sturdy.values.Join
import sturdy.values.Widen
import sturdy.values.{Topped, given}
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}

class ApronIntegerOpsTest extends AnyFunSuite:

  class IntApronCallFrame[Data, Var](apron: Apron, initData: Data, initVars: Iterable[(Var, Topped[Texpr1Node])] = Iterable.empty)(using Join[Topped[Texpr1Node]], Widen[Topped[Texpr1Node]])
    extends ApronCallFrame[Data, Var, Topped[Texpr1Node]](apron, initData, v => Some(v), _ => None, identity, identity, initVars)

  def instantiateIntOps() : (IntegerOps[Int, Topped[Texpr1Node]], Apron) =
    implicit val failure: Failure = new ConcreteFailure
    val manager = new Polka(false)
    implicit val apron: Apron = new Apron(manager)
    var callFrame: IntApronCallFrame[String, String] = null
    implicit val effects: EffectStack = new EffectStack(List(callFrame))
    callFrame = new IntApronCallFrame(apron, "initial call frame")
    implicit val intervalOps: IntervalIntegerOps[Int] = new IntervalIntegerOps[Int](50)
    implicit val orderOps: ApronOrderingOps = new ApronOrderingOps
    implicit val eqOps: ApronEqOps = new ApronEqOps
    val intOps : IntegerOps[Int, Topped[Texpr1Node]] = implicitly
    (intOps, apron)


  test("Random Integer"){
    val (intOps, apron) = instantiateIntOps()
    assert(apron.getBound(intOps.randomInteger()) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
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
    val x = apron.freshConstraintVariable("x")
    val cond = apron.makeConstraint(x, Tcons1.EQ)
    val notCond = apron.negateExpr(cond)

    println(cond)
    println(notCond)
    assert(notCond == apron.makeConstraint(x, Tcons1.DISEQ))
  }

  test("Negate Expr : DISEQ") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x")
    val cond = apron.makeConstraint(x, Tcons1.DISEQ)
    val notCond = apron.negateExpr(cond)

    println(cond)
    println(notCond)
    assert(notCond == apron.makeConstraint(x, Tcons1.EQ))
  }


  test("Negate Expr : SUP") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x")
    val cond = apron.makeConstraint(intOps.sub(x,intOps.integerLit(4)), Tcons1.SUP)
    val notCond = apron.negateExpr(cond)

    println(cond)
    println(notCond)
    apron.constrain(notCond)
    assert(apron.getBound(x) == Interval(Double.NegativeInfinity, 4))
  }

  test("Negate Expr : SUPEQ") {
    val (intOps, apron) = instantiateIntOps()
    val x = apron.freshConstraintVariable("x")
    val cond = apron.makeConstraint(intOps.sub(x,intOps.integerLit(2)), Tcons1.SUPEQ)
    val notCond = apron.negateExpr(cond)

    println(cond)
    println(notCond)
    apron.constrain(notCond)
    assert(apron.getBound(x) == Interval(Double.NegativeInfinity, 1))
  }
