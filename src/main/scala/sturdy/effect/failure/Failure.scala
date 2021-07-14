package sturdy.effect.failure

trait FailureException extends Throwable

trait Failure:
  @throws[FailureException]
  def fail(msg: String): Nothing
