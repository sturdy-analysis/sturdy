package sturdy.values.strings

import sturdy.data.{JOption, MayJoin}
import sturdy.effect.failure.Failure


class LiftedStringOps[V, S](extract: V => S, inject: S => V)(using ops: StringOps[S]) extends StringOps[V]:
  def stringLit(s: String): V = inject(ops.stringLit(s))
  override def concat(s1: V, s2: V): V = inject(ops.concat(extract(s1), extract(s2)))
