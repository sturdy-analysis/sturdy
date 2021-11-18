package sturdy.values.booleans

class LiftedBooleanBranching[B, VB, R](extract: B => VB)(using val ops: BooleanBranching[VB, R]) extends BooleanBranching[B, R]:
  override def boolBranch(v: B, thn: => R, els: => R): R =
    ops.boolBranch(extract(v), thn, els)