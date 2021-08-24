package sturdy.values.relational

trait CompareOps[V, B]:
  def lt(v1: V, v2: V): B
  def le(v1: V, v2: V): B
  def ge(v1: V, v2: V): B
  def gt(v1: V, v2: V): B


given ConcreteCompareOps: CompareOps[Double, Boolean] with
  def lt(v1: Double, v2: Double): Boolean = v1 < v2
  def le(v1: Double, v2: Double): Boolean = v1 <= v2
  def ge(v1: Double, v2: Double): Boolean = v1 >= v2
  def gt(v1: Double, v2: Double): Boolean = v1 > v2
