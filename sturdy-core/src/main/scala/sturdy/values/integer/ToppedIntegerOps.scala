package sturdy.values.integer

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{Join, Topped}
import sturdy.values.config
import sturdy.values.config.Bits
import sturdy.values.config.BytesSize
import sturdy.values.config.UnsupportedConfiguration

import java.nio.ByteBuffer
import java.nio.ByteOrder

given ToppedIntegerOps[B, T] (using ops: IntegerOps[B, T], f: Failure, eff: EffectStack): IntegerOps[B, Topped[T]] with

  def integerLit(i: B): Topped[T] = Topped.Actual(ops.integerLit(i))
  def randomInteger(): Topped[T] = Topped.Top

  def add(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.add, v2)
  def sub(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.sub, v2)
  def mul(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.mul, v2)

  def max(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.max, v2)
  def min(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.min, v2)
  def absolute(v: Topped[T]): Topped[T] = v.unary(ops.absolute)

  private inline def safeDiv[TT >: T](op: (T, TT) => T, v1: Topped[T], v2: Topped[T]): Topped[T] =
    if (v2 == Topped.Top)
      eff.joinWithFailure(v1.binary(op, v2))(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    else
      v1.binary(op, v2)
  def div(v1: Topped[T], v2: Topped[T]): Topped[T] = safeDiv(ops.div, v1, v2)
  def divUnsigned(v1: Topped[T], v2: Topped[T]): Topped[T] = safeDiv(ops.divUnsigned, v1, v2)
  def remainder(v1: Topped[T], v2: Topped[T]): Topped[T] = safeDiv(ops.remainder, v1, v2)
  def remainderUnsigned(v1: Topped[T], v2: Topped[T]): Topped[T] = safeDiv(ops.remainderUnsigned, v1, v2)
  def modulo(v1: Topped[T], v2: Topped[T]): Topped[T] = safeDiv(ops.modulo, v1, v2)
  def gcd(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.gcd, v2)

  def bitAnd(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.bitAnd, v2)
  def bitOr(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.bitOr, v2)
  def bitXor(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.bitXor, v2)
  def shiftLeft(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.shiftLeft, shift)
  def shiftRight(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.shiftRight, shift)
  def shiftRightUnsigned(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.shiftRightUnsigned, shift)
  def rotateLeft(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.rotateLeft, shift)
  def rotateRight(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.rotateRight, shift)
  def countLeadingZeros(v: Topped[T]): Topped[T] = v.unary(ops.countLeadingZeros)
  def countTrailingZeros(v: Topped[T]): Topped[T] = v.unary(ops.countTrailingZeros)
  def nonzeroBitCount(v: Topped[T]): Topped[T] = v.unary(ops.nonzeroBitCount)
