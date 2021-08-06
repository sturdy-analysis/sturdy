package sturdy.values.doubles

class LiftedDoubleOps[V, D](extract: V => D, inject: D => V)(using ops: DoubleOps[D]) extends DoubleOps[V]:
  def numLit(d: Double): V = inject(ops.numLit(d))
  def randomDouble(): V = inject(ops.randomDouble())
  def abs(v1: V): V = inject(ops.abs(extract(v1)))
  def log(v1: V): V = inject(ops.log(extract(v1)))
  def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
  def max(v1: V, v2: V): V = inject(ops.max(extract(v1), extract(v2)))
  def min(v1: V, v2: V): V = inject(ops.max(extract(v1), extract(v2)))

class LiftedDoubleIntOps[V, I, UV, UI](extract: V => UV, inject: UI => I)(using ops: DoubleIntOps[UV,UI]) extends DoubleIntOps[V,I]:
  override def floor(v1: V): I = inject(ops.floor(extract(v1)))
  override def ceiling(v1: V): I = inject(ops.floor(extract(v1)))

class LiftedDoubleBoolOps[V, B, UV, UB](extract: V => UV, inject: UB => B)(using ops: DoubleBoolOps[UV,UB]) extends DoubleBoolOps[V,B]:
  override def isZero(v1: V): B = inject(ops.isZero(extract(v1)))
  override def isPositive(v1: V): B = inject(ops.isPositive(extract(v1)))
  override def isNegative(v1: V): B = inject(ops.isNegative(extract(v1)))
