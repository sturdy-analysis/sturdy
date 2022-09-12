package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.{Texpr1Node, Polka, *}
import gmp.*
import sturdy.data.{JOptionC, CombineUnit, noJoin}
import sturdy.apron.JoinTexpr1Node
import sturdy.effect.{ComputationJoiner, EffectStack, SturdyFailure}
import sturdy.values.integer.{ConcreteIntegerOps, IntegerDivisionByZero, ApronIntegerOps, IntervalIntegerOps, given}
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.failure.{FailureKind, CollectedFailures, ConcreteFailure, Failure, AFallible}
import sturdy.values.Join
import sturdy.values.Widen
import sturdy.values.floating.ApronFloatOps
import sturdy.values.{Topped, given}
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}
import sturdy.values.utils.given

import scala.language.reflectiveCalls

class ApronFloatOpsTest extends AnyFunSuite:

  class FloatApronCallFrame[Data, Var](apron: Apron, initData: Data, initVars: Iterable[(Var, Texpr1Node)] = Iterable.empty)(using Join[Texpr1Node], Widen[Texpr1Node])
    extends ApronCallFrame[Data, Var, Texpr1Node](apron, initData, v => Some(v), v => Some(v), identity, identity, initVars)

  def instantiateFloatOps() : (ApronFloatOps[Float], Apron) =
    implicit val failure: Failure = new ConcreteFailure
    val manager = new Polka(false)
    val alloc = new ApronAllocRoundRobin(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    var callFrame: FloatApronCallFrame[String, String] = null
    implicit val effects: EffectStack = new EffectStack(List(callFrame))
    callFrame = new FloatApronCallFrame(apron, "initial call frame")
    implicit val orderOps: ApronOrderingOps = new ApronOrderingOps
    implicit val eqOps: ApronEqOps = new ApronEqOps
    val floatOps = new ApronFloatOps[Float]
    (floatOps, apron)

  def convertToFloat(x : Interval) : Interval =
    val inf, sup = Mpfr(24)
    x.inf.toMpfr(inf, 0)
    x.sup.toMpfr(sup, 0)
    Interval(inf.doubleValue(24).toFloat, sup.doubleValue(24).toFloat)

  test("Floating Lit : Positive"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floatingLit(3.4))) == Interval(3.4f, 3.4f))
  }

  test("Floating Lit : Negative"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floatingLit(-10.5))) == Interval(-10.5f, -10.5f))
  }

  test("Integer Lit : Positive") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floatingLit(4))) == Interval(4f, 4f))
  }

  test("Integer Lit : Negative") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floatingLit(-10))) == Interval(-10f, -10f))
  }

  test("Floating Lit : MaxFloat"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floatingLit(Float.MaxValue))) == Interval(Float.MaxValue, Float.MaxValue))
  }

  test("Floating Lit : MinFloat") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floatingLit(Float.MinValue))) == Interval(Float.MinValue, Float.MinValue))
  }

  test("Floating Lit : Smallest Float") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floatingLit(Float.MinPositiveValue))) == Interval(Float.MinPositiveValue, Float.MinPositiveValue))
  }

  // This failing test case show that it's not possible to approximate soundly the behavior of floating point operation because of the rational conversion done by apron
  // This test should pass if the apron manager doesn't convert to rational
  test("Floating Lit : Significand size") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(apron.getBound(floatOps.sub(floatOps.floatingLit(Math.pow(2,24).toFloat), floatOps.add(floatOps.floatingLit(Math.pow(2,24).toFloat), floatOps.floatingLit(1)))) == Interval(Math.pow(2,24).toFloat - (Math.pow(2,24)+1).toFloat, Math.pow(2,24).toFloat - (Math.pow(2,24)+1).toFloat))
  }

//  test("Random Integer"){
//    val (floatOps, apron) = instantiateFloatOps()
//    assert(convertToFloat(apron.getBound(x.node)) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
//  }

  test("Addition"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.add(floatOps.floatingLit(-1), floatOps.floatingLit(1)))) == Interval(0f, 0f))
  }

  test("Addition : Positive"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.add(floatOps.floatingLit(1.5), floatOps.floatingLit(0.25)))) == Interval(1.75f, 1.75f))
  }

  test("Addition : Negative"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.add(floatOps.floatingLit(-10f), floatOps.floatingLit(6.4)))) == Interval(-3.6f, -3.6f))
  }

  test("Addition : Top"){
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.add(x.node, floatOps.floatingLit(3.3)))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Substraction") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.sub(floatOps.floatingLit(-1), floatOps.floatingLit(1)))) == Interval(-2f, -2f))
  }

  test("Substraction : Positive") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.sub(floatOps.floatingLit(1.5), floatOps.floatingLit(0.25)))) == Interval(1.25f, 1.25f))
  }

  test("Substraction : Negative") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.sub(floatOps.floatingLit(-10f), floatOps.floatingLit(6.4f)))) == Interval(-16.4f, -16.4f))
  }

  test("Substraction : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.sub(x.node, floatOps.floatingLit(3.3)))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Multiplication : Top"){
  val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(2), x.node))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Multiplication : Positive Integers"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(1), floatOps.floatingLit(2)))) == Interval(2f, 2f))
  }

  test("Multiplication : Negative Integers"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(-2), floatOps.floatingLit(7)))) == Interval(-14f, -14f))
  }

  test("Multiplication : Zero and Integer"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(0), floatOps.floatingLit(2)))) == Interval(0f, 0f))
  }

  test("Multiplication : Integer and Zero"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(1), floatOps.floatingLit(0)))) == Interval(0f, 0f))
  }

  test("Multiplication : Top and Zero"){
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.mul(x.node, floatOps.floatingLit(0)))) == Interval(0f, 0f))
  }

  test("Multiplication : Positive Floats"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(4.5), floatOps.floatingLit(2)))) == Interval(9f, 9f))
  }

  test("Multiplication : Positive Floats less than one"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(9), floatOps.floatingLit(0.3)))) == Interval(9f * 0.3f, 9f * 0.3f))
  }

  test("Multiplication : Negative Floats"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(1f / 3), floatOps.floatingLit(-12f)))) == Interval(-4f, -4f))
  }

  test("Multiplication : Negative Floats less than one"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(-0.5f), floatOps.floatingLit(math.Pi.toFloat)))) == Interval(-math.Pi.toFloat / 2, -math.Pi.toFloat / 2))
  }

  test("Divison : Positive Integers"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.div(floatOps.floatingLit(5), floatOps.floatingLit(2)))) == Interval(2.5f, 2.5f))
  }

  test("Divison : Negative Integers"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.div(floatOps.floatingLit(-3), floatOps.floatingLit(2)))) == Interval(-1.5f, -1.5f))
  }

  test("Divison : Positive Floats"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.div(floatOps.floatingLit(5.5), floatOps.floatingLit(2)))) == Interval(2.75f, 2.75f))
  }

  test("Divison : Positive Floats less than one"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.div(floatOps.floatingLit(0.5), floatOps.floatingLit(0.25)))) == Interval(2f, 2f))
  }

  test("Divison : Negative Floats"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.div(floatOps.floatingLit(-2.5), floatOps.floatingLit(10)))) == Interval(-0.25f, -0.25f))
  }

  test("Divison : Negative Floats less than one"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.div(floatOps.floatingLit(-0.1), floatOps.floatingLit(3)))) == Interval(-1 / 30f, -1 / 30f))
  }

  test("Division : by zero") {
    val (floatOps, apron) = instantiateFloatOps()
    try {
      val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
      floatOps.div(x.node, floatOps.floatingLit(0))
      assert(false)
    }
    catch {
      case _: SturdyFailure => assert(true)
      case _: Throwable => assert(false)
    }
  }

  test("Min"){
    val(floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.min(floatOps.floatingLit(2.5), floatOps.floatingLit(-6.1)))) == Interval(-6.1f, -6.1f))
  }

  test("Min : Top"){
    val(floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.min(floatOps.floatingLit(2.5), x.node))) == Interval(Float.NegativeInfinity, 2.5f))
  }

  test("Max") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.max(floatOps.floatingLit(2.5), floatOps.floatingLit(-6.1)))) == Interval(2.5f, 2.5f))
  }

  test("Max : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.max(floatOps.floatingLit(2.5), x.node))) == Interval(2.5f, Float.PositiveInfinity))
  }

  test("Absolute") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.absolute(floatOps.floatingLit(-3.4)))) == Interval(3.4f, 3.4f))
  }

  test("Absolute : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.absolute(x.node))) == Interval(0, Float.PositiveInfinity))
  }

  test("Negated : Positive") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.negated(floatOps.floatingLit(3.2)))) == Interval(-3.2f, -3.2f))
  }

  test("Negated : Negative") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.negated(floatOps.floatingLit(-6.666)))) == Interval(6.666f, 6.666f))
  }

  test("Negated : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.negated(x.node))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  // ???
  test("Squareroot : Positive") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.sqrt(floatOps.floatingLit(8f)))) == Interval(Math.sqrt(8).toFloat, Math.sqrt(8).toFloat))
  }

  test("Squareroot : Negative") {
    val (floatOps, _) = instantiateFloatOps()
    try {
      floatOps.sqrt(floatOps.floatingLit(-3.5))
      assert(false)
    }
    catch {
      case _: SturdyFailure => assert(true)
      case _: Throwable => assert(false)
    }
  }

  test("Squareroot : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.sqrt(x.node))) == Interval(0f, Float.PositiveInfinity))
  }

  test("Ceiling"){
  val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.ceil(floatOps.floatingLit(4.5)))) == Interval(5, 5))
  }

  test("Ceiling : Top"){
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.ceil(x.node))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Floor") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floor(floatOps.floatingLit(4.5)))) == Interval(4, 4))
  }

  test("Floor : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.floor(x.node))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Truncate") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.truncate(floatOps.floatingLit(4.5)))) == Interval(4, 4))
  }

  test("Truncate : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.truncate(x.node))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Nearest") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.nearest(floatOps.floatingLit(4.6f)))) == Interval(5, 5))
  }

  test("Nearest : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.nearest(x.node))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Copysign : Positive Negative") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.copysign(floatOps.floatingLit(4.5), floatOps.floatingLit(-6)))) == Interval(-4.5, -4.5))
  }

  test("Copysign : Positive Positive") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.copysign(floatOps.floatingLit(4.5), floatOps.floatingLit(6)))) == Interval(4.5, 4.5))
  }

  test("Copysign : Negative Negative") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.copysign(floatOps.floatingLit(-4.5), floatOps.floatingLit(-6)))) == Interval(-4.5, -4.5))
  }

  test("Copysign : Negative Positive") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.copysign(floatOps.floatingLit(-4.5), floatOps.floatingLit(6)))) == Interval(4.5, 4.5))
  }

  test("Copysign : Top negative") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.copysign(x.node, floatOps.floatingLit(-1)))) == Interval(Float.NegativeInfinity, 0))
  }

  test("Copysign : Top positive") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.copysign(x.node, floatOps.floatingLit(8)))) == Interval(0, Float.PositiveInfinity))
  }

  test("Copysign : Positive TOP") {
    val (floatOps, apron) = instantiateFloatOps()
    val x = apron.addDoubleVariable("x", ApronAllocationSite.LocalIntVar("x"))
    assert(convertToFloat(apron.getBound(floatOps.copysign(floatOps.floatingLit(-6), x.node))) == Interval(-6f, 6f))
  }
