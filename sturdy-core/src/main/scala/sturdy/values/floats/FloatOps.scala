package sturdy.values.floats

import scala.util.Random

trait FloatOps[V]:
  def floatLit(f: Float): V
  def randomFloat(): V

  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  def min(v1: V, v2: V): V 
  def max(v1: V, v2: V): V 

  def absolute(v: V): V 
  def negated(v: V): V 
  def sqrt(v: V): V 
  def ceil(v: V): V 
  def floor(v: V): V 
  def truncate(v: V): V 
  def nearest(v: V): V 
  def copysign(v: V, sign: V): V 

given ConcreteFloatOps: FloatOps[Float] with
  def floatLit(f: Float): Float = f
  def randomFloat(): Float = Random.nextFloat()
  def add(v1: Float, v2: Float): Float = v1 + v2
  def sub(v1: Float, v2: Float): Float = v1 - v2
  def mul(v1: Float, v2: Float): Float = v1 * v2
  def div(v1: Float, v2: Float): Float = v1 / v2

  def min(v1: Float, v2: Float): Float = Math.min(v1, v2)
  def max(v1: Float, v2: Float): Float = Math.max(v1, v2)

  def absolute(v: Float): Float = Math.abs(v)
  def negated(v: Float): Float = -v
  def sqrt(v: Float): Float = Math.sqrt(v).toFloat
  def ceil(v: Float): Float = v.ceil
  def floor(v: Float): Float = v.floor
  def truncate(v: Float): Float = if (v < 0) v.ceil else v.floor
  def nearest(v: Float): Float =
  // copied from https://github.com/satabin/swam/blob/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime/F32.scala#L30
    if (v.isInfinite || v.isNaN || v.isWhole)
      v
    else
      Math.copySign((Math.round(v / 2) * 2).toFloat, v)
  def copysign(v: Float, sign: Float): Float = Math.copySign(v, sign)
