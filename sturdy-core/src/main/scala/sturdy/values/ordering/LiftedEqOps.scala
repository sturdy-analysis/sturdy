package sturdy.values.ordering

class LiftedEqOps[V, B, UV, UB](extract: V => UV, inject: UB => B)(using ops: EqOps[UV, UB]) extends EqOps[V, B]:
  inline override def equ(v1: V, v2: V): B = inject(ops.equ(extract(v1), extract(v2)))
  inline override def neq(v1: V, v2: V): B = inject(ops.neq(extract(v1), extract(v2)))
