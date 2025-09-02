package sturdy.values.booleans

import sturdy.data.{*, given}
import sturdy.values.{Join, Topped}
import sturdy.effect.failure.{AssertionFailure, Failure}
import sturdy.effect.EffectStack

given ToppedBooleanSelection[B, R](using ops: BooleanSelection[B, R])(using Join[R]): BooleanSelection[Topped[B], R] with
  override def boolSelect(v: Topped[B], ifTrue: R, ifFalse: R): R = v match
    case Topped.Top => Join(ifTrue, ifFalse).get
    case Topped.Actual(b) => ops.boolSelect(b, ifTrue, ifFalse)

given ToppedBooleanBranching[B, R](using ops: BooleanBranching[B, R])(using EffectStack, Join[R]): BooleanBranching[Topped[B], R] with
  override def boolBranch(v: Topped[B], thn: => R, els: => R): R = v match
    case Topped.Top => joinComputations(thn)(els)
    case Topped.Actual(b) => ops.boolBranch(b, thn, els)

given ToppedBreakIf[B](using failure: Failure, effectStack: EffectStack): BreakIf[Topped[Boolean]] with
  override def breakIf(cond: Topped[Boolean])(break: effectStack.State => Unit): Unit =
    val state = effectStack.getState
    cond match
      case Topped.Actual(true) => assertCondition(cond, state)
      case Topped.Actual(false) => break(effectStack.getState); assertCondition(cond, state)
      case Topped.Top => joinComputations { break(state) } { }; assertCondition(cond, state)

  override def assertCondition(cond: Topped[Boolean], state: effectStack.State): Unit =
    effectStack.setState(state)
    cond match
      case Topped.Actual(true) => {}
      case Topped.Actual(false) => failure.fail(AssertionFailure(cond), s"Expected condition $cond to be true, but the condition was false")
      case Topped.Top => joinWithFailure {} { failure.fail(AssertionFailure(cond), s"Expected condition $cond to be true, but the condition was false") }