package sturdy.values.floating

import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.config
import sturdy.values.config.UnsupportedConfiguration
import sturdy.values.convert.*
import sturdy.values.relational.CompareOps
import sturdy.values.relational.OrderingOps
import sturdy.values.relational.EqOps

import scala.util.Random
import java.lang.Float as JFloat
import java.lang.Double as JDouble
import java.nio.ByteBuffer
import java.nio.ByteOrder

given Structural[Float] with {}

given ConcreteFloatOps: FloatOps[Float, Float] with
  def floatingLit(f: Float): Float = f
  def randomFloat(): Float = Random.nextFloat()
  def add(v1: Float, v2: Float): Float = v1 + v2
  def sub(v1: Float, v2: Float): Float = v1 - v2
  def mul(v1: Float, v2: Float): Float = v1 * v2
  def div(v1: Float, v2: Float): Float = v1 / v2

  def min(v1: Float, v2: Float): Float = Math.min(v1, v2)
  def max(v1: Float, v2: Float): Float = Math.max(v1, v2)

  def remainder(v1: Float, v2: Float): Float = v1 % v2

  def absolute(v: Float): Float = Math.abs(v)
  def negated(v: Float): Float = -v
  def sqrt(v: Float): Float = Math.sqrt(v).toFloat
  def ceil(v: Float): Float = v.ceil
  def floor(v: Float): Float = v.floor
  def truncate(v: Float): Float = if (v < 0) v.ceil else v.floor
  def nearest(v: Float): Float =
  // copied from https://github.com/satabin/swam/blob/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime/F32.scala#L30
    if (v.isInfinite || v.isNaN || v.isWhole)
      v
    else
      Math.copySign((Math.round(v / 2) * 2).toFloat, v)
  def copysign(v: Float, sign: Float): Float = Math.copySign(v, sign)


given EqOps[Float, Boolean] with
  override def equ(v1: Float, v2: Float): Boolean = v1 == v2
  override def neq(v1: Float, v2: Float): Boolean = v1 != v2

given OrderingOps[Float, Boolean] with
  def lt(v1: Float, v2: Float): Boolean = v1 < v2
  def le(v1: Float, v2: Float): Boolean = v1 <= v2
  def ge(v1: Float, v2: Float): Boolean = v1 >= v2
  def gt(v1: Float, v2: Float): Boolean = v1 > v2

given CompareOps[Float, Int] with
  override def cmp(v1: Float, v2: Float): Int = v1.compareTo(v2)

given ConcreteConvertFloatInt(using fa: Failure): ConvertFloatInt[Float, Int] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(f: Float, conf: config.Overflow && config.Bits) = conf match
    case (_ && config.Bits.Raw) => JFloat.floatToRawIntBits(f)
    case (config.Overflow.Allow && config.Bits.Signed) => f.toInt
    case (config.Overflow.Allow && config.Bits.Unsigned) => f.toLong.toInt
    case (config.Overflow.Fail && config.Bits.Signed) =>
      if (f.isNaN)
        fa.fail(ConversionFailure, s"float $f cannot be converted")
      else if (f >= -Int.MinValue.toFloat || f < Int.MinValue.toFloat)
        fa.fail(ConversionFailure, s"float $f out of integer range")
      else
        f.toInt
    case (config.Overflow.Fail && config.Bits.Unsigned) =>
      if (f.isNaN)
        fa.fail(ConversionFailure, s"float $f cannot be converted")
      else if (f >= -Int.MinValue.toDouble * 2.0d || f <= -1.0f)
        fa.fail(ConversionFailure, s"float $f out of integer range")
      else
        f.toLong.toInt
    case (config.Overflow.JumpToBounds && config.Bits.Signed) =>
      if (f.isNaN)
        0
      else if (f >= -Int.MinValue.toFloat)
        Int.MaxValue
      else if (f < Int.MinValue.toFloat)
        Int.MinValue
      else
        f.toInt
    case (config.Overflow.JumpToBounds && config.Bits.Unsigned) =>
      if (f.isNaN)
        0
      else if (f >= -Int.MinValue.toFloat * 2.0f)
        -1
      else if (f < 0.0f)
        0
      else
        f.toLong.toInt
    case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)


given ConcreteConvertFloatLong(using fa: Failure): ConvertFloatLong[Float, Long] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(f: Float, conf: config.Overflow && config.Bits) = conf match
    case (config.Overflow.Allow && config.Bits.Signed) => f.toLong
//    case (config.Overflow.Allow, config.Bits.Unsigned) => ???
    case (config.Overflow.Fail && config.Bits.Signed) =>
      if (f.isNaN)
        fa.fail(ConversionFailure, s"float $f cannot be converted")
      else if (f >= -Long.MinValue.toFloat || f < Long.MinValue.toFloat)
        fa.fail(ConversionFailure, s"float $f out of long range")
      else
        f.toLong
    case (config.Overflow.Fail && config.Bits.Unsigned) =>
      if (f.isNaN)
        fa.fail(ConversionFailure, s"float $f cannot be converted")
      else if (f >= -Long.MinValue.toFloat * 2.0d || f <= -1.0d)
        fa.fail(ConversionFailure, s"float $f out of long range")
      else if (f >= -Long.MinValue.toFloat)
        (f - 9223372036854775808.0d).toLong | Long.MinValue
      else
        f.toLong
    case (config.Overflow.JumpToBounds && config.Bits.Signed) =>
      if (f.isNaN)
        0
      else if (f >= -Long.MinValue.toFloat)
        Long.MaxValue
      else if (f < Long.MinValue.toFloat)
        Long.MinValue
      else
        f.toLong
    case (config.Overflow.JumpToBounds && config.Bits.Unsigned) =>
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
    case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)


given ConcreteConvertFloatDouble: ConvertFloatDouble[Float, Double] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  override def apply(f: Float, conf: NilCC.type): Double =
    if (!f.isNaN) {
      f.toDouble
    } else {
      val nan32bits = JFloat.floatToRawIntBits(f) & 0X00000000FFFFFFFFL
      val signField = (nan32bits >>> 31) << 63
      val significandField = (nan32bits << 41) >>> 12
      val fields = signField | significandField
      val nan64bits = 0X7FF8000000000000L | fields
      JDouble.longBitsToDouble(nan64bits)
    }

given ConcreteConvertFloatBytes: ConvertFloatBytes[Float, Seq[Byte]] with
  override def apply(from: Float, conf: SomeCC[ByteOrder]): Seq[Byte] =
    val buf = ByteBuffer.allocate(4)
    buf.order(conf.t)
    buf.putFloat(0, from)
    buf.array().toSeq

given ConcreteConvertBytesFloat: ConvertBytesFloat[Seq[Byte], Float] with
  override def apply(from: Seq[Byte], conf: SomeCC[ByteOrder]): Float =
    val buf = ByteBuffer.wrap(from.toArray)
    buf.order(conf.t)
    buf.getFloat
