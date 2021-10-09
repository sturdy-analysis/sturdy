package sturdy.values.ints

import sturdy.effect.failure.FailureKind
import sturdy.values.config
import sturdy.values.convert.Convert

import java.nio.ByteBuffer
import java.nio.ByteOrder

case object IntDivisionByZero extends FailureKind
case object IntOverflow extends FailureKind

trait IntOps[V]:
  def intLit(i: Int): V
  def randomInt(): V
  
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V

  def max(v1: V, v2: V): V
  def min(v1: V, v2: V): V

  def div(v1: V, v2: V): V
  def divUnsigned(v1: V, v2: V): V
  /** Maintains the sign of v1 */
  def remainder(v1: V, v2: V): V
  def remainderUnsigned(v1: V, v2: V): V
  /** Yields positive remainder of v1/v2 */
  def modulo(v1: V, v2: V): V
  def gcd(v1: V, v2: V): V
  final def lcm(v1: V, v2: V): V = mul(div(absolute(v1), gcd(v1, v2)), absolute(v2))

  def absolute(v: V): V
  def bitAnd(v1: V, v2: V): V
  def bitOr(v1: V, v2: V): V
  def bitXor(v1: V, v2: V): V
  def shiftLeft(v: V, shift: V): V
  def shiftRight(v: V, shift: V): V
  def shiftRightUnsigned(v: V, shift: V): V
  def rotateLeft(v: V, shift: V): V
  def rotateRight(v: V, shift: V): V
  def countLeadingZeros(v: V): V
  def countTrailinZeros(v: V): V
  def nonzeroBitCount(v: V): V

type ConvertIntLong[VFrom, VTo] = Convert[Int, Long, VFrom, VTo, config.Bits]
type ConvertIntFloat[VFrom, VTo] = Convert[Int, Float, VFrom, VTo, config.Bits]
type ConvertIntDouble[VFrom, VTo] = Convert[Int, Double, VFrom, VTo, config.Bits]
type ConvertIntBytes[VFrom, VTo] = Convert[Int, Seq[Byte], VFrom, VTo, (config.BytesSize, ByteOrder)]
type ConvertBytesInt[VFrom, VTo] = Convert[Seq[Byte], Int, VFrom, VTo, (config.BytesSize, ByteOrder, config.Bits)]
