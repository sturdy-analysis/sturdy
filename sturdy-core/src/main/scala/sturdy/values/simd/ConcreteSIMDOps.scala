package sturdy.values.simd

import sturdy.effect.failure.Failure
import sturdy.values.config.BytePadding.None
import sturdy.values.config.{BitSign, BytePadding, BytesSize, Overflow, UnsupportedConfiguration}
import sturdy.values.convert.{&&, GaloisConnection, SomeCC}
import sturdy.values.integer.IntegerDivisionByZero
import sturdy.values.simd.LaneCodec.{*, given}

import java.nio.{ByteBuffer, ByteOrder}

trait LaneCodec[T] {
  def bytes: Int

  def get(bb: ByteBuffer): T
  def put(bb: ByteBuffer, v: T): Unit

  def allOnes: T
  def allZeroes: T
}

object LaneCodec {
  given byteCodec: LaneCodec[Byte] with
    val bytes = 1

    override def get(bb: ByteBuffer): Byte = bb.get
    override def put(bb: ByteBuffer, v: Byte): Unit = bb.put(v)

    override def allOnes: Byte = 0xFF.toByte
    override def allZeroes: Byte = 0x00.toByte

  given shortCodec: LaneCodec[Short] with
    val bytes = 2

    override def get(bb: ByteBuffer): Short = bb.getShort
    override def put(bb: ByteBuffer, v: Short): Unit = bb.putShort(v)

    override def allOnes: Short = 0xFFFF.toShort
    override def allZeroes: Short = 0x0000.toShort

  given intCodec: LaneCodec[Int] with
    val bytes = 4

    override def get(bb: ByteBuffer): Int = bb.getInt
    override def put(bb: ByteBuffer, v: Int): Unit = bb.putInt(v)

    override def allOnes: Int = 0xFFFFFFFF
    override def allZeroes: Int = 0x00000000

  given longCodec: LaneCodec[Long] with
    val bytes = 8

    override def get(bb: ByteBuffer): Long = bb.getLong
    override def put(bb: ByteBuffer, v: Long): Unit = bb.putLong(v)

    override def allOnes: Long = 0xFFFFFFFFFFFFFFFFL
    override def allZeroes: Long = 0x0000000000000000L

  given floatCodec: LaneCodec[Float] with
    val bytes = 4

    override def get(bb: ByteBuffer): Float = bb.getFloat
    override def put(bb: ByteBuffer, v: Float): Unit = bb.putFloat(v)

    override def allOnes: Float = java.lang.Float.intBitsToFloat(0xFFFFFFFF)
    override def allZeroes: Float = 0.0f

  given doubleCodec: LaneCodec[Double] with
    val bytes = 8

    override def get(bb: ByteBuffer): Double = bb.getDouble
    override def put(bb: ByteBuffer, v: Double): Unit = bb.putDouble(v)

    override def allOnes: Double = java.lang.Double.longBitsToDouble(0xFFFFFFFFFFFFFFFFL)
    override def allZeroes: Double = 0.0d
}

enum ConcreteLaneShape[T]:
  case I8(num: Numeric[Byte]) extends ConcreteLaneShape[Byte]
  case I16(num: Numeric[Short]) extends ConcreteLaneShape[Short]
  case I32(num: Numeric[Int]) extends ConcreteLaneShape[Int]
  case I64(num: Numeric[Long]) extends ConcreteLaneShape[Long]
  case F32(num: Numeric[Float]) extends ConcreteLaneShape[Float]
  case F64(num: Numeric[Double]) extends ConcreteLaneShape[Double]

  def numeric: Numeric[T] =
    this match
      case I8(num) => num
      case I16(num) => num
      case I32(num) => num
      case I64(num) => num
      case F32(num) => num
      case F64(num) => num

given ConcreteSIMDOps[V]
(using f: Failure, galoisI32: GaloisConnection[Int, V], galoisI64: GaloisConnection[Long, V], galoisF32: GaloisConnection[Float, V], galoisF64: GaloisConnection[Double, V]): SIMDOps[Array[Byte], Array[Byte], V, Byte] with
  override def vectorLit(i: Array[Byte]): Array[Byte] = i

  // Unary operations

  override def vectorAbs(shape: LaneShape, v: Array[Byte]): Array[Byte] = {
    val concreteLane = toConcreteLaneShape(shape)
    numericVectorUnop(concreteLane, v)(concreteLane.numeric.abs)
  }

  override def vectorNeg(shape: LaneShape, v: Array[Byte]): Array[Byte] = {
    val concreteLane = toConcreteLaneShape(shape)
    numericVectorUnop(concreteLane, v)(concreteLane.numeric.negate)
  }

  override def vectorSqrt(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(Math.sqrt(f).toFloat))
    case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(Math.sqrt(d)))
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation sqrt does not support shape: $shape")

  override def vectorCeil(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(Math.ceil(f).toFloat))
    case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(Math.ceil(d)))
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation ceil does not support shape: $shape")

  override def vectorFloor(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(Math.floor(f).toFloat))
    case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(Math.floor(d)))
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation floor does not support shape: $shape")

  override def vectorTrunc(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(if f.isNaN then f else if f > 0 then f.floor else f.ceil))
    case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(if d.isNaN then d else if d > 0 then d.floor else d.ceil))
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation trunc does not support shape: $shape")

  override def vectorNearest(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 => vectorUnop[Float](v)(f => Math.rint(f).toFloat)
    case LaneShape.F64 => vectorUnop[Double](v)(d => Math.rint(d))
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation nearest does not support shape: $shape")

  override def vectorPopCount(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => vectorUnop[Byte](v)(b => Integer.bitCount(b & 0xFF).toByte)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation pop count does not support shape: $shape")

  override def vectorNot(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.V128 => vectorUnop[Byte](v)(b => (~b).toByte)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation not does not support shape: $shape")

  override def vectorBitmask(shape: LaneShape, v: Array[Byte]): V = {
    def bitmask[T](laneCount: Int, codec: LaneCodec[T], signBit: T => Boolean): V = {
      val buf = ByteBuffer.wrap(v.reverse)
      var result = 0
      for (i <- 0 until laneCount) {
        val value = codec.get(buf)
        if (signBit(value)) result |= (1 << i)
      }
      galoisI32.asAbstract(result)
    }
    shape match
      case LaneShape.I8 => bitmask(16, summon[LaneCodec[Byte]], (x: Byte) => (x & 0x80) != 0)
      case LaneShape.I16 => bitmask(8, summon[LaneCodec[Short]], (x: Short) => (x & 0x8000) != 0)
      case LaneShape.I32 => bitmask(4, summon[LaneCodec[Int]], (x: Int) => x < 0)
      case LaneShape.I64 => bitmask(2, summon[LaneCodec[Long]], (x: Long) => x < 0)
      case _ => f.fail(UnsupportedConfiguration, s"SIMD operation bitmask does not support shape: $shape")
  }

  // Binary operations
  override def vectorAdd(shape: LaneShape, overflow: Overflow, sign: BitSign, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    (overflow, sign) match
      case (Overflow.JumpToBounds, BitSign.Signed) => vectorAddSatS(shape, v1, v2)
      case (Overflow.JumpToBounds, BitSign.Unsigned) => vectorAddSatU(shape, v1, v2)
      case (_, BitSign.Raw) =>
        val concreteLane = toConcreteLaneShape(shape)
        numericVectorBinop(concreteLane, v1, v2)(concreteLane.numeric.plus)
      case _ => f.fail(UnsupportedConfiguration, s"SIMD operation add does not support config: $overflow and $sign")

  private def vectorAddSatU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.min(255, (a & 0xFF) + (b & 0xFF)).toByte)
    case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.min(65535, (a & 0xFFFF) + (b & 0xFFFF)).toShort)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation AddSatU does not support shape: $shape")

  private def vectorAddSatS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => saturateSigned(a + b, -128, 127).toByte)
    case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => saturateSigned(a + b, -32768, 32767).toShort)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation AddSatS does not support shape: $shape")

  override def vectorSub(shape: LaneShape, overflow: Overflow, sign: BitSign, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    (overflow, sign) match
      case (Overflow.JumpToBounds, BitSign.Signed) => vectorSubSatS(shape, v1, v2)
      case (Overflow.JumpToBounds, BitSign.Unsigned) => vectorSubSatU(shape, v1, v2)
      case (_, BitSign.Raw) =>
        val concreteLane = toConcreteLaneShape(shape)
        numericVectorBinop(concreteLane, v1, v2)(concreteLane.numeric.minus)
      case _ => f.fail(UnsupportedConfiguration, s"SIMD operation sub does not support config: $overflow and $sign")

  private def vectorSubSatU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.max(0, (a & 0xFF) - (b & 0xFF)).toByte)
    case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.max(0, (a & 0xFFFF) - (b & 0xFFFF)).toShort)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation SubSatU does not support shape: $shape")

  private def vectorSubSatS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => saturateSigned(a - b, -128, 127).toByte)
    case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => saturateSigned(a - b, -32768, 32767).toShort)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation SubSatS does not support shape: $shape")

  override def vectorMul(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    val concreteLane = toConcreteLaneShape(shape)
    numericVectorBinop(concreteLane, v1, v2)(concreteLane.numeric.times)

  override def vectorDiv(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 =>
      if extractLanes(v2, summon[LaneCodec[Byte]]).contains(0)
      then f.fail(IntegerDivisionByZero, "division by zero in i8")
      else vectorBinop[Byte](v1, v2)((a, b) => (a / b).toByte)

    case LaneShape.I16 =>
      if extractLanes(v2, summon[LaneCodec[Short]]).contains(0)
      then f.fail(IntegerDivisionByZero, "division by zero in i16")
      else vectorBinop[Short](v1, v2)((a, b) => (a / b).toShort)

    case LaneShape.I32 =>
      if extractLanes(v2, summon[LaneCodec[Int]]).contains(0)
      then f.fail(IntegerDivisionByZero, "division by zero in i32")
      else vectorBinop[Int](v1, v2)(_ / _)

    case LaneShape.I64 =>
      if extractLanes(v2, summon[LaneCodec[Long]]).contains(0L)
      then f.fail(IntegerDivisionByZero, "division by zero in i64")
      else vectorBinop[Long](v1, v2)(_ / _)

    case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => canonicalNaN(a / b))
    case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => canonicalNaN(a / b))
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation div does not support shape: $shape")

  override def vectorMin(shape: LaneShape, config: BitSign, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = config match
    case BitSign.Signed | BitSign.Raw =>
      val concreteLane = toConcreteLaneShape(shape)
      numericVectorBinop(concreteLane, v1, v2)(concreteLane.numeric.min)
    case BitSign.Unsigned => vectorMinU(shape, v1, v2)

  override def vectorMax(shape: LaneShape, config: BitSign, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = config match
    case BitSign.Signed | BitSign.Raw =>
      val concreteLane = toConcreteLaneShape(shape)
      numericVectorBinop(concreteLane, v1, v2)(concreteLane.numeric.max)
    case BitSign.Unsigned => vectorMaxU(shape, v1, v2)

  private def vectorMinU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.min(a & 0xFF, b & 0xFF).toByte)
    case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.min(a & 0xFFFF, b & 0xFFFF).toShort)
    case LaneShape.I32 => vectorBinop[Int](v1, v2)((a, b) => Math.min(a.toLong & 0xFFFFFFFFL, b.toLong & 0xFFFFFFFFL).toInt)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation MinU does not support shape: $shape")

  private def vectorMaxU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.max(a & 0xFF, b & 0xFF).toByte)
    case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.max(a & 0xFFFF, b & 0xFFFF).toShort)
    case LaneShape.I32 => vectorBinop[Int](v1, v2)((a, b) => Math.max(a.toLong & 0xFFFFFFFFL, b.toLong & 0xFFFFFFFFL).toInt)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation MaxU does not support shape: $shape")

  override def vectorPMin(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => if (b < a) b else a)
    case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => if (b < a) b else a)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation PMin does not support shape: $shape")

  override def vectorPMax(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => if (a < b) b else a)
    case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => if (a < b) b else a)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation PMax does not support shape: $shape")

  override def vectorAvrgU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => (((a & 0xFF) + (b & 0xFF) + 1) / 2).toByte)
    case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => (((a & 0xFFFF) + (b & 0xFFFF) + 1) / 2).toShort)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation AvrgU does not support shape: $shape")

  override def vectorQ15MulrSatS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I16 =>
      val codec = summon[LaneCodec[Short]]
      val lane1 = extractLanes(v1, codec)
      val lane2 = extractLanes(v2, codec)

      val result = lane1.zip(lane2).map { case (a, b) =>
        val product = a.toInt * b.toInt
        val rounded = (product + 0x4000) >> 15
        saturateSigned(rounded, Short.MinValue, Short.MaxValue).toShort
      }

      encodeLanes(result, codec)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation Q15MulrSatS does not support shape: $shape")

  override def vectorDotS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I16 =>
      val codecShort = summon[LaneCodec[Short]]
      val lane1 = extractLanes(v1, codecShort)
      val lane2 = extractLanes(v2, codecShort)
      val products = lane1.zip(lane2).map { case (a, b) => a.toInt * b.toInt }
      val summed = products.grouped(2).map(_.sum).toArray
      val codecInt = summon[LaneCodec[Int]]
      encodeLanes(summed, codecInt)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation DotS does not support shape: $shape")

  // Ternary operations
  override def vectorBitselect(shape: LaneShape, v1: Array[Byte], v2: Array[Byte], mask: Array[Byte]): Array[Byte] = shape match
    case LaneShape.V128 =>
      val result = new Array[Byte](v1.length)
      for (i <- v1.indices) {
        val b1: Byte = v1(i)
        val b2: Byte = v2(i)
        val m: Byte = mask(i)
        result(i) = ((b1 ^ b2) & m ^ b2).toByte
      }
      result
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation bitselect does not support shape: $shape")

  // Relational operations

  override def vectorEq(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    val concrete = toConcreteLaneShape(shape)
    numericVectorRelop(concrete, v1, v2)(concrete.numeric.equiv)

  override def vectorNe(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => vectorRelop[Byte](v1, v2)(_ != _)
    case LaneShape.I16 => vectorRelop[Short](v1, v2)(_ != _)
    case LaneShape.I32 => vectorRelop[Int](v1, v2)(_ != _)
    case LaneShape.I64 => vectorRelop[Long](v1, v2)(_ != _)
    case LaneShape.F32 => vectorRelop[Float](v1, v2)((a, b) => canonicalNaN(a) != canonicalNaN(b))
    case LaneShape.F64 => vectorRelop[Double](v1, v2)((a, b) => canonicalNaN(a) != canonicalNaN(b))
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation not equal does not support shape: $shape")

  override def vectorLt(shape: LaneShape, config: BitSign, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = config match
    case BitSign.Signed | BitSign.Raw =>
      val concrete = toConcreteLaneShape(shape)
      numericVectorRelop(concrete, v1, v2)(concrete.numeric.lt)
    case BitSign.Unsigned =>
      shape match
        case LaneShape.I8 => vectorRelop[Byte](v1, v2)((a, b) => (a & 0xFF) < (b & 0xFF))
        case LaneShape.I16 => vectorRelop[Short](v1, v2)((a, b) => (a & 0xFFFF) < (b & 0xFFFF))
        case LaneShape.I32 => vectorRelop[Int](v1, v2)((a, b) => Integer.compareUnsigned(a, b) < 0)
        case _ => f.fail(UnsupportedConfiguration, s"SIMD operation less than does not support shape: $shape")

  override def vectorAnd(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.V128 => vectorBinop[Byte](v1, v2)((a, b) => (a & b).toByte)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation does and not support shape: $shape")

  override def vectorOr(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.V128 => vectorBinop[Byte](v1, v2)((a, b) => (a | b).toByte)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation does or not support shape: $shape")

  override def vectorXor(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = shape match
    case LaneShape.V128 => vectorBinop[Byte](v1, v2)((a, b) => (a ^ b).toByte)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation xor does not support shape: $shape")

  // Shift operations
  override def vectorShift(shape: LaneShape, dir: ShiftDirection, config: BitSign, v: Array[Byte], shiftV: V): Array[Byte] =
    (dir, config) match
      case (ShiftDirection.Left, _) => genericShift(shape, v, shiftV, galoisI32.concretize, shiftLeftOp)
      case (ShiftDirection.Right, BitSign.Signed) => genericShift(shape, v, shiftV, galoisI32.concretize, shiftRightSignedOp)
      case (ShiftDirection.Right, BitSign.Unsigned) => genericShift(shape, v, shiftV, galoisI32.concretize, shiftRightUnsignedOp)
      case _ => f.fail(UnsupportedConfiguration, s"SIMD shift does not support config/dir: $config / $dir")

  def genericShift(shape: LaneShape, v: Array[Byte], shiftV: V, extraction: V => Int, op: (Any, Int) => Any): Array[Byte] = shape match
    case LaneShape.I8 => shift(v, extraction(shiftV) % 8, op)(using summon[LaneCodec[Byte]])
    case LaneShape.I16 => shift(v, extraction(shiftV) % 16, op)(using summon[LaneCodec[Short]])
    case LaneShape.I32 => shift(v, extraction(shiftV) % 32, op)(using summon[LaneCodec[Int]])
    case LaneShape.I64 => shift(v, extraction(shiftV) % 64, op)(using summon[LaneCodec[Long]])
    case _ => f.fail(UnsupportedConfiguration, s"SIMD shift does not support shape: $shape")

  def shift[T](v: Array[Byte], shift: Int, op: (Any, Int) => Any)(using codec: LaneCodec[T]): Array[Byte] = {
    val in = ByteBuffer.wrap(v)
    val out = ByteBuffer.allocate(v.length)
    while (in.remaining() >= codec.bytes) {
      val raw = codec.get(in)
      val shifted = op(raw, shift).asInstanceOf[T]
      codec.put(out, shifted)
    }
    out.array()
  }

  val shiftLeftOp: (Any, Int) => Any = {
    case (b: Byte, s) => (((b & 0xFF) << s) & 0xFF).toByte
    case (s: Short, sh) => (((s & 0xFFFF) << sh) & 0xFFFF).toShort
    case (i: Int, sh) => i << sh
    case (l: Long, sh) => l << sh
  }

  val shiftRightUnsignedOp: (Any, Int) => Any = {
    case (b: Byte, s) => ((b & 0xFF) >>> s).toByte
    case (s: Short, sh) => ((s & 0xFFFF) >>> sh).toShort
    case (i: Int, sh) => i >>> sh
    case (l: Long, sh) => l >>> sh
  }

  val shiftRightSignedOp: (Any, Int) => Any = {
    case (b: Byte, s) => (b >> s).toByte
    case (s: Short, sh) => (s >> sh).toShort
    case (i: Int, sh) => i >> sh
    case (l: Long, sh) => l >> sh
  }

  // Conversion operations
  private def transformLanes[A, B](v: Array[Byte], inOffset: Int, lanes: Int)(using inCodec: LaneCodec[A], outCodec: LaneCodec[B])(f: A => B): Array[Byte] = {
    val in = ByteBuffer.wrap(v, inOffset, lanes * inCodec.bytes)
    val out = ByteBuffer.allocate(lanes * outCodec.bytes)
    for (i <- 0 until lanes) {
      outCodec.put(out, f(inCodec.get(in)))
    }
    out.array()
  }

  override def vectorConvert(shape: LaneShape, config: BitSign, v: Array[Byte]): Array[Byte] = config match
    case BitSign.Signed =>
      shape match
        case LaneShape.I32 => transformLanes[Int, Float](v, 0, 4)(using summon[LaneCodec[Int]], summon[LaneCodec[Float]])(_.toFloat)
        case _ => f.fail(UnsupportedConfiguration, s"SIMD convert does not support shape: $shape")
    case BitSign.Unsigned =>
      shape match
        case LaneShape.I32 => transformLanes[Int, Float](v, 0, 4)(using summon[LaneCodec[Int]], summon[LaneCodec[Float]])(v => (v.toLong & 0xFFFFFFFFL).toFloat)
        case _ => f.fail(UnsupportedConfiguration, s"SIMD convert does not support shape: $shape")
    case BitSign.Raw => throw new IllegalArgumentException("Raw bit sign is not supported for vectorConvert")

  override def vectorConvertLow(shape: LaneShape, config: BitSign, v: Array[Byte]): Array[Byte] = config match
    case BitSign.Signed =>
      shape match
        case LaneShape.I32 => transformLanes[Int, Double](v, 8, 2)(using summon[LaneCodec[Int]], summon[LaneCodec[Double]])(_.toDouble)
        case _ => f.fail(UnsupportedConfiguration, s"SIMD convert low does not support shape: $shape")
    case BitSign.Unsigned =>
      shape match
        case LaneShape.I32 => transformLanes[Int, Double](v, 8, 2)(using summon[LaneCodec[Int]], summon[LaneCodec[Double]])(v => (v.toLong & 0xFFFFFFFFL).toDouble)
        case _ => f.fail(UnsupportedConfiguration, s"SIMD convert low does not support shape: $shape")
    case BitSign.Raw => throw new IllegalArgumentException("Raw bit sign is not supported for vectorConvertLow")

  private def clampFloatToI32(d: Double, signed: Boolean): Int =
    if d.isNaN then 0
    else if signed then
      if d >= Int.MaxValue.toDouble then Int.MaxValue
      else if d <= Int.MinValue.toDouble then Int.MinValue
      else d.toInt
    else if d >= 4294967295.0 then 0xFFFFFFFF
    else if d <= 0.0 then 0
    else d.toLong.toInt

  private def truncSatFloatToI32(v: Array[Byte], signed: Boolean, mode: TruncMode): Array[Byte] = mode match
    case TruncMode.Sat =>
      transformLanes[Float, Int](v, 0, 4)(using summon[LaneCodec[Float]], summon[LaneCodec[Int]]) { f =>
        clampFloatToI32(f.toDouble, signed)
      }

    case TruncMode.SatZero =>
      val out = ByteBuffer.allocate(16)
      val outCodec = summon[LaneCodec[Int]]
      for (i <- 0 until 2) {
        outCodec.put(out, outCodec.allZeroes)
      }
      val high = transformLanes[Double, Int](v, 0, 2)(using summon[LaneCodec[Double]], summon[LaneCodec[Int]]) { d =>
        clampFloatToI32(d, signed)
      }
      out.put(high)
      out.array()

  override def vectorTruncSat(shape: LaneShape, mode: TruncMode, config: BitSign, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 | LaneShape.F64 => truncSatFloatToI32(v, signed = config == BitSign.Signed, mode)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation trunc sat does not support shape: $shape")

  override def vectorDemoteZero(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F64 =>
      val out = ByteBuffer.allocate(16)
      val outCodec = summon[LaneCodec[Float]]
      for (i <- 0 until 2) {
        outCodec.put(out, outCodec.allZeroes)
      }
      val low = transformLanes[Double, Float](v, 0, 2)(using summon[LaneCodec[Double]], summon[LaneCodec[Float]])(_.toFloat)
      out.put(low)
      out.array()
    case _ => f.fail(UnsupportedConfiguration, s"SIMD demote zero does not support shape: $shape")

  override def vectorPromoteLow(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 => transformLanes[Float, Double](v, 0, 2)(using summon[LaneCodec[Float]], summon[LaneCodec[Double]])(_.toDouble)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD promote low does not support shape: $shape")

  private def narrow[I, O](a: Array[Byte], b: Array[Byte], lanes: Int)(using inCodec: LaneCodec[I], outCodec: LaneCodec[O])(f: I => O): Array[Byte] = {
    val inA = ByteBuffer.wrap(a)
    val inB = ByteBuffer.wrap(b)
    val out = ByteBuffer.allocate(lanes * 2 * outCodec.bytes)
    var i = 0
    for(i <- 0 until lanes) {
      outCodec.put(out, f(inCodec.get(inA)))
    }
    for(i <- 0 until lanes) {
      outCodec.put(out, f(inCodec.get(inB)))
    }
    out.array()
  }

  override def vectorNarrow(from: LaneShape, to: LaneShape, config: BitSign, a: Array[Byte], b: Array[Byte]): Array[Byte] = config match {
    case BitSign.Signed => vectorNarrowS(from, to, a, b)
    case BitSign.Unsigned => vectorNarrowU(from, to, a, b)
    case BitSign.Raw => throw new IllegalArgumentException("Raw bit sign is not supported for vectorNarrow")
  }

  private def vectorNarrowU(from: LaneShape, to: LaneShape, a: Array[Byte], b: Array[Byte]): Array[Byte] =
    (from, to) match
      case (LaneShape.I16, LaneShape.I8) =>
        narrow[Short, Byte](a, b, 8)(using summon[LaneCodec[Short]], summon[LaneCodec[Byte]]) { s =>
          Math.min(255, Math.max(0, s.toInt)).toByte
        }

      case (LaneShape.I32, LaneShape.I16) =>
        narrow[Int, Short](a, b, 4)(using summon[LaneCodec[Int]], summon[LaneCodec[Short]]) { i =>
          Math.min(65535, Math.max(0, i)).toShort
        }
      case _ => f.fail(UnsupportedConfiguration, s"SIMD narrow does not support narrowing from $from to $to")

  private def vectorNarrowS(from: LaneShape, to: LaneShape, a: Array[Byte], b: Array[Byte]): Array[Byte] =
    (from, to) match
      case (LaneShape.I16, LaneShape.I8) =>
        narrow[Short, Byte](a, b, 8)(using summon[LaneCodec[Short]], summon[LaneCodec[Byte]]) { s =>
          saturateSigned(s.toInt, -128, 127).toByte
        }

      case (LaneShape.I32, LaneShape.I16) =>
        narrow[Int, Short](a, b, 4)(using summon[LaneCodec[Int]], summon[LaneCodec[Short]]) { i =>
          saturateSigned(i, -32768, 32767).toShort
        }
      case _ => f.fail(UnsupportedConfiguration, s"SIMD narrow does not support narrowing from $from to $to")

  override def vectorExtend(from: LaneShape, to: LaneShape, half: Half, config: BitSign, v: Array[Byte]): Array[Byte] = config match
    case BitSign.Signed => vectorExtendS(from, to, half, v)
    case BitSign.Unsigned => vectorExtendU(from, to, half, v)
    case BitSign.Raw => throw new IllegalArgumentException("Raw bit sign is not supported for vectorExtend")

  private def vectorExtendU(from: LaneShape, to: LaneShape, half: Half, v: Array[Byte]): Array[Byte] =
    (from, to, half) match
      case (LaneShape.I8, LaneShape.I16, Half.Low) =>
        transformLanes[Byte, Short](v, 8, 8)(using summon[LaneCodec[Byte]], summon[LaneCodec[Short]])((x: Byte) => (x & 0xFF).toShort)
      case (LaneShape.I8, LaneShape.I16, Half.High) =>
        transformLanes[Byte, Short](v, 0, 8)(using summon[LaneCodec[Byte]], summon[LaneCodec[Short]])((x: Byte) => (x & 0xFF).toShort)

      case (LaneShape.I16, LaneShape.I32, Half.Low) =>
        transformLanes[Short, Int](v, 8, 4)(using summon[LaneCodec[Short]], summon[LaneCodec[Int]])((x: Short) => x & 0xFFFF)
      case (LaneShape.I16, LaneShape.I32, Half.High) =>
        transformLanes[Short, Int](v, 0, 4)(using summon[LaneCodec[Short]], summon[LaneCodec[Int]])((x: Short) => x & 0xFFFF)

      case (LaneShape.I32, LaneShape.I64, Half.Low) =>
        transformLanes[Int, Long](v, 8, 2)(using summon[LaneCodec[Int]], summon[LaneCodec[Long]])((x: Int) => x & 0xFFFFFFFFL)
      case (LaneShape.I32, LaneShape.I64, Half.High) =>
        transformLanes[Int, Long](v, 0, 2)(using summon[LaneCodec[Int]], summon[LaneCodec[Long]])((x: Int) => x & 0xFFFFFFFFL)
      case _ => f.fail(UnsupportedConfiguration, s"SIMD does not support extending from $from to $to")

  private def vectorExtendS(from: LaneShape, to: LaneShape, half: Half, v: Array[Byte]): Array[Byte] =
    (from, to, half) match
      case (LaneShape.I8, LaneShape.I16, Half.Low) =>
        transformLanes[Byte, Short](v, 8, 8)(using summon[LaneCodec[Byte]], summon[LaneCodec[Short]])((x: Byte) => x.toShort)
      case (LaneShape.I8, LaneShape.I16, Half.High) =>
        transformLanes[Byte, Short](v, 0, 8)(using summon[LaneCodec[Byte]], summon[LaneCodec[Short]])((x: Byte) => x.toShort)

      case (LaneShape.I16, LaneShape.I32, Half.Low) =>
        transformLanes[Short, Int](v, 8, 4)(using summon[LaneCodec[Short]], summon[LaneCodec[Int]])(_.toInt)
      case (LaneShape.I16, LaneShape.I32, Half.High) =>
        transformLanes[Short, Int](v, 0, 4)(using summon[LaneCodec[Short]], summon[LaneCodec[Int]])(_.toInt)

      case (LaneShape.I32, LaneShape.I64, Half.Low) =>
        transformLanes[Int, Long](v, 8, 2)(using summon[LaneCodec[Int]], summon[LaneCodec[Long]])(_.toLong)
      case (LaneShape.I32, LaneShape.I64, Half.High) =>
        transformLanes[Int, Long](v, 0, 2)(using summon[LaneCodec[Int]], summon[LaneCodec[Long]])(_.toLong)
      case _ => f.fail(UnsupportedConfiguration, s"SIMD operation does not support extending from $from to $to")

  private def extAddGeneric[A, B](v: Array[Byte], pairs: Int)(using inCodec: LaneCodec[A], outCodec: LaneCodec[B])(toInt: A => Int, fromInt: Int => B): Array[Byte] = {
    val in = ByteBuffer.wrap(v)
    val out = ByteBuffer.allocate(16)
    for (i <- 0 until pairs) {
      val a = toInt(inCodec.get(in))
      val b = toInt(inCodec.get(in))
      outCodec.put(out, fromInt(a + b))
    }
    out.array()
  }

  override def vectorExtAdd(shape: LaneShape, config: BitSign, v1: Array[Byte]): Array[Byte] = config match
    case BitSign.Signed => vectorExtAddS(shape, v1)
    case BitSign.Unsigned => vectorExtAddU(shape, v1)
    case BitSign.Raw => throw new IllegalArgumentException("Raw bit sign is not supported for vectorExtAdd")

  private def vectorExtAddU(shape: LaneShape, v1: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => extAddGeneric[Byte, Short](v1, 8)(using summon[LaneCodec[Byte]], summon[LaneCodec[Short]])(b => b & 0xFF, s => s.toShort)
    case LaneShape.I16 => extAddGeneric[Short, Int](v1, 4)(using summon[LaneCodec[Short]], summon[LaneCodec[Int]])(s => s & 0xFFFF, i => i)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation ExtAddU does not support shape: $shape")

  private def vectorExtAddS(shape: LaneShape, v1: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => extAddGeneric[Byte, Short](v1, 8)(using summon[LaneCodec[Byte]], summon[LaneCodec[Short]])(_.toInt, s => s.toShort)
    case LaneShape.I16 => extAddGeneric[Short, Int](v1, 4)(using summon[LaneCodec[Short]], summon[LaneCodec[Int]])(_.toInt, i => i)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD operation ExtAddS does not support shape: $shape")

  // Lane operations

  override def extractLane(shape: LaneShape, config: BitSign, v: Array[Byte], lane: Byte): V = config match
    case BitSign.Signed => extractLaneS(shape, v, lane)
    case BitSign.Unsigned => extractLaneU(shape, v, lane)
    case BitSign.Raw => extractLane(shape, v, lane)

  private def extractLane(shape: LaneShape, v: Array[Byte], lane: Byte): V = shape match
    case LaneShape.I32 => galoisI32.asAbstract(extractLanes(v, summon[LaneCodec[Int]])(3 - (lane & 0x03)))
    case LaneShape.I64 => galoisI64.asAbstract(extractLanes(v, summon[LaneCodec[Long]])(1 - (lane & 0x01)))
    case LaneShape.F32 => galoisF32.asAbstract(extractLanes(v, summon[LaneCodec[Float]])(3 - (lane & 0x03)))
    case LaneShape.F64 => galoisF64.asAbstract(extractLanes(v, summon[LaneCodec[Double]])(1 - (lane & 0x01)))
    case _ => f.fail(UnsupportedConfiguration, s"SIMD extract does not support shape: $shape")


  private def extractLaneU(shape: LaneShape, v: Array[Byte], lane: Byte): V = shape match
    case LaneShape.I8 =>
      val b = extractLanes(v, summon[LaneCodec[Byte]])(15 - (lane & 0x0F))
      galoisI32.asAbstract(b & 0xFF)
    case LaneShape.I16 =>
      val s = extractLanes(v, summon[LaneCodec[Short]])(7 - (lane & 0x07))
      galoisI32.asAbstract(s & 0xFFFF)
    case _ => throw new IllegalArgumentException("Invalid shape for unsigned extract")

  private def extractLaneS(shape: LaneShape, v: Array[Byte], lane: Byte): V = shape match
    case LaneShape.I8 =>
      val b = extractLanes(v, summon[LaneCodec[Byte]])(15 - (lane & 0x0F))
      galoisI32.asAbstract(b.toInt)
    case LaneShape.I16 =>
      val s = extractLanes(v, summon[LaneCodec[Short]])(7 - (lane & 0x07))
      galoisI32.asAbstract(s.toInt)
    case _ => throw new IllegalArgumentException("Invalid shape for signed extract")

  override def replaceLane(shape: LaneShape, v: Array[Byte], lane: Byte, value: V): Array[Byte] = shape match
    case LaneShape.I8 =>
      val codec = summon[LaneCodec[Byte]]
      val lanes = extractLanes(v, codec).updated(15 - (lane & 0x0F), galoisI32.concretize(value).toByte)
      encodeLanes(lanes, codec)

    case LaneShape.I16 =>
      val codec = summon[LaneCodec[Short]]
      val lanes = extractLanes(v, codec).updated(7 - (lane & 0x07), galoisI32.concretize(value).toShort)
      encodeLanes(lanes, codec)

    case LaneShape.I32 =>
      val codec = summon[LaneCodec[Int]]
      val lanes = extractLanes(v, codec).updated(3 - (lane & 0x03), galoisI32.concretize(value))
      encodeLanes(lanes, codec)

    case LaneShape.I64 =>
      val codec = summon[LaneCodec[Long]]
      val lanes = extractLanes(v, codec).updated(1 - (lane & 0x01), galoisI64.concretize(value))
      encodeLanes(lanes, codec)

    case LaneShape.F32 =>
      val codec = summon[LaneCodec[Float]]
      val lanes = extractLanes(v, codec).updated(3 - (lane & 0x03), galoisF32.concretize(value))
      encodeLanes(lanes, codec)

    case LaneShape.F64 =>
      val codec = summon[LaneCodec[Double]]
      val lanes = extractLanes(v, codec).updated(1 - (lane & 0x01), galoisF64.concretize(value))
      encodeLanes(lanes, codec)

    case _ => f.fail(UnsupportedConfiguration, s"SIMD replace lane does not support shape: $shape")

  override def shuffleLanes(shape: LaneShape, a: Array[Byte], b: Array[Byte], lanes: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 =>
      (0 until 16).map { i =>
        val idx = lanes(i) & 0xFF
        if idx < 16 then a(15 - idx) else b(15 - (idx - 16))
      }.reverse.toArray
    case _ => f.fail(UnsupportedConfiguration, s"SIMD shuffle lane does not support shape: $shape")

  override def swizzleLanes(shape: LaneShape, a: Array[Byte], s: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 =>
      (0 until 16).map { i =>
        val idx = s(i)
        if idx < 16 && idx >= 0 then a(15 - idx) else 0.toByte
      }.toArray
    case _ => f.fail(UnsupportedConfiguration, s"SIMD swizzle lane does not support shape: $shape")

  override def splat(shape: LaneShape, v: V): Array[Byte] = shape match
    case LaneShape.I8 =>
      val codec = summon[LaneCodec[Byte]]
      val value = galoisI32.concretize(v).toByte
      encodeLanes(Seq.fill(16)(value), codec)
    case LaneShape.I16 =>
      val codec = summon[LaneCodec[Short]]
      val value = galoisI32.concretize(v).toShort
      encodeLanes(Seq.fill(8)(value), codec)
    case LaneShape.I32 =>
      val codec = summon[LaneCodec[Int]]
      val value = galoisI32.concretize(v)
      encodeLanes(Seq.fill(4)(value), codec)
    case LaneShape.I64 =>
      val codec = summon[LaneCodec[Long]]
      val value = galoisI64.concretize(v)
      encodeLanes(Seq.fill(2)(value), codec)
    case LaneShape.F32 =>
      val codec = summon[LaneCodec[Float]]
      val value = galoisF32.concretize(v)
      encodeLanes(Seq.fill(4)(value), codec)
    case LaneShape.F64 =>
      val codec = summon[LaneCodec[Double]]
      val value = galoisF64.concretize(v)
      encodeLanes(Seq.fill(2)(value), codec)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD splat does not support shape: $shape")

  override def zeroPad(shape: LaneShape, v: V): Array[Byte] = shape match
    case LaneShape.I32 =>
      val codec = summon[LaneCodec[Int]]
      val value = galoisI32.concretize(v)
      encodeLanes(Seq(0, 0, 0, value), codec)
    case LaneShape.I64 =>
      val codec = summon[LaneCodec[Long]]
      val value = galoisI64.concretize(v)
      encodeLanes(Seq(0L, value), codec)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD zero pad does not support shape: $shape")


  override def vectorAllTrue(shape: LaneShape, v: Array[Byte]): V = shape match
    case LaneShape.I8 =>
      val codec = summon[LaneCodec[Byte]]
      val lanes = extractLanes(v, codec)
      if !lanes.contains(codec.allZeroes) then galoisI32.asAbstract(1) else galoisI32.asAbstract(0)
    case LaneShape.I16 =>
      val codec = summon[LaneCodec[Short]]
      val lanes = extractLanes(v, codec)
      if !lanes.contains(codec.allZeroes) then galoisI32.asAbstract(1) else galoisI32.asAbstract(0)
    case LaneShape.I32 =>
      val codec = summon[LaneCodec[Int]]
      val lanes = extractLanes(v, codec)
      if !lanes.contains(codec.allZeroes) then galoisI32.asAbstract(1) else galoisI32.asAbstract(0)
    case LaneShape.I64 =>
      val codec = summon[LaneCodec[Long]]
      val lanes = extractLanes(v, codec)
      if !lanes.contains(codec.allZeroes) then galoisI32.asAbstract(1) else galoisI32.asAbstract(0)
    case _ => f.fail(UnsupportedConfiguration, s"SIMD all true does not support shape: $shape")

  override def vectorAnyTrue(shape: LaneShape, v: Array[Byte]): V =
    if(v.forall(b => b == 0)) then galoisI32.asAbstract(0) else galoisI32.asAbstract(1)

  private def vectorUnop[T: LaneCodec](v: Array[Byte])(op: T => T): Array[Byte] =
    val codec = summon[LaneCodec[T]]
    val lanes = extractLanes(v, codec).map(op)
    encodeLanes(lanes, codec)

  private def numericVectorUnop[T](concreteShape: ConcreteLaneShape[T], v: Array[Byte])(op: T => T): Array[Byte] = concreteShape match
    case ConcreteLaneShape.I8(_) => vectorUnop[T](v)(op)
    case ConcreteLaneShape.I16(_) => vectorUnop[T](v)(op)
    case ConcreteLaneShape.I32(_) => vectorUnop[T](v)(op)
    case ConcreteLaneShape.I64(_) => vectorUnop[T](v)(op)
    case ConcreteLaneShape.F32(_) => vectorUnop[T](v)(op)
    case ConcreteLaneShape.F64(_) => vectorUnop[T](v)(op)

  private def saturateSigned(x: Int, min: Int, max: Int): Int =
    if x < min then min else if x > max then max else x

  private def canonicalNaN(a: Float): Float =
    if a.isNaN then java.lang.Float.intBitsToFloat(0x7FC00000)
    else a

  private def canonicalNaN(a: Double): Double =
    if a.isNaN then java.lang.Double.longBitsToDouble(0x7FF8000000000000L)
    else a

  private def vectorBinop[T: LaneCodec](v1: Array[Byte], v2: Array[Byte])(op: (T, T) => T): Array[Byte] =
    val codec = summon[LaneCodec[T]]
    val lanesV1 = extractLanes(v1, codec)
    val lanesV2 = extractLanes(v2, codec)
    val resultLanes = lanesV1.zip(lanesV2).map(op.tupled)
    encodeLanes(resultLanes, codec)

  private def numericVectorBinop[T](concreteShape: ConcreteLaneShape[T], v1: Array[Byte], v2: Array[Byte])(op: (T, T) => T): Array[Byte] = concreteShape match
    case ConcreteLaneShape.I8(_) => vectorBinop[T](v1, v2)(op)
    case ConcreteLaneShape.I16(_) => vectorBinop[T](v1, v2)(op)
    case ConcreteLaneShape.I32(_) => vectorBinop[T](v1, v2)(op)
    case ConcreteLaneShape.I64(_) => vectorBinop[T](v1, v2)(op)
    case ConcreteLaneShape.F32(_) => vectorBinop[T](v1, v2)(op)
    case ConcreteLaneShape.F64(_) => vectorBinop[T](v1, v2)(op)

  private def vectorRelop[T: LaneCodec](v1: Array[Byte], v2: Array[Byte])(op: (T, T) => Boolean): Array[Byte] =
    val codec = summon[LaneCodec[T]]
    val lanesV1 = extractLanes(v1, codec)
    val lanesV2 = extractLanes(v2, codec)
    val resultLanes = lanesV1.zip(lanesV2).map { case (a, b) => if op(a, b) then codec.allOnes else codec.allZeroes }
    encodeLanes(resultLanes, codec)

  private def numericVectorRelop[T](concreteShape: ConcreteLaneShape[T], v1: Array[Byte], v2: Array[Byte])(op: (T, T) => Boolean): Array[Byte] = concreteShape match
    case ConcreteLaneShape.I8(_) => vectorRelop[T](v1, v2)(op)
    case ConcreteLaneShape.I16(_) => vectorRelop[T](v1, v2)(op)
    case ConcreteLaneShape.I32(_) => vectorRelop[T](v1, v2)(op)
    case ConcreteLaneShape.I64(_) => vectorRelop[T](v1, v2)(op)
    case ConcreteLaneShape.F32(_) => vectorRelop[T](v1, v2)(op)
    case ConcreteLaneShape.F64(_) => vectorRelop[T](v1, v2)(op)


  private def extractLanes[T](bytes: Array[Byte], codec: LaneCodec[T]): Seq[T] =
    val bb = ByteBuffer.wrap(bytes)
    val count = bytes.length / codec.bytes
    Seq.tabulate(count)(_ => codec.get(bb))

  private def encodeLanes[T](lanes: Seq[T], codec: LaneCodec[T]): Array[Byte] =
    val bb = ByteBuffer.allocate(lanes.size * codec.bytes)
    lanes.foreach(codec.put(bb, _))
    bb.array()

  private def toConcreteLaneShape(laneShape: LaneShape): ConcreteLaneShape[Any] = laneShape match
    case LaneShape.I8 => ConcreteLaneShape.I8(implicitly[Numeric[Byte]]).asInstanceOf[ConcreteLaneShape[Any]]
    case LaneShape.I16 => ConcreteLaneShape.I16(implicitly[Numeric[Short]]).asInstanceOf[ConcreteLaneShape[Any]]
    case LaneShape.I32 => ConcreteLaneShape.I32(implicitly[Numeric[Int]]).asInstanceOf[ConcreteLaneShape[Any]]
    case LaneShape.I64 => ConcreteLaneShape.I64(implicitly[Numeric[Long]]).asInstanceOf[ConcreteLaneShape[Any]]
    case LaneShape.F32 => ConcreteLaneShape.F32(implicitly[Numeric[Float]]).asInstanceOf[ConcreteLaneShape[Any]]
    case LaneShape.F64 => ConcreteLaneShape.F64(implicitly[Numeric[Double]]).asInstanceOf[ConcreteLaneShape[Any]]
    case LaneShape.V128 => throw new IllegalArgumentException("V128 cannot be converted to a concrete lane shape")

given ConcreteConvertBytesVector: ConvertBytesVec[Seq[Byte], Array[Byte]] with
  override def apply(from: Seq[Byte], conf: BytesSize && BytePadding && BitSign && SomeCC[ByteOrder]): Array[Byte] = {
    val padding = conf.c1.c1.c2
    val signed = conf.c1.c2 match {
      case BitSign.Signed => true
      case _ => false
    }
    val wrappingBytes = padding.wrapBytes
    val zeroBytes = padding.totalBytes

    if (padding == None) {
      return if conf.c2.t == ByteOrder.LITTLE_ENDIAN then from.toArray.reverse else from.toArray
    }

    val groups = from.grouped(wrappingBytes).toSeq
    val paddedGroups = groups.reverse.flatMap { group =>
      val g = group.reverse.padTo(wrappingBytes, 0.toByte)
      val padByte =
        if signed && (g.head & 0x80) != 0 then 0xFF.toByte
        else 0.toByte
      Seq.fill(zeroBytes / 2)(padByte) ++ g
    }

    val result = paddedGroups.toArray
    if conf.c2.t == ByteOrder.LITTLE_ENDIAN then result else result.reverse
  }

given ConcreteConvertVectorBytes: ConvertVecBytes[Array[Byte], Seq[Byte]] with
  override def apply(from: Array[Byte], conf: BytesSize && SomeCC[ByteOrder]): Seq[Byte] = {
    val arr = if conf.c2.t == ByteOrder.LITTLE_ENDIAN then from.reverse else from
    arr.toSeq
  }