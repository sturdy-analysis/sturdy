package sturdy.values.simd

import sturdy.effect.failure.Failure
import sturdy.values.Topped
import sturdy.values.Topped.Top
import sturdy.values.config.*
import sturdy.values.convert.{&&, GaloisConnection, SomeCC}
import sturdy.values.integer.NumericInterval

import java.nio.ByteOrder

given ToppedSIMDOps[V]
(using f: Failure, ops: ConcreteSIMDOps[V], galoisI32: GaloisConnection[Topped[Int], V], galoisI64: GaloisConnection[Topped[Long], V], galoisF32: GaloisConnection[Topped[Float], V], galoisF64: GaloisConnection[Topped[Double], V]): SIMDOps[Array[Byte], Topped[Array[Byte]], V, Byte] with
  override def vectorLit(i: Array[Byte]): Topped[Array[Byte]] = Topped.Actual(i)

  // Unary operations
  override def vectorAbs(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorAbs(shape, _))
  override def vectorNeg(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorNeg(shape, _))
  override def vectorSqrt(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorSqrt(shape, _))
  override def vectorCeil(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorCeil(shape, _))
  override def vectorFloor(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorFloor(shape, _))
  override def vectorTrunc(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorTrunc(shape, _))
  override def vectorNearest(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorNearest(shape, _))
  override def vectorPopCount(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorPopCount(shape, _))
  override def vectorNot(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorNot(shape, _))
  override def vectorBitmask(shape: LaneShape, v: Topped[Array[Byte]]): V = if v.isTop then galoisI32.asAbstract(Topped.Top) else ops.vectorBitmask(shape, v.get)

  // Binary operations
  override def vectorAdd(shape: LaneShape, overflow: Overflow, sign: BitSign, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorAdd(shape, overflow, sign, _, _), v2)
  override def vectorSub(shape: LaneShape, overflow: Overflow, sign: BitSign, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorSub(shape, overflow, sign, _, _), v2)
  override def vectorMul(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMul(shape, _, _), v2)
  override def vectorDiv(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorDiv(shape, _, _), v2)
  override def vectorMin(shape: LaneShape, config: BitSign, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMin(shape, config, _, _), v2)
  override def vectorMax(shape: LaneShape, config: BitSign, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorMax(shape, config, _, _), v2)
  override def vectorPMin(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorPMin(shape, _, _), v2)
  override def vectorPMax(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorPMax(shape, _, _), v2)
  override def vectorAvrgU(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorAvrgU(shape, _, _), v2)
  override def vectorQ15MulrSatS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorQ15MulrSatS(shape, _, _), v2)
  override def vectorDotS(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorDotS(shape, _, _), v2)
  override def vectorAnd(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorAnd(shape, _, _), v2)
  override def vectorOr(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorOr(shape, _, _), v2)

  // Relational operations
  override def vectorXor(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorXor(shape, _, _), v2)
  override def vectorEq(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorEq(shape, _, _), v2)
  override def vectorNe(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorNe(shape, _, _), v2)
  override def vectorLt(shape: LaneShape, config: BitSign, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.binary(ops.vectorLt(shape, config, _, _), v2)

  // Ternary operations
  override def vectorBitselect(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]], mask: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorTernop(shape, v1, v2, mask, ops.vectorBitselect)

  // Shift operations
  private def constantVectorShift(shape: LaneShape, v: Topped[Array[Byte]], shift: V, op: (Any, Int) => Any): Topped[Array[Byte]] =
    if (v.isTop || galoisI32.concretize(shift).isTop) { // this uses bijectionI32 because v is always I32 here
      Top
    } else {
      Topped.Actual(ops.genericShift(shape, v.get, shift, i => galoisI32.concretize(i).get, op))
    }
    
  override def vectorShift(shape: LaneShape, dir: ShiftDirection, config: BitSign, v: Topped[Array[Byte]], shift: V): Topped[Array[Byte]] =
    (dir, config) match {
      case (ShiftDirection.Left, _) => constantVectorShift(shape, v, shift, ops.shiftLeftOp)
      case (ShiftDirection.Right, BitSign.Signed) => constantVectorShift(shape, v, shift, ops.shiftRightSignedOp)
      case (ShiftDirection.Right, BitSign.Unsigned) => constantVectorShift(shape, v, shift, ops.shiftRightUnsignedOp)
      case _ => throw new IllegalArgumentException(s"Unsupported shift direction or bit sign: $dir, $config")
    }
    
  // Conversion operations
  override def vectorConvert(shape: LaneShape, config: BitSign, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorConvert(shape, config, _))
  override def vectorConvertLow(shape: LaneShape, config: BitSign, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorConvertLow(shape, config, _))
  override def vectorTruncSat(shape: LaneShape, mode: TruncMode, config: BitSign, v: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorTruncop(shape, mode, config, v, ops.vectorTruncSat)
  override def vectorDemoteZero(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorDemoteZero(shape, _))
  override def vectorPromoteLow(shape: LaneShape, v: Topped[Array[Byte]]): Topped[Array[Byte]] = v.unary(ops.vectorPromoteLow(shape, _))
  override def vectorNarrow(from: LaneShape, to: LaneShape, config: BitSign, a: Topped[Array[Byte]], b: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorNarrowop(from, to, config, a, b, ops.vectorNarrow)
  override def vectorExtend(from: LaneShape, to: LaneShape, half: Half, config: BitSign, v: Topped[Array[Byte]]): Topped[Array[Byte]] = vectorExtendop(from, to, half, config, v, ops.vectorExtend)
  override def vectorExtAdd(shape: LaneShape, config: BitSign, v1: Topped[Array[Byte]]): Topped[Array[Byte]] = v1.unary(ops.vectorExtAdd(shape, config, _))

  // Lane operations
  override def extractLane(shape: LaneShape, config: BitSign, v: Topped[Array[Byte]], lane: Byte): V = if v.isTop then getValueTop(shape) else ops.extractLane(shape, config, v.get, lane)
  override def replaceLane(shape: LaneShape, v: Topped[Array[Byte]], lane: Byte, value: V): Topped[Array[Byte]] = 
    if (v.isTop || valueIsTop(shape, value)) {
      Top
    } else {
      Topped.Actual(ops.replaceLane(shape, v.get, lane, value))
    }
  override def shuffleLanes(shape: LaneShape, a: Topped[Array[Byte]], b: Topped[Array[Byte]], lanes: Array[Byte]): Topped[Array[Byte]] = a.binary(ops.shuffleLanes(shape, _, _, lanes), b)
  override def swizzleLanes(shape: LaneShape, a: Topped[Array[Byte]], s: Topped[Array[Byte]]): Topped[Array[Byte]] = a.binary(ops.swizzleLanes(shape, _, _), s)
  override def splat(shape: LaneShape, v: V): Topped[Array[Byte]] = 
    if (valueIsTop(shape, v)) {
      Top
    } else {
      Topped.Actual(ops.splat(shape, v))
    }
  override def zeroPad(shape: LaneShape, v: V): Topped[Array[Byte]] = 
    if (valueIsTop(shape, v)) {
      Top
    } else {
      Topped.Actual(ops.zeroPad(shape, v))
    }

  // Test operations
  override def vectorAllTrue(shape: LaneShape, v: Topped[Array[Byte]]): V = if v.isTop then galoisI32.asAbstract(Topped.Top) else ops.vectorAllTrue(shape, v.get)
  override def vectorAnyTrue(shape: LaneShape, v: Topped[Array[Byte]]): V = if v.isTop then galoisI32.asAbstract(Topped.Top) else ops.vectorAnyTrue(shape, v.get)

  // Helper methods
  private def vectorTernop(shape: LaneShape, v1: Topped[Array[Byte]], v2: Topped[Array[Byte]], mask: Topped[Array[Byte]], op: (LaneShape, Array[Byte], Array[Byte], Array[Byte]) => Array[Byte]): Topped[Array[Byte]] =
    if (v1.isTop || v2.isTop || mask.isTop) {
      Top
    } else {
      val result = op(shape, v1.get, v2.get, mask.get)
      Topped.Actual(result)
    }

  private def vectorTruncop(shape: LaneShape, mode: TruncMode, config: BitSign, v: Topped[Array[Byte]], op: (LaneShape, TruncMode, BitSign, Array[Byte]) => Array[Byte]): Topped[Array[Byte]] =
    if (v.isTop) {
      Top
    } else {
      val result = op(shape, mode, config, v.get)
      Topped.Actual(result)
    }

  private def vectorNarrowop(from: LaneShape, to: LaneShape, config: BitSign, a: Topped[Array[Byte]], b: Topped[Array[Byte]], op: (LaneShape, LaneShape, BitSign, Array[Byte], Array[Byte]) => Array[Byte]): Topped[Array[Byte]] =
    if (a.isTop || b.isTop) {
      Top
    } else {
      val result = op(from, to, config, a.get, b.get)
      Topped.Actual(result)
    }

  private def vectorExtendop(from: LaneShape, to: LaneShape, half: Half, config: BitSign, v: Topped[Array[Byte]], op: (LaneShape, LaneShape, Half, BitSign, Array[Byte]) => Array[Byte]): Topped[Array[Byte]] =
    if (v.isTop) {
      Top
    } else {
      val result = op(from, to, half, config, v.get)
      Topped.Actual(result)
    }

  private def valueIsTop(shape: LaneShape, v: V): Boolean = {
    shape match {
      case LaneShape.I8 | LaneShape.I16 | LaneShape.I32 => galoisI32.concretize(v).isTop
      case LaneShape.I64 => galoisI64.concretize(v).isTop
      case LaneShape.F32 => galoisF32.concretize(v).isTop
      case LaneShape.F64 => galoisF64.concretize(v).isTop
      case _ => throw new IllegalArgumentException(s"Unsupported lane shape: $shape")
    }
  }

  private def getValueTop(shape: LaneShape): V = {
    shape match {
      case LaneShape.I8 | LaneShape.I16 | LaneShape.I32 => galoisI32.asAbstract(Topped.Top)
      case LaneShape.I64 => galoisI64.asAbstract(Topped.Top)
      case LaneShape.F32 => galoisF32.asAbstract(Topped.Top)
      case LaneShape.F64 => galoisF64.asAbstract(Topped.Top)
      case _ => throw new IllegalArgumentException(s"Unsupported lane shape: $shape")
    }
  }

given ToppedConvertBytesVector(using conv: ConvertBytesVec[Seq[Byte], Array[Byte]]): ConvertBytesVec[Seq[Topped[Byte]], Topped[Array[Byte]]] with
  override def apply(from: Seq[Topped[Byte]], conf: BytesSize && BytePadding && BitSign && SomeCC[ByteOrder]): Topped[Array[Byte]] =
    if (from.exists(p => p.isTop)) {
      Topped.Top
    } else {
      val bytes = from.map(_.get).toArray
      Topped.Actual(conv(bytes, conf))
    }

given ToppedConvertVectorBytes: ConvertVecBytes[Topped[Array[Byte]], Seq[Topped[Byte]]] with
  override def apply(from: Topped[Array[Byte]], conf: BytesSize && SomeCC[ByteOrder]): Seq[Topped[Byte]] =
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
  override def apply(from: Seq[NumericInterval[Byte]], conf: BytesSize && BytePadding && BitSign && SomeCC[ByteOrder]): Topped[Array[Byte]] = ???


given ToppedIntervalConvertVectorBytes: ConvertVecBytes[Topped[Array[Byte]], Seq[NumericInterval[Byte]]] with
  override def apply(from: Topped[Array[Byte]], conf: BytesSize && SomeCC[ByteOrder]): Seq[NumericInterval[Byte]] = ???
