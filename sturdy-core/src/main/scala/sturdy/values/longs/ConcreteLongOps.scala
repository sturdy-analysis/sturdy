package sturdy.values.longs

import scala.util.Random
import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.config
import sturdy.values.config.Bits
import sturdy.values.config.UnsupportedConfiguration
import sturdy.values.convert.Convert
import sturdy.values.relational.CompareOps
import sturdy.values.relational.EqOps

import java.lang.Float as JFloat
import java.lang.Long as JLong
import java.lang.Double as JDouble
import java.nio.ByteBuffer
import java.nio.ByteOrder


given ConcreteLongOps(using f: Failure): LongOps[Long] with
  def longLit(l: Long): Long = l
  def randomLong(): Long = Random.nextLong()
  def add(v1: Long, v2: Long): Long = v1 + v2
  def sub(v1: Long, v2: Long): Long = v1 - v2
  def mul(v1: Long, v2: Long): Long = v1 * v2
  def div(v1: Long, v2: Long): Long =
    if (v2 == 0)
      f.fail(LongDivisionByZero, s"$v1 / $v2")
    else if (v1 == Long.MinValue && v2 == -1)
      f.fail(LongOverflow, s"$v1 / $v2")
    else
      v1 / v2
  def divUnsigned(v1: Long, v2: Long): Long =
    if (v2 == 0)
      f.fail(LongDivisionByZero, s"$v1 / $v2")
    else
      JLong.divideUnsigned(v1, v2)
  def remainder(v1: Long, v2: Long): Long =
    if (v2 == 0)
      f.fail(LongDivisionByZero, s"$v1 / $v2")
    else
      v1 % v2
  def remainderUnsigned(v1: Long, v2: Long): Long =
    if (v2 == 0)
      f.fail(LongDivisionByZero, s"$v1 / $v2")
    else
      JLong.remainderUnsigned(v1, v2)

  def bitAnd(v1: Long, v2: Long): Long = v1 & v2
  def bitOr(v1: Long, v2: Long): Long = v1 | v2
  def bitXor(v1: Long, v2: Long): Long = v1 ^ v2
  def shiftLeft(v: Long, shift: Long): Long = v << shift
  def shiftRight(v: Long, shift: Long): Long = v >> shift
  def shiftRightUnsigned(v: Long, shift: Long): Long = v >>> shift
  def rotateLeft(v: Long, shift: Long): Long = JLong.rotateLeft(v, shift.toInt)
  def rotateRight(v: Long, shift: Long): Long = JLong.rotateRight(v, shift.toInt)
  def countLeadingZeros(v: Long): Long = JLong.numberOfLeadingZeros(v)
  def countTrailinZeros(v: Long): Long = JLong.numberOfTrailingZeros(v)
  def nonzeroBitCount(v: Long): Long = JLong.bitCount(v)

given Structural[Int] with {}

given EqOps[Long, Boolean] with
  override def equ(v1: Long, v2: Long): Boolean = v1 == v2
  override def neq(v1: Long, v2: Long): Boolean = v1 != v2

given CompareOps[Long, Boolean] with
  def lt(v1: Long, v2: Long): Boolean = v1 < v2
  def le(v1: Long, v2: Long): Boolean = v1 <= v2
  def ge(v1: Long, v2: Long): Boolean = v1 >= v2
  def gt(v1: Long, v2: Long): Boolean = v1 > v2

given ConcreteConvertLongInt: ConvertLongInt[Long, Int] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(l: Long, conf: Unit): Int = (l % (1L << 32)).toInt

given ConcreteConvertLongFloat: ConvertLongFloat[Long, Float] with
  private val convC = JFloat.parseFloat("0x1p12")
  override def apply(l: Long, conf: Bits): Float = conf match
    case config.Bits.Signed =>
      if (Math.abs(l) < 0X10000000000000L) {
        l.toFloat
      } else {
        val r = if ((l & 0XFFFL) == 0L) 0L else 1L
        ((l >> 12) | r).toFloat * convC
      }
    case config.Bits.Unsigned =>
      if (JLong.compareUnsigned(l, 0X10000000000000L) < 0) {
        l.toFloat
      } else {
        val r = if ((l & 0XFFFL) == 0L) 0L else 1L
        ((l >>> 12) | r).toFloat * convC
      }
    case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)

given ConcreteConvertLongDouble: ConvertLongDouble[Long, Double] with
  override def apply(l: Long, conf: Bits): Double = conf match
    case config.Bits.Signed => l.toDouble
    case config.Bits.Unsigned =>
      if (l >= 0L)
        l.toDouble
      else
        ((l >>> 1) | (l & 1L)) * 2.0d
    case config.Bits.Raw => JDouble.longBitsToDouble(l)

given ConcreteConvertLongBytes: ConvertLongBytes[Long, Seq[Byte]] with
  override def apply(from: Long, conf: (config.BytesSize, ByteOrder)): Seq[Byte] =
    val buf = ByteBuffer.allocate(conf._1.bytes)
    buf.order(conf._2)
    conf._1 match
      case config.BytesSize.Byte => buf.put(0, (from % (1L << 8)).toByte)
      case config.BytesSize.Short => buf.putShort(0, (from % (1L << 16)).toShort)
      case config.BytesSize.Int => buf.putInt(0, (from % (1L << 32)).toInt)
      case config.BytesSize.Long => buf.putLong(0, from)
    buf.array().toSeq

given ConcreteConvertBytesLong: ConvertBytesLong[Seq[Byte], Long] with
  override def apply(from: Seq[Byte], conf: (config.BytesSize, ByteOrder, config.Bits)): Long =
    val buf = ByteBuffer.wrap(from.toArray)
    buf.order(conf._2)
    (conf._1, conf._3) match
      case (config.BytesSize.Byte, config.Bits.Signed) => buf.get.toLong
      case (config.BytesSize.Byte, config.Bits.Unsigned) => buf.get & 0xFFL
      case (config.BytesSize.Short, config.Bits.Signed) => buf.getShort.toLong
      case (config.BytesSize.Short, config.Bits.Unsigned) => buf.getShort & 0xFFFFL
      case (config.BytesSize.Int, config.Bits.Signed) => buf.getInt.toLong
      case (config.BytesSize.Int, config.Bits.Unsigned) => buf.getInt & 0xFFFFFFFFL
      case (config.BytesSize.Long, _) => buf.getLong()
      case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)

