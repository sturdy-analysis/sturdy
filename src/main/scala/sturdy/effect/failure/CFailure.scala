package sturdy.effect.failure

case class CFailureException(msg: String) extends FailureException

trait CFailure extends Failure:
  override def fail(msg: String): Nothing =
    throw CFailureException(msg)
