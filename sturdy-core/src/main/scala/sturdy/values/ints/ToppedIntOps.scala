package sturdy.values.ints

import sturdy.effect.failure.Failure
import sturdy.values.Topped

given ToppedIntOps[T](using ops: IntOps[T], f: Failure): IntOps[Topped[T]] with
  def intLit(i: Int): Topped[T] = Topped.Actual(ops.intLit(i))
  def randomInt(): Topped[T] = Topped.Top

  def add(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.add, v2)
  def sub(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.sub, v2)
  def mul(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.mul, v2)

  def max(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.max, v2)
  def min(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.min, v2)

  def div(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.div, v2)
  def divUnsigned(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.divUnsigned, v2)
  def remainder(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.remainder, v2)
  def remainderUnsigned(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.remainderUnsigned, v2)
  def modulo(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.modulo, v2)
  def gcd(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.gcd, v2)

  def absolute(v: Topped[T]): Topped[T] = v.unary(ops.absolute)
  def bitAnd(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.bitAnd, v2)
  def bitOr(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.bitOr, v2)
  def bitXor(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.bitXor, v2)
  def shiftLeft(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.shiftLeft, shift)
  def shiftRight(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.shiftRight, shift)
  def shiftRightUnsigned(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.shiftRightUnsigned, shift)
  def rotateLeft(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.rotateLeft, shift)
  def rotateRight(v: Topped[T], shift: Topped[T]): Topped[T] = v.binary(ops.rotateRight, shift)
  def countLeadingZeros(v: Topped[T]): Topped[T] = v.unary(ops.countLeadingZeros)
  def countTrailinZeros(v: Topped[T]): Topped[T] = v.unary(ops.countTrailinZeros)
  def nonzeroBitCount(v: Topped[T]): Topped[T] = v.unary(ops.nonzeroBitCount)
  