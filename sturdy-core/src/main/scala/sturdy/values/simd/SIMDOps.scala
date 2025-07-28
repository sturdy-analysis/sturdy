package sturdy.values.simd

trait SIMDOps[B, V]:
  def vectorLit(i: B): V