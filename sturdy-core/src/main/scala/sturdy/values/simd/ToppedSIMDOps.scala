package sturdy.values.simd

import sturdy.effect.failure.Failure
import sturdy.values.Topped

given ToppedSIMDOps[B, T] (using f: Failure): SIMDOps[B, Topped[T]] with {

  override def vectorLit(i: B): Topped[T] = ???
}