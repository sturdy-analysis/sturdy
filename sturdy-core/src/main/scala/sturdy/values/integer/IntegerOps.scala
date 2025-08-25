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

trait IntegerOpsWithSignInterpretation[B,V](byteSize: Int) extends IntegerOps[B, V]:

  inline def signedMinValue(numBytes: Int): BigInt =
    -BigInt(2).pow(numBytes * 8 - 1)

  inline def signedMaxValue(numBytes: Int): BigInt =
    BigInt(2).pow(numBytes * 8 - 1) - 1

  inline def unsignedMinValue(numBytes: Int): BigInt =
    0

  inline def unsignedMaxValue(numBytes: Int): BigInt =
    BigInt(2).pow(numBytes * 8)

  inline def interpretSignedAsUnsigned(v: V): V = interpretSignedAsUnsigned(v, byteSize)
  def interpretSignedAsUnsigned(v: V, fromNumBytes: Int): V

  inline def interpretUnsignedAsSigned(v: V): V = interpretUnsignedAsSigned(v, byteSize)
  def interpretUnsignedAsSigned(v: V, fromNumBytes: Int): V

  override inline def divUnsigned(v1: V, v2: V): V =
    interpretUnsignedAsSigned(div(interpretSignedAsUnsigned(v1), interpretSignedAsUnsigned(v2)))

  override inline def remainderUnsigned(v1: V, v2: V): V =
    interpretUnsignedAsSigned(remainder(interpretSignedAsUnsigned(v1), interpretSignedAsUnsigned(v2)))

  override inline def shiftRightUnsigned(v: V, shift: V): V =
    interpretUnsignedAsSigned(shiftRight(interpretSignedAsUnsigned(v), shift))


trait OverflowIntegers[V]:
  def wasOverflown(v: V): Topped[Boolean]

/** Overflow-aware integer operations for base type B, represented as V */
trait StrictIntegerOps[B, V, J[_] <: MayJoin[_]]:
  def addStrict(v1: V, v2: V): JOption[J, V]
  def subStrict(v1: V, v2: V): JOption[J, V]
  def mulStrict(v1: V, v2: V): JOption[J, V]

type ConvertIntLong[VFrom, VTo] = Convert[Int, Long, VFrom, VTo, config.BitSign]
type ConvertIntFloat[VFrom, VTo] = Convert[Int, Float, VFrom, VTo, config.BitSign]
type ConvertIntDouble[VFrom, VTo] = Convert[Int, Double, VFrom, VTo, config.BitSign]
type ConvertIntBytes[VFrom, VTo] = Convert[Int, Seq[Byte], VFrom, VTo, config.BytesSize && SomeCC[ByteOrder]]
type ConvertBytesInt[VFrom, VTo] = Convert[Seq[Byte], Int, VFrom, VTo, config.BytesSize && SomeCC[ByteOrder] && config.BitSign]

type ConvertLongInt[VFrom, VTo] = Convert[Long, Int, VFrom, VTo, NilCC.type]
type ConvertLongFloat[VFrom, VTo] = Convert[Long, Float, VFrom, VTo, config.BitSign]
type ConvertLongDouble[VFrom, VTo] = Convert[Long, Double, VFrom, VTo, config.BitSign]
type ConvertLongBytes[VFrom, VTo] = Convert[Long, Seq[Byte], VFrom, VTo, config.BytesSize && SomeCC[ByteOrder]]
type ConvertBytesLong[VFrom, VTo] = Convert[Seq[Byte], Long, VFrom, VTo, config.BytesSize && SomeCC[ByteOrder] && config.BitSign]
