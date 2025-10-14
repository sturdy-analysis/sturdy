package sturdy.values.integer

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.failure.Failure
import sturdy.values.Topped

class LiftedIntegerOps[B, V, I](extract: V => I, inject: I => V)(using ops: IntegerOps[B, I]) extends IntegerOps[B, V]:
  inline def integerLit(i: B): V = inject(ops.integerLit(i))
  inline def randomInteger(): V = inject(ops.randomInteger())

  def add(v1: V, v2: V): V = inject(ops.add(extract(v1), extract(v2)))
  def sub(v1: V, v2: V): V = inject(ops.sub(extract(v1), extract(v2)))
  def mul(v1: V, v2: V): V = inject(ops.mul(extract(v1), extract(v2)))
  inline def max(v1: V, v2: V): V = inject(ops.max(extract(v1), extract(v2)))
  inline def min(v1: V, v2: V): V = inject(ops.min(extract(v1), extract(v2)))
  inline def absolute(v: V): V = inject(ops.absolute(extract(v)))

  inline def div(v1: V, v2: V): V = inject(ops.div(extract(v1), extract(v2)))
  def divUnsigned(v1: V, v2: V): V = inject(ops.divUnsigned(extract(v1), extract(v2)))
  inline def remainder(v1: V, v2: V): V = inject(ops.remainder(extract(v1), extract(v2)))
  def remainderUnsigned(v1: V, v2: V): V = inject(ops.remainderUnsigned(extract(v1), extract(v2)))
  inline def modulo(v1: V, v2: V): V = inject(ops.modulo(extract(v1), extract(v2)))
  inline def gcd(v1: V, v2: V): V = inject(ops.gcd(extract(v1), extract(v2)))

  def bitAnd(v1: V, v2: V): V = inject(ops.bitAnd(extract(v1), extract(v2)))
  def bitOr(v1: V, v2: V): V = inject(ops.bitOr(extract(v1), extract(v2)))
  def bitXor(v1: V, v2: V): V = inject(ops.bitXor(extract(v1), extract(v2)))
  inline def shiftLeft(v: V, shift: V): V = inject(ops.shiftLeft(extract(v), extract(shift)))
  inline def shiftRight(v: V, shift: V): V = inject(ops.shiftRight(extract(v), extract(shift)))
  def shiftRightUnsigned(v: V, shift: V): V = inject(ops.shiftRightUnsigned(extract(v), extract(shift)))
  inline def rotateLeft(v: V, shift: V): V = inject(ops.rotateLeft(extract(v), extract(shift)))
  inline def rotateRight(v: V, shift: V): V = inject(ops.rotateRight(extract(v), extract(shift)))
  inline def countLeadingZeros(v: V): V = inject(ops.countLeadingZeros(extract(v)))
  inline def countTrailingZeros(v: V): V = inject(ops.countTrailingZeros(extract(v)))
  inline def nonzeroBitCount(v: V): V = inject(ops.nonzeroBitCount(extract(v)))
  inline def invertBits(v: V): V = inject(ops.invertBits(extract(v)))

class LiftedIntegerOpsWithSignInterpretation[B, V, I](extract: V => I, inject: I => V)(using ops: IntegerOpsWithSignInterpretation[B, I]) extends LiftedIntegerOps[B,V,I](extract, inject) with IntegerOpsWithSignInterpretation[B, V]:
  override def interpretSignedAsUnsigned(v: V): V = inject(ops.interpretSignedAsUnsigned(extract(v)))
  override def interpretSignedAsUnsigned(v: V, fromNumBytes: Int): V = inject(ops.interpretSignedAsUnsigned(extract(v), fromNumBytes))
  override def interpretUnsignedAsSigned(v: V): V = inject(ops.interpretUnsignedAsSigned(extract(v)))
  override def interpretUnsignedAsSigned(v: V, fromNumBytes: Int): V = inject(ops.interpretUnsignedAsSigned(extract(v), fromNumBytes))


final class LiftedStrictIntegerOps[B, V, I, J[_] <: MayJoin[_]](extract: V => I, inject: I => V)(using ops: StrictIntegerOps[B, I, J]) extends StrictIntegerOps[B, V, J]:
  inline def addStrict(v1: V, v2: V): JOption[J, V] = ops.addStrict(extract(v1), extract(v2)).map(inject)
  inline def subStrict(v1: V, v2: V): JOption[J, V] = ops.subStrict(extract(v1), extract(v2)).map(inject)
  inline def mulStrict(v1: V, v2: V): JOption[J, V] = ops.mulStrict(extract(v1), extract(v2)).map(inject)
