package sturdy.values.ints

import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.convert.Convert
import sturdy.values.relational.EqOps
import sturdy.values.config
import sturdy.values.config.UnsupportedConfiguration
import sturdy.values.relational.CompareOps

import scala.util.Random
import java.lang.Float as JFloat

given ConcreteIntOps(using f: Failure): IntOps[Int] with
  def intLit(i: Int): Int = i
  def randomInt(): Int = Random.nextInt()

  def add(v1: Int, v2: Int): Int = v1 + v2
  def sub(v1: Int, v2: Int): Int = v1 - v2
  def mul(v1: Int, v2: Int): Int = v1 * v2

  def max(v1: Int, v2: Int): Int = v1.max(v2)
  def min(v1: Int, v2: Int): Int = v1.min(v2)

  def div(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else if (v1 == Int.MinValue && v2 == -1)
      f.fail(IntOverflow, s"$v1 / $v2")
    else
      v1 / v2
  def divUnsigned(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else
      Integer.divideUnsigned(v1, v2)
  def remainder(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else
      v1 % v2
  def remainderUnsigned(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
    else
      Integer.remainderUnsigned(v1, v2)
  def modulo(v1: Int, v2: Int): Int =
    if (v2 == 0)
      f.fail(IntDivisionByZero, s"$v1 / $v2")
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

given Structural[Int] with {}

given EqOps[Int, Boolean] with
  override def equ(v1: Int, v2: Int): Boolean = v1 == v2
  override def neq(v1: Int, v2: Int): Boolean = v1 != v2

given CompareOps[Int, Boolean] with
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
    case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)

given ConcreteConvertIntDouble: ConvertIntDouble[Int, Double] with
  /*
   * Most conversion rules have been copied from:
   *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
   */
  def apply(i: Int, conf: config.Bits): Double = conf match
    case config.Bits.Signed => i.toDouble
    case config.Bits.Unsigned => (i & 0X00000000FFFFFFFFL).toDouble
    case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)


