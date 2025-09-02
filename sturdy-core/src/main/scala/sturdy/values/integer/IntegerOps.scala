package sturdy.values.integer

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.failure.FailureKind
import sturdy.values.{Topped, config}
import sturdy.values.convert.&&
import sturdy.values.convert.Convert
import sturdy.values.convert.NilCC
import sturdy.values.convert.SomeCC

import java.nio.ByteBuffer
import java.nio.ByteOrder

case object IntegerDivisionByZero extends FailureKind
case object IntegerOverflow extends FailureKind

/** Integer operations for base type B, represented as V */
trait IntegerOps[B, V]:
  def integerLit(i: B): V
  def randomInteger(): V
  
  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V

  def max(v1: V, v2: V): V
  def min(v1: V, v2: V): V
  def absolute(v: V): V

  def div(v1: V, v2: V): V
  def divUnsigned(v1: V, v2: V): V
  /** Maintains the sign of v1 */
  def remainder(v1: V, v2: V): V
  def remainderUnsigned(v1: V, v2: V): V
  /** Yields positive remainder of v1/v2 */
  def modulo(v1: V, v2: V): V
  def gcd(v1: V, v2: V): V
  final def lcm(v1: V, v2: V): V = mul(div(absolute(v1), gcd(v1, v2)), absolute(v2))

  /** Binary integer operations for base type B, represented as V */
  def bitAnd(v1: V, v2: V): V
  def bitOr(v1: V, v2: V): V
  def bitXor(v1: V, v2: V): V
  def shiftLeft(v: V, shift: V): V
  def shiftRight(v: V, shift: V): V
  def shiftRightUnsigned(v: V, shift: V): V
  def rotateLeft(v: V, shift: V): V
  def rotateRight(v: V, shift: V): V
  def countLeadingZeros(v: V): V
  def countTrailingZeros(v: V): V
  def nonzeroBitCount(v: V): V
  def invertBits(v: V): V
  

trait OverflowIntegers[V]:
  def wasOverflown(v: V): Topped[Boolean]

/** Overflow-aware integer operations for base type B, represented as V */
trait StrictIntegerOps[B, V, J[_] <: MayJoin[_]]:
  def addStrict(v1: V, v2: V): JOption[J, V]
  def subStrict(v1: V, v2: V): JOption[J, V]
  def mulStrict(v1: V, v2: V): JOption[J, V]

// TODO: consider adjusting config types for more general support
type ConvertByteInt[VFrom, VTo] = Convert[Byte, Int, VFrom, VTo, NilCC.type]
type ConvertShortInt[VFrom, VTo] = Convert[Short, Int, VFrom, VTo, NilCC.type]
type ConvertCharInt[VFrom, VTo] = Convert[Char, Int, VFrom, VTo, NilCC.type]
type ConvertIntByte[VFrom, VTo] = Convert[Int, Byte, VFrom, VTo, NilCC.type]
type ConvertIntShort[VFrom, VTo] = Convert[Int, Short, VFrom, VTo, NilCC.type]
type ConvertIntChar[VFrom, VTo] = Convert[Int, Char, VFrom, VTo, NilCC.type]

type ConvertIntLong[VFrom, VTo] = Convert[Int, Long, VFrom, VTo, config.Bits]
type ConvertIntFloat[VFrom, VTo] = Convert[Int, Float, VFrom, VTo, config.Bits]
type ConvertIntDouble[VFrom, VTo] = Convert[Int, Double, VFrom, VTo, config.Bits]
type ConvertIntBytes[VFrom, VTo] = Convert[Int, Seq[Byte], VFrom, VTo, config.BytesSize && SomeCC[ByteOrder]]
type ConvertBytesInt[VFrom, VTo] = Convert[Seq[Byte], Int, VFrom, VTo, config.BytesSize && SomeCC[ByteOrder] && config.Bits]

type ConvertLongInt[VFrom, VTo] = Convert[Long, Int, VFrom, VTo, NilCC.type]
type ConvertLongFloat[VFrom, VTo] = Convert[Long, Float, VFrom, VTo, config.Bits]
type ConvertLongDouble[VFrom, VTo] = Convert[Long, Double, VFrom, VTo, config.Bits]
type ConvertLongBytes[VFrom, VTo] = Convert[Long, Seq[Byte], VFrom, VTo, config.BytesSize && SomeCC[ByteOrder]]
type ConvertBytesLong[VFrom, VTo] = Convert[Seq[Byte], Long, VFrom, VTo, config.BytesSize && SomeCC[ByteOrder] && config.Bits]
