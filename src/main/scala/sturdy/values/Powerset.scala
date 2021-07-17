package sturdy.values

import sturdy.values.relational.EqOps

case class Powerset[A](val set: Set[A]) extends AnyVal {
  def size: Int = set.size
  def ++(that: Powerset[A]) = Powerset(this.set ++ that.set)
}
object Powerset {
  def apply[A](a: A): Powerset[A] = Powerset(Set(a))
}

given PowersetJoin[A]: JoinValue[Powerset[A]] with
  override def joinValues(v1: Powerset[A], v2: Powerset[A]): Powerset[A] = new Powerset(v1.set ++ v2.set)

given PowersetCertainEqualOps[A](using ops: EqOps[A, Boolean]): EqOps[Powerset[A], Topped[Boolean]] with
  override def equ(v1: Powerset[A], v2: Powerset[A]): Topped[Boolean] =
    if (v1.set.size == 1 && v2.set.size == 1 && ops.equ(v1.set.head, v2.set.head))
      Topped.Actual(true)
    else {
      val intersect = v1.set.withFilter(a1 => v2.set.exists(a2 => ops.equ(a1, a2)))
      intersect.foreach(_ => return Topped.Top) // non-empty intersection
      Topped.Actual(false)
    }

  override def neq(v1: Powerset[A], v2: Powerset[A]): Topped[Boolean] = equ(v1, v2).map(!_)
