package sturdy.values.rational

import sturdy.effect.failure.Failure

class LiftedRationalOps[V, D](extract: V => D, inject: D => V)(using ops: RationalOps[D])(using Failure) extends RationalOps[V]:
  def rationalLit(i1: Int, i2: Int): V = inject(ops.rationalLit(i1, i2))

