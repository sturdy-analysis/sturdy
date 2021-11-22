package sturdy.data

import sturdy.effect.Effectful
import sturdy.values.Join
import sturdy.values.Powerset

trait JOption[J[_] <: MayJoin[_], A]:
  def option[B](default: => B)(f: A => B): J[B] ?=> B
  inline final def getOrElse(default: => A): J[A] ?=> A =
    option(default)(identity)
  inline final def orElseAndThen[B](default: => A)(f: A => B): J[B] ?=> B =
    option(f(default))(f)
  final def get: J[A] ?=> A = option(throw new MatchError(this))(identity)

  def map[B](f: A => B): JOption[J, B]
  def flatMap[B](f: A => JOption[J, B]): JOption[J, B]



case class SomeJOption[J[_] <: MayJoin[_], A](a: A) extends JOption[J, A]:
  override def option[B](default: => B)(f: A => B): J[B] ?=> B = f(a)
  override def map[B](f: A => B): JOption[J, B] = SomeJOption(f(a))
  override def flatMap[B](f: A => JOption[J, B]): JOption[J, B] = f(a)



enum JOptionC[A] extends JOption[NoJoin, A]:
  case None()
  case Some(a: A)

  override def option[B](default: => B)(f: A => B): NoJoin[B] ?=> B = this match
    case None() => default
    case Some(a) => f(a)

  override def map[B](f: A => B): JOptionC[B] = this match
    case None() => None()
    case Some(a) => Some(f(a))

  override def flatMap[B](f: A => JOption[NoJoin, B]): JOption[NoJoin, B] = this match
    case None() => None()
    case Some(a) => f(a)

object JOptionC:
  inline def none[A]: JOptionC[A] = JOptionC.None()
  inline def some[A](a: A): JOptionC[A] = JOptionC.Some(a)
  def apply[A](opt: scala.Option[A]): JOptionC[A] = opt match
    case scala.Some(a) => JOptionC.Some(a)
    case scala.None => JOptionC.None()



enum JOptionA[A] extends JOption[WithJoin, A]:
  case None()
  case NoneSome(a: A)
  case Some(a: A)

  override def option[B](default: => B)(f: A => B): WithJoin[B] ?=> B = this match
    case Some(a) => f(a)
    case NoneSome(a) => joinComputations(f(a))(default)
    case None() => default

  override def map[B](f: A => B): JOptionA[B] = this match
    case None() => None()
    case NoneSome(a) => NoneSome(f(a))
    case Some(a) => Some(f(a))

  override def flatMap[B](f: A => JOption[WithJoin, B]): JOption[WithJoin, B] = this match
    case None() => None()
    case NoneSome(a) => f(a) match
      case None() => None()
      case NoneSome(b) => NoneSome(b)
      case Some(b) => NoneSome(b)
      case other => throw new IllegalArgumentException(s"Cannot flatMap OptionA to different type $other")
    case Some(a) => f(a)

  def joinDeep[AA <: A](that: JOptionA[AA])(using Join[A]): JOptionA[A] = (this, that) match
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

object JOptionA:
  inline def none[A]: JOptionA[A] = JOptionA.None()
  inline def noneSome[A](a: A): JOptionA[A] = JOptionA.NoneSome(a)
  inline def some[A](a: A): JOptionA[A] = JOptionA.Some(a)
  def apply[A](opt: scala.Option[A]): JOptionA[A] = opt match
    case scala.Some(a) => JOptionA.Some(a)
    case scala.None => JOptionA.None()

//case class JOptionPower[A](opt: JOption[WithJoin, Powerset[A]]) extends JOption[WithJoin, A]:
//  override def option[B](default: => B)(f: A => B): WithJoin[B] ?=> B =
//    opt.option(default)(p => mapJoin(p.set, f))
//
//  override def map[B](f: A => B): JOptionPower[B] =
//    JOptionPower(opt.map(_.map(f)))
//
//  override def flatMap[B](f: A => JOption[WithJoin, B]): JOptionPower[B] =
//    JOptionPower(opt.flatMap())

enum JOptionPowerset[A] extends JOption[WithJoin, A]:
  case None()
  case NoneSome(as: Powerset[A])
  case Some(as: Powerset[A])

  override def option[B](default: => B)(f: A => B): WithJoin[B] ?=> B = this match
    case Some(as) => mapJoin(as.set, f)
    case NoneSome(as) => joinComputations(mapJoin(as.set, f))(default)
    case None() => default

  override def map[B](f: A => B): JOptionPowerset[B] = this match
    case None() => None()
    case NoneSome(as) => NoneSome(as.map(f))
    case Some(as) => Some(as.map(f))

  override def flatMap[B](f: A => JOption[WithJoin, B]): JOption[WithJoin, B] = this match
    case None() => None()
    case NoneSome(as) => flat(as, f, mustNone = true)
    case Some(as) => flat(as, f, mustNone = false)

  private def flat[B](as: Powerset[A], f: A => JOption[WithJoin, B], mustNone: Boolean): JOptionPowerset[B] =
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
  def joinDeep(that: JOptionPowerset[A]): JOptionPowerset[A] = (this, that) match
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