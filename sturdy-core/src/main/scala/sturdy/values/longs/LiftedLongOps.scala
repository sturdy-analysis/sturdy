package sturdy.values.longs

import sturdy.effect.failure.Failure

class LiftedLongOps[V, I](extract: V => I, inject: I => V)(using ops: LongOps[I]) extends LongOps[V]:
  def longLit(l: Long): V = inject(ops.longLit(l))
  def randomLong(): V = inject(ops.randomLong())
  
  def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  
  def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
