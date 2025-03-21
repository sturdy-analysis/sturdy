package sturdy.effect.failure

import sturdy.effect.Effect
import sturdy.effect.SturdyFailure

trait FailureKind
trait DivergingKind extends FailureKind
object RuntimeFailure extends FailureKind

/** Effect [[Failure]] causes a fatal errors, which terminate program execution and cannot be recovered from. */
trait Failure extends Effect:
  @throws[SturdyFailure]
  def fail(kind: FailureKind, msg: String): Nothing
  inline def apply(kind: FailureKind, msg: String): Nothing = fail(kind, msg)

object Failure:
  def apply(kind: FailureKind, msg: String)(using f: Failure): Nothing =
    f.fail(kind, msg)