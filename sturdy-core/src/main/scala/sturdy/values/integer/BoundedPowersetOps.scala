package sturdy.values.integer

import sturdy.values.BoundedPowerset

given BoundedPowersetOps[I, A](using ops: IntegerOps[I, A]): IntegerOps[I, BoundedPowerset[A]] with
  override def add(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] =
    for (a <- v1; b <- v2) yield ops.add(a, b)

  override def mul(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] =
    for (a <- v1; b <- v2) yield ops.mul(a, b)

  override def bitAnd(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def bitOr(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def bitXor(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def shiftLeft(v: BoundedPowerset[A], shift: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def shiftRight(v: BoundedPowerset[A], shift: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def shiftRightUnsigned(v: BoundedPowerset[A], shift: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def rotateLeft(v: BoundedPowerset[A], shift: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def rotateRight(v: BoundedPowerset[A], shift: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def countLeadingZeros(v: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def countTrailingZeros(v: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def nonzeroBitCount(v: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def invertBits(v: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def integerLit(i: I): BoundedPowerset[A] = ???
  override def randomInteger(): BoundedPowerset[A] = ???
  override def sub(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def max(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def min(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def absolute(v: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def div(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def divUnsigned(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???

  override def gcd(v1: _root_.sturdy.values.BoundedPowerset[A], v2: _root_.sturdy.values.BoundedPowerset[A]): _root_.sturdy.values.BoundedPowerset[A] = ???

  override def remainderUnsigned(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def modulo(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???
  override def remainder(v1: BoundedPowerset[A], v2: BoundedPowerset[A]): BoundedPowerset[A] = ???


