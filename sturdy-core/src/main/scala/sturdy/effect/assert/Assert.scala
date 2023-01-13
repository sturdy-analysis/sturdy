package sturdy.effect.assert

import sturdy.effect.Effect
import sturdy.effect.Concrete
import sturdy.effect.failure.{Failure,FailureKind}
import sturdy.values.booleans.{BooleanOps, BooleanBranching}

trait Assert[A, -Context] extends Effect:
  def apply(a: A, ctx: Context): Unit

case class AssertionFailure[Context](exp: Context) extends FailureKind

class CAssert[A, Context](using f: Failure, b: BooleanBranching[A, Unit]) extends Assert[A, Context], Concrete:

  override def apply(a: A, c: Context): Unit = 
    b.boolBranch(a, 
      {}, 
      {f.fail(AssertionFailure(c), " assertion " + c.toString + " failed")}
    )