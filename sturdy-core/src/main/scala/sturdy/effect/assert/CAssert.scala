package sturdy.effect.assert

import sturdy.effect.Concrete
import sturdy.effect.failure.{Failure,FailureKind}
import sturdy.values.booleans.{BooleanOps, BooleanBranching}

import sturdy.effect.assert.Assert

case class AssertionFailure[Context](exp: Context) extends FailureKind:
  def failAssert()(using f: Failure) = f.fail(this, "") // or this, this.label?
// make it a class, parameterised by the assert statement and location

class CAssert[A, Context](using f: Failure, b: BooleanBranching[A, Unit]) extends Assert[A, Context], Concrete:

  override def apply(a: A, c: Context): Unit = 
    b.boolBranch(a, 
      {}, 
      {AssertionFailure(c).failAssert()}
    )