package sturdy.effect.failure

trait FailureException extends Throwable
trait FailureKind

trait Failure:
  @throws[FailureException]
  def fail(kind: FailureKind, msg: String): Nothing
