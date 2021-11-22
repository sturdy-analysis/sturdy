package sturdy.effect.failure

import sturdy.effect.Effectful
import sturdy.effect.SturdyFailure
import sturdy.values.Powerset
import sturdy.values.Abstractly
import sturdy.values.PartialOrder

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

case object AFailureCollectException extends SturdyFailure

class AFailureCollect extends Failure with Effectful:
  protected val failures: ListBuffer[(FailureKind,String)] = ListBuffer()

  override def fail(kind: FailureKind, msg: String): Nothing =
    failures += kind -> msg
    throw AFailureCollectException
  
  def fallible[A](f: => A): AFallible[A] =
    try {
      val res = f
      if (failures.isEmpty)
        AFallible.Unfailing(res)
      else
        AFallible.MaybeFailing(res, Powerset(failures.toSet))
    } catch {
      case AFailureCollectException => AFallible.Failing(Powerset(failures.toSet))
      case ex => throw ex
    }

  override type State = List[(FailureKind,String)]
  override def getState: List[(FailureKind,String)] = failures.toList
  override def setState(s: List[(FailureKind, String)]): Unit =
    failures.clear()
    failures ++= s

