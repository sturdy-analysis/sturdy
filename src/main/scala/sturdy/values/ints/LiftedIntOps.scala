package sturdy.values.ints

import sturdy.effect.failure.Failure

class LiftedIntOps[V, D](extract: V => D, inject: D => V)(using ops: IntOps[D])(using Failure) extends IntOps[V]:
  def numLit(d: Double): V = inject(ops.numLit(d))
  def randomDouble(): V = inject(ops.randomDouble())
  def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
