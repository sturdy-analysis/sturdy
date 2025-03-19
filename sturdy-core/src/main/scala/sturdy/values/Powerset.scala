package sturdy.values

import sturdy.data.{MakeJoined, mapJoin}
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.EffectStack
import sturdy.values.ordering.EqOps

case class Powerset[A](set: Set[A]) extends AnyVal {
  def size: Int = set.size
  def ++(that: Powerset[A]): Powerset[A] = Powerset(this.set ++ that.set)
  def map[B](f: A => B): Powerset[B] = Powerset(set.map(f))
  def mapJoin[B](f: A => B)(using EffectStack): Powerset[B] =
    sturdy.data.mapJoin(set, b => Powerset(f(b)))
  def foldJoin[B](f: A => B)(using EffectStack, Join[B]): B =
    sturdy.data.mapJoin(set, b => f(b))
  def foreach(f: A => Unit): Unit = set.foreach(f)
  override def toString: String = s"Powerset(${set.mkString(", ")})"
}
object Powerset {
  def empty[A]: Powerset[A] = new Powerset[A](Set.empty[A])
  def apply[A](as: A*): Powerset[A] = new Powerset(Set.from(as))
}

given finitePowerset[T](using Finite[T]): Finite[Powerset[T]] with {}

given powersetPO[T]: PartialOrder[Powerset[T]] with
  override def lteq(x: Powerset[T], y: Powerset[T]): Boolean = x.set.subsetOf(y.set)

given JoinPowerset[A]: Join[Powerset[A]] with
  override def apply(v1: Powerset[A], v2: Powerset[A]): MaybeChanged[Powerset[A]] =
    val joinedSet = v1.set ++ v2.set
    MaybeChanged(new Powerset(joinedSet), joinedSet.size > v1.set.size)

given powersetCertainEqualOps[A](using ops: EqOps[A, Boolean]): EqOps[Powerset[A], Topped[Boolean]] with
  override def equ(v1: Powerset[A], v2: Powerset[A]): Topped[Boolean] =
    if (v1.set.size == 1 && v2.set.size == 1)
      Topped.Actual(ops.equ(v1.set.head, v2.set.head))
    else {
      for (a1 <- v1.set; a2 <- v2.set)
        if (ops.equ(a1, a2))
          return Topped.Top
      Topped.Actual(false)
    }

  override def neq(v1: Powerset[A], v2: Powerset[A]): Topped[Boolean] = equ(v1, v2).map(!_)

given powersetUncertainEqualOps[A](using ops: EqOps[A, Topped[Boolean]]): EqOps[Powerset[A], Topped[Boolean]] with
  override def equ(v1: Powerset[A], v2: Powerset[A]): Topped[Boolean] =
    if (v1.set.size == 1 && v2.set.size == 1)
      ops.equ(v1.set.head, v2.set.head)
    else {
      for (a1 <- v1.set; a2 <- v2.set) ops.equ(a1, a2) match
        case Topped.Top => return Topped.Top
        case Topped.Actual(true) => return Topped.Top
        case _ => // nothing
      Topped.Actual(false)
    }

  override def neq(v1: Powerset[A], v2: Powerset[A]): Topped[Boolean] = equ(v1, v2).map(!_)


given powersetContainsOneSound[C, A](using s: Soundness[C, A]): Soundness[C, Powerset[A]] with
  override def isSound(c: C, as: Powerset[A]): IsSound =
    if (as.set.exists(a => s.isSound(c, a).isSound))
      IsSound.Sound
    else
      IsSound.NotSound(s"$as did not contain any sound abstraction of $c")

given powersetOptionSound[C, A](using s: Soundness[C, A]): Soundness[Option[C], Powerset[A]] with
  override def isSound(c: Option[C], as: Powerset[A]): IsSound = c match
      case None => IsSound.Sound
      case Some(c) => Soundness.isSound(c, as)

enum BoundedPowerset[A]:
  case Inbounds(set: Set[A], bound: Int)
  case Unbound()

  def map[B](f: A => B): BoundedPowerset[B] = this match
    case Inbounds(set, bound) => Inbounds(set.map(f), bound)
    case Unbound() => Unbound()

  def flatMap[B](f: A => BoundedPowerset[B]): BoundedPowerset[B] = this match
    case Inbounds(set, bound) =>
      val bs = set.map(f)
      if (bs.forall(_.isInstanceOf[Inbounds[B]])) {
        val bset = bs.flatMap(_.asInstanceOf[Inbounds[B]].set)
        BoundedPowerset(bound, bset)
      } else {
        Unbound()
      }
    case Unbound() => Unbound()

object BoundedPowerset {
  def empty[A](bound: Int): BoundedPowerset[A] = BoundedPowerset.Inbounds(Set.empty, bound)
  def apply[A](bound: Int, as: Set[A]): BoundedPowerset[A] =
    if (as.size <= bound)
      BoundedPowerset.Inbounds(as, bound)
    else
      BoundedPowerset.Unbound()
  def apply[A](bound: Int, as: A*): BoundedPowerset[A] = BoundedPowerset(bound, as.toSet)
}

given CombineBoundedPowerset[A, W <: Widening]: Combine[BoundedPowerset[A], W] with
  override def apply(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): MaybeChanged[BoundedPowerset[A]] = (v1, v2) match
    case (BoundedPowerset.Unbound(), _) => Unchanged(BoundedPowerset.Unbound())
    case (_, BoundedPowerset.Unbound()) => Changed(BoundedPowerset.Unbound())
    case (BoundedPowerset.Inbounds(set1, bound1), BoundedPowerset.Inbounds(set2, bound2)) =>
      val joinedSet = set1 ++ set2
      MaybeChanged(BoundedPowerset(bound1.max(bound2), joinedSet), joinedSet.size > set1.size)
