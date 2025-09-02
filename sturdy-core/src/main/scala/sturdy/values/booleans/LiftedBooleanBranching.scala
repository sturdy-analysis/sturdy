package sturdy.values.booleans

import sturdy.effect.EffectStack

final class LiftedBooleanSelection[B, VB, R](extract: B => VB)(using ops: BooleanSelection[VB, R]) extends BooleanSelection[B, R]:
  inline override def boolSelect(v: B, ifTrue: R, ifFalse: R): R = ops.boolSelect(extract(v), ifTrue, ifFalse)

final class LiftedBooleanBranching[B, VB, R](extract: B => VB)(using ops: BooleanBranching[VB, R]) extends BooleanBranching[B, R]:
  inline override def boolBranch(v: B, thn: => R, els: => R): R = ops.boolBranch(extract(v), thn, els)

final class LiftedBreakIf[B,VB](extract: B => VB)(using effectStack: EffectStack, ops: BreakIf[VB]) extends BreakIf[B]:
  inline override def breakIf(cond: B)(break: effectStack.State => Unit): Unit = ops.breakIf(extract(cond))(break)
  inline override def assertCondition(cond: B, state: effectStack.State): Unit = ops.assertCondition(extract(cond), state)