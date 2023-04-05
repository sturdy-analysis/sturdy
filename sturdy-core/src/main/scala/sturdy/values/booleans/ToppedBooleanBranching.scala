package sturdy.values.booleans

import sturdy.data.{joinComputations, WithJoin, NoJoin, unit, MakeJoined}
import sturdy.values.{Topped, Join}
import sturdy.effect.failure.Failure
import sturdy.effect.EffectStack

given ToppedBooleanSelection[B, R](using ops: BooleanSelection[B, R])(using Join[R]): BooleanSelection[Topped[B], R] with
  override def boolSelect(v: Topped[B], ifTrue: R, ifFalse: R): R = v match
    case Topped.Top => Join(ifTrue, ifFalse).get
    case Topped.Actual(b) => ops.boolSelect(b, ifTrue, ifFalse)

given ToppedBooleanBranching[B, R](using ops: BooleanBranching[B, R])(using EffectStack, Join[R]): BooleanBranching[Topped[B], R] with
  override def boolBranch(v: Topped[B], thn: => R, els: => R): R = v match
    case Topped.Top => joinComputations(thn)(els)
    case Topped.Actual(b) => ops.boolBranch(b, thn, els)