package sturdy.data

import sturdy.effect.Effectful
import sturdy.values.JoinValue

trait Option[J[_], A]:
  def option[B](default: => B)(f: A => B): J[B] ?=> B
  inline final def getOrElse(default: => A): J[A] ?=> A =
    option(default)(identity)
  inline final def orElseAndThen[B](default: => A)(f: A => B): J[B] ?=> B =
    option(f(default))(f)
  final def get: J[A] ?=> A = option(throw new MatchError(this))(identity)



enum OptionC[A] extends Option[NoJoin, A]:
  case None()
  case Some(a: A)

  override def option[B](default: => B)(f: A => B): NoJoin[B] ?=> B = this match
    case None() => default
    case Some(a) => f(a)

object OptionC:
  inline def none[A]: OptionC[A] = OptionC.None()
  inline def some[A](a: A): OptionC[A] = OptionC.Some(a)
  def apply[A](opt: scala.Option[A]): OptionC[A] = opt match
    case scala.Some(a) => OptionC.Some(a)
    case scala.None => OptionC.None()

enum OptionA[A] extends Option[Join, A]:
  case None()
  case NoneSome(as: Iterable[A])
  case Some(as: Iterable[A])

  override def option[B](default: => B)(f: A => B): Join[B] ?=> B = this match
    case Some(as) if as.nonEmpty =>
      summon[Effectful].joinComputationsIterable(as.map(a => () => f(a)))
    case NoneSome(as) if as.nonEmpty =>
      summon[Effectful].joinComputationsIterable(as.map(a => () => f(a)) ++ Iterable.single(() => default))
    case _ => default

  def +[AA <: A](a: AA): OptionA[A] = this match
    case None() => Some(Iterable.single(a))
    case NoneSome(as) => NoneSome(as concat Iterable.single(a))
    case Some(as) => Some(as concat Iterable.single(a))

  def joinShallow[AA <: A](that: OptionA[AA]): OptionA[A] = (this, that) match
    case (None(), None()) => None()
    case (None(), NoneSome(as2)) => NoneSome(as2)
    case (None(), Some(as2)) => NoneSome(as2)
    case (NoneSome(as1), None()) => NoneSome(as1)
    case (NoneSome(as1), NoneSome(as2)) => NoneSome(as1 ++ as2)
    case (NoneSome(as1), Some(as2)) => NoneSome(as1 ++ as2)
    case (Some(as1), None()) => NoneSome(as1)
    case (Some(as1), NoneSome(as2)) => NoneSome(as1 ++ as2)
    case (Some(as1), Some(as2)) => Some(as1 ++ as2)

  def joinDeep[AA <: A](that: OptionA[AA])(using JoinValue[A]): OptionA[A] = (this, that) match
    case (None(), None()) => None()
    case (None(), NoneSome(e2::Nil)) => NoneSome(e2::Nil)
    case (None(), Some(e2::Nil)) => NoneSome(e2::Nil)
    case (NoneSome(e1::Nil), None()) => NoneSome(e1::Nil)
    case (NoneSome(e1::Nil), NoneSome(e2::Nil)) => NoneSome(JoinValue.join(e1,e2)::Nil)
    case (NoneSome(e1::Nil), Some(e2::Nil)) => NoneSome(JoinValue.join(e1,e2)::Nil)
    case (Some(e1::Nil), None()) => NoneSome(e1::Nil)
    case (Some(e1::Nil), NoneSome(e2::Nil)) => NoneSome(JoinValue.join(e1,e2)::Nil)
    case (Some(e1::Nil), Some(e2::Nil)) => Some(JoinValue.join(e1,e2)::Nil)
    case _ => throw new IllegalStateException()

object OptionA:
  inline def none[A]: OptionA[A] = OptionA.None()
  inline def noneSome[A](as: A*): OptionA[A] = OptionA.NoneSome(as)
  inline def some[A](as: A*): OptionA[A] = OptionA.Some(as)
  def apply[A](opt: scala.Option[A]): OptionA[A] = opt match
    case scala.Some(a) => OptionA.Some(Iterable.single(a))
    case scala.None => OptionA.None()