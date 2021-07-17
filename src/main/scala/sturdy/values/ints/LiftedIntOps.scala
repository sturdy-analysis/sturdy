package sturdy.values.ints

import sturdy.effect.failure.Failure

class LiftedIntOps[V, I](extract: V => I, inject: I => V)(using ops: IntOps[I])(using Failure) extends IntOps[V]:
  def intLit(i: Int): V = inject(ops.intLit(i))
  def randomInt(): V = inject(ops.randomInt())
  def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
