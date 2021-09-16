package sturdy.effect.except

import sturdy.effect.EitherCompute
import sturdy.effect.Effectful
import sturdy.effect.EitherComputeAbstract
import sturdy.effect.Join
import sturdy.values.JoinValue

import scala.collection.mutable.ListBuffer
import scala.util.Success

case object AbstractException extends ExceptException:
  override def toString: String = s"Exception (abstract)"


trait JoinedExcept[E](using exJoin: JoinValue[E]) extends Except[E], Effectful:
  override type ExceptJoin[A] = Join[A]

  protected var exception: Option[E] = None

  def getException: Option[E] = exception

  override def throws(ex: E): Nothing =
    exception match
      case None => exception = Some(ex)
      case Some(old) => exception = Some(exJoin.joinValues(old, ex))
    throw AbstractException

  override def tries[A](f: => A): EitherComputeAbstract[A, E] =
    try {
      val a = f
      exception match
        case None => EitherComputeAbstract.Left(a)
        case Some(ex) => EitherComputeAbstract.LeftRight(a, ex)
    } catch {
      case AbstractException => EitherComputeAbstract.Right(exception.get)
      case ex => throw ex
    }

  override def joinFailedComputations(failA: Throwable, failB: Throwable): Throwable = (failA, failB) match
    case (AbstractException, AbstractException) => AbstractException
    case (AbstractException, ex) => ex
    case (ex, AbstractException) => ex
    case _ => super.joinFailedComputations(failA, failB)
