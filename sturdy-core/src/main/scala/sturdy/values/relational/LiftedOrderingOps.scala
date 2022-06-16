package sturdy.values.relational

class LiftedOrderingOps[V, B, UV, UB](extract: V => UV, inject: UB => B)(using ops: OrderingOps[UV, UB]) extends OrderingOps[V, B]:
  override def lt(v1: V, v2: V): B = inject(ops.lt(extract(v1), extract(v2)))
  override def le(v1: V, v2: V): B = inject(ops.le(extract(v1), extract(v2)))
