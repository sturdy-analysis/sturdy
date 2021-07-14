package sturdy.values.relational

trait EqOps[V, B]:
  def equ(v1: V, v2: V): B
  def neq(v1: V, v2: V): B

given BooleanEqOps: EqOps[Boolean, Boolean] with
  def equ(v1: Boolean, v2: Boolean): Boolean = v1 == v2
  def neq(v1: Boolean, v2: Boolean): Boolean = v1 != v2

given DoubleEqOps: EqOps[Double, Boolean] with
  def equ(v1: Double, v2: Double): Boolean = v1 == v2
  def neq(v1: Double, v2: Double): Boolean = v1 != v2
