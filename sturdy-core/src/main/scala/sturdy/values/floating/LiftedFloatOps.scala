package sturdy.values.floating

final class LiftedFloatOps[B, V, D](extract: V => D, inject: D => V)(using ops: FloatOps[B, D]) extends FloatOps[B, V]:
  inline def floatingLit(f: B): V = inject(ops.floatingLit(f))

  inline def NaN: V = inject(ops.NaN)

  inline def posInfinity: V = inject(ops.posInfinity)

  inline def negInfinity: V = inject(ops.negInfinity)

  inline def randomFloat(): V = inject(ops.randomFloat())
  inline def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  inline def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  inline def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  inline def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))

  inline def min(v1: V, v2: V): V = inject(ops.min(extract(v1), extract(v2)))
  inline def max(v1: V, v2: V): V = inject(ops.max(extract(v1), extract(v2)))

  inline def absolute(v: V): V = inject(ops.absolute(extract(v)))
  inline def negated(v: V): V = inject(ops.negated(extract(v)))
  inline def sqrt(v: V): V = inject(ops.sqrt(extract(v)))
  inline def ceil(v: V): V = inject(ops.ceil(extract(v)))
  inline def floor(v: V): V = inject(ops.floor(extract(v)))
  inline def truncate(v: V): V = inject(ops.truncate(extract(v)))
  inline def nearest(v: V): V = inject(ops.nearest(extract(v)))
  inline def copysign(v: V, sign: V): V = inject(ops.copysign(extract(v), extract(sign)))
