package sturdy.values.relational

trait CompareOps[V, I]:
  /** yields -1 if v1 < v2, 0 if v1 == v2, 1 if v1 > v2 */
  def cmp(v1: V, v2: V): I

class LiftedCompareOps[V, I, UV, UI](extract: V => UV, inject: UI => I)(using ops: CompareOps[UV, UI]) extends CompareOps[V, I]:
  override def cmp(v1: V, v2: V): I = inject(ops.cmp(extract(v1), extract(v2)))
