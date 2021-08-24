package sturdy.effect.failure

import scala.util.control.NonFatal

case class CFailureException(kind: FailureKind, msg: String) extends FailureException:
  override def toString: String = s"Failure $kind: $msg"

trait CFailure extends Failure:
  override def fail(kind: FailureKind, msg: String): Nothing =
    throw CFailureException(kind, msg)

  def fallible[A](f: => A): CFallible[A] =
    try {
      val res = f
      CFallible.Unfailing(res)
    } catch {
      case CFailureException(kind, msg) => CFallible.Failing(kind, msg)
      case NonFatal(ex) => CFallible.Failing(RuntimeFailure, ex.toString)
    }
