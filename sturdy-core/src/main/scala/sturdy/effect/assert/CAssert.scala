package sturdy.effect.assert

import sturdy.effect.Concrete
import sturdy.effect.failure.{Failure,FailureKind}
import sturdy.values.booleans.{BooleanOps, BooleanBranching}

import sturdy.effect.assert.Assert

object AssertionFailure extends FailureKind

class CAssert[A, Context](using f: Failure, b: BooleanBranching[A, Unit]) extends Assert[A, Context], Concrete:

  override def apply(a: A, c: Context): Unit = 
    b.boolBranch(a, 
      {}, 
      {f.fail(AssertionFailure, "failed to prove assertion at " + c)}
    )