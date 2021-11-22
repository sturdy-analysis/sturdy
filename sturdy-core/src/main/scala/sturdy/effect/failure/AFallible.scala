package sturdy.effect.failure

import sturdy.values.Abstractly
import sturdy.values.PartialOrder
import sturdy.values.Powerset

enum AFallible[T]:
  case Unfailing(t: T)
  case Failing(msgs: Powerset[(FailureKind,String)])
  case MaybeFailing(t: T, msgs: Powerset[(FailureKind,String)])

given cfallibleAbstractly[C, A](using abs: Abstractly[C, A]): Abstractly[CFallible[C], AFallible[A]] with
  override def abstractly(c: CFallible[C]): AFallible[A] = c match
    case CFallible.Unfailing(c) => AFallible.Unfailing(abs.abstractly(c))
    case CFallible.Failing(kind, msg) => AFallible.Failing(Powerset(kind -> msg))

given afallibleAbstractly[C, A](using abs: Abstractly[C, A]): Abstractly[AFallible[C], AFallible[A]] with
  override def abstractly(a: AFallible[C]): AFallible[A] = a match
    case AFallible.Unfailing(t) => AFallible.Unfailing(abs.abstractly(t))
    case AFallible.MaybeFailing(t, msgs) => AFallible.MaybeFailing(abs.abstractly(t), msgs)
    case AFallible.Failing(msgs) => AFallible.Failing(msgs)

given falliblePO[T](using po: PartialOrder[T]): PartialOrder[AFallible[T]] with
  override def lteq(x: AFallible[T], y: AFallible[T]): Boolean = (x, y) match
    case (AFallible.Unfailing(t1), AFallible.Unfailing(t2)) => po.lteq(t1, t2)
    case (AFallible.Failing(fails1), AFallible.Failing(fails2)) => fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
    case (AFallible.Unfailing(t1), AFallible.MaybeFailing(t2, fails2)) => po.lteq(t1, t2)
    case (AFallible.Failing(fails1), AFallible.MaybeFailing(t2, fails2)) => fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
    case (AFallible.MaybeFailing(t1, fails1), AFallible.MaybeFailing(t2, fails2)) => po.lteq(t1, t2) && fails1.set.map(_._1).subsetOf(fails2.set.map(_._1))
    case _ => false
