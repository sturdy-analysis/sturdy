package sturdy.effect.failure

import sturdy.effect.Effect
import sturdy.effect.Monotone
import sturdy.effect.RecurrentCall
import sturdy.effect.SturdyFailure
import sturdy.values.{*, given}

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

case object AFailureCollectException extends SturdyFailure

class CollectedFailures[K <: FailureKind](using Finite[K]) extends Failure, Monotone:
  protected var failureKinds: Set[FailureKind] = Set()
  protected val failures: ListBuffer[(FailureKind,String)] = ListBuffer()

  override def fail(kind: FailureKind, msg: String): Nothing =
    failureKinds += kind
    failures += ((kind, msg))
    throw AFailureCollectException
  
  def fallible[A](f: => A): AFallible[A] =
    try {
      val res = f
      if (failures.isEmpty)
        AFallible.Unfailing(res)
      else
        AFallible.MaybeFailing(res, Powerset(failures.toSet))
    } catch {
      case exc: SturdyFailure => AFallible.Failing(Powerset(failures.toSet))
      case recur: RecurrentCall => AFallible.Diverging(recur)
      case ex => throw ex
    }
