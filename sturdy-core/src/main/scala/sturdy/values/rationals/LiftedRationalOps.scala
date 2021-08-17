package sturdy.values.rationals

import sturdy.effect.failure.Failure

class LiftedRationalOps[V, D](extract: V => D, inject: D => V)(using ops: RationalOps[D])(using Failure) extends RationalOps[V]:
  override def rationalLit(i1: Int, i2: Int): V = inject(ops.rationalLit(i1, i2))
  override def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  override def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
  override def max(v1: V, v2: V): V = inject(ops.max(extract(v1), extract(v2)))
  override def min(v1: V, v2: V): V = inject(ops.min(extract(v1), extract(v2)))
  override def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  override def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  override def absolute(v1: V): V = inject(ops.absolute(extract(v1)))
  override def floor(v1: V): V = inject(ops.floor(extract(v1)))
  override def ceil(v1: V): V = inject(ops.ceil(extract(v1)))


