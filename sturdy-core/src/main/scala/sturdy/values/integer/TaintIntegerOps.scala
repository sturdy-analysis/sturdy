package sturdy.values.integer

import sturdy.values.abstraction.taint.*

given TaintIntegerOps[B, V] (using ops: IntegerOps[B, V]): IntegerOps[B, TaintProduct[V]] with
  def integerLit(i: B): TaintProduct[V] = untainted(ops.integerLit(i))
  def randomInteger(): TaintProduct[V] = untainted(ops.randomInteger())

  def add(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.add, v2)
  def sub(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.sub, v2)
  def mul(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.mul, v2)

  def max(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.max, v2)
  def min(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.min, v2)
  def absolute(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.absolute)

  def div(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.div, v2)
  def divUnsigned(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.divUnsigned, v2)
  def remainder(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.remainder, v2)
  def remainderUnsigned(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.remainderUnsigned, v2)
  def modulo(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.modulo, v2)
  def gcd(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.gcd, v2)

  def bitAnd(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitAnd, v2)
  def bitOr(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitOr, v2)
  def bitXor(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitXor, v2)
  def shiftLeft(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftLeft, shift)
  def shiftRight(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftRight, shift)
  def shiftRightUnsigned(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftRightUnsigned, shift)
  def rotateLeft(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.rotateLeft, shift)
  def rotateRight(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.rotateRight, shift)
  def countLeadingZeros(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.countLeadingZeros)
  def countTrailingZeros(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.countTrailingZeros)
  def nonzeroBitCount(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.nonzeroBitCount)
