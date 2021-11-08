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

  def map[B](f: A => B): Option[J, B]
  def flatMap[B](f: A => Option[J, B]): Option[J, B]

case class SomeOption[J[_], A](a: A) extends Option[J, A]:
  override def option[B](default: => B)(f: A => B): J[B] ?=> B = f(a)
  override def map[B](f: A => B): Option[J, B] = SomeOption(f(a))
  override def flatMap[B](f: A => Option[J, B]): Option[J, B] = f(a)

enum OptionC[A] extends Option[NoJoin, A]:
  case None()
  case Some(a: A)

  override def option[B](default: => B)(f: A => B): NoJoin[B] ?=> B = this match
    case None() => default
    case Some(a) => f(a)

  override def map[B](f: A => B): OptionC[B] = this match
    case None() => None()
    case Some(a) => Some(f(a))

  override def flatMap[B](f: A => Option[NoJoin, B]): Option[NoJoin, B] = this match
    case None() => None()
    case Some(a) => f(a)

object OptionC:
  inline def none[A]: OptionC[A] = OptionC.None()
  inline def some[A](a: A): OptionC[A] = OptionC.Some(a)
  def apply[A](opt: scala.Option[A]): OptionC[A] = opt match
    case scala.Some(a) => OptionC.Some(a)
    case scala.None => OptionC.None()

enum OptionA[A] extends Option[WithJoin, A]:
  case None()
  case NoneSome(a: A)
  case Some(a: A)

  override def option[B](default: => B)(f: A => B): WithJoin[B] ?=> B = this match
    case Some(a) => f(a)
    case NoneSome(a) => joinComputations(f(a))(default)
    case None() => default

  override def map[B](f: A => B): OptionA[B] = this match
    case None() => None()
    case NoneSome(a) => NoneSome(f(a))
    case Some(a) => Some(f(a))

  override def flatMap[B](f: A => Option[WithJoin, B]): Option[WithJoin, B] = this match
    case None() => None()
    case NoneSome(a) => f(a) match
      case None() => None()
      case NoneSome(b) => NoneSome(b)
      case Some(b) => NoneSome(b)
      case other => throw new IllegalArgumentException(s"Cannot flatMap OptionA to different type $other")
    case Some(a) => f(a)

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

object OptionA:
  inline def none[A]: OptionA[A] = OptionA.None()
  inline def noneSome[A](a: A): OptionA[A] = OptionA.NoneSome(a)
  inline def some[A](a: A): OptionA[A] = OptionA.Some(a)
  def apply[A](opt: scala.Option[A]): OptionA[A] = opt match
    case scala.Some(a) => OptionA.Some(a)
    case scala.None => OptionA.None()

enum OptionPowerset[A] extends Option[WithJoin, A]:
  case None()
  case NoneSome(as: Powerset[A])
  case Some(as: Powerset[A])

  override def option[B](default: => B)(f: A => B): WithJoin[B] ?=> B = this match
    case Some(as) => mapJoin(as.set, f)
    case NoneSome(as) => joinComputations(mapJoin(as.set, f))(default)
    case None() => default

  override def map[B](f: A => B): OptionPowerset[B] = this match
    case None() => None()
    case NoneSome(as) => NoneSome(as.map(f))
    case Some(as) => Some(as.map(f))

  override def flatMap[B](f: A => Option[WithJoin, B]): Option[WithJoin, B] = this match
    case None() => None()
    case NoneSome(as) => flat(as, f, mustNone = true)
    case Some(as) => flat(as, f, mustNone = false)

  private def flat[B](as: Powerset[A], f: A => Option[WithJoin, B], mustNone: Boolean) =
    var bs = Set[B]()
    var none = mustNone
    as.foreach { a => f(a) match
      case None() =>
        none = true
      case NoneSome(xs) =>
        none = true
        bs ++= xs.set
      case Some(xs) =>
        bs ++= xs.set
      case other =>
        throw new IllegalArgumentException(s"Cannot flatMap OptionPowerset to different type $other")
    }
    if (bs.isEmpty)
      None()
    else if (none)
      NoneSome(Powerset(bs))
    else
      Some(Powerset(bs))

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