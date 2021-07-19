package sturdy.values

import sturdy.IsSound
import sturdy.Soundness
import sturdy.values.relational.EqOps

case class Powerset[A](val set: Set[A]) extends AnyVal {
  def size: Int = set.size
  def ++(that: Powerset[A]) = Powerset(this.set ++ that.set)
  def map[B](f: A => B): Powerset[B] = Powerset(set.map(f))
}
object Powerset {
  def empty[A]: Powerset[A] = Powerset[A](Set.empty)
  def apply[A](as: A*): Powerset[A] = Powerset(Set.from(as))
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

given PowersetContainsOneSound[C, A](using s: Soundness[C, A]): Soundness[C, Powerset[A]] with
  override def isSound(c: C, as: Powerset[A]): IsSound =
    if (as.set.exists(a => s.isSound(c, a).isSound))
      IsSound.Sound
    else
      IsSound.NotSound(s"$as did not contain any sound abstraction of $c")
