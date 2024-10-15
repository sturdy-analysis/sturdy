package sturdy.gradual.values.integer

import sturdy.gradual.GradualOps
import sturdy.gradual.fix.GradualLogger
import sturdy.values.PartialOrder
import sturdy.values.integer.IntegerOps

trait IntegerOpsGradualization[B, V] {
  type Safe <: IntegerOps[B, V]
  type Unsafe <: IntegerOps[B, V]
}

trait OverflowGradualization[B, V] extends IntegerOpsGradualization[B, V]

trait GradualIntegerOps[B, V, Gradual[X, Y] <: IntegerOpsGradualization[X, Y]] extends IntegerOps[B, V]

given GradualizedIntegerOps[B, V, Gradual[X, Y] <: IntegerOpsGradualization[X, Y]]
(using
 po: PartialOrder[V],
 g: GradualOps[V],
 gradual: Gradual[B, V],
 safe: gradual.Safe,
 unsafe: gradual.Unsafe): GradualIntegerOps[B, V, Gradual] with {
  override def integerLit(i: B): V = g.withCheck(safe.integerLit(i))(unsafe.integerLit(i))

  override def randomInteger(): V = g.withCheck(safe.randomInteger())(unsafe.randomInteger())

  override def add(v1: V, v2: V): V = g.withCheck(safe.add(v1, v2))(unsafe.add(v1, v2))

  override def sub(v1: V, v2: V): V = g.withCheck(safe.sub(v1, v2))(unsafe.sub(v1, v2))

  override def mul(v1: V, v2: V): V = g.withCheck(safe.mul(v1, v2))(unsafe.mul(v1, v2))

  override def max(v1: V, v2: V): V = g.withCheck(safe.max(v1, v2))(unsafe.max(v1, v2))

  override def min(v1: V, v2: V): V = g.withCheck(safe.min(v1, v2))(unsafe.min(v1, v2))

  override def absolute(v: V): V = g.withCheck(safe.absolute(v))(unsafe.absolute(v))

  override def div(v1: V, v2: V): V = g.withCheck(safe.div(v1, v2))(unsafe.div(v1, v2))

  override def divUnsigned(v1: V, v2: V): V = g.withCheck(safe.divUnsigned(v1, v2))(unsafe.divUnsigned(v1, v2))

  override def remainder(v1: V, v2: V): V = g.withCheck(safe.remainder(v1, v2))(unsafe.remainder(v1, v2))

  override def remainderUnsigned(v1: V, v2: V): V = g.withCheck(safe.remainderUnsigned(v1, v2))(unsafe.remainderUnsigned(v1, v2))

  override def modulo(v1: V, v2: V): V = g.withCheck(safe.modulo(v1, v2))(unsafe.modulo(v1, v2))

  override def gcd(v1: V, v2: V): V = g.withCheck(safe.gcd(v1, v2))(unsafe.gcd(v1, v2))

  override def bitAnd(v1: V, v2: V): V = g.withCheck(safe.bitAnd(v1, v2))(unsafe.bitAnd(v1, v2))

  override def bitOr(v1: V, v2: V): V = g.withCheck(safe.bitOr(v1, v2))(unsafe.bitOr(v1, v2))

  override def bitXor(v1: V, v2: V): V = g.withCheck(safe.bitXor(v1, v2))(unsafe.bitXor(v1, v2))

  override def shiftLeft(v: V, shift: V): V = g.withCheck(safe.shiftLeft(v, shift))(unsafe.shiftLeft(v, shift))

  override def shiftRight(v: V, shift: V): V = g.withCheck(safe.shiftRight(v, shift))(unsafe.shiftRight(v, shift))

  override def shiftRightUnsigned(v: V, shift: V): V = g.withCheck(safe.shiftRightUnsigned(v, shift))(unsafe.shiftRightUnsigned(v, shift))

  override def rotateLeft(v: V, shift: V): V = g.withCheck(safe.rotateLeft(v, shift))(unsafe.rotateLeft(v, shift))

  override def rotateRight(v: V, shift: V): V = g.withCheck(safe.rotateRight(v, shift))(unsafe.rotateRight(v, shift))

  override def countLeadingZeros(v: V): V = g.withCheck(safe.countLeadingZeros(v))(unsafe.countLeadingZeros(v))

  override def countTrailingZeros(v: V): V = g.withCheck(safe.countTrailingZeros(v))(unsafe.countTrailingZeros(v))

  override def nonzeroBitCount(v: V): V = g.withCheck(safe.nonzeroBitCount(v))(unsafe.nonzeroBitCount(v))

  override def invertBits(v: V): V = g.withCheck(safe.invertBits(v))(unsafe.invertBits(v))
}