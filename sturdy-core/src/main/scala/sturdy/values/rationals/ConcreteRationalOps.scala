package sturdy.values.rationals

import sturdy.effect.failure.Failure
import org.apache.commons.math3.exception.MathArithmeticException
import org.apache.commons.math3.fraction.Fraction

final class Rational(val f: Fraction) extends AnyVal:
  def isZero: Boolean = f == Fraction.ZERO
  override def toString: String = f.toString

object Rational:
  inline def apply(f: Fraction): Rational = new Rational(f)
  inline def apply(i: Int): Rational = new Rational(new Fraction(i))
  inline def apply(d: Double): Rational = new Rational(new Fraction(d))
  def apply(i1: Int, i2: Int)(using f: Failure): Rational =
    try new Rational(new Fraction(i1, i2))
    catch {
      case _: MathArithmeticException => f.fail(RationalDivisionByZero, s"$i1 / $i2")
    }

given concreteRationalOps(using f: Failure): RationalOps[Rational] with
  def rationalLit(i1: Int, i2: Int): Rational = Rational(i1, i2)
  def add(v1: Rational, v2: Rational): Rational = Rational(v1.f.add(v2.f))
  def sub(v1: Rational, v2: Rational): Rational = Rational(v1.f.subtract(v2.f))
  def mul(v1: Rational, v2: Rational): Rational = Rational(v1.f.multiply(v2.f))
  def div(v1: Rational, v2: Rational): Rational = Rational(v1.f.divide(v2.f))
  def max(v1: Rational, v2: Rational): Rational = if (v1.f.compareTo(v2.f) >= 0) v1 else v2
  def min(v1: Rational, v2: Rational): Rational = if (v1.f.compareTo(v2.f) <= 0) v1 else v2
  def absolute(v: Rational): Rational = Rational(v.f.abs())
  override def floor(v: Rational): Rational =
    if (v.f.getDenominator == 1)
      v
    else
      Rational(Math.floorDiv(v.f.getNumerator, v.f.getDenominator))
  override def ceil(v: Rational): Rational = {
    val denom = v.f.getDenominator
    if (denom == 1)
      v
    else {
      val num = v.f.getNumerator
      var r = num / denom
      // if the signs are different and modulo not zero, round up
      if ((num ^ denom) < 0 && (r * denom != num)) r += 1
      Rational(r)
    }
  }

given ConcreteConvertIntRational: ConvertIntRational[Int, Rational] with
  override def apply(i: Int, conf: Unit): Rational = Rational(i)
given ConcreteConvertRationalInt: ConvertRationalInt[Rational, Int] with
  override def apply(r: Rational, conf: Unit): Int = r.f.intValue()

given ConcreteConvertDoubleRational: ConvertDoubleRational[Double, Rational] with
  override def apply(d: Double, conf: Unit): Rational = Rational(new Fraction(d))
given ConcreteConvertRationalDouble: ConvertRationalDouble[Rational, Double] with
  override def apply(r: Rational, conf: Unit): Double = r.f.doubleValue()
