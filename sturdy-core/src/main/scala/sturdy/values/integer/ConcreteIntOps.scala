package sturdy.values.integer

import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.convert.*
import sturdy.values.relational.EqOps
import sturdy.values.config
import sturdy.values.config.UnsupportedConfiguration
import sturdy.values.relational.OrderingOps

import scala.util.Random
import java.lang.Float as JFloat
import java.nio.ByteBuffer
import java.nio.ByteOrder

given Structural[Int] with {}

given ConcreteIntegerOps(using f: Failure): IntegerOps[Int, Int] with
  def integerLit(i: Int): Int = i
  def randomInteger(): Int = Random.nextInt()

  def add(v1: Int, v2: Int): Int = v1 + v2
  def sub(v1: Int, v2: Int): Int = v1 - v2
  def mul(v1: Int, v2: Int): Int = v1 * v2

  def max(v1: Int, v2: Int): Int = v1.max(v2)
  def min(v1: Int, v2: Int): Int = v1.min(v2)

  def div(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    else
      v1 / v2
  def divUnsigned(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    else
      Integer.divideUnsigned(v1, v2)
  def remainder(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    else
      v1 % v2
  def remainderUnsigned(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    else
      Integer.remainderUnsigned(v1, v2)
  def modulo(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    else {
      val r = v1 % v2
      if (r < 0)
        r + v2
      else
        r
    }
  def gcd(v1: Int, v2: Int): Int = BigInt(v1).gcd(BigInt(v2)).toInt

  def absolute(v: Int): Int = v.abs
  def bitAnd(v1: Int, v2: Int): Int = v1 & v2
  def bitOr(v1: Int, v2: Int): Int = v1 | v2
  def bitXor(v1: Int, v2: Int): Int = v1 ^ v2
  def shiftLeft(v: Int, shift: Int): Int = v << shift
  def shiftRight(v: Int, shift: Int): Int = v >> shift
  def shiftRightUnsigned(v: Int, shift: Int): Int = v >>> shift
  def rotateLeft(v: Int, shift: Int): Int = Integer.rotateLeft(v, shift)
  def rotateRight(v: Int, shift: Int): Int = Integer.rotateRight(v, shift)
  def countLeadingZeros(v: Int): Int = Integer.numberOfLeadingZeros(v)
  def countTrailinZeros(v: Int): Int = Integer.numberOfTrailingZeros(v)
  def nonzeroBitCount(v: Int): Int = Integer.bitCount(v)

given EqOps[Int, Boolean] with
  override def equ(v1: Int, v2: Int): Boolean = v1 == v2
  override def neq(v1: Int, v2: Int): Boolean = v1 != v2

given OrderingOps[Int, Boolean] with
  def lt(v1: Int, v2: Int): Boolean = v1 < v2
  def le(v1: Int, v2: Int): Boolean = v1 <= v2
  def ge(v1: Int, v2: Int): Boolean = v1 >= v2
  def gt(v1: Int, v2: Int): Boolean = v1 > v2

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
    buf.array().toSeq

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
