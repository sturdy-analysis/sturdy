package sturdy.effect.failure

import sturdy.effect.SturdyException

trait FailureException extends SturdyException

trait FailureKind
object RuntimeFailure extends FailureKind

trait Failure:
  @throws[FailureException]
  def fail(kind: FailureKind, msg: String): Nothing
