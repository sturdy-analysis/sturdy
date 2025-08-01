package sturdy.values.simd

import sturdy.effect.failure.Failure
import sturdy.values.Topped
import sturdy.values.config.BytesSize
import sturdy.values.convert.{&&, SomeCC}
import sturdy.values.integer.{IntegerDivisionByZero, NumericInterval}

import java.nio.{ByteBuffer, ByteOrder}

given ToppedSIMDOps[B, Vec, V](using f: Failure): SIMDOps[B, Topped[Vec], V, Byte] with
  def vectorLit(i: B): Topped[Vec] = ???

  def vectorAbs(shape: LaneShape, v: Topped[Vec]): Topped[Vec] = ???

  def vectorNeg(shape: LaneShape, v: Topped[Vec]): Topped[Vec] = ???

  def vectorSqrt(shape: LaneShape, v: Topped[Vec]): Topped[Vec] = ???

  def vectorCeil(shape: LaneShape, v: Topped[Vec]): Topped[Vec] = ???

  def vectorFloor(shape: LaneShape, v: Topped[Vec]): Topped[Vec] = ???

  def vectorTrunc(shape: LaneShape, v: Topped[Vec]): Topped[Vec] = ???

  def vectorNearest(shape: LaneShape, v: Topped[Vec]): Topped[Vec] = ???

  def vectorPopCount(shape: LaneShape, v: Topped[Vec]): Topped[Vec] = ???

  def vectorAdd(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorSub(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorMul(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorDiv(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorMin(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorMax(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorPMin(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorPMax(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorMinU(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorMinS(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorMaxU(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorMaxS(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorAddSatU(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorAddSatS(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorSubSatU(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorSubSatS(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorAvrgU(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorQ15MulrSatS(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorEq(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorNe(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorLt(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorLtU(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorLtS(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorGt(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorGtU(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorGtS(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorLe(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorLeU(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorLeS(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorGe(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorGeU(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def vectorGeS(shape: LaneShape, v1: Topped[Vec], v2: Topped[Vec]): Topped[Vec] = ???

  def extractLane(shape: LaneShape, v: Topped[Vec], lane: Byte): V = ???

  def extractLaneU(shape: LaneShape, v: Topped[Vec], lane: Byte): V = ???

  def extractLaneS(shape: LaneShape, v: Topped[Vec], lane: Byte): V = ???


given ToppedConvertIntervalBytesVector: ConvertBytesVec[Seq[NumericInterval[Byte]], Topped[Array[Byte]]] with
  def apply(from: Seq[NumericInterval[Byte]], conf: BytesSize && SomeCC[ByteOrder]): Topped[Array[Byte]] = ???

