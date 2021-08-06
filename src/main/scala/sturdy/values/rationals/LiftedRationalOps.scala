package sturdy.values.rationals

import sturdy.effect.failure.Failure

class LiftedRationalOps[V, D](extract: V => D, inject: D => V)(using ops: RationalOps[D])(using Failure) extends RationalOps[V]:
  override def rationalLit(i1: Int, i2: Int): V = inject(ops.rationalLit(i1, i2))
  override def abs(v1: V): V = inject(ops.abs(extract(v1)))
  override def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  override def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
  override def max(v1: V, v2: V): V = inject(ops.max(extract(v1), extract(v2)))
  override def min(v1: V, v2: V): V = inject(ops.min(extract(v1), extract(v2)))
  override def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  override def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))

class LiftedRationalIntOps[V, I, UV, UI](extract: V => UV, inject: UI => I)(using ops: RationalIntOps[UV,UI]) extends RationalIntOps[V,I]:
  override def floor(v1: V): I = inject(ops.floor(extract(v1)))
  override def ceiling(v1: V): I = inject(ops.ceiling(extract(v1)))

class LiftedRationalDoubleOps[V, D, UV, UD](extract: V => UV, inject: UD => D)(using ops: RationalDoubleOps[UV,UD]) extends RationalDoubleOps[V,D]:
  override def log(v1: V): D = inject(ops.log(extract(v1)))

class LiftedRationalBoolOps[V, B, UV, UB](extract: V => UV, inject: UB => B)(using ops: RationalBoolOps[UV,UB]) extends RationalBoolOps[V,B]:
  override def isZero(v: V): B = inject(ops.isZero(extract(v)))
  override def isPositive(v: V): B = inject(ops.isPositive(extract(v)))
  override def isNegative(v: V): B = inject(ops.isNegative(extract(v)))