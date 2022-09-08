package sturdy.values.strings

import sturdy.data.{JOption, MayJoin}
import sturdy.effect.failure.Failure

class LiftedStringOps[B, V, I](extract: V => I, inject: I => V)(using ops: StringOps[B, I]) extends StringOps[B, V]:
  def stringLit(i: B): V = inject(ops.stringLit(i))
  