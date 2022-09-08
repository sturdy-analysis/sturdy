package sturdy.values.strings

import sturdy.data.JOption
import sturdy.data.JOptionC
import sturdy.data.MayJoin
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.convert.*
import sturdy.values.relational.{UnsignedOrderingOps, OrderingOps, EqOps}
import sturdy.values.config
import sturdy.values.config.UnsupportedConfiguration

import scala.util.Random
import java.lang.Float as JFloat
import java.nio.ByteBuffer
import java.nio.ByteOrder

given Structural[String] with {}

given ConcreteStringOps(using f: Failure): StringOps[String, String] with
  def stringLit(s: String): String = s

/*
given ConcreteStrictIntegerOps: StrictStringsOps[Int, Int, NoJoin] with
  override def addStrict(v1: Int, v2: Int): JOptionC[Int] =
    try JOptionC.Some(StrictMath.addExact(v1, v2))
    catch { case _: ArithmeticException => JOptionC.none }
  def subStrict(v1: Int, v2: Int): JOptionC[Int] =
    try JOptionC.Some(StrictMath.subtractExact(v1, v2))
    catch { case _: ArithmeticException => JOptionC.none }
  def mulStrict(v1: Int, v2: Int): JOptionC[Int] =
    try JOptionC.Some(StrictMath.multiplyExact(v1, v2))
    catch { case _: ArithmeticException => JOptionC.none }



given EqOps[Int, Boolean] with
  override def equ(v1: Int, v2: Int): Boolean = v1 == v2
  override def neq(v1: Int, v2: Int): Boolean = v1 != v2

given OrderingOps[Int, Boolean] with
  def lt(v1: Int, v2: Int): Boolean = v1 < v2
  def le(v1: Int, v2: Int): Boolean = v1 <= v2

given ConcreteConvertIntLong: ConvertIntLong[Int, Long] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(i: Int, conf: config.Bits): Long = conf match
    case config.Bits.Signed => i.toLong
    case config.Bits.Unsigned => i & 0X00000000FFFFFFFFL
    case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)

given ConcreteConvertIntFloat: ConvertIntFloat[Int, Float] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(i: Int, conf: config.Bits): Float = conf match
    case config.Bits.Signed => i.toFloat
    case config.Bits.Unsigned =>
      if (i >= 0)
        i.toFloat
      else
        ((i >>> 1) | (i & 1)).toFloat * 2.0f
    case config.Bits.Raw => JFloat.intBitsToFloat(i)

given ConcreteConvertIntDouble: ConvertIntDouble[Int, Double] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(i: Int, conf: config.Bits): Double = conf match
    case config.Bits.Signed => i.toDouble
    case config.Bits.Unsigned => (i & 0X00000000FFFFFFFFL).toDouble
    case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)

given ConcreteConvertIntBytes: ConvertIntBytes[Int, Seq[Byte]] with
  override def apply(from: Int, conf: config.BytesSize && SomeCC[ByteOrder]): Seq[Byte] =
    val buf = ByteBuffer.allocate(conf.c1.bytes)
    buf.order(conf.c2.t)
    conf._1 match
      case config.BytesSize.Byte => buf.put(0, (from % (1 << 8)).toByte)
      case config.BytesSize.Short => buf.putShort(0, (from % (1 << 16)).toShort)
      case config.BytesSize.Int => buf.putInt(0, from)
      case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)
    collection.immutable.ArraySeq.unsafeWrapArray(buf.array())

given ConcreteConvertBytesInt: ConvertBytesInt[Seq[Byte], Int] with
  override def apply(from: Seq[Byte], conf: config.BytesSize && SomeCC[ByteOrder] && config.Bits): Int =
    val buf = ByteBuffer.wrap(from.toArray)
    buf.order(conf.c1.c2.t)
    (conf.c1.c1, conf.c2) match
      case (config.BytesSize.Byte, config.Bits.Signed) => buf.get.toInt
      case (config.BytesSize.Byte, config.Bits.Unsigned) => buf.get & 0xFF
      case (config.BytesSize.Short, config.Bits.Signed) => buf.getShort.toInt
      case (config.BytesSize.Short, config.Bits.Unsigned) => buf.getShort & 0xFFFF
      case (config.BytesSize.Int, _) => buf.getInt
      case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)

given ConcreteIntUnsignedOrderingOps: UnsignedOrderingOps[Int, Boolean] with
  override def ltUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) < 0
  override def leUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) <= 0


*/