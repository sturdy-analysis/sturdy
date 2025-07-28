package sturdy.values.simd

import sturdy.effect.failure.Failure

given ConcreteSIMDOps (using f: Failure): SIMDOps [Array[Byte], Array[Byte]] with
  def vectorLit(i: Array[Byte]): Array[Byte] = i

