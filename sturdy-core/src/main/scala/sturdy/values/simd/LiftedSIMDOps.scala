package sturdy.values.simd

import sturdy.values.config.*
import sturdy.values.convert.&&

class LiftedSIMDOps[B, V, I](extract: V => I, inject: I => V)(using ops: SIMDOps[B, I, V, Byte]) extends SIMDOps[B, V, V, Byte] {
  override def  vectorLit(i: B): V = inject(ops.vectorLit(i))

  override def  vectorAbs(shape: LaneShape, v: V): V = inject(ops.vectorAbs(shape, extract(v)))
  override def  vectorNeg(shape: LaneShape, v: V): V = inject(ops.vectorNeg(shape, extract(v)))
  override def  vectorSqrt(shape: LaneShape, v: V): V = inject(ops.vectorSqrt(shape, extract(v)))
  override def  vectorCeil(shape: LaneShape, v: V): V = inject(ops.vectorCeil(shape, extract(v)))
  override def  vectorFloor(shape: LaneShape, v: V): V = inject(ops.vectorFloor(shape, extract(v)))
  override def  vectorTrunc(shape: LaneShape, v: V): V = inject(ops.vectorTrunc(shape, extract(v)))
  override def  vectorNearest(shape: LaneShape, v: V): V = inject(ops.vectorNearest(shape, extract(v)))
  override def  vectorPopCount(shape: LaneShape, v: V): V = inject(ops.vectorPopCount(shape, extract(v)))
  override def  vectorNot(shape: LaneShape, v: V): V = inject(ops.vectorNot(shape, extract(v)))
  override def  vectorBitmask(shape: LaneShape, v: V): V = ops.vectorBitmask(shape, extract(v))

  override def  vectorAdd(shape: LaneShape, config: Overflow && BitSign, v1: V, v2: V): V = inject(ops.vectorAdd(shape, config, extract(v1), extract(v2)))
  override def  vectorSub(shape: LaneShape, config: Overflow && BitSign, v1: V, v2: V): V = inject(ops.vectorSub(shape, config, extract(v1), extract(v2)))
  override def  vectorMul(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorMul(shape, extract(v1), extract(v2)))
  override def  vectorDiv(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorDiv(shape, extract(v1), extract(v2)))
  override def  vectorMin(shape: LaneShape, config: BitSign, v1: V, v2: V): V = inject(ops.vectorMin(shape, config, extract(v1), extract(v2)))
  override def  vectorMax(shape: LaneShape, config: BitSign, v1: V, v2: V): V = inject(ops.vectorMax(shape, config, extract(v1), extract(v2)))
  override def  vectorPMin(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorPMin(shape, extract(v1), extract(v2)))
  override def  vectorPMax(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorPMax(shape, extract(v1), extract(v2)))
  override def  vectorAvrgU(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorAvrgU(shape, extract(v1), extract(v2)))
  override def  vectorQ15MulrSatS(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorQ15MulrSatS(shape, extract(v1), extract(v2)))
  override def  vectorDotS(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorDotS(shape, extract(v1), extract(v2)))

  override def  vectorEq(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorEq(shape, extract(v1), extract(v2)))
  override def  vectorNe(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorNe(shape, extract(v1), extract(v2)))
  override def  vectorLt(shape: LaneShape, config: BitSign, v1: V, v2: V): V = inject(ops.vectorLt(shape, config, extract(v1), extract(v2)))
  override def  vectorGt(shape: LaneShape, config: BitSign, v1: V, v2: V): V = inject(ops.vectorGt(shape, config, extract(v1), extract(v2)))
  override def  vectorLe(shape: LaneShape, config: BitSign, v1: V, v2: V): V = inject(ops.vectorLe(shape, config, extract(v1), extract(v2)))
  override def  vectorGe(shape: LaneShape, config: BitSign, v1: V, v2: V): V = inject(ops.vectorGe(shape, config, extract(v1), extract(v2)))
  override def  vectorAnd(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorAnd(shape, extract(v1), extract(v2)))
  override def  vectorAndNot(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorAndNot(shape, extract(v1), extract(v2)))
  override def  vectorOr(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorOr(shape, extract(v1), extract(v2)))
  override def  vectorXor(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorXor(shape, extract(v1), extract(v2)))

  override def  vectorShift(shape: LaneShape, dir: ShiftDirection, config: BitSign, v: V, shift: V): V = inject(ops.vectorShift(shape, dir, config, extract(v), shift))

  override def  vectorBitselect(shape: LaneShape, v1: V, v2: V, mask: V): V = inject(ops.vectorBitselect(shape, extract(v1), extract(v2), extract(mask)))

  override def  vectorConvert(shape: LaneShape, config: BitSign, v: V): V = inject(ops.vectorConvert(shape, config, extract(v)))
  override def  vectorConvertLow(shape: LaneShape, config: BitSign, v: V): V = inject(ops.vectorConvertLow(shape, config, extract(v)))
  override def  vectorTruncSat(shape: LaneShape, mode: TruncMode, config: BitSign, v: V): V = inject(ops.vectorTruncSat(shape, mode, config, extract(v)))
  override def  vectorDemoteZero(shape: LaneShape, v: V): V = inject(ops.vectorDemoteZero(shape, extract(v)))
  override def  vectorPromoteLow(shape: LaneShape, v: V): V = inject(ops.vectorPromoteLow(shape, extract(v)))
  override def  vectorNarrow(from: LaneShape, to: LaneShape, config: BitSign, a: V, b: V): V = inject(ops.vectorNarrow(from, to, config, extract(a), extract(b)))
  override def  vectorExtend(from: LaneShape, to: LaneShape, half: Half, config: BitSign, v: V): V = inject(ops.vectorExtend(from, to, half, config, extract(v)))
  override def  vectorExtAdd(shape: LaneShape, config: BitSign, v1: V): V = inject(ops.vectorExtAdd(shape, config, extract(v1)))

  override def  extractLane(shape: LaneShape, config: BitSign, v: V, lane: Byte): V = ops.extractLane(shape, config, extract(v), lane)
  override def  replaceLane(shape: LaneShape, v: V, lane: Byte, value: V): V = inject(ops.replaceLane(shape, extract(v), lane, value))
  override def  shuffleLanes(shape: LaneShape, v1: V, v2: V, lanes: B): V = inject(ops.shuffleLanes(shape, extract(v1), extract(v2), lanes))
  override def  swizzleLanes(shape: LaneShape, a: V, s: V): V = inject(ops.swizzleLanes(shape, extract(a), extract(s)))
  override def  splat(shape: LaneShape, v: V): V = inject(ops.splat(shape, v))
  override def  zeroPad(shape: LaneShape, v: V): V = inject(ops.zeroPad(shape, v))

  override def  vectorAllTrue(shape: LaneShape, v: V): V = ops.vectorAllTrue(shape, extract(v))
  override def  vectorAnyTrue(shape: LaneShape, v: V): V = ops.vectorAnyTrue(shape, extract(v))
}
