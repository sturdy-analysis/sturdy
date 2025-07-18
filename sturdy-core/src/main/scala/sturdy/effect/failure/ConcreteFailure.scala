package sturdy.effect.failure

import sturdy.effect.Concrete
import sturdy.effect.SturdyFailure

import scala.util.control.NonFatal

case class CFailureException(kind: FailureKind, msg: String) extends SturdyFailure:
  override def toString: String = s"Failure $kind: $msg"

case class CFailureStackOverflow(e: StackOverflowError) extends SturdyFailure:
  override def toString: String = s"Failure StackOverflow"

class ConcreteFailure extends Failure, Concrete:
  override def fail(kind: FailureKind, msg: String): Nothing =
    throw CFailureException(kind, msg)
  def stackOverflow(e: StackOverflowError): Nothing =
    throw CFailureStackOverflow(e)

  def fallible[A](f: => A): CFallible[A] =
    try {
      val res = f
      CFallible.Unfailing(res)
    } catch {
      case CFailureException(kind, msg) => CFallible.Failing(kind, msg)
      case CFailureStackOverflow(e) => CFallible.StackOverflow(e)
      case ex => throw ex
    }
