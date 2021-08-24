package sturdy.values.longs

import sturdy.effect.failure.Failure

class LiftedLongOps[V, I](extract: V => I, inject: I => V)(using ops: LongOps[I]) extends LongOps[V]:
  def longLit(l: Long): V = inject(ops.longLit(l))
  def randomLong(): V = inject(ops.randomLong())
  
  def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  
  def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
  def divUnsigned(v1: V, v2: V): V = inject(ops.divUnsigned(extract(v1), extract(v2)))
  def remainder(v1: V, v2: V): V = inject(ops.remainder(extract(v1), extract(v2)))
  def remainderUnsigned(v1: V, v2: V): V = inject(ops.remainderUnsigned(extract(v1), extract(v2)))

  def bitAnd(v1: V, v2: V): V = inject(ops.bitAnd(extract(v1), extract(v2)))
  def bitOr(v1: V, v2: V): V = inject(ops.bitOr(extract(v1), extract(v2)))
  def bitXor(v1: V, v2: V): V = inject(ops.bitXor(extract(v1), extract(v2)))
  def shiftLeft(v: V, shift: V): V = inject(ops.shiftLeft(extract(v), extract(shift)))
  def shiftRight(v: V, shift: V): V = inject(ops.shiftRight(extract(v), extract(shift)))
  def shiftRightUnsigned(v: V, shift: V): V = inject(ops.shiftRightUnsigned(extract(v), extract(shift)))
  def rotateLeft(v: V, shift: V): V = inject(ops.rotateLeft(extract(v), extract(shift)))
  def rotateRight(v: V, shift: V): V = inject(ops.rotateRight(extract(v), extract(shift)))
  def countLeadingZeros(v: V): V = inject(ops.countLeadingZeros(extract(v)))
  def countTrailinZeros(v: V): V = inject(ops.countTrailinZeros(extract(v)))
  def nonzeroBitCount(v: V): V = inject(ops.nonzeroBitCount(extract(v)))