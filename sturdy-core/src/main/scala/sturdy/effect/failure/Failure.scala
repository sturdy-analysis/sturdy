package sturdy.effect.failure

import sturdy.effect.SturdyException

trait FailureException extends Throwable
trait FailureKind
object RuntimeFailure extends FailureKind


trait Failure extends SturdyException:
  @throws[FailureException]
  def fail(kind: FailureKind, msg: String): Nothing
