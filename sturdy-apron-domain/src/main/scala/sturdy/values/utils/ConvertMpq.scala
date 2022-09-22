package sturdy.values.utils

import gmp.Mpq

trait ConvertMpq[B:Numeric]:
  def apply(a: Mpq) : B

given ConvertMpq[Int] with
  override def apply(a: Mpq): Int = a.doubleValue().toInt

given ConvertMpq[Long] with
  override def apply(a: Mpq): Long = a.doubleValue().toLong

given ConvertMpq[Float] with
  override def apply(a: Mpq): Float = a.doubleValue().toFloat

given ConvertMpq[Byte] with
  override def apply(a: Mpq): Byte = a.doubleValue().toByte

given ConvertMpq[Double] with
  override def apply(a: Mpq): Double = a.doubleValue()

