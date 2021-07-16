package sturdy.effect.failure

case class CFailureException(kind: FailureKind, msg: String) extends FailureException

trait CFailure extends Failure:
  override def fail(kind: FailureKind, msg: String): Nothing =
    throw CFailureException(kind, msg)
