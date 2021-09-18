package sturdy.data

import sturdy.effect.Effectful

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

object OptionA:
  inline def none[A]: OptionA[A] = OptionA.None()
  inline def noneSome[A](as: A*): OptionA[A] = OptionA.NoneSome(as)
  inline def some[A](as: A*): OptionA[A] = OptionA.Some(as)
