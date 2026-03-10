package sturdy.values.integer

import sturdy.data.JOptionC
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.failure.Failure
import sturdy.values.{Structural, config}
import sturdy.values.convert.*
import sturdy.values.ordering.{EqOps, OrderingOps, UnsignedOrderingOps}
import sturdy.values.config.{Bits, Overflow, UnsupportedConfiguration, unsupportedConfiguration}

import scala.util.Random
import java.lang.Float as JFloat
import java.lang.Double as JDouble
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
        if(v2 >= 0)
          r + v2
        else
          r - v2
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
  def countTrailingZeros(v: Int): Int = Integer.numberOfTrailingZeros(v)
  def nonzeroBitCount(v: Int): Int = Integer.bitCount(v)
  def invertBits(v: Int): Int = ~v

given ConcreteStrictIntegerOps: StrictIntegerOps[Int, Int, NoJoin] with
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

given ConcreteConvertByteInt(using failure: Failure): ConvertByteInt[Byte, Int] with
  override def apply(from: Byte, conf: Bits): Int = conf match
    case Bits.Signed => from.toInt
    case Bits.Unsigned => from.toInt & 0X000000FF
    case _ =>
      unsupportedConfiguration(conf, this)

given ConcreteConvertShortInt(using failure: Failure): ConvertShortInt[Short, Int] with
  override def apply(from: Short, conf: Bits): Int = conf match
    case Bits.Signed => from.toInt
    case Bits.Unsigned => from.toInt & 0X0000FFFF
    case _ =>
      unsupportedConfiguration(conf, this)

given ConcreteConvertCharInt(): ConvertCharInt[Char, Int] with
  override def apply(from: Char, conf: NilCC.type): Int = from.toInt

given ConcreteConvertIntByte(using failure: Failure): ConvertIntByte[Int, Byte] with
  override def apply(from: Int, conf: Overflow): Byte = conf match
    case Overflow.Allow => from.toByte
    case Overflow.Fail => failure.fail(ConversionFailure, s"int $from out of byte range")
    case Overflow.JumpToBounds if from > Byte.MaxValue => Byte.MaxValue
    case Overflow.JumpToBounds if from < Byte.MinValue => Byte.MinValue
    case Overflow.JumpToBounds => from.toByte

given ConcreteConvertIntShort(using failure: Failure): ConvertIntShort[Int, Short] with
  override def apply(from: Int, conf: Overflow): Short = conf match
    case Overflow.Allow => from.toShort
    case Overflow.Fail => failure.fail(ConversionFailure, s"int $from out of short range")
    case Overflow.JumpToBounds if from > Short.MaxValue => Short.MaxValue
    case Overflow.JumpToBounds if from < Short.MinValue => Short.MinValue
    case Overflow.JumpToBounds => from.toShort

given ConcreteConvertIntChar(using failure: Failure): ConvertIntChar[Int, Char] with
  override def apply(from: Int, conf: config.Overflow): Char = conf match
    case Overflow.Allow => from.toChar
    case Overflow.Fail => failure.fail(ConversionFailure, s"int $from out of char range")
    case Overflow.JumpToBounds if from > Char.MaxValue => Char.MaxValue
    case Overflow.JumpToBounds if from < Char.MinValue => Char.MinValue
    case Overflow.JumpToBounds => from.toChar

given ConcreteConvertIntLong(using failure: Failure): ConvertIntLong[Int, Long] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(i: Int, conf: config.Bits): Long = conf match
    case config.Bits.Signed => i.toLong
    case config.Bits.Unsigned => i & 0X00000000FFFFFFFFL
    case _ =>
      unsupportedConfiguration(conf, this)
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
    case config.Bits.Raw => JDouble.longBitsToDouble(i)

given ConcreteConvertIntBytes(using failure: Failure): ConvertIntBytes[Int, Seq[Byte]] with
  override def apply(from: Int, conf: config.BytesSize && SomeCC[ByteOrder]): Seq[Byte] =
    val buf = ByteBuffer.allocate(conf.c1.bytes)
    buf.order(conf.c2.t)
    conf._1 match
      case config.BytesSize.Byte => buf.put(0, (from % (1 << 8)).toByte)
      case config.BytesSize.Short => buf.putShort(0, (from % (1 << 16)).toShort)
      case config.BytesSize.Int => buf.putInt(0, from)
      case _ => unsupportedConfiguration(conf, this)
    collection.immutable.ArraySeq.unsafeWrapArray(buf.array())

given ConcreteConvertBytesInt(using failue:Failure): ConvertBytesInt[Seq[Byte], Int] with
  override def apply(from: Seq[Byte], conf: config.BytesSize && SomeCC[ByteOrder] && config.Bits): Int =
    val buf = ByteBuffer.wrap(from.toArray)
    buf.order(conf.c1.c2.t)
    (conf.c1.c1, conf.c2) match
      case (config.BytesSize.Byte, config.Bits.Signed) => buf.get.toInt
      case (config.BytesSize.Byte, config.Bits.Unsigned) => buf.get & 0xFF
      case (config.BytesSize.Short, config.Bits.Signed) => buf.getShort.toInt
      case (config.BytesSize.Short, config.Bits.Unsigned) => buf.getShort & 0xFFFF
      case (config.BytesSize.Int, _) => buf.getInt
      case _ => unsupportedConfiguration(conf, this.getClass.getSimpleName)

given ConcreteIntUnsignedOrderingOps: UnsignedOrderingOps[Int, Boolean] with
  override def ltUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) < 0
  override def leUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) <= 0
