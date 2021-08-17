package sturdy.values.doubles

class LiftedDoubleOps[V, D](extract: V => D, inject: D => V)(using ops: DoubleOps[D]) extends DoubleOps[V]:
  def doubleLit(d: Double): V = inject(ops.doubleLit(d))
  def randomDouble(): V = inject(ops.randomDouble())
  def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))

  def min(v1: V, v2: V): V = inject(ops.min(extract(v1), extract(v2)))
  def max(v1: V, v2: V): V = inject(ops.max(extract(v1), extract(v2)))

  def absolute(v: V): V = inject(ops.absolute(extract(v)))
  def negated(v: V): V = inject(ops.negated(extract(v)))
  def sqrt(v: V): V = inject(ops.sqrt(extract(v)))
  def ceil(v: V): V = inject(ops.ceil(extract(v)))
  def floor(v: V): V = inject(ops.floor(extract(v)))
  def truncate(v: V): V = inject(ops.truncate(extract(v)))
  def nearest(v: V): V = inject(ops.nearest(extract(v)))
  def copysign(v: V, sign: V): V = inject(ops.copysign(extract(v), extract(sign)))

  def logNatural(v: V): V = inject(ops.logNatural(extract(v)))
