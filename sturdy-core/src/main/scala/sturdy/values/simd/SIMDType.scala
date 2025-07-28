package sturdy.values.simd

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.types.BaseType

import scala.reflect.ClassTag


given TypeSIMDOps[B: ClassTag](using f: Failure, j: EffectStack): SIMDOps[B, BaseType[B]] with
  def vectorLit(i: B): BaseType[B] = BaseType[B]
