package sturdy.values.floating

import sturdy.values.abstraction.taint.*

given TaintFloatOps[B, V] (using ops: FloatOps[B, V]): FloatOps[B, TaintProduct[V]] with
  def floatingLit(f: B): TaintProduct[V] =  untainted(ops.floatingLit(f))
  def randomFloat(): TaintProduct[V] = untainted(ops.randomFloat())

  def add(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.add, v2)
  def sub(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.sub, v2)
  def mul(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.mul, v2)
  def div(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.div, v2)
  def min(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.min, v2)
  def max(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.max, v2)

  def absolute(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.absolute)
  def negated(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.negated)
  def sqrt(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.sqrt)
  def ceil(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.ceil)
  def floor(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.floor)
  def truncate(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.truncate)
  def nearest(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.nearest)
  def copysign(v: TaintProduct[V], sign: TaintProduct[V]): TaintProduct[V] = v.binary(ops.copysign, sign)
