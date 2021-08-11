package sturdy.values.floats

class LiftedFloatOps[V, D](extract: V => D, inject: D => V)(using ops: FloatOps[D]) extends FloatOps[V]:
  def floatLit(f: Float): V = inject(ops.floatLit(f))
  def randomFloat(): V = inject(ops.randomFloat())
  def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
