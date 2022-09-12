package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.{Polka, Texpr1Node, *}
import gmp.*
import sturdy.data.{CombineUnit, JOptionC, noJoin}
import sturdy.apron.JoinTexpr1Node
import sturdy.effect.{ComputationJoiner, EffectStack, SturdyFailure}
import sturdy.values.integer.{ApronIntegerOps, ConcreteIntegerOps, IntegerDivisionByZero, IntervalIntegerOps, given}
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.failure.{AFallible, CollectedFailures, ConcreteFailure, Failure, FailureKind}
import sturdy.values.Join
import sturdy.values.Widen
import sturdy.values.floating.ApronFloatOps
import sturdy.values.{Topped, given}
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}
import sturdy.values.utils.given

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
    val inf, sup = new Mpfr()
    x.inf.toMpfr(inf, 64)
    x.sup.toMpfr(sup, 64)
    Interval(inf.doubleValue(64).toFloat, sup.doubleValue(64).toFloat)

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

  test("Floating Lit : MaxInt"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floatingLit(Float.MaxValue))) == Interval(Float.MaxValue, Float.MaxValue))
  }

  test("Floating Lit : MinInt") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floatingLit(Float.MinValue))) == Interval(Float.MinValue, Float.MinValue))
  }

  test("Random Integer"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.randomFloat())) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

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
    assert(convertToFloat(apron.getBound(floatOps.add(floatOps.randomFloat(), floatOps.floatingLit(3.3)))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
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
    assert(convertToFloat(apron.getBound(floatOps.sub(floatOps.randomFloat(), floatOps.floatingLit(3.3)))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Multiplication : Top"){
  val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.floatingLit(2), floatOps.randomFloat()))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
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
    assert(convertToFloat(apron.getBound(floatOps.mul(floatOps.randomFloat(), floatOps.floatingLit(0)))) == Interval(0f, 0f))
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
    val (floatOps, _) = instantiateFloatOps()
    try {
      floatOps.div(floatOps.randomFloat(), floatOps.floatingLit(0))
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
    assert(convertToFloat(apron.getBound(floatOps.min(floatOps.floatingLit(2.5), floatOps.randomFloat()))) == Interval(Float.NegativeInfinity, 2.5f))
  }

  test("Max") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.max(floatOps.floatingLit(2.5), floatOps.floatingLit(-6.1)))) == Interval(2.5f, 2.5f))
  }

  test("Max : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.max(floatOps.floatingLit(2.5), floatOps.randomFloat()))) == Interval(2.5f, Float.PositiveInfinity))
  }

  test("Absolute") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.absolute(floatOps.floatingLit(-3.4)))) == Interval(3.4f, 3.4f))
  }

  test("Absolute : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.absolute(floatOps.randomFloat()))) == Interval(0, Double.PositiveInfinity))
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
    assert(convertToFloat(apron.getBound(floatOps.negated(floatOps.randomFloat()))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  // ???
  test("Squareroot : Positive") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.sqrt(floatOps.floatingLit(8f)))) == Interval(Math.sqrt(10).toFloat, Math.sqrt(10).toFloat))
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
    assert(convertToFloat(apron.getBound(floatOps.sqrt(floatOps.randomFloat()))) == Interval(0f, Float.PositiveInfinity))
  }

  test("Ceiling"){
  val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.ceil(floatOps.floatingLit(4.5)))) == Interval(5, 5))
  }

  test("Ceiling : Top"){
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.ceil(floatOps.randomFloat()))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Floor") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floor(floatOps.floatingLit(4.5)))) == Interval(4, 4))
  }

  test("Floor : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.floor(floatOps.randomFloat()))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Truncate") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.truncate(floatOps.floatingLit(4.5)))) == Interval(4, 4))
  }

  test("Truncate : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.truncate(floatOps.randomFloat()))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
  }

  test("Nearest") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.nearest(floatOps.floatingLit(4.6f)))) == Interval(5, 5))
  }

  test("Nearest : Top") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.nearest(floatOps.randomFloat()))) == Interval(Float.NegativeInfinity, Float.PositiveInfinity))
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
    assert(convertToFloat(apron.getBound(floatOps.copysign(floatOps.randomFloat(), floatOps.floatingLit(-1)))) == Interval(Float.NegativeInfinity, 0))
  }

  test("Copysign : Top positive") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.copysign(floatOps.randomFloat(), floatOps.floatingLit(8)))) == Interval(0, Float.PositiveInfinity))
  }

  test("Copysign : Positive TOP") {
    val (floatOps, apron) = instantiateFloatOps()
    assert(convertToFloat(apron.getBound(floatOps.copysign(floatOps.floatingLit(-6), floatOps.randomFloat()))) == Interval(-6f, 6f))
  }
