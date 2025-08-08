package sturdy.values.booleans

import sturdy.values.Topped
import sturdy.values.Topped.*

final class LiftedBooleanOps[V, B](extract: V => B, inject: B => V)(using ops: BooleanOps[B]) extends BooleanOps[V]:
  inline def boolLit(b: Boolean): V = inject(ops.boolLit(b))
  inline def not(v: V): V = inject(ops.not(extract(v)))
  inline def and(v1: V, v2: V): V = inject(ops.and(extract(v1), extract(v2)))
  inline def or(v1: V, v2: V): V = inject(ops.or(extract(v1), extract(v2)))
