package sturdy.values.conversion

import sturdy.effect.failure.Failure

import java.lang.{Double => JDouble}

trait ConvertLongDoubleOps[L, D]:
  def doubleToLong(d: D): L
  def doubleToLongUnsigned(d: D): L
  def doubleToLongExact(d: D): L
  def doubleToLongExactUnsigned(d: D): L
  def doubleToLongSaturating(d: D): L
  def doubleToLongSaturatingUnsigned(d: D): L
  def doubleToRawLong(d: D): L

  def longToDouble(l: L): D
  def longToDoubleUnsigned(l: L): D
  def longToRawDoulbe(l: L): D

given concreteConvertLongDoubleOps(using f: Failure): ConvertLongDoubleOps[Long, Double] with
  def doubleToLong(d: Double): Long = d.toLong
  def doubleToLongUnsigned(d: Double): Long = d.toLong.toLong
  def doubleToLongExact(d: Double): Long =
    if (d.isNaN)
      f.fail(ConversionFailure, s"double $d cannot be converted")
    else if (d >= -Long.MinValue.toDouble || d < Long.MinValue.toDouble)
      f.fail(ConversionFailure, s"double $d out of long range")
    else
      d.toLong
  def doubleToLongExactUnsigned(d: Double): Long =
    if (d.isNaN)
      f.fail(ConversionFailure, s"double $d cannot be converted")
    else if (d >= -Long.MinValue.toDouble * 2.0d || d <= -1.0d)
      f.fail(ConversionFailure, s"double $d out of long range")
    else if (d >= -Long.MinValue.toDouble)
      (d - 9223372036854775808.0d).toLong | Long.MinValue
    else
      d.toLong
  def doubleToLongSaturating(d: Double): Long =
    if (d.isNaN)
      0
    else if (d >= -Long.MinValue.toDouble)
      Long.MaxValue
    else if (d < Long.MinValue.toDouble)
      Long.MinValue
    else
      d.toLong
  def doubleToLongSaturatingUnsigned(d: Double): Long =
    if (d.isNaN)
      0
    else if (d >= -Long.MinValue.toDouble * 2.0d)
      -1
    else if (d < 0.0d)
      0
    else if (d >= -Long.MinValue.toDouble)
      (d - 9223372036854775808.0d).toLong | Long.MinValue
    else
      d.toLong
  def doubleToRawLong(d: Double): Long = JDouble.doubleToRawLongBits(d)

  def longToDouble(l: Long): Double = l.toDouble
  def longToDoubleUnsigned(l: Long): Double =
    if (l >= 0L)
      l.toDouble
    else
      ((l >>> 1) | (l & 1L)) * 2.0d
  def longToRawDoulbe(l: Long): Double = JDouble.longBitsToDouble(l)
