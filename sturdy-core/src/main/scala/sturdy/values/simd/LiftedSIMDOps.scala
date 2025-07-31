package sturdy.values.simd

class LiftedSIMDOps[B, V, I](extract: V => I, inject: I => V)(using ops: SIMDOps[B, I]) extends SIMDOps[B, V] {
  def vectorLit(i: B): V = inject(ops.vectorLit(i))

  def vectorAbs(shape: LaneShape, v: V): V = inject(ops.vectorAbs(shape, extract(v)))
  def vectorNeg(shape: LaneShape, v: V): V = inject(ops.vectorNeg(shape, extract(v)))
  def vectorSqrt(shape: LaneShape, v: V): V = inject(ops.vectorSqrt(shape, extract(v)))
  def vectorCeil(shape: LaneShape, v: V): V = inject(ops.vectorCeil(shape, extract(v)))
  def vectorFloor(shape: LaneShape, v: V): V = inject(ops.vectorFloor(shape, extract(v)))
  def vectorTrunc(shape: LaneShape, v: V): V = inject(ops.vectorTrunc(shape, extract(v)))
  def vectorNearest(shape: LaneShape, v: V): V = inject(ops.vectorNearest(shape, extract(v)))
  def vectorPopCount(shape: LaneShape, v: V): V = inject(ops.vectorPopCount(shape, extract(v)))
  
  def vectorAdd(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorAdd(shape, extract(v1), extract(v2)))
  def vectorSub(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorSub(shape, extract(v1), extract(v2)))
  def vectorMul(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorMul(shape, extract(v1), extract(v2)))
  def vectorDiv(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorDiv(shape, extract(v1), extract(v2)))
  def vectorMin(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorMin(shape, extract(v1), extract(v2)))
  def vectorMax(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorMax(shape, extract(v1), extract(v2)))
  def vectorPMin(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorPMin(shape, extract(v1), extract(v2)))
  def vectorPMax(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorPMax(shape, extract(v1), extract(v2)))
  def vectorMinU(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorMinU(shape, extract(v1), extract(v2)))
  def vectorMinS(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorMinS(shape, extract(v1), extract(v2)))
  def vectorMaxU(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorMaxU(shape, extract(v1), extract(v2)))
  def vectorMaxS(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorMaxS(shape, extract(v1), extract(v2)))
  def vectorAddSatU(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorAddSatU(shape, extract(v1), extract(v2)))
  def vectorAddSatS(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorAddSatS(shape, extract(v1), extract(v2)))
  def vectorSubSatU(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorSubSatU(shape, extract(v1), extract(v2)))
  def vectorSubSatS(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorSubSatS(shape, extract(v1), extract(v2)))
  def vectorAvrgU(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorAvrgU(shape, extract(v1), extract(v2)))
  def vectorQ15MulrSatS(shape: LaneShape, v1: V, v2: V): V = inject(ops.vectorQ15MulrSatS(shape, extract(v1), extract(v2)))
}
