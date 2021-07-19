package sturdy.effect.failure

case class CFailureException(kind: FailureKind, msg: String) extends FailureException:
  override def toString: String = s"Failure $kind: $msg"

trait CFailure extends Failure:
  override def fail(kind: FailureKind, msg: String): Nothing =
    throw CFailureException(kind, msg)
