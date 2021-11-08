package sturdy.data

import sturdy.effect.Effectful
import sturdy.values.Join
import sturdy.values.Powerset

trait Option[J[_], A]:
  def option[B](default: => B)(f: A => B): J[B] ?=> B
  inline final def getOrElse(default: => A): J[A] ?=> A =
    option(default)(identity)
  inline final def orElseAndThen[B](default: => A)(f: A => B): J[B] ?=> B =
    option(f(default))(f)
  final def get: J[A] ?=> A = option(throw new MatchError(this))(identity)


case class LiftedOption[J[_],X,Y,R](x: Option[J,X], y: Option[J,Y], combine: (X,Y) => R) extends Option[J, R]:
  override def option[B](default: => B)(f: R => B): J[B] ?=> B =
    x.option(default)(x => y.option(default)(y => f(combine(x,y))))
    
case class SingletonOption[J[_], A](a: A) extends Option[J, A]:
  override def option[B](default: => B)(f: A => B): J[B] ?=> B =
    f(a)

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

enum OptionA[A] extends Option[WithJoin, A]:
  case None()
  case NoneSome(as: A)
  case Some(as: A)

  override def option[B](default: => B)(f: A => B): WithJoin[B] ?=> B = this match
    case Some(a) => f(a)
    case NoneSome(a) => joinComputations(f(a))(default)
    case None() => default

  def joinDeep[AA <: A](that: OptionA[AA])(using Join[A]): OptionA[A] = (this, that) match
    case (None(), None()) => None()
    case (None(), NoneSome(a2)) => NoneSome(a2)
    case (None(), Some(a2)) => NoneSome(a2)
    case (NoneSome(a1), None()) => NoneSome(a1)
    case (NoneSome(a1), NoneSome(a2)) => NoneSome(Join(a1,a2).get)
    case (NoneSome(a1), Some(a2)) => NoneSome(Join(a1,a2).get)
    case (Some(a1), None()) => NoneSome(a1)
    case (Some(a1), NoneSome(a2)) => NoneSome(Join(a1,a2).get)
    case (Some(a1), Some(a2)) => Some(Join(a1,a2).get)
    case _ => throw new IllegalStateException()

enum OptionPowerset[A] extends Option[WithJoin, A]:
  case None()
  case NoneSome(as: Powerset[A])
  case Some(as: Powerset[A])

  override def option[B](default: => B)(f: A => B): WithJoin[B] ?=> B = this match
    case Some(as) => mapJoin(as.set, f)
    case NoneSome(as) => joinComputations(mapJoin(as.set, f))(default)
    case None() => default

  import sturdy.values.JoinPowerset
  def joinDeep(that: OptionPowerset[A]): OptionPowerset[A] = (this, that) match
    case (None(), None()) => None()
    case (None(), NoneSome(a2)) => NoneSome(a2)
    case (None(), Some(a2)) => NoneSome(a2)
    case (NoneSome(a1), None()) => NoneSome(a1)
    case (NoneSome(a1), NoneSome(a2)) => NoneSome(Join(a1,a2).get)
    case (NoneSome(a1), Some(a2)) => NoneSome(Join(a1,a2).get)
    case (Some(a1), None()) => NoneSome(a1)
    case (Some(a1), NoneSome(a2)) => NoneSome(Join(a1,a2).get)
    case (Some(a1), Some(a2)) => Some(Join(a1,a2).get)
    case _ => throw new IllegalStateException()