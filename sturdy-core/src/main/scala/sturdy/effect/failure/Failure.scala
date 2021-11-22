package sturdy.effect.failure

import sturdy.effect.Effectful
import sturdy.effect.SturdyFailure

trait FailureKind
object RuntimeFailure extends FailureKind

trait Failure extends Effectful:
  @throws[SturdyFailure]
  def fail(kind: FailureKind, msg: String): Nothing
  inline def apply(kind: FailureKind, msg: String): Nothing = fail(kind, msg)

object Failure:
  def apply(kind: FailureKind, msg: String)(using f: Failure): Nothing =
    f.fail(kind, msg)