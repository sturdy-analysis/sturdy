package sturdy.values.relational

import sturdy.values.Structural

trait EqOps[V, B]:
  def equ(v1: V, v2: V): B
  def neq(v1: V, v2: V): B

object EqOps:
  def equ[V, B](v1: V, v2: V)(using ops: EqOps[V, B]): B =
    ops.equ(v1, v2)
  def neq[V, B](v1: V, v2: V)(using ops: EqOps[V, B]): B =
    ops.neq(v1, v2)

given StructuralEqOps[A](using Structural[A]): EqOps[A, Boolean] with
  override def equ(v1: A, v2: A): Boolean = v1 == v2
  override def neq(v1: A, v2: A): Boolean = v1 != v2
