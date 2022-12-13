package sturdy.effect.assert

import sturdy.effect.Concrete
import sturdy.effect.failure.{Failure,FailureKind}
import sturdy.values.booleans.{BooleanOps, BooleanBranching}

import sturdy.effect.assert.Assert

object AssertionFailure extends FailureKind

class CAssert[A](using f: Failure, b: BooleanBranching[A, Unit]) extends Assert[A], Concrete:

  override def apply(a: A): Unit = 
    b.boolBranch(a, 
      {}, 
      {f.fail(AssertionFailure, "failed to prove assertion...")}
    )