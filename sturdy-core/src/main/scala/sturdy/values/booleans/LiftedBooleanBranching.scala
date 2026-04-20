package sturdy.values.booleans

import sturdy.effect.EffectStack
import sturdy.values.{Join, Widen}

final class LiftedBooleanSelection[B, VB, R](extract: B => VB)(using ops: BooleanSelection[VB, R]) extends BooleanSelection[B, R]:
  inline override def boolSelect(v: B, ifTrue: R, ifFalse: R): R = ops.boolSelect(extract(v), ifTrue, ifFalse)

final class LiftedBooleanBranching[B, VB, R](extract: B => VB)(using ops: BooleanBranching[VB, R]) extends BooleanBranching[B, R]:
  inline override def boolBranch(v: B, thn: => R, els: => R): R = ops.boolBranch(extract(v), thn, els)

final class LiftedBreakIf[B,VB](extract: B => VB)(using val ops: BreakIf[VB]) extends BreakIf[B]:
  override type State = ops.State
  inline override def break(br: ops.State => Unit): Unit = ops.break(br)
  inline override def breakIf(cond: B)(break: State => Unit): Unit = ops.breakIf(extract(cond))(break)
  inline override def assertCondition(cond: B, state: State): Unit = ops.assertCondition(extract(cond), state)
  inline override def joinClosingOver[Body](using Join[Body]): Join[(Body, State)] = ops.joinClosingOver
  inline override def widenClosingOver[Body](using Widen[Body]): Widen[(Body, State)] = ops.widenClosingOver