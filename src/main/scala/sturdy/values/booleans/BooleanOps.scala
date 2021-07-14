package sturdy.values.booleans

trait BooleanOps[V]:
  def boolLit(b: Boolean): V
  def and(v1: V, v2: V): V
  def not(v: V): V
  def or(v1: V, v2: V): V

given ConcreteBooleanOps: BooleanOps[Boolean] with
  def boolLit(b: Boolean): Boolean = b
  def and(v1: Boolean, v2: Boolean): Boolean = v1 && v2
  def not(v: Boolean): Boolean = !v
  def or(v1: Boolean, v2: Boolean): Boolean = v1 || v2

