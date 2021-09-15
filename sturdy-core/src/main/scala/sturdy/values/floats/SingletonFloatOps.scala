package sturdy.values.floats

import sturdy.effect.failure.Failure
import sturdy.values.Singleton

given SingletonFloatOps: FloatOps[Singleton[Float]] with
  val ops = ConcreteFloatOps

  def floatLit(f: Float): Singleton[Float] = Singleton.Single(f)
  def randomFloat(): Singleton[Float] = Singleton.NoSingleton

  def add(v1: Singleton[Float], v2: Singleton[Float]): Singleton[Float] = v1.binary(ops.add, v2)
  def sub(v1: Singleton[Float], v2: Singleton[Float]): Singleton[Float] = v1.binary(ops.sub, v2)
  def mul(v1: Singleton[Float], v2: Singleton[Float]): Singleton[Float] = v1.binary(ops.mul, v2)
  def div(v1: Singleton[Float], v2: Singleton[Float]): Singleton[Float] = v1.binary(ops.div, v2)
  def min(v1: Singleton[Float], v2: Singleton[Float]): Singleton[Float] = v1.binary(ops.min, v2)
  def max(v1: Singleton[Float], v2: Singleton[Float]): Singleton[Float] = v1.binary(ops.max, v2)

  def absolute(v: Singleton[Float]): Singleton[Float] = v.unary(ops.absolute)
  def negated(v: Singleton[Float]): Singleton[Float] = v.unary(ops.negated)
  def sqrt(v: Singleton[Float]): Singleton[Float] = v.unary(ops.sqrt)
  def ceil(v: Singleton[Float]): Singleton[Float] = v.unary(ops.ceil)
  def floor(v: Singleton[Float]): Singleton[Float] = v.unary(ops.floor)
  def truncate(v: Singleton[Float]): Singleton[Float] = v.unary(ops.truncate)
  def nearest(v: Singleton[Float]): Singleton[Float] = v.unary(ops.nearest)
  def copysign(v: Singleton[Float], sign: Singleton[Float]): Singleton[Float] = v.binary(ops.copysign, sign)
