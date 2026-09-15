package sturdy.values.ordering

class LiftedUnsignedOrderingOps[V, B, UV, UB](extract: V => UV, inject: UB => B)(using ops: UnsignedOrderingOps[UV, UB]) extends UnsignedOrderingOps[V, B]:
  inline override def ltUnsigned(v1: V, v2: V): B = inject(ops.ltUnsigned(extract(v1), extract(v2)))
  inline override def leUnsigned(v1: V, v2: V): B = inject(ops.leUnsigned(extract(v1), extract(v2)))
