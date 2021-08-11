package sturdy.values.conversion

import sturdy.effect.failure.Failure

import java.lang.{Float => JFloat}

trait ConvertIntFloatOps[I, F]:
  def floatToInt(f: F): I
  def floatToIntUnsigned(f: F): I
  def floatToIntExact(f: F): I
  def floatToIntExactUnsigned(f: F): I
  def floatToIntSaturating(f: F): I
  def floatToIntSaturatingUnsigned(f: F): I
  def floatToRawInt(f: F): I

  def intToFloat(i: I): F
  def intToFloatUnsigned(i: I): F
  def intToRawFloat(i: I): F


given concreteConvertIntFloatOps(using fa: Failure): ConvertIntFloatOps[Int, Float] with
  def floatToInt(f: Float): Int = f.toInt
  def floatToIntUnsigned(f: Float): Int = f.toLong.toInt
  def floatToIntExact(f: Float): Int =
    if (f.isNaN)
      fa.fail(ConversionFailure, s"float $f cannot be converted")
    else if (f >= -Int.MinValue.toFloat || f <= Int.MinValue.toFloat - 1)
      fa.fail(ConversionFailure, s"float $f out of integer range")
    else
      f.toInt
  def floatToIntExactUnsigned(f: Float): Int =
    if (f.isNaN)
      fa.fail(ConversionFailure, s"float $f cannot be converted")
    else if (f >= -Int.MinValue.toDouble * 2.0d || f <= -1.0f)
      fa.fail(ConversionFailure, s"float $f out of integer range")
    else
      f.toLong.toInt
  def floatToIntSaturating(f: Float): Int =
    if (f.isNaN)
      0
    else if (f >= -Int.MinValue.toFloat)
      Int.MaxValue
    else if (f < Int.MinValue.toFloat)
      Int.MinValue
    else
      f.toInt
  def floatToIntSaturatingUnsigned(f: Float): Int =
    if (f.isNaN)
      0
    else if (f >= -Int.MinValue.toFloat * 2.0f)
      -1
    else if (f < 0.0f)
      0
    else
      f.toLong.toInt
  def floatToRawInt(f: Float): Int = JFloat.floatToRawIntBits(f)

  def intToFloat(i: Int): Float = i.toFloat
  def intToFloatUnsigned(i: Int): Float =
    if (i >= 0)
      i.toFloat
    else
      ((i >>> 1) | (i & 1)).toFloat * 2.0f
  def intToRawFloat(i: Int): Float = JFloat.intBitsToFloat(i)
