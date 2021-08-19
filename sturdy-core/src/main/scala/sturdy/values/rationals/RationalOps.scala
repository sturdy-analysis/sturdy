package sturdy.values.rationals

import org.apache.commons.math3.exception.MathArithmeticException
import sturdy.effect.failure.{Failure, FailureKind}
import org.apache.commons.math3.fraction.Fraction

case object RatioDivisionByZero extends FailureKind

trait RationalOps[V]:
  def rationalLit(i1: Int, i2: Int): V
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V

  def max(v1: V, v2: V): V
  def min(v1: V, v2: V): V

  def absolute(v: V): V
  def floor(v: V): V
  def ceil(v: V): V

object Rational:
  def apply(i1: Int, i2: Int)(using f: Failure): Fraction =
    try new Fraction(i1, i2)
    catch {
      case _: MathArithmeticException => f.fail(RatioDivisionByZero, s"$i1 / $i2")
    }

given concreteRationalOps(using f: Failure): RationalOps[Fraction] with
  def rationalLit(i1: Int, i2: Int): Fraction = Rational(i1, i2)
  def add(v1: Fraction, v2: Fraction): Fraction = v1.add(v2)
  def sub(v1: Fraction, v2: Fraction): Fraction = v1.subtract(v2)
  def mul(v1: Fraction, v2: Fraction): Fraction = v1.multiply(v2)
  def div(v1: Fraction, v2: Fraction): Fraction = v1.divide(v2)
  def max(v1: Fraction, v2: Fraction): Fraction = if (v1.compareTo(v2) >= 0) v1 else v2
  def min(v1: Fraction, v2: Fraction): Fraction = if (v1.compareTo(v2) <= 0) v1 else v2
  def absolute(v: Fraction): Fraction = v.abs()
  override def floor(v: Fraction): Fraction =
    if (v.getDenominator == 1)
      v
    else
      new Fraction(Math.floorDiv(v.getNumerator, v.getDenominator))
  override def ceil(v: Fraction): Fraction = {
    val denom = v.getDenominator
    if (denom == 1)
      v
    else {
      val num = v.getNumerator
      var r = num / denom
      // if the signs are different and modulo not zero, round up
      if ((num ^ denom) < 0 && (r * denom != num)) r += 1
      new Fraction(r)
    }
  }

trait ConvertIntRationalOps[I, R]:
  def intToRational(i: I): R
  def rationalToInt(r: R): I
given concreteConvertIntRationalOps: ConvertIntRationalOps[Int, Fraction] with
  def intToRational(i: Int): Fraction = new Fraction(i)
  def rationalToInt(r: Fraction): Int = r.intValue()

trait ConvertDoubleRationalOps[D, R]:
  def doubleToRational(d: D): R
  def rationalToDouble(r: R): D
given concreteConvertDoubleRationalOps: ConvertDoubleRationalOps[Double, Fraction] with
  def doubleToRational(d: Double): Fraction = new Fraction(d)
  def rationalToDouble(r: Fraction): Double = r.doubleValue()
