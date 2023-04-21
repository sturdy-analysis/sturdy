package sturdy.values.integer

import sturdy.values.{Reduce, ReducedProduct}

given ReducedProductIntegerOps[A, B, Lit](using intA: IntegerOps[Lit,A],
                                                intB: IntegerOps[Lit,B],
                                                reduce: Reduce[A,B]):
                                          IntegerOps[Lit, ReducedProduct[A,B]] with
  override def integerLit(i: Lit): ReducedProduct[A, B] =
    ReducedProduct(intA.integerLit(i), intB.integerLit(i)).reduce

  override def randomInteger(): ReducedProduct[A, B] =
    ReducedProduct(intA.randomInteger(), intB.randomInteger())

  override def add(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.add, intB.add)
  override def sub(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.sub, intB.sub)
  override def mul(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.mul, intB.mul)
  override def max(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.max, intB.max)

  override def min(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.min, intB.min)

  override def absolute(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, intA.absolute, intB.absolute)

  override def div(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.div, intB.div)

  override def divUnsigned(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.divUnsigned, intB.divUnsigned)

  override def remainder(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.remainder, intB.remainder)

  override def remainderUnsigned(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.remainderUnsigned, intB.remainderUnsigned)

  override def modulo(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.modulo, intB.modulo)

  override def gcd(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.gcd, intB.gcd)

  override def bitAnd(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.bitAnd, intB.bitAnd)

  override def bitOr(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.bitOr, intB.bitOr)

  override def bitXor(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v1, v2, intA.bitXor, intB.bitXor)

  override def shiftLeft(v: ReducedProduct[A, B], shift: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v, shift, intA.shiftLeft, intB.shiftLeft)

  override def shiftRight(v: ReducedProduct[A, B], shift: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v, shift, intA.shiftRight, intB.shiftRight)

  override def shiftRightUnsigned(v: ReducedProduct[A, B], shift: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v, shift, intA.shiftRightUnsigned, intB.shiftRightUnsigned)

  override def rotateLeft(v: ReducedProduct[A, B], shift: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v, shift, intA.rotateLeft, intB.rotateLeft)

  override def rotateRight(v: ReducedProduct[A, B], shift: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.binop(v, shift, intA.rotateRight, intB.rotateRight)

  override def countLeadingZeros(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, intA.countLeadingZeros, intB.countLeadingZeros)

  override def countTrailingZeros(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, intA.countTrailingZeros, intB.countTrailingZeros)

  override def nonzeroBitCount(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, intA.nonzeroBitCount, intB.nonzeroBitCount)

  override def invertBits(v: ReducedProduct[A, B]): ReducedProduct[A, B] =
    ReducedProduct.unop(v, intA.invertBits, intB.invertBits)
