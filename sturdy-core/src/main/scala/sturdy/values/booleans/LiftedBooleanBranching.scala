package sturdy.values.booleans

final class LiftedBooleanSelection[B, VB, R](extract: B => VB)(using ops: BooleanSelection[VB, R]) extends BooleanSelection[B, R]:
  inline override def boolSelect(v: B, ifTrue: R, ifFalse: R): R = ops.boolSelect(extract(v), ifTrue, ifFalse)

final class LiftedBooleanBranching[B, VB, R](extract: B => VB)(using ops: BooleanBranching[VB, R]) extends BooleanBranching[B, R]:
  inline override def boolBranch(v: B, thn: => R, els: => R): R = ops.boolBranch(extract(v), thn, els)