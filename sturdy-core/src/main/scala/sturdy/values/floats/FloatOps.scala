package sturdy.values.floats

import scala.util.Random

trait FloatOps[V]:
  def floatLit(f: Float): V
  def randomFloat(): V

  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  def min(v1: V, v2: V): V = ???
  def max(v1: V, v2: V): V = ???

  def absolute(v: V): V = ???
  def negated(v: V): V = ???
  def sqrt(v: V): V = ???
  def ceil(v: V): V = ???
  def floor(v: V): V = ???
  def truncate(v: V): V = ???
  def nearest(v: V): V = ???
  def copysign(v: V, sign: V): V = ???

given ConcreteFloatOps: FloatOps[Float] with
  def floatLit(f: Float): Float = f
  def randomFloat(): Float = Random.nextFloat()
  def add(v1: Float, v2: Float): Float = v1 + v2
  def sub(v1: Float, v2: Float): Float = v1 - v2
  def mul(v1: Float, v2: Float): Float = v1 * v2
  def div(v1: Float, v2: Float): Float = v1 / v2
