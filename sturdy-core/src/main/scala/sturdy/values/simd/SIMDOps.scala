package sturdy.values.simd

import sturdy.values.convert.{Convert, SomeCC, &&}
import sturdy.values.config

import java.nio.ByteOrder

sealed trait LaneShape
object LaneShape {
  case object I8 extends LaneShape
  case object I16 extends LaneShape
  case object I32 extends LaneShape
  case object I64 extends LaneShape
  case object F32 extends LaneShape
  case object F64 extends LaneShape
}

trait SIMDOps[B, Vec, V, LaneIdx]:
  def vectorLit(i: B): Vec
  // Binary operations
  def vectorAbs(shape: LaneShape, v: Vec): Vec
  def vectorNeg(shape: LaneShape, v: Vec): Vec
  def vectorSqrt(shape: LaneShape, v: Vec): Vec
  def vectorCeil(shape: LaneShape, v: Vec): Vec
  def vectorFloor(shape: LaneShape, v: Vec): Vec
  def vectorTrunc(shape: LaneShape, v: Vec): Vec
  def vectorNearest(shape: LaneShape, v: Vec): Vec
  def vectorPopCount(shape: LaneShape, v: Vec): Vec

  // Unary operations
  def vectorAdd(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorSub(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorMul(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorDiv(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorMin(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorMax(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorPMin(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorPMax(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorMinU(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorMinS(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorMaxU(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorMaxS(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorAddSatU(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorAddSatS(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorSubSatU(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorSubSatS(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorAvrgU(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorQ15MulrSatS(shape: LaneShape, v1: Vec, v2: Vec): Vec

  // Relational operations
  def vectorEq(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorNe(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorLt(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorLtU(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorLtS(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorGt(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorGtU(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorGtS(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorLe(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorLeU(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorLeS(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorGe(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorGeU(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorGeS(shape: LaneShape, v1: Vec, v2: Vec): Vec

  // Lane operations
  def extractLane(shape: LaneShape, v: Vec, lane: LaneIdx): V
  def extractLaneU(shape: LaneShape, v: Vec, lane: LaneIdx): V
  def extractLaneS(shape: LaneShape, v: Vec, lane: LaneIdx): V

type ConvertBytesVec[VFrom, VTo] = Convert[Seq[Byte], Seq[Byte], VFrom, VTo, config.BytesSize && SomeCC[ByteOrder]]