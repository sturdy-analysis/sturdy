package sturdy.values.booleans

class LiftedBooleanBranching[V, B, J[_]](extract: V => B)(using val ops: BooleanBranching[B, J]) extends BooleanBranching[V, J]:
  override def boolBranch[A](v: V, thn: => A, els: => A): J[A] ?=> A =
    ops.boolBranch(extract(v), thn, els)