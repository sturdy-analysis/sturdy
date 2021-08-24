package sturdy.effect.failure

import sturdy.values.{Abstractly, PartialOrder, Powerset}

enum AFallible[T]:
  case Unfailing(t: T)
  case Failing(msgs: Powerset[(FailureKind,String)])
  case MaybeFailing(t: T, msgs: Powerset[(FailureKind,String)])

given fallibleAbstractly[C, A](using abs: Abstractly[C, A]): Abstractly[CFallible[C], AFallible[A]] with
  override def abstractly(c: CFallible[C]): AFallible[A] = c match
    case CFallible.Unfailing(c) => AFallible.Unfailing(abs.abstractly(c))
    case CFallible.Failing(kind, msg) => AFallible.Failing(Powerset(kind -> msg))

given falliblePO[T](using po: PartialOrder[T]): PartialOrder[AFallible[T]] with
  override def lteq(x: AFallible[T], y: AFallible[T]): Boolean = (x, y) match
    case (AFallible.Unfailing(t1), AFallible.Unfailing(t2)) => po.lteq(t1, t2)
    case (AFallible.Failing(fails1), AFallible.Failing(fails2)) => fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
    case (AFallible.Unfailing(t1), AFallible.MaybeFailing(t2, fails2)) => po.lteq(t1, t2)
    case (AFallible.Failing(fails1), AFallible.MaybeFailing(t2, fails2)) => fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
    case (AFallible.MaybeFailing(t1, fails1), AFallible.MaybeFailing(t2, fails2)) => po.lteq(t1, t2) && fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
    case _ => false
