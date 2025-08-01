package sturdy.values.simd

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.config.BytesSize
import sturdy.values.convert.{&&, SomeCC}
import sturdy.values.types.BaseType

import java.nio.ByteOrder
import scala.reflect.ClassTag


given TypeSIMDOps[B: ClassTag, V](using f: Failure, j: EffectStack): SIMDOps[B, BaseType[B], V, Byte] with
  def vectorLit(i: B): BaseType[B] = BaseType[B]

  def vectorAbs(shape: LaneShape, v: BaseType[B]): BaseType[B] = ???

  def vectorNeg(shape: LaneShape, v: BaseType[B]): BaseType[B] = ???

  def vectorSqrt(shape: LaneShape, v: BaseType[B]): BaseType[B] = ???

  def vectorCeil(shape: LaneShape, v: BaseType[B]): BaseType[B] = ???

  def vectorFloor(shape: LaneShape, v: BaseType[B]): BaseType[B] = ???

  def vectorTrunc(shape: LaneShape, v: BaseType[B]): BaseType[B] = ???

  def vectorNearest(shape: LaneShape, v: BaseType[B]): BaseType[B] = ???

  def vectorPopCount(shape: LaneShape, v: BaseType[B]): BaseType[B] = ???

  def vectorAdd(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorSub(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorMul(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorDiv(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorMin(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorMax(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorPMin(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorPMax(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorMinU(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorMinS(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorMaxU(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorMaxS(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorAddSatU(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorAddSatS(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorSubSatU(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorSubSatS(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorAvrgU(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorQ15MulrSatS(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorEq(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorNe(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorLt(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorLtU(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorLtS(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorGt(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorGtU(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorGtS(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorLe(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorLeU(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorLeS(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorGe(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorGeU(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def vectorGeS(shape: LaneShape, v1: BaseType[B], v2: BaseType[B]): BaseType[B] = ???

  def extractLane(shape: LaneShape, v: BaseType[B], lane: Byte): V = ???

  def extractLaneU(shape: LaneShape, v: BaseType[B], lane: Byte): V = ???

  def extractLaneS(shape: LaneShape, v: BaseType[B], lane: Byte): V = ???


given TypedConvertBytesVector: ConvertBytesVec[BaseType[Seq[Byte]], BaseType[Array[Byte]]] with
  def apply(from: BaseType[Seq[Byte]], conf: BytesSize && SomeCC[ByteOrder]): BaseType[Array[Byte]] = ???

