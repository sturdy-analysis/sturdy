package sturdy.effect.failure

import sturdy.effect.SturdyFailure

trait FailureKind
object RuntimeFailure extends FailureKind

trait Failure:
  @throws[SturdyFailure]
  def fail(kind: FailureKind, msg: String): Nothing

object Failure:
  def apply(kind: FailureKind, msg: String)(using f: Failure): Nothing =
    f.fail(kind, msg)