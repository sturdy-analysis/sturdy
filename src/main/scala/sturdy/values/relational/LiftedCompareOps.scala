package sturdy.values.relational

class LiftedCompareOps[V, B, UV, UB](extract: V => UV, inject: UB => B)(using ops: CompareOps[UV, UB]) extends CompareOps[V, B]:
  def lt(v1: V, v2: V): B = inject(ops.lt(extract(v1), extract(v2)))
  def le(v1: V, v2: V): B = inject(ops.le(extract(v1), extract(v2)))
  def ge(v1: V, v2: V): B = inject(ops.ge(extract(v1), extract(v2)))
  def gt(v1: V, v2: V): B = inject(ops.gt(extract(v1), extract(v2)))
  def isZero(v1: V): B = inject(ops.isZero(extract(v1)))
  def isPositive(v1: V): B = inject(ops.isPositive(extract(v1)))
  def isNegative(v1: V): B = inject(ops.isNegative(extract(v1)))
  def isOdd(v1: V): B = inject(ops.isOdd(extract(v1)))
  def isEven(v1: V): B = inject(ops.isEven(extract(v1)))
