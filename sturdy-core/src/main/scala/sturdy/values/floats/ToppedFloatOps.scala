package sturdy.values.floats

import sturdy.effect.failure.Failure
import sturdy.values.Topped

import java.nio.ByteBuffer
import java.nio.ByteOrder

given ToppedFloatOps[T](using ops: FloatOps[T]): FloatOps[Topped[T]] with
  def floatLit(f: Float): Topped[T] = Topped.Actual(ops.floatLit(f))
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

given ToppedConvertFloatBytes[T, B](using c: ConvertFloatBytes[T, Seq[B]]): ConvertFloatBytes[Topped[T], Seq[Topped[B]]] with
  override def apply(from: Topped[T], conf: ByteOrder): Seq[Topped[B]] = from match
    case Topped.Top => Seq.fill(4)(Topped.Top)
    case Topped.Actual(v) => c(v, conf).map(Topped.Actual.apply)
