package sturdy.values.simd

sealed trait LaneShape
object LaneShape {
  case object I8 extends LaneShape
  case object I16 extends LaneShape
  case object I32 extends LaneShape
  case object I64 extends LaneShape
  case object F32 extends LaneShape
  case object F64 extends LaneShape
}

trait SIMDOps[B, V]:
  def vectorLit(i: B): V
  // Binary operations
  def vectorAbs(shape: LaneShape, v: V): V
  def vectorNeg(shape: LaneShape, v: V): V
  def vectorSqrt(shape: LaneShape, v: V): V
  def vectorCeil(shape: LaneShape, v: V): V
  def vectorFloor(shape: LaneShape, v: V): V
  def vectorTrunc(shape: LaneShape, v: V): V
  def vectorNearest(shape: LaneShape, v: V): V
  def vectorPopCount(shape: LaneShape, v: V): V

  // Unary operations
  def vectorAdd(shape: LaneShape, v1: V, v2: V): V
  def vectorSub(shape: LaneShape, v1: V, v2: V): V
  def vectorMul(shape: LaneShape, v1: V, v2: V): V
  def vectorDiv(shape: LaneShape, v1: V, v2: V): V
  def vectorMin(shape: LaneShape, v1: V, v2: V): V
  def vectorMax(shape: LaneShape, v1: V, v2: V): V
  def vectorPMin(shape: LaneShape, v1: V, v2: V): V
  def vectorPMax(shape: LaneShape, v1: V, v2: V): V
  def vectorMinU(shape: LaneShape, v1: V, v2: V): V
  def vectorMinS(shape: LaneShape, v1: V, v2: V): V
  def vectorMaxU(shape: LaneShape, v1: V, v2: V): V
  def vectorMaxS(shape: LaneShape, v1: V, v2: V): V
  def vectorAddSatU(shape: LaneShape, v1: V, v2: V): V
  def vectorAddSatS(shape: LaneShape, v1: V, v2: V): V
  def vectorSubSatU(shape: LaneShape, v1: V, v2: V): V
  def vectorSubSatS(shape: LaneShape, v1: V, v2: V): V
  def vectorAvrgU(shape: LaneShape, v1: V, v2: V): V
  def vectorQ15MulrSatS(shape: LaneShape, v1: V, v2: V): V