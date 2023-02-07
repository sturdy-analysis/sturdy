package sturdy.effect.failure

import sturdy.effect.failure.{Failure,FailureKind}
import sturdy.values.booleans.BooleanBranching

case class AssertionFailure[Context](exp: Context) extends FailureKind

def assert[A, Info](a: A, info: Info)(using f: Failure, b: BooleanBranching[A, Unit]): Unit =
  b.boolBranch(a,
    {},
    {f.fail(AssertionFailure(info), s"assert($a, $info) failed")}
  )