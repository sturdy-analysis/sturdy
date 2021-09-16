package sturdy.values.longs

import scala.util.Random
import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.config
import sturdy.values.config.Bits
import sturdy.values.config.UnsupportedConfiguration
import sturdy.values.convert.Convert

import java.lang.Float as JFloat
import java.lang.Long as JLong
import java.lang.Double as JDouble


given ConcreteLongOps(using f: Failure): LongOps[Long] with
  def longLit(l: Long): Long = l
  def randomLong(): Long = Random.nextLong()
  def add(v1: Long, v2: Long): Long = v1 + v2
  def sub(v1: Long, v2: Long): Long = v1 - v2
  def mul(v1: Long, v2: Long): Long = v1 * v2
  def div(v1: Long, v2: Long): Long =
    if (v2 == 0)
      f.fail(LongDivisionByZero, s"$v1 / $v2")
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

given Structural[Long] with {}

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
    case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)
