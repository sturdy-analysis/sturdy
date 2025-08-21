package sturdy.values.floating

import sturdy.values.convert.{&&, *}
import sturdy.values.config
import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.config.{BitSign, Overflow, UnsupportedConfiguration, unsupportedConfiguration}
import sturdy.values.ordering.OrderingOps
import sturdy.values.ordering.EqOps

import scala.util.Random
import java.lang.Float as JFloat
import java.lang.Double as JDouble
import java.nio.ByteBuffer
import java.nio.ByteOrder

given Structural[Double] with {}

given ConcreteDoubleOps: FloatOps[Double, Double] with
  def floatingLit(f: Double): Double = f
  def randomFloat(): Double = Random.nextDouble()
  def add(v1: Double, v2: Double): Double = v1 + v2
  def sub(v1: Double, v2: Double): Double = v1 - v2
  def mul(v1: Double, v2: Double): Double = v1 * v2
  def div(v1: Double, v2: Double): Double = v1 / v2

  def min(v1: Double, v2: Double): Double = Math.min(v1, v2)
  def max(v1: Double, v2: Double): Double = Math.max(v1, v2)

  def absolute(v: Double): Double = Math.abs(v)
  def negated(v: Double): Double = -v
  def sqrt(v: Double): Double = Math.sqrt(v)
  def ceil(v: Double): Double = v.ceil
  def floor(v: Double): Double = v.floor
  def truncate(v: Double): Double = if (v < 0) v.ceil else v.floor
  def nearest(v: Double): Double =
  // copied from https://github.com/satabin/swam/blob/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime/F64.scala#L30
    if (v.isInfinite || v.isNaN || v.isWhole)
      v
    else
      Math.copySign((Math.round(v / 2) * 2).toDouble, v)
  def copysign(v: Double, sign: Double): Double = Math.copySign(v, sign)

  def logNatural(v: Double): Double = Math.log(v)

given EqOps[Double, Boolean] with
  override def equ(v1: Double, v2: Double): Boolean = v1 == v2
  override def neq(v1: Double, v2: Double): Boolean = v1 != v2

given OrderingOps[Double, Boolean] with
  def lt(v1: Double, v2: Double): Boolean = v1 < v2
  def le(v1: Double, v2: Double): Boolean = v1 <= v2

given ConcreteConvertDoubleInt(using f: Failure): ConvertDoubleInt[Double, Int] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(d: Double, conf: config.Overflow && config.BitSign) = conf match
    case (config.Overflow.Allow && config.BitSign.Signed) => d.toInt
    case (config.Overflow.Allow && config.BitSign.Unsigned) => d.toLong.toInt
    case (config.Overflow.Fail && config.BitSign.Signed) =>
      if (d.isNaN)
        f.fail(ConversionFailure, s"double $d cannot be converted")
      else if (d >= -Int.MinValue.toDouble || d <= Int.MinValue.toDouble - 1)
        f.fail(ConversionFailure, s"double $d out of integer range")
      else
        d.toInt
    case (config.Overflow.JumpToBounds && config.BitSign.Signed) =>
      if (d.isNaN)
        0
      else if (d >= -Int.MinValue.toDouble)
        Int.MaxValue
      else if (d < Int.MinValue)
        Int.MinValue
      else
        d.toInt
    case (config.Overflow.Fail && config.BitSign.Unsigned) =>
      if (d.isNaN)
        f.fail(ConversionFailure, s"double $d cannot be converted")
      else if (d >= -Int.MinValue.toDouble * 2.0d || d <= -1.0d)
        f.fail(ConversionFailure, s"double $d out of integer range")
      else
        d.toLong.toInt
    case (config.Overflow.JumpToBounds && config.BitSign.Unsigned) =>
      if (d.isNaN)
        0
      else if (d >= -Int.MinValue.toDouble * 2.0d)
        -1
      else if (d < 0.0)
        0
      else
        d.toLong.toInt
    case _ => unsupportedConfiguration(conf, this)


given ConcreteConvertDoubleLong(using f: Failure): ConvertDoubleLong[Double, Long] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(d: Double, conf: config.Overflow && config.BitSign) = conf match
    case (_ && config.BitSign.Raw) => JDouble.doubleToRawLongBits(d)
    case (config.Overflow.Allow && config.BitSign.Signed) => d.toLong
//    case (config.Overflow.Allow, config.Bits.Unsigned) => ???
    case (config.Overflow.Fail && config.BitSign.Signed) =>
      if (d.isNaN)
        f.fail(ConversionFailure, s"double $d cannot be converted")
      else if (d >= -Long.MinValue.toDouble || d < Long.MinValue.toDouble)
        f.fail(ConversionFailure, s"double $d out of long range")
      else
        d.toLong
    case (config.Overflow.JumpToBounds && config.BitSign.Signed) =>
      if (d.isNaN)
        0
      else if (d >= -Long.MinValue.toDouble)
        Long.MaxValue
      else if (d < Long.MinValue.toDouble)
        Long.MinValue
      else
        d.toLong
    case (config.Overflow.Fail && config.BitSign.Unsigned) =>
      if (d.isNaN)
        f.fail(ConversionFailure, s"double $d cannot be converted")
      else if (d >= -Long.MinValue.toDouble * 2.0d || d <= -1.0d)
        f.fail(ConversionFailure, s"double $d out of long range")
      else if (d >= -Long.MinValue.toDouble)
        (d - 9223372036854775808.0d).toLong | Long.MinValue
      else
        d.toLong
    case (config.Overflow.JumpToBounds && config.BitSign.Unsigned) =>
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
    case _ => unsupportedConfiguration(conf, this)

given ConcreteConvertDoubleFloat: ConvertDoubleFloat[Double, Float] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  override def apply(d: Double, conf: NilCC.type): Float =
    if (!d.isNaN) {
      d.toFloat
    } else {
      val nan64bits = JDouble.doubleToRawLongBits(d)
      val signField = (nan64bits >>> 63) << 31
      val significandField = (nan64bits << 12) >>> 41
      val fields = signField | significandField
      val nan32bits = 0x7fc00000 | fields.toInt
      JFloat.intBitsToFloat(nan32bits)
    }

given ConcreteConvertDoubleBytes: ConvertDoubleBytes[Double, Seq[Byte]] with
  override def apply(from: Double, conf: config.BytesSize && SomeCC[ByteOrder]): Seq[Byte] =
    val buf = ByteBuffer.allocate(8)
    buf.order(conf.c2.t)
    buf.putDouble(0, from)
    collection.immutable.ArraySeq.unsafeWrapArray(buf.array())

given ConcreteConvertBytesDouble: ConvertBytesDouble[Seq[Byte], Double] with
  override def apply(from: Seq[Byte], conf: SomeCC[ByteOrder]): Double =
    val buf = ByteBuffer.wrap(from.toArray)
    buf.order(conf.t)
    buf.getDouble

