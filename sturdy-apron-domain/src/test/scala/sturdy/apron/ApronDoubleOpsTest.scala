package sturdy.apron

import apron.*
import gmp.*
import org.scalatest.funsuite.AnyFunSuite
import sturdy.apron.JoinTexpr1Node
import sturdy.data.{CombineUnit, JOptionC, noJoin}
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.failure.*
import sturdy.effect.{ComputationJoiner, EffectStack, SturdyFailure}
import sturdy.values.floating.ApronFloatOps
import sturdy.values.integer.{ApronIntegerOps, ConcreteIntegerOps, IntegerDivisionByZero, IntervalIntegerOps, given}
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}
import sturdy.values.utils.given
import sturdy.values.{Join, Topped, Widen, given}

class ApronDoubleOpsTest extends AnyFunSuite:

  def instantiateDoubleOps() : (ApronFloatOps[Double], Apron) =
    implicit val failure: Failure = new ConcreteFailure
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    implicit val effects: EffectStack = new EffectStack(List(failure, apron))
    implicit val orderOps: ApronOrderingOps = new ApronOrderingOps
    implicit val eqOps: ApronEqOps = new ApronEqOps
    val doubleOps = new ApronFloatOps[Double]
    (doubleOps, apron)

  def convertToDouble(x : Interval) : Interval =
    val inf, sup = new Mpfr(53)
    x.inf.toMpfr(inf, 0)
    x.sup.toMpfr(sup, 0)
    Interval(inf.doubleValue(53), sup.doubleValue(53))

  test("Floating Lit : Positive"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floatingLit(3.4))) == Interval(3.4, 3.4))
  }

  test("Floating Lit : Negative"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floatingLit(-10.5))) == Interval(-10.5, -10.5))
  }

  test("Integer Lit : Positive") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floatingLit(4))) == Interval(4, 4))
  }

  test("Integer Lit : Negative") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floatingLit(-10))) == Interval(-10, -10))
  }

  test("Floating Lit : MaxDouble"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floatingLit(Double.MaxValue))) == Interval(Double.MaxValue, Double.MaxValue))
  }

  test("Floating Lit : MinDouble") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floatingLit(Double.MinValue))) == Interval(Double.MinValue, Double.MinValue))
  }

  test("Floating Lit : Smallest Double") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floatingLit(Double.MinPositiveValue))) == Interval(Double.MinPositiveValue, Double.MinPositiveValue))
  }

  test("Floating Lit : Significand size") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(apron.getBound(doubleOps.sub(doubleOps.floatingLit(Math.pow(2,52) ), doubleOps.add(doubleOps.floatingLit(Math.pow(2,52)), doubleOps.floatingLit(1)))) == Interval(Math.pow(2,53) + 1, Math.pow(2, 53) + 1))
  }

  test("Random Integer"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.randomFloat())) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Addition"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.add(doubleOps.floatingLit(-1), doubleOps.floatingLit(1)))) == Interval(0, 0))
  }

  test("Addition : Positive"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.add(doubleOps.floatingLit(1.5), doubleOps.floatingLit(0.25)))) == Interval(1.75, 1.75))
  }

  test("Addition : Negative"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.add(doubleOps.floatingLit(-10), doubleOps.floatingLit(6.4)))) == Interval(-3.6, -3.6))
  }

  test("Addition : Top"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.add(doubleOps.randomFloat(), doubleOps.floatingLit(3.3)))) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Substraction") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.sub(doubleOps.floatingLit(-1), doubleOps.floatingLit(1)))) == Interval(-2, -2))
  }

  test("Substraction : Positive") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.sub(doubleOps.floatingLit(1.5), doubleOps.floatingLit(0.25)))) == Interval(1.25, 1.25))
  }

  test("Substraction : Negative") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.sub(doubleOps.floatingLit(-10), doubleOps.floatingLit(6.4)))) == Interval(-16.4, -16.4))
  }

  test("Substraction : Top") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.sub(doubleOps.randomFloat(), doubleOps.floatingLit(3.3)))) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Multiplication : Top"){
  val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.floatingLit(2), doubleOps.randomFloat()))) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Multiplication : Positive Integers"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.floatingLit(1), doubleOps.floatingLit(2)))) == Interval(2, 2))
  }

  test("Multiplication : Negative Integers"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.floatingLit(-2), doubleOps.floatingLit(7)))) == Interval(-14, -14))
  }

  test("Multiplication : Zero and Integer"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.floatingLit(0), doubleOps.floatingLit(2)))) == Interval(0, 0))
  }

  test("Multiplication : Integer and Zero"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.floatingLit(1), doubleOps.floatingLit(0)))) == Interval(0, 0))
  }

  test("Multiplication : Top and Zero"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.randomFloat(), doubleOps.floatingLit(0)))) == Interval(0, 0))
  }

  test("Multiplication : Positive Floats"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.floatingLit(4.5), doubleOps.floatingLit(2)))) == Interval(9, 9))
  }

  test("Multiplication : Positive Floats less than one"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.floatingLit(9), doubleOps.floatingLit(0.3)))) == convertToDouble(Interval(9d*0.3d, 9d*0.3d)))
  }

  test("blablabla"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floatingLit(9d*0.3d))) == Interval(9d*0.3d, 9d*0.3d))
  }

  test("Multiplication : Negative Floats"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.floatingLit(1d/3d), doubleOps.floatingLit(-12)))) == Interval(-4, -4))
  }

  test("Multiplication : Negative Floats less than one"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.mul(doubleOps.floatingLit(-0.5), doubleOps.floatingLit(math.Pi)))) == Interval(-math.Pi / 2d, -math.Pi / 2d))
  }

  test("Divison : Positive Integers"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.div(doubleOps.floatingLit(5), doubleOps.floatingLit(2)))) == Interval(2.5, 2.5))
  }

  test("Divison : Negative Integers"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.div(doubleOps.floatingLit(-3), doubleOps.floatingLit(2)))) == Interval(-1.5, -1.5))
  }

  test("Divison : Positive Floats"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.div(doubleOps.floatingLit(5.5), doubleOps.floatingLit(2)))) == Interval(2.75, 2.75))
  }

  test("Divison : Positive Floats less than one"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.div(doubleOps.floatingLit(0.5), doubleOps.floatingLit(0.25)))) == Interval(2, 2))
  }

  test("Divison : Negative Floats"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.div(doubleOps.floatingLit(-2.5), doubleOps.floatingLit(10)))) == Interval(-0.25, -0.25))
  }

  test("Divison : Negative Floats less than one"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.div(doubleOps.floatingLit(-0.1), doubleOps.floatingLit(3)))) == Interval(-1 / 30d, -1 / 30d))
  }

  test("Division : by zero") {
    val (doubleOps, _) = instantiateDoubleOps()
    try {
      doubleOps.div(doubleOps.randomFloat(), doubleOps.floatingLit(0))
      assert(false)
    }
    catch {
      case _: SturdyFailure => assert(true)
      case _: Throwable => assert(false)
    }
  }

  test("Min"){
    val(doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.min(doubleOps.floatingLit(2.5), doubleOps.floatingLit(-6.1)))) == Interval(-6.1, -6.1))
  }

  test("Min : Top"){
    val(doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.min(doubleOps.floatingLit(2.5), doubleOps.randomFloat()))) == Interval(Double.NegativeInfinity, 2.5))
  }

  test("Max") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.max(doubleOps.floatingLit(2.5), doubleOps.floatingLit(-6.1)))) == Interval(2.5, 2.5))
  }

  test("Max : Top") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.max(doubleOps.floatingLit(2.5), doubleOps.randomFloat()))) == Interval(2.5, Double.PositiveInfinity))
  }

  test("Absolute") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.absolute(doubleOps.floatingLit(-3.4)))) == Interval(3.4, 3.4))
  }

  test("Absolute : Top") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.absolute(doubleOps.randomFloat()))) == Interval(0, Double.PositiveInfinity))
  }

  test("Negated : Positive") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.negated(doubleOps.floatingLit(3.2)))) == Interval(-3.2, -3.2))
  }

  test("Negated : Negative") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.negated(doubleOps.floatingLit(-6.666)))) == Interval(6.666, 6.666))
  }

  test("Negated : Top") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.negated(doubleOps.randomFloat()))) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  // ???
  test("Squareroot : Positive") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.sqrt(doubleOps.floatingLit(8f)))) == Interval(Math.sqrt(8), Math.sqrt(8)))
  }

  test("Squareroot : Negative") {
    val (doubleOps, _) = instantiateDoubleOps()
    try {
      doubleOps.sqrt(doubleOps.floatingLit(-3.5))
      assert(false)
    }
    catch {
      case _: SturdyFailure => assert(true)
      case _: Throwable => assert(false)
    }
  }

  test("Squareroot : Top") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.sqrt(doubleOps.randomFloat()))) == Interval(0, Double.PositiveInfinity))
  }

  test("Ceiling"){
  val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.ceil(doubleOps.floatingLit(4.5)))) == Interval(5, 5))
  }

  test("Ceiling : Top"){
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.ceil(doubleOps.randomFloat()))) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Floor") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floor(doubleOps.floatingLit(4.5)))) == Interval(4, 4))
  }

  test("Floor : Top") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.floor(doubleOps.randomFloat()))) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Truncate") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.truncate(doubleOps.floatingLit(4.5)))) == Interval(4, 4))
  }

  test("Truncate : Top") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.truncate(doubleOps.randomFloat()))) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Nearest") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.nearest(doubleOps.floatingLit(4.6f)))) == Interval(5, 5))
  }

  test("Nearest : Top") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.nearest(doubleOps.randomFloat()))) == Interval(Double.NegativeInfinity, Double.PositiveInfinity))
  }

  test("Copysign : Positive Negative") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.copysign(doubleOps.floatingLit(4.5), doubleOps.floatingLit(-6)))) == Interval(-4.5, -4.5))
  }

  test("Copysign : Positive Positive") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.copysign(doubleOps.floatingLit(4.5), doubleOps.floatingLit(6)))) == Interval(4.5, 4.5))
  }

  test("Copysign : Negative Negative") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.copysign(doubleOps.floatingLit(-4.5), doubleOps.floatingLit(-6)))) == Interval(-4.5, -4.5))
  }

  test("Copysign : Negative Positive") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.copysign(doubleOps.floatingLit(-4.5), doubleOps.floatingLit(6)))) == Interval(4.5, 4.5))
  }

  test("Copysign : Top negative") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.copysign(doubleOps.randomFloat(), doubleOps.floatingLit(-1)))) == Interval(Double.NegativeInfinity, 0))
  }

  test("Copysign : Top positive") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.copysign(doubleOps.randomFloat(), doubleOps.floatingLit(8)))) == Interval(0, Double.PositiveInfinity))
  }

  test("Copysign : Positive TOP") {
    val (doubleOps, apron) = instantiateDoubleOps()
    assert(convertToDouble(apron.getBound(doubleOps.copysign(doubleOps.floatingLit(-6), doubleOps.randomFloat()))) == Interval(-6, 6))
  }
