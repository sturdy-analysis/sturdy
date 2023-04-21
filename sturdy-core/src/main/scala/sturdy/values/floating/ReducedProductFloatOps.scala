package sturdy.values.floating

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{Reduce, ReducedProduct, Topped}
import sturdy.values.convert.*

import java.nio.{ByteBuffer, ByteOrder}
given ReducedProductFloatOps[A, B, Lit](using floatA: FloatOps[Lit,A],
                                              floatB: FloatOps[Lit,B],
                                              reduce: Reduce[A,B]):
                                        FloatOps[Lit, ReducedProduct[A,B]] with
  override def floatingLit(l: Lit): ReducedProduct[A, B] =
    ReducedProduct(floatA.floatingLit(l),floatB.floatingLit(l)).reduce

  override def randomFloat(): ReducedProduct[A, B] =
    // Skip reduction, because random floats are probably going to be top anyways.
    ReducedProduct(floatA.randomFloat(),floatB.randomFloat())

  override def add(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, floatA.add, floatB.add)

  override def sub(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, floatA.sub, floatB.sub)

  override def mul(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, floatA.mul, floatB.mul)

  override def div(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, floatA.div, floatB.div)

  override def min(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, floatA.min, floatB.min)

  override def max(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, floatA.max, floatB.max)

  override def absolute(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, floatA.absolute, floatB.absolute)

  override def negated(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, floatA.negated, floatB.negated)

  override def sqrt(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, floatA.sqrt, floatB.sqrt)

  override def ceil(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, floatA.ceil, floatB.ceil)

  override def floor(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, floatA.floor, floatB.floor)

  override def truncate(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, floatA.truncate, floatB.truncate)

  override def nearest(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, floatA.nearest, floatB.nearest)

  override def copysign(v: ReducedProduct[A, B], sign: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v, sign, floatA.copysign, floatB.copysign)
