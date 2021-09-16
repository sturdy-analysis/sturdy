package sturdy.effect.failure

import sturdy.effect.Effectful
import sturdy.effect.Effectful.StarvedJoin
import sturdy.values.Powerset
import sturdy.values.Abstractly
import sturdy.values.PartialOrder

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

case object AFailureCollectException extends FailureException

trait AFailureCollect extends Failure with Effectful:
  protected val failures: ListBuffer[(FailureKind,String)] = ListBuffer()

  def getFailures: List[(FailureKind,String)] = failures.toList

  override def fail(kind: FailureKind, msg: String): Nothing =
    failures += kind -> msg
    throw AFailureCollectException

  override def joinFailedComputations(failA: Throwable, failB: Throwable): Throwable = (failA, failB) match
    case (AFailureCollectException, AFailureCollectException) => AFailureCollectException
    case (AFailureCollectException, ex) => ex
    case (ex, AFailureCollectException) => ex
    case _ => super.joinFailedComputations(failA, failB)

  def fallible[A](f: => A): AFallible[A] =
    try {
      val res = f
      if (failures.isEmpty)
        AFallible.Unfailing(res)
      else
        AFallible.MaybeFailing(res, Powerset(failures.toSet))
    } catch {
      case AFailureCollectException => AFallible.Failing(Powerset(failures.toSet))
      case NonFatal(ex) => AFallible.Failing(Powerset(failures.toSet + (RuntimeFailure -> ex.toString)))
    }
