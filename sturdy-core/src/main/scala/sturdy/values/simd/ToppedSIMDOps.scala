package sturdy.values.simd

import sturdy.effect.failure.Failure
import sturdy.values.Topped
import sturdy.values.Topped.Top
import sturdy.values.config.{Bits, BytePadding, BytesSize}
import sturdy.values.convert.{&&, Bijection, SomeCC}
import sturdy.values.integer.NumericInterval

import java.nio.ByteOrder

given ToppedSIMDOps[V]
(using f: Failure, ops: ConcreteSIMDOps[V], bijectionI32: Bijection[Topped[Int], V], bijectionI64: Bijection[Topped[Long], V], bijectionF32: Bijection[Topped[Float], V], bijectionF64: Bijection[Topped[Double], V]): SIMDOps[Array[Byte], Topped[Array[Byte]], V, Byte] with
  def vectorLit(i: Array[Byte]): Topped[Array[Byte]] = Topped.Actual(i)

  // Unary operations
  def vectorAbs(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorAbs(shape, _))
  def vectorNeg(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorNeg(shape, _))
  def vectorSqrt(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorSqrt(shape, _))
  def vectorCeil(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorCeil(shape, _))
  def vectorFloor(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorFloor(shape, _))
  def vectorTrunc(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorTrunc(shape, _))
  def vectorNearest(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorNearest(shape, _))
  def vectorPopCount(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorPopCount(shape, _))
  def vectorNot(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorNot(shape, _))
  def vectorBitmask(shape: LaneShape, v: Topped[Array[Byte]]): V = if v.isTop then getValueTop(shape) else ops.vectorBitmask(shape, v.get)

  // Binary operations
  def vectorAdd(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorAdd(shape, _, _), v2)
  def vectorSub(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorSub(shape, _, _), v2)
  def vectorMul(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMul(shape, _, _), v2)
  def vectorDiv(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorDiv(shape, _, _), v2)
  def vectorMin(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMin(shape, _, _), v2)
  def vectorMax(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMax(shape, _, _), v2)
  def vectorPMin(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorPMin(shape, _, _), v2)
  def vectorPMax(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorPMax(shape, _, _), v2)
  def vectorMinU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMinU(shape, _, _), v2)
  def vectorMinS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMinS(shape, _, _), v2)
  def vectorMaxU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMaxU(shape, _, _), v2)
  def vectorMaxS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMaxS(shape, _, _), v2)
  def vectorAddSatU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorAddSatU(shape, _, _), v2)
  def vectorAddSatS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorAddSatS(shape, _, _), v2)
  def vectorSubSatU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorSubSatU(shape, _, _), v2)
  def vectorSubSatS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorSubSatS(shape, _, _), v2)
  def vectorAvrgU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorAvrgU(shape, _, _), v2)
  def vectorQ15MulrSatS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorQ15MulrSatS(shape, _, _), v2)
  def vectorDotS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorDotS(shape, _, _), v2)
  def vectorAnd(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorAnd(shape, _, _), v2)
  def vectorAndNot(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorAndNot(shape, _, _), v2)
  def vectorOr(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorOr(shape, _, _), v2)

  // Relational operations
  def vectorXor(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorXor(shape, _, _), v2)
  def vectorEq(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorEq(shape, _, _), v2)
  def vectorNe(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorNe(shape, _, _), v2)
  def vectorLtU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorLtU(shape, _, _), v2)
  def vectorLtS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorLtS(shape, _, _), v2)
  def vectorGtU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorGtU(shape, _, _), v2)
  def vectorGtS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorGtS(shape, _, _), v2)
  def vectorLeU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorLeU(shape, _, _), v2)
  def vectorLeS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorLeS(shape, _, _), v2)
  def vectorGeU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorGeU(shape, _, _), v2)
  def vectorGeS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorGeS(shape, _, _), v2)

  // Ternary operations
  def vectorBitselect(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]], mask: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorTernop(shape, v1, v2, mask, ops.vectorBitselect)

  // Shift operations
  def constantVectorShift(shape: LaneShape, v: Topped[Array[Byte]], shift: V, op: (Any, Int) => Any): Topped[Array[Byte]] =
    if (v.isTop || bijectionI32.unapply(shift).isTop) { // this uses bijectionI32 because v is always I32 here
      Top
    } else {
      Topped.Actual(ops.genericShift(shape, v.get, shift, i => bijectionI32.unapply(i).get, op))
    }
  
  def vectorShiftLeft(shape: LaneShape, v: Topped[Array[Byte]], shift: V): Topped[Array[Byte]] = constantVectorShift(shape, v, shift, ops.shiftLeftOp)
  def vectorShiftRightU(shape: LaneShape, v: Topped[Array[Byte]], shift: V): Topped[Array[Byte]] = constantVectorShift(shape, v, shift, ops.shiftRightUnsignedOp)
  def vectorShiftRightS(shape: LaneShape, v: Topped[Array[Byte]], shift: V): Topped[Array[Byte]] = constantVectorShift(shape, v, shift, ops.shiftRightSignedOp)

  // Conversion operations
  def vectorConvertU(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorConvertU(shape, _))
  def vectorConvertS(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorConvertS(shape, _))
  def vectorConvertLowU(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorConvertLowU(shape, _))
  def vectorConvertLowS(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorConvertLowS(shape, _))
  def vectorTruncSatU(shape: LaneShape, mode: TruncMode, v: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorTruncop(shape, mode, v, ops.vectorTruncSatU)
  def vectorTruncSatS(shape: LaneShape, mode: TruncMode, v: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorTruncop(shape, mode, v, ops.vectorTruncSatS)
  def vectorDemoteZero(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorDemoteZero(shape, _))
  def vectorPromoteLow(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorPromoteLow(shape, _))
  def vectorNarrowU(from: LaneShape, to: LaneShape, a: Topped[Array[Byte]], b: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorNarrowop(from, to, a, b, ops.vectorNarrowU)
  def vectorNarrowS(from: LaneShape, to: LaneShape, a: Topped[Array[Byte]], b: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorNarrowop(from, to, a, b, ops.vectorNarrowS)
  def vectorExtendU(from: LaneShape, to: LaneShape, half: Half, v: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorExtendop(from, to, half, v, ops.vectorExtendU)
  def vectorExtendS(from: LaneShape, to: LaneShape, half: Half, v: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorExtendop(from, to, half, v, ops.vectorExtendS)
  def vectorExtAddU(shape: LaneShape, v1: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.unary(ops.vectorExtAddU(shape, _))
  def vectorExtAddS(shape: LaneShape, v1: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.unary(ops.vectorExtAddS(shape, _))

  // Lane operations
  def extractLane(shape: LaneShape, v: Topped[Array[Byte]], lane: Byte): V = if v.isTop then getValueTop(shape) else ops.extractLane(shape, v.get, lane)
  def extractLaneU(shape: LaneShape, v: Topped[Array[Byte]], lane: Byte): V = if v.isTop then getValueTop(shape) else ops.extractLaneU(shape, v.get, lane)
  def extractLaneS(shape: LaneShape, v: Topped[Array[Byte]], lane: Byte): V = if v.isTop then getValueTop(shape) else ops.extractLaneS(shape, v.get, lane)
  def replaceLane(shape: LaneShape, v: Topped[Array[Byte]], lane: Byte, value: V): Topped[Array[Byte]] = 
    if (v.isTop || valueIsTop(shape, value)) {
      Top
    } else {
      Topped.Actual(ops.replaceLane(shape, v.get, lane, value))
    }
  def shuffleLanes(shape: LaneShape, a: Topped[Array[Byte]], b: Topped[Array[Byte]], lanes: Array[Byte]): Topped[Array[Byte]] = a.binary(ops.shuffleLanes(shape, _, _, lanes), b)
  def swizzleLanes(shape: LaneShape, a: Topped[Array[Byte]], s: Topped[Array[Byte]]): Topped[Array[Byte]] = a.binary(ops.swizzleLanes(shape, _, _), s)
  def splat(shape: LaneShape, v: V): Topped[Array[Byte]] = 
    if (valueIsTop(shape, v)) {
      Top
    } else {
      Topped.Actual(ops.splat(shape, v))
    }
  def zeroPad(shape: LaneShape, v: V): Topped[Array[Byte]] = 
    if (valueIsTop(shape, v)) {
      Top
    } else {
      Topped.Actual(ops.zeroPad(shape, v))
    }

  // Test operations
  def vectorAllTrue(shape: LaneShape, v: Topped[Array[Byte]]): V = if v.isTop then bijectionI32.apply(Topped.Top) else ops.vectorAllTrue(shape, v.get)
  def vectorAnyTrue(shape: LaneShape, v: Topped[Array[Byte]]): V = if v.isTop then bijectionI32.apply(Topped.Top) else ops.vectorAnyTrue(shape, v.get)

  // Helper methods
  private def vectorTernop(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]], mask: Topped[Array[Byte]], op: (LaneShape, Array[Byte], Array[Byte], Array[Byte]) => Array[Byte]): Topped[Array[Byte]] =
    if (v1.isTop || v2.isTop || mask.isTop) {
      Top
    } else {
      val result = op(shape, v1.get, v2.get, mask.get)
      Topped.Actual(result)
    }

  private def vectorTruncop(shape: LaneShape, mode: TruncMode, v: Topped[Array[Byte]], op: (LaneShape, TruncMode, Array[Byte]) => Array[Byte]): Topped[Array[Byte]] =
    if (v.isTop) {
      Top
    } else {
      val result = op(shape, mode, v.get)
      Topped.Actual(result)
    }

  private def vectorNarrowop(from: LaneShape, to: LaneShape, a: Topped[Array[Byte]], b: Topped[Array[Byte]], op: (LaneShape, LaneShape, Array[Byte], Array[Byte]) => Array[Byte]): Topped[Array[Byte]] =
    if (a.isTop || b.isTop) {
      Top
    } else {
      val result = op(from, to, a.get, b.get)
      Topped.Actual(result)
    }

  private def vectorExtendop(from: LaneShape, to: LaneShape, half: Half, v: Topped[Array[Byte]], op: (LaneShape, LaneShape, Half, Array[Byte]) => Array[Byte]): Topped[Array[Byte]] =
    if (v.isTop) {
      Top
    } else {
      val result = op(from, to, half, v.get)
      Topped.Actual(result)
    }

  private def valueIsTop(shape: LaneShape, v: V): Boolean = {
    shape match {
      case LaneShape.I8 | LaneShape.I16 | LaneShape.I32 => bijectionI32.unapply(v).isTop
      case LaneShape.I64 => bijectionI64.unapply(v).isTop
      case LaneShape.F32 => bijectionF32.unapply(v).isTop
      case LaneShape.F64 => bijectionF64.unapply(v).isTop
      case _ => throw new IllegalArgumentException(s"Unsupported lane shape: $shape")
    }
  }

  private def getValueTop(shape: LaneShape): V = {
    shape match {
      case LaneShape.I8 | LaneShape.I16 | LaneShape.I32 => bijectionI32.apply(Topped.Top)
      case LaneShape.I64 => bijectionI64.apply(Topped.Top)
      case LaneShape.F32 => bijectionF32.apply(Topped.Top)
      case LaneShape.F64 => bijectionF64.apply(Topped.Top)
      case _ => throw new IllegalArgumentException(s"Unsupported lane shape: $shape")
    }
  }

given ToppedConvertBytesVector(using conv: ConvertBytesVec[Seq[Byte], Array[Byte]]): ConvertBytesVec[Seq[Topped[Byte]], Topped[Array[Byte]]] with
  def apply(from: Seq[Topped[Byte]], conf: BytesSize && BytePadding && Bits && SomeCC[ByteOrder]): Topped[Array[Byte]] =
    if (from.exists(p => p.isTop)) {
      Topped.Top
    } else {
      val bytes = from.map(_.get).toArray
      Topped.Actual(conv(bytes, conf))
    }

given ToppedConvertVectorBytes: ConvertVecBytes[Topped[Array[Byte]], Seq[Topped[Byte]]] with
  def apply(from: Topped[Array[Byte]], conf: BytesSize && SomeCC[ByteOrder]): Seq[Topped[Byte]] =
    if (from.isTop) {
      Seq.fill(16)(Topped.Top)
    } else {
      val out = from.get.map(Topped.Actual(_))
      if (conf.c2 != ByteOrder.LITTLE_ENDIAN) {
        out.reverse
      } else {
        out
      }
    }

// Intervals
given ToppedIntervalConvertBytesVector: ConvertBytesVec[Seq[NumericInterval[Byte]], Topped[Array[Byte]]] with
  def apply(from: Seq[NumericInterval[Byte]], conf: BytesSize && BytePadding && Bits && SomeCC[ByteOrder]): Topped[Array[Byte]] = ???


given ToppedIntervalConvertVectorBytes: ConvertVecBytes[Topped[Array[Byte]], Seq[NumericInterval[Byte]]] with
  def apply(from: Topped[Array[Byte]], conf: BytesSize && SomeCC[ByteOrder]): Seq[NumericInterval[Byte]] = ???
