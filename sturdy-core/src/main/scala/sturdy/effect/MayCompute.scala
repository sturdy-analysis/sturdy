package sturdy.effect

import sturdy.values.JoinValue

trait MayCompute[J[_], A]:
  def withDefault[B](default: => B)(f: A => B): J[B] ?=> B
  inline final def orElse(default: => A): J[A] ?=> A =
    withDefault(default)(identity)
  inline final def orElseAndThen[B](default: => A)(f: A => B): J[B] ?=> B =
    withDefault(f(default))(f)
  final def get: J[A] ?=> A = withDefault(throw new MatchError(this))(identity)


enum MayComputeConcrete[A] extends MayCompute[NoJoin, A]:
  case Computes(a: A)
  case ComputesNot()

  override def withDefault[B](default: => B)(f: A => B): NoJoin[B] ?=> B = this match
    case Computes(a) => f(a)
    case ComputesNot() => default

object MayComputeConcrete:
  def apply[A](opt: Option[A]): MayComputeConcrete[A] = opt match
    case Some(a) => MayComputeConcrete.Computes(a)
    case None => MayComputeConcrete.ComputesNot()



enum MayComputeOne[A] extends MayCompute[Join, A]:
  case Computes(a: A)
  case MaybeComputes(a: A)
  case ComputesNot()

  override def withDefault[B](default: => B)(f: A => B): Join[B] ?=> B = this match
    case Computes(a) => f(a)
    case MaybeComputes(a) => summon[Effectful].joinComputations(f(a))(default)
    case ComputesNot() => default


enum MayComputeMany[A] extends MayCompute[Join, A]:
  case Computes(as: Iterable[A])
  case MaybeComputes(as: Iterable[A])
  case ComputesNot()

  override def withDefault[B](default: => B)(f: A => B): Join[B] ?=> B = this match
    case Computes(as) if as.nonEmpty =>
      summon[Effectful].joinComputationsIterable(as.map(a => () => f(a)))
    case MaybeComputes(as) if as.nonEmpty =>
      summon[Effectful].joinComputationsIterable(as.map(a => () => f(a)) ++ Iterable.single(() => default))
    case _ => default
