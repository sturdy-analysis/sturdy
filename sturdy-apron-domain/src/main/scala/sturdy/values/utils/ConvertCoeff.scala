package sturdy.values.utils

import apron.Interval
import gmp.{Mpfr, Mpq}
import sturdy.values.integer.NumericInterval

trait ConvertCoeff[From, To]:
  def apply(a: From) : To


// Probably not sound
given ConvertCoeff[Mpq, Seq[Byte]] with
  override def apply(a: Mpq): Seq[Byte] = BigInt(a.doubleValue().toLong).toByteArray.toSeq

given ConvertCoeff[Mpq, Int] with
  override def apply(a: Mpq): Int = a.doubleValue().toInt

given ConvertCoeff[Mpq, Long] with
  override def apply(a: Mpq): Long = a.doubleValue().toLong

given ConvertCoeff[Mpq, Float] with
  override def apply(a: Mpq): Float = a.doubleValue().toFloat

given ConvertCoeff[Mpq, Double] with
  override def apply(a: Mpq): Double = a.doubleValue()

given ConvertCoeff[Mpfr, Int] with
  override def apply(a: Mpfr): Int = a.doubleValue(53).toInt

// Probably not sound
given ConvertCoeff[Mpfr, Seq[Byte]] with
  override def apply(a: Mpfr): Seq[Byte] = BigInt(a.doubleValue(53).toLong).toByteArray.toSeq

given ConvertCoeff[Mpfr, Long] with
  override def apply(a: Mpfr): Long = a.doubleValue(53).toLong

given ConvertCoeff[Mpfr, Float] with
  override def apply(a: Mpfr): Float = a.doubleValue(53).toFloat

given ConvertCoeff[Mpfr, Double] with
  override def apply(a: Mpfr): Double = a.doubleValue(53)