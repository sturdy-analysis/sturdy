package sturdy.effect.except

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.JoinValue
import sturdy.values.exceptions.Exceptional

import scala.collection.mutable.ListBuffer
import scala.util.Success

case object AbstractException extends ExceptException:
  override def toString: String = s"Exception (abstract)"


trait JoinedExcept[Exc, E](using val exceptional: Exceptional[Exc, E, Join], eJoin: JoinValue[E]) extends Except[Exc, E], Effectful:
  override type ExceptJoin[A] = Join[A]

  protected var exception: OptionA[E] = OptionA.none

  def getException: OptionA[E] = exception

  override def throws(ex: Exc): Nothing =
    val e = exceptional.exception(ex)
    exception += e
    throw AbstractException

  override protected def tries[A](f: => A): EitherA[A, E] =
    try {
      val a = f
      exception match
        case OptionA.None() => EitherA.Left(Iterable.single(a))
        case OptionA.Some(exs) => EitherA.Right(exs)
        case OptionA.NoneSome(exs) => EitherA.LeftRight(Iterable.single(a), exs)
    } catch {
      case AbstractException =>
        exception match
          case OptionA.None() => throw new IllegalStateException(s"exception cannot be None here")
          case OptionA.NoneSome(exs) => throw new IllegalStateException(s"exception cannot be NoneSome here")
          case OptionA.Some(exs) => EitherA.Right(exs)
      case ex => throw ex
    }

  override def joinFailedComputations(failA: Throwable, failB: Throwable): Throwable = (failA, failB) match
    case (AbstractException, AbstractException) => AbstractException
    case (AbstractException, ex) => ex
    case (ex, AbstractException) => ex
    case _ => super.joinFailedComputations(failA, failB)
