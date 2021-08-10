package sturdy.values.booleans

import sturdy.values.Topped
import sturdy.values.Topped._

class LiftedBooleanOps[V, B](extract: V => B, inject: B => V)(using ops: BooleanOps[B]) extends BooleanOps[V]:
  def boolLit(b: Boolean): V = inject(ops.boolLit(b))
  def not(v: V): V = inject(ops.not(extract(v)))
  def and(v1: V, v2: V): V = inject(ops.and(extract(v1), extract(v2)))
  def or(v1: V, v2: V): V = inject(ops.or(extract(v1), extract(v2)))
