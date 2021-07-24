package sturdy.values.chars

import sturdy.effect.failure.Failure

class LiftedCharOps[V, D](extract: V => D, inject: D => V)(using ops: CharOps[D])(using Failure) extends CharOps[V]:
  def charLit(c: Char): V = inject(ops.charLit(c))

