package sturdy.values.relational

trait EqOps[V, B]:
  def equ(v1: V, v2: V): B
  def neq(v1: V, v2: V): B

object EqOps:
  def equ[V, B](v1: V, v2: V)(using ops: EqOps[V, B]): B =
    ops.equ(v1, v2)
  def neq[V, B](v1: V, v2: V)(using ops: EqOps[V, B]): B =
    ops.neq(v1, v2)

class EqualsEqOps[A] extends EqOps[A, Boolean]:
  override def equ(v1: A, v2: A): Boolean = v1 == v2
  override def neq(v1: A, v2: A): Boolean = v1 != v2

given EqOps[Boolean, Boolean] = new EqualsEqOps[Boolean]
given EqOps[Double, Boolean] = new EqualsEqOps[Double]
