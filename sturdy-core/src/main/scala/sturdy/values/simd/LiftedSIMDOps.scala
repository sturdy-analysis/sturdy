package sturdy.values.simd

class LiftedSIMDOps[B, V, I](extract: V => I, inject: I => V)(using ops: SIMDOps[B, I]) extends SIMDOps[B, V] {
  def vectorLit(i: B): V = inject(ops.vectorLit(i))
  
}
