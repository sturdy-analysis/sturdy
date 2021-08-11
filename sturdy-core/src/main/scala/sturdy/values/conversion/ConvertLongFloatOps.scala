package sturdy.values.conversion

import sturdy.effect.failure.Failure

import java.lang.{Long => JLong, Float => JFloat}

trait ConvertLongFloatOps[L, F]:
  def floatToLong(f: F): L
  def floatToLongUnsigned(f: F): L
  def floatToLongExact(f: F): L
  def floatToLongExactUnsigned(f: F): L
  def floatToLongSaturating(f: F): L
  def floatToLongSaturatingUnsigned(f: F): L

  def longToFloat(l: L): F
  def longToFloatUnsigned(l: L): F

given concreteConvertLongFloatOps(using fa: Failure): ConvertLongFloatOps[Long, Float] with
  def floatToLong(f: Float): Long = f.toLong
  def floatToLongUnsigned(f: Float): Long = f.toLong.toLong
  def floatToLongExact(f: Float): Long =
    if (f.isNaN)
      fa.fail(ConversionFailure, s"float $f cannot be converted")
    else if (f >= -Long.MinValue.toFloat || f < Long.MinValue.toFloat)
      fa.fail(ConversionFailure, s"float $f out of long range")
    else
      f.toLong
  def floatToLongExactUnsigned(f: Float): Long =
    if (f.isNaN)
      fa.fail(ConversionFailure, s"float $f cannot be converted")
    else if (f >= -Long.MinValue.toFloat * 2.0d || f <= -1.0d)
      fa.fail(ConversionFailure, s"float $f out of long range")
    else if (f >= -Long.MinValue.toFloat)
      (f - 9223372036854775808.0d).toLong | Long.MinValue
    else
      f.toLong
  def floatToLongSaturating(f: Float): Long =
    if (f.isNaN)
      0
    else if (f >= -Long.MinValue.toFloat)
      Long.MaxValue
    else if (f < Long.MinValue.toFloat)
      Long.MinValue
    else
      f.toLong
  def floatToLongSaturatingUnsigned(f: Float): Long =
    if (f.isNaN)
      0
    else if (f >= -Long.MinValue.toFloat * 2.0d)
      -1
    else if (f < 0.0d)
      0
    else if (f >= -Long.MinValue.toFloat)
      (f - 9223372036854775808.0d).toLong | Long.MinValue
    else
      f.toLong

  private val convC = JFloat.parseFloat("0x1p12")
  def longToFloat(l: Long): Float =
    if (Math.abs(l) < 0X10000000000000L) {
      l.toFloat
    } else {
      val r = if ((l & 0XFFFL) == 0L) 0L else 1L
      ((l >> 12) | r).toFloat * convC
    }
  def longToFloatUnsigned(l: Long): Float =
    if (JLong.compareUnsigned(l, 0X10000000000000L) < 0) {
      l.toFloat
    } else {
      val r = if ((l & 0XFFFL) == 0L) 0L else 1L
      ((l >>> 12) | r).toFloat * convC
    }
