package sturdy.values.floating

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.Topped
import sturdy.values.convert.*

import java.nio.ByteBuffer
import java.nio.ByteOrder

given ToppedFloatOps[B, T] (using ops: FloatOps[B, T]): FloatOps[B, Topped[T]] with
  def floatingLit(f: B): Topped[T] = Topped.Actual(ops.floatingLit(f))
  def NaN: Topped[T] = Topped.Actual(ops.NaN)
  def posInfinity: Topped[T] = Topped.Actual(ops.posInfinity)
  def negInfinity: Topped[T] = Topped.Actual(ops.negInfinity)

  def randomFloat(): Topped[T] = Topped.Top

  def add(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.add, v2)
  def sub(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.sub, v2)
  def mul(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.mul, v2)
  def div(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.div, v2)
  def min(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.min, v2)
  def max(v1: Topped[T], v2: Topped[T]): Topped[T] = v1.binary(ops.max, v2)

  def absolute(v: Topped[T]): Topped[T] = v.unary(ops.absolute)
  def negated(v: Topped[T]): Topped[T] = v.unary(ops.negated)
  def sqrt(v: Topped[T]): Topped[T] = v.unary(ops.sqrt)
  def ceil(v: Topped[T]): Topped[T] = v.unary(ops.ceil)
  def floor(v: Topped[T]): Topped[T] = v.unary(ops.floor)
  def truncate(v: Topped[T]): Topped[T] = v.unary(ops.truncate)
  def nearest(v: Topped[T]): Topped[T] = v.unary(ops.nearest)
  def copysign(v: Topped[T], sign: Topped[T]): Topped[T] = v.binary(ops.copysign, sign)
