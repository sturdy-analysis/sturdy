package sturdy.effect.failure

import sturdy.effect.JoinComputation
import sturdy.effect.JoinComputation.StarvedJoin
import sturdy.values.Powerset
import sturdy.values.{PartialOrder, Abstractly}

import scala.collection.mutable.ListBuffer

case object AFailureCollectException extends FailureException

trait AFailureCollect extends Failure with JoinComputation:
  protected val failures: ListBuffer[(FailureKind,String)] = ListBuffer()

  def getFailures: List[(FailureKind,String)] = failures.toList

  override def fail(kind: FailureKind, msg: String): Nothing =
    failures += kind -> msg
    throw AFailureCollectException

  override def joinComputations[A](f: => A)(g: => A): Join[A] =
    try super.joinComputations(f)(g) catch {
      case StarvedJoin(AFailureCollectException, AFailureCollectException) =>
        throw AFailureCollectException
      case ex => throw ex
    }

  def fallible[A](f: => A): AFallible[A] =
    try {
      val res = f
      if (failures.isEmpty)
        AFallible.Unfailing(res)
      else
        AFallible.MaybeFailing(res, Powerset(failures.toSet))
    } catch {
      case AFailureCollectException => AFallible.Failing(Powerset(failures.toSet))
      case ex: StackOverflowError => throw ex
      case ex => AFallible.Failing(Powerset(failures.toSet + (RuntimeFailure -> ex.toString)))
    }
