package sturdy.values.strings

import sturdy.effect.failure.Failure

class LiftedStringOps[V, D](extract: V => D, inject: D => V)(using ops: StringOps[D])(using Failure) extends StringOps[V]:
  def stringLit(s: String): V = inject(ops.stringLit(s))

