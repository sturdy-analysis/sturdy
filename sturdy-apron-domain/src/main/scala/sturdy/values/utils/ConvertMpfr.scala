package sturdy.values.utils

import gmp.Mpfr

trait ConvertMpfr[B:Numeric]:
  def apply(a: Mpfr) : B

given ConvertMpfr[Int] with
  override def apply(a: Mpfr): Int = a.doubleValue(53).toInt

given ConvertMpfr[Long] with
  override def apply(a: Mpfr): Long = a.doubleValue(53).toLong

given ConvertMpfr[Float] with
  override def apply(a: Mpfr): Float = a.doubleValue(53).toFloat

given ConvertMpfr[Byte] with
  override def apply(a: Mpfr): Byte = a.doubleValue(53).toByte

given ConvertMpfr[Double] with
  override def apply(a: Mpfr): Double = a.doubleValue(53)

