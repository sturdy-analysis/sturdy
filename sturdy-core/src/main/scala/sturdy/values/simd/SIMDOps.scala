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
  case object V128 extends LaneShape
}

sealed trait Half
object Half {
  case object Low extends Half
  case object High extends Half
}
sealed trait TruncMode
object TruncMode {
  case object Sat extends TruncMode
  case object SatZero extends TruncMode
}

trait SIMDOps[B, Vec, V, LaneIdx]:
  def vectorLit(i: B): Vec
  // Unary operations
  def vectorAbs(shape: LaneShape, v: Vec): Vec
  def vectorNeg(shape: LaneShape, v: Vec): Vec
  def vectorSqrt(shape: LaneShape, v: Vec): Vec
  def vectorCeil(shape: LaneShape, v: Vec): Vec
  def vectorFloor(shape: LaneShape, v: Vec): Vec
  def vectorTrunc(shape: LaneShape, v: Vec): Vec
  def vectorNearest(shape: LaneShape, v: Vec): Vec
  def vectorPopCount(shape: LaneShape, v: Vec): Vec
  def vectorNot(shape: LaneShape, v: Vec): Vec
  def vectorBitmask(shape: LaneShape, v: Vec): V

  // Binary operations
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
  def vectorDotS(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorAnd(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorAndNot(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorOr(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorXor(shape: LaneShape, v1: Vec, v2: Vec): Vec

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
  
  // Ternary operations
  def vectorBitselect(shape: LaneShape, v1: Vec, v2: Vec, mask: Vec): Vec
  
  // Shift operations
  def vectorShiftLeft(shape: LaneShape, v: Vec, shift: V): Vec
  def vectorShiftRightU(shape: LaneShape, v: Vec, shift: V): Vec
  def vectorShiftRightS(shape: LaneShape, v: Vec, shift: V): Vec
  
  // Conversion operations
  def vectorConvertU(shape: LaneShape, v: Vec): Vec
  def vectorConvertS(shape: LaneShape, v: Vec): Vec
  def vectorConvertLowU(shape: LaneShape, v: Vec): Vec
  def vectorConvertLowS(shape: LaneShape, v: Vec): Vec
  def vectorTruncSatU(shape: LaneShape, mode: TruncMode, v: Vec): Vec
  def vectorTruncSatS(shape: LaneShape, mode: TruncMode, v: Vec): Vec
  def vectorDemoteZero(shape: LaneShape, v: Vec): Vec
  def vectorPromoteLow(shape: LaneShape, v: Vec): Vec
  def vectorNarrowU(from: LaneShape, to: LaneShape, a: Vec, b: Vec): Vec
  def vectorNarrowS(from: LaneShape, to: LaneShape, a: Vec, b: Vec): Vec
  def vectorExtendU(from: LaneShape, to: LaneShape, half: Half, v: Vec): Vec
  def vectorExtendS(from: LaneShape, to: LaneShape, half: Half, v: Vec): Vec
  def vectorExtAddU(shape: LaneShape, v1: Vec): Vec
  def vectorExtAddS(shape: LaneShape, v1: Vec): Vec
  
  // Lane operations
  def extractLane(shape: LaneShape, v: Vec, lane: LaneIdx): V
  def extractLaneU(shape: LaneShape, v: Vec, lane: LaneIdx): V
  def extractLaneS(shape: LaneShape, v: Vec, lane: LaneIdx): V
  def replaceLane(shape: LaneShape, v: Vec, lane: LaneIdx, value: V): Vec
  def shuffleLanes(shape: LaneShape, a: Vec, b: Vec, lanes: B): Vec
  def swizzleLanes(shape: LaneShape, a: Vec, s: Vec): Vec
  def splat(shape: LaneShape, v: V): Vec
  def zeroPad(shape: LaneShape, v: V): Vec

  // Test operations
  def vectorAllTrue(shape: LaneShape, v: Vec): V
  def vectorAnyTrue(shape: LaneShape, v: Vec): V

given DummySIMDOps[B, Vec, V, LaneIdx]: SIMDOps[B, Vec, V, LaneIdx] with {
  def vectorLit(i: B): Vec = ???
  def vectorAbs(shape: LaneShape, v: Vec): Vec = ???
  def vectorNeg(shape: LaneShape, v: Vec): Vec = ???
  def vectorSqrt(shape: LaneShape, v: Vec): Vec = ???
  def vectorCeil(shape: LaneShape, v: Vec): Vec = ???
  def vectorFloor(shape: LaneShape, v: Vec): Vec = ???
  def vectorTrunc(shape: LaneShape, v: Vec): Vec = ???
  def vectorNearest(shape: LaneShape, v: Vec): Vec = ???
  def vectorPopCount(shape: LaneShape, v: Vec): Vec = ???
  def vectorNot(shape: LaneShape, v: Vec): Vec = ???
  def vectorBitmask(shape: LaneShape, v: Vec): V = ???

  def vectorAdd(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorExtAddU(shape: LaneShape, v1: Vec): Vec = ???
  def vectorExtAddS(shape: LaneShape, v1: Vec): Vec = ???
  def vectorSub(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorMul(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorDiv(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorMin(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorMax(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorPMin(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorPMax(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorMinU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorMinS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorMaxU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorMaxS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorAddSatU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorAddSatS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorSubSatU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorSubSatS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorAvrgU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorQ15MulrSatS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorDotS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorEq(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorNe(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorLt(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorLtU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorLtS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorGt(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorGtU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorGtS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorLe(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorLeU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorLeS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorGe(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorGeU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorGeS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorAnd(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorAndNot(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorOr(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  def vectorXor(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???

  def vectorShiftLeft(shape: LaneShape, v: Vec, shift: V): Vec = ???
  def vectorShiftRightU(shape: LaneShape, v: Vec, shift: V): Vec = ???
  def vectorShiftRightS(shape: LaneShape, v: Vec, shift: V): Vec = ???

  def vectorBitselect(shape: LaneShape, v1: Vec, v2: Vec, mask: Vec): Vec = ???

  def vectorConvertU(shape: LaneShape, v: Vec): Vec = ???
  def vectorConvertS(shape: LaneShape, v: Vec): Vec = ???
  def vectorConvertLowU(shape: LaneShape, v: Vec): Vec = ???
  def vectorConvertLowS(shape: LaneShape, v: Vec): Vec = ???
  def vectorTruncSatU(shape: LaneShape, mode: TruncMode, v: Vec): Vec = ???
  def vectorTruncSatS(shape: LaneShape, mode: TruncMode, v: Vec): Vec = ???
  def vectorDemoteZero(shape: LaneShape, v: Vec): Vec = ???
  def vectorPromoteLow(shape: LaneShape, v: Vec): Vec = ???
  def vectorNarrowU(from: LaneShape, to: LaneShape, a: Vec, b: Vec): Vec = ???
  def vectorNarrowS(from: LaneShape, to: LaneShape, a: Vec, b: Vec): Vec = ???
  def vectorExtendU(from: LaneShape, to: LaneShape, half: Half, v: Vec): Vec = ???
  def vectorExtendS(from: LaneShape, to: LaneShape, half: Half, v: Vec): Vec = ???

  def extractLane(shape: LaneShape, v: Vec, lane: LaneIdx): V = ???
  def extractLaneU(shape: LaneShape, v: Vec, lane: LaneIdx): V = ???
  def extractLaneS(shape: LaneShape, v: Vec, lane: LaneIdx): V = ???
  def replaceLane(shape: LaneShape, v: Vec, lane: LaneIdx, value: V): Vec = ???
  def shuffleLanes(shape: LaneShape, a: Vec, b: Vec, lanes: B): Vec = ???
  def swizzleLanes(shape: LaneShape, a: Vec, s: Vec): Vec = ???
  def splat(shape: LaneShape, v: V): Vec = ???
  def zeroPad(shape: LaneShape, v: V): Vec = ???

  def vectorAllTrue(shape: LaneShape, v: Vec): V = ???
  def vectorAnyTrue(shape: LaneShape, v: Vec): V = ???
}


type ConvertBytesVec[VFrom, VTo] = Convert[Seq[Byte], Seq[Byte], VFrom, VTo, config.BytesSize && config.Padding && config.Bits && SomeCC[ByteOrder]]
type ConvertVecBytes[VFrom, VTo] = Convert[Seq[Byte], Seq[Byte], VFrom, VTo, config.BytesSize && SomeCC[ByteOrder]]