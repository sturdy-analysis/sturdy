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
  protected var failures: Map[FailureKind, Set[String]] = Map()

  override def fail(kind: FailureKind, msg: String): Nothing =
    failures.get(kind) match
      case Some(msgs) => failures += kind -> (msgs + msg)
      case None       => failures += kind -> Set(msg)
    throw AFailureCollectException
  
  def fallible[A](f: => A): AFallible[A] =
    try {
      val res = f
      if (failures.isEmpty)
        AFallible.Unfailing(res)
      else
        AFallible.MaybeFailing(res, Powerset(failureSet))
    } catch {
      case _: SturdyFailure => AFallible.Failing(Powerset(failureSet))
      case recur: RecurrentCall => AFallible.Diverging(recur)
      case ex => throw ex
    }

  private def failureSet: Set[(FailureKind, String)] = failures.iterator.flatMap((k, msgs) => msgs.map((k,_))).toSet