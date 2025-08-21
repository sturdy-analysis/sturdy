package sturdy.values.simd

import sturdy.values.convert.{&&, Convert, ConvertConfig, SomeCC}
import sturdy.values.config.*

import java.nio.ByteOrder


enum LaneShape:
  case I8
  case I16
  case I32
  case I64
  case F32
  case F64
  case V128
  
enum Half:
  case Low
  case High

enum TruncMode:
  case Sat
  case SatZero

enum ShiftDirection:
  case Left
  case Right

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
  def vectorAdd(shape: LaneShape, config: Overflow && BitSign, v1: Vec, v2: Vec): Vec
  def vectorSub(shape: LaneShape, config: Overflow && BitSign, v1: Vec, v2: Vec): Vec
  def vectorMul(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorDiv(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorMin(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec
  def vectorMax(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec
  def vectorPMin(shape: LaneShape, v1: Vec, v2: Vec): Vec
  def vectorPMax(shape: LaneShape, v1: Vec, v2: Vec): Vec
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
  def vectorLt(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec
  def vectorGt(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec
  def vectorLe(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec
  def vectorGe(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec
  
  // Ternary operations
  def vectorBitselect(shape: LaneShape, v1: Vec, v2: Vec, mask: Vec): Vec
  
  // Shift operations
  def vectorShift(shape: LaneShape, dir: ShiftDirection, config: BitSign, v: Vec, shift: V): Vec
  
  // Conversion operations
  def vectorConvert(shape: LaneShape, config: BitSign, v: Vec): Vec
  def vectorConvertLow(shape: LaneShape, config: BitSign, v: Vec): Vec
  def vectorTruncSat(shape: LaneShape, mode: TruncMode, config: BitSign, v: Vec): Vec
  def vectorDemoteZero(shape: LaneShape, v: Vec): Vec
  def vectorPromoteLow(shape: LaneShape, v: Vec): Vec
  def vectorNarrow(from: LaneShape, to: LaneShape, config: BitSign, a: Vec, b: Vec): Vec
  def vectorExtend(from: LaneShape, to: LaneShape, half: Half, config: BitSign, v: Vec): Vec
  def vectorExtAdd(shape: LaneShape, config: BitSign, v1: Vec): Vec
  
  // Lane operations
  def extractLane(shape: LaneShape, config: BitSign, v: Vec, lane: LaneIdx): V
  def replaceLane(shape: LaneShape, v: Vec, lane: LaneIdx, value: V): Vec
  def shuffleLanes(shape: LaneShape, a: Vec, b: Vec, lanes: B): Vec
  def swizzleLanes(shape: LaneShape, a: Vec, s: Vec): Vec
  def splat(shape: LaneShape, v: V): Vec
  def zeroPad(shape: LaneShape, v: V): Vec

  // Test operations
  def vectorAllTrue(shape: LaneShape, v: Vec): V
  def vectorAnyTrue(shape: LaneShape, v: Vec): V

given DummySIMDOps[B, Vec, V, LaneIdx]: SIMDOps[B, Vec, V, LaneIdx] with {
  override def vectorLit(i: B): Vec = ???

  override def vectorAbs(shape: LaneShape, v: Vec): Vec = ???
  override def vectorNeg(shape: LaneShape, v: Vec): Vec = ???
  override def vectorSqrt(shape: LaneShape, v: Vec): Vec = ???
  override def vectorCeil(shape: LaneShape, v: Vec): Vec = ???
  override def vectorFloor(shape: LaneShape, v: Vec): Vec = ???
  override def vectorTrunc(shape: LaneShape, v: Vec): Vec = ???
  override def vectorNearest(shape: LaneShape, v: Vec): Vec = ???
  override def vectorPopCount(shape: LaneShape, v: Vec): Vec = ???
  override def vectorNot(shape: LaneShape, v: Vec): Vec = ???
  override def vectorBitmask(shape: LaneShape, v: Vec): V = ???

  override def vectorAdd(shape: LaneShape, config: Overflow && BitSign, v1: Vec, v2: Vec): Vec = ???
  override def vectorSub(shape: LaneShape, config: Overflow && BitSign, v1: Vec, v2: Vec): Vec = ???
  override def vectorMul(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorDiv(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorMin(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec = ???
  override def vectorMax(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec = ???
  override def vectorPMin(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorPMax(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorAvrgU(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorQ15MulrSatS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorDotS(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorAnd(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorAndNot(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorOr(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorXor(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???

  override def vectorEq(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorNe(shape: LaneShape, v1: Vec, v2: Vec): Vec = ???
  override def vectorLt(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec = ???
  override def vectorGt(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec = ???
  override def vectorLe(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec = ???
  override def vectorGe(shape: LaneShape, config: BitSign, v1: Vec, v2: Vec): Vec = ???
  override def vectorBitselect(shape: LaneShape, v1: Vec, v2: Vec, mask: Vec): Vec = ???

  override def vectorShift(shape: LaneShape, dir: ShiftDirection, config: BitSign, v: Vec, shift: V): Vec = ???
  override def vectorConvert(shape: LaneShape, config: BitSign, v: Vec): Vec = ???
  override def vectorConvertLow(shape: LaneShape, config: BitSign, v: Vec): Vec = ???
  override def vectorTruncSat(shape: LaneShape, mode: TruncMode, config: BitSign, v: Vec): Vec = ???
  override def vectorDemoteZero(shape: LaneShape, v: Vec): Vec = ???
  override def vectorPromoteLow(shape: LaneShape, v: Vec): Vec = ???
  override def vectorNarrow(from: LaneShape, to: LaneShape, config: BitSign, a: Vec, b: Vec): Vec = ???
  override def vectorExtend(from: LaneShape, to: LaneShape, half: Half, config: BitSign, v: Vec): Vec = ???
  override def vectorExtAdd(shape: LaneShape, config: BitSign, v1: Vec): Vec = ???

  override def extractLane(shape: LaneShape, config: BitSign, v: Vec, lane: LaneIdx): V = ???
  override def replaceLane(shape: LaneShape, v: Vec, lane: LaneIdx, value: V): Vec = ???
  override def shuffleLanes(shape: LaneShape, a: Vec, b: Vec, lanes: B): Vec = ???
  override def swizzleLanes(shape: LaneShape, a: Vec, s: Vec): Vec = ???
  override def splat(shape: LaneShape, v: V): Vec = ???
  override def zeroPad(shape: LaneShape, v: V): Vec = ???

  override def vectorAllTrue(shape: LaneShape, v: Vec): V = ???
  override def vectorAnyTrue(shape: LaneShape, v: Vec): V = ???
}


type ConvertBytesVec[VFrom, VTo] = Convert[Seq[Byte], Seq[Byte], VFrom, VTo, BytesSize && BytePadding && BitSign && SomeCC[ByteOrder]]
type ConvertVecBytes[VFrom, VTo] = Convert[Seq[Byte], Seq[Byte], VFrom, VTo, BytesSize && SomeCC[ByteOrder]]