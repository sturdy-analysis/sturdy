package sturdy.values.simd

import sturdy.effect.failure.Failure
import sturdy.values.Topped

given ToppedSIMDOps[B, T] (using f: Failure): SIMDOps[B, Topped[T]] with 
  def vectorLit(i: B): Topped[T] = ???

  def vectorAbs(shape: LaneShape, v: Topped[T]): Topped[T] = ???

  def vectorNeg(shape: LaneShape, v: Topped[T]): Topped[T] = ???

  def vectorSqrt(shape: LaneShape, v: Topped[T]): Topped[T] = ???

  def vectorCeil(shape: LaneShape, v: Topped[T]): Topped[T] = ???

  def vectorFloor(shape: LaneShape, v: Topped[T]): Topped[T] = ???

  def vectorTrunc(shape: LaneShape, v: Topped[T]): Topped[T] = ???

  def vectorNearest(shape: LaneShape, v: Topped[T]): Topped[T] = ???

  def vectorPopCount(shape: LaneShape, v: Topped[T]): Topped[T] = ???

  def vectorAdd(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorSub(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorMul(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorDiv(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorMin(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorMax(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorPMin(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorPMax(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorMinU(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorMinS(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorMaxU(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorMaxS(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorAddSatU(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorAddSatS(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorSubSatU(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorSubSatS(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorAvrgU(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???

  def vectorQ15MulrSatS(shape: LaneShape, v1: Topped[T], v2: Topped[T]): Topped[T] = ???