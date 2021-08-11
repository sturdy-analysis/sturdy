package sturdy.values.conversion

import sturdy.effect.failure.Failure

import java.lang.Double as JDouble

trait ConvertIntDoubleOps[I, D]:
  def doubleToInt(d: D): I
  def doubleToIntUnsigned(d: D): I
  def doubleToIntExact(d: D): I
  def doubleToIntExactUnsigned(d: D): I
  def doubleToIntSaturating(d: D): I
  def doubleToIntSaturatingUnsigned(d: D): I

  def intToDouble(i: I): D
  def intToDoubleUnsigned(i: I): D

given concreteConvertIntDoubleOps(using f: Failure): ConvertIntDoubleOps[Int, Double] with
  def doubleToInt(d: Double): Int = d.toInt
  def doubleToIntUnsigned(d: Double): Int = d.toLong.toInt
  def doubleToIntExact(d: Double): Int =
    if (d.isNaN)
      f.fail(ConversionFailure, s"double $d cannot be converted")
    else if (d >= -Int.MinValue.toDouble || d <= Int.MinValue.toDouble - 1)
      f.fail(ConversionFailure, s"double $d out of integer range")
    else
      d.toInt
  def doubleToIntExactUnsigned(d: Double): Int =
    if (d.isNaN)
      f.fail(ConversionFailure, s"double $d cannot be converted")
    else if (d >= -Int.MinValue.toDouble * 2.0d || d <= -1.0)
      f.fail(ConversionFailure, s"double $d out of integer range")
    else
      d.toLong.toInt
  def doubleToIntSaturating(d: Double): Int =
    if (d.isNaN)
      0
    else if (d >= -Int.MinValue.toDouble)
      Int.MaxValue
    else if (d < Int.MinValue)
      Int.MinValue
    else
      d.toInt
  def doubleToIntSaturatingUnsigned(d: Double): Int =
    if (d.isNaN)
      0
    else if (d >= -Int.MinValue.toDouble * 2.0d)
      -1
    else if (d < 0.0)
      0
    else
      d.toLong.toInt

  def intToDouble(i: Int): Double = i.toDouble
  def intToDoubleUnsigned(i: Int): Double = (i & 0X00000000FFFFFFFFL).toDouble
