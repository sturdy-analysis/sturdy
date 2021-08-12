package sturdy.effect

import sturdy.values.JoinValue

trait MayCompute[A, Join[_], JoinComp]:
  def withDefault[B](default: => B)(f: A => B)(using JoinComp)(using Join[B]): B
  inline final def orElse(default: => A)(using JoinComp)(using Join[A]): A =
    withDefault(default)(identity)
  inline final def orElseAndThen[B](default: => A)(f: A => B)(using JoinComp)(using Join[B]): B =
    withDefault(f(default))(f)


private final class NoJoin[A]
given noJoin[A]: NoJoin[A] = new NoJoin

enum CMayCompute[A] extends MayCompute[A, NoJoin, Unit]:
  case Computes(a: A)
  case ComputesNot()

  override def withDefault[B](default: => B)(f: A => B)(using Unit)(using NoJoin[B]): B = this match
    case Computes(a) => f(a)
    case ComputesNot() => default

object CMayCompute:
  def apply[A](opt: Option[A]): CMayCompute[A] = opt match
    case Some(a) => CMayCompute.Computes(a)
    case None => CMayCompute.ComputesNot()



enum AMayComputeOne[A] extends MayCompute[A, JoinValue, JoinComputation]:
  case Computes(a: A)
  case MaybeComputes(a: A)
  case ComputesNot()

  override def withDefault[B](default: => B)(f: A => B)(using j: JoinComputation)(using JoinValue[B]): B = this match
    case Computes(a) => f(a)
    case MaybeComputes(a) => j.joinComputations(f(a))(default)
    case ComputesNot() => default


enum AMayComputeMany[A] extends MayCompute[A, JoinValue, JoinComputation]:
  case Computes(as: Iterable[A])
  case MaybeComputes(as: Iterable[A])
  case ComputesNot()

  override def withDefault[B](default: => B)(f: A => B)(using j: JoinComputation)(using JoinValue[B]): B = this match
    case Computes(as) if as.nonEmpty => j.joinComputationsIterable(as.map(a => () => f(a)))
    case MaybeComputes(as) if as.nonEmpty => j.joinComputationsIterable(as.map(a => () => f(a)) ++ Iterable.single(() => default))
    case _ => default
