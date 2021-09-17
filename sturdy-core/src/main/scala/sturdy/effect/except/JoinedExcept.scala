package sturdy.effect.except

import sturdy.effect.EitherCompute
import sturdy.effect.Effectful
import sturdy.effect.EitherComputeAbstract
import sturdy.effect.Join
import sturdy.values.JoinValue
import sturdy.values.exceptions.Exceptional

import scala.collection.mutable.ListBuffer
import scala.util.Success

case object AbstractException extends ExceptException:
  override def toString: String = s"Exception (abstract)"


trait JoinedExcept[Exc, E](using val exceptional: Exceptional[Exc, E, Join], eJoin: JoinValue[E]) extends Except[Exc, E], Effectful:
  override type ExceptJoin[A] = Join[A]

  protected var exception: Option[E] = None

  def getException: Option[E] = exception

  override def throws(ex: Exc): Nothing =
    val e = exceptional.exception(ex)
    exception match
      case None => exception = Some(e)
      case Some(old) => exception = Some(eJoin.joinValues(old, e))
    throw AbstractException

  override protected def tries[A](f: => A): EitherComputeAbstract[A, E] =
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
