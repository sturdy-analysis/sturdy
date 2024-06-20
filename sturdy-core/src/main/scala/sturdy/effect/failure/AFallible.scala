package sturdy.effect.failure

import sturdy.{IsSound, Soundness}
import sturdy.effect.RecurrentCall
import sturdy.effect.failure.AFallible.{Diverging, MaybeFailing}
import sturdy.values.Abstractly
import sturdy.values.PartialOrder
import sturdy.values.Powerset

enum AFallible[T]:
  case Unfailing(t: T)
  case Failing(msgs: Powerset[(FailureKind,String)])
  case MaybeFailing(t: T, msgs: Powerset[(FailureKind,String)])
  case Diverging(recur: RecurrentCall)

  def isFailing: Boolean = this match
    case Failing(_) => true
    case _ => false

  def isSucceeding: Boolean = !isFailing
  
  def get: Option[T] = this match
    case Unfailing(t) => Some(t)
    case MaybeFailing(t, _) => Some(t)
    case _ => None
  
  def failures: Powerset[(FailureKind, String)] = this match
    case Unfailing(_) => Powerset.empty
    case MaybeFailing(_, fails) => fails
    case Failing(fails) => fails
    case Diverging(_) => throw new MatchError(this)

given cfallibleAbstractly[C, A](using abs: Abstractly[C, A]): Abstractly[CFallible[C], AFallible[A]] with
  override def apply(c: CFallible[C]): AFallible[A] = c match
    case CFallible.Unfailing(c) => AFallible.Unfailing(abs.apply(c))
    case CFallible.Failing(kind, msg) => AFallible.Failing(Powerset(kind -> msg))

given afallibleAbstractly[C, A](using abs: Abstractly[C, A]): Abstractly[AFallible[C], AFallible[A]] with
  override def apply(a: AFallible[C]): AFallible[A] = a match
    case AFallible.Unfailing(t) => AFallible.Unfailing(abs.apply(t))
    case AFallible.MaybeFailing(t, msgs) => AFallible.MaybeFailing(abs.apply(t), msgs)
    case AFallible.Failing(msgs) => AFallible.Failing(msgs)
    case AFallible.Diverging(recur) => AFallible.Diverging(recur)

given falliblePO[T](using po: PartialOrder[T]): PartialOrder[AFallible[T]] with
  override def lteq(x: AFallible[T], y: AFallible[T]): Boolean = (x, y) match
    case (AFallible.Diverging(_), _) => true
    case (AFallible.Unfailing(t1), AFallible.Unfailing(t2)) => po.lteq(t1, t2)
    case (AFallible.Failing(fails1), AFallible.Failing(fails2)) => fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
    case (AFallible.Unfailing(t1), AFallible.MaybeFailing(t2, fails2)) => po.lteq(t1, t2)
    case (AFallible.Failing(fails1), AFallible.MaybeFailing(t2, fails2)) => fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
    case (AFallible.MaybeFailing(t1, fails1), AFallible.MaybeFailing(t2, fails2)) => po.lteq(t1, t2) && fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
    case _ => false

given soundnessAFallible[C,A](using Soundness[C,A]): Soundness[CFallible[C], AFallible[A]] = {
  case (CFallible.Failing(kind,msg), AFallible.Failing(failures)) =>
    IsSound(failures.map(_._1).set.contains(kind), s"Abstract failures ${failures.map(_._1)} do not contain concrete failure ${kind}")
  case (CFallible.Failing(kind,msg), AFallible.MaybeFailing(_,failures)) =>
    IsSound(failures.map(_._1).set.contains(kind), s"Abstract failures ${failures.map(_._1)} do not contain concrete failure ${kind}")
  case (CFallible.Unfailing(c), AFallible.Unfailing(a)) => Soundness.isSound(c,a)
  case (CFallible.Unfailing(c), AFallible.MaybeFailing(a,_)) => Soundness.isSound(c,a)
  case (cfallible,afallible) =>
    IsSound.NotSound(s"Abstract Fallible $afallible does not overapproximate concrete fallible $cfallible")
}