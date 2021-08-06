package sturdy.values.ints

import sturdy.effect.failure.Failure

class LiftedIntOps[V, D](extract: V => D, inject: D => V)(using ops: IntOps[D])(using Failure) extends IntOps[V]:
  def intLit(i: Int): V = inject(ops.intLit(i))
  def randomInt(): V = inject(ops.randomInt())
  def abs(v1: V): V = inject(ops.abs(extract(v1)))
  def floor(v1: V): V = inject(ops.floor(extract(v1)))
  def ceiling(v1: V): V = inject(ops.ceiling(extract(v1)))
  def quotient(v1: V, v2: V): V = inject(ops.quotient(extract(v1), extract(v2)))
  def remainder(v1: V, v2: V): V = inject(ops.remainder(extract(v1), extract(v2)))
  def modulo(v1: V, v2: V): V = inject(ops.modulo(extract(v1), extract(v2)))
  def max(v1: V, v2: V): V = inject(ops.max(extract(v1), extract(v2)))
  def min(v1: V, v2: V): V = inject(ops.min(extract(v1), extract(v2)))
  def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
  def gcd(v1: V, v2: V): V = inject(ops.gcd(extract(v1), extract(v2)))
  def lcm(v1: V, v2: V): V = inject(ops.lcm(extract(v1), extract(v2)))

class LiftedIntDoubleOps[V, D, UV, UD](extract: V => UV, inject: UD => D)(using ops: IntDoubleOps[UV,UD]) extends IntDoubleOps[V,D]:
  override def log(v1: V): D = inject(ops.log(extract(v1)))

class LiftedIntBoolOps[V, B, UV, UB](extract: V => UV, inject: UB => B)(using ops: IntBoolOps[UV,UB]) extends IntBoolOps[V,B]:
  override def isZero(v1: V): B = inject(ops.isZero(extract(v1)))
  override def isPositive(v1: V): B = inject(ops.isPositive(extract(v1)))
  override def isNegative(v1: V): B = inject(ops.isNegative(extract(v1)))
  override def isOdd(v1: V): B = inject(ops.isOdd(extract(v1)))
  override def isEven(v1: V): B = inject(ops.isEven(extract(v1)))
