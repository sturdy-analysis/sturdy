package sturdy.values.ordering

final class LiftedOrderingOps[V, B, UV, UB](extract: V => UV, inject: UB => B)(using ops: OrderingOps[UV, UB]) extends OrderingOps[V, B]:
  inline override def lt(v1: V, v2: V): B = inject(ops.lt(extract(v1), extract(v2)))
  inline override def le(v1: V, v2: V): B = inject(ops.le(extract(v1), extract(v2)))
