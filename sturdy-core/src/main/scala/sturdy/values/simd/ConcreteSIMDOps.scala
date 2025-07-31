package sturdy.values.simd

import sturdy.effect.failure.Failure
import sturdy.values.integer.IntegerDivisionByZero

import java.nio.ByteBuffer

trait LaneCodec[T] {
  def bytes: Int
  def get(bb: ByteBuffer): T
  def put(bb: ByteBuffer, v: T): Unit
}

object LaneCodec {
  given byteCodec: LaneCodec[Byte] with
    val bytes = 1

    def get(bb: ByteBuffer) = bb.get
    def put(bb: ByteBuffer, v: Byte) = bb.put(v)

  given shortCodec: LaneCodec[Short] with
    val bytes = 2

    def get(bb: ByteBuffer) = bb.getShort
    def put(bb: ByteBuffer, v: Short) = bb.putShort(v)

  given intCodec: LaneCodec[Int] with
    val bytes = 4

    def get(bb: ByteBuffer) = bb.getInt
    def put(bb: ByteBuffer, v: Int) = bb.putInt(v)

  given longCodec: LaneCodec[Long] with
    val bytes = 8

    def get(bb: ByteBuffer) = bb.getLong
    def put(bb: ByteBuffer, v: Long) = bb.putLong(v)

  given floatCodec: LaneCodec[Float] with
    val bytes = 4

    def get(bb: ByteBuffer) = bb.getFloat
    def put(bb: ByteBuffer, v: Float) = bb.putFloat(v)

  given doubleCodec: LaneCodec[Double] with
    val bytes = 8

    def get(bb: ByteBuffer) = bb.getDouble
    def put(bb: ByteBuffer, v: Double) = bb.putDouble(v)
}

given ConcreteSIMDOps (using f: Failure): SIMDOps [Array[Byte], Array[Byte]] with
  def vectorLit(i: Array[Byte]): Array[Byte] = i

  // Unary operations

  def vectorAbs(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorUnop[Byte](v)(b => b.abs)
      case LaneShape.I16 => vectorUnop[Short](v)(s => s.abs)
      case LaneShape.I32 => vectorUnop[Int](v)(Math.abs)
      case LaneShape.I64 => vectorUnop[Long](v)(Math.abs)
      case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(Math.abs(f)))
      case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(Math.abs(d)))

  def vectorNeg(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorUnop[Byte](v)(b => (-b).toByte)
      case LaneShape.I16 => vectorUnop[Short](v)(s => (-s).toShort)
      case LaneShape.I32 => vectorUnop[Int](v)(-_)
      case LaneShape.I64 => vectorUnop[Long](v)(-_)
      case LaneShape.F32 => vectorUnop[Float](v)(-_)
      case LaneShape.F64 => vectorUnop[Double](v)(-_)

  def vectorSqrt(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(Math.sqrt(f).toFloat))
      case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(Math.sqrt(d)))

  def vectorCeil(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorUnop[Float](v)(f => Math.ceil(f).toFloat)
      case LaneShape.F64 => vectorUnop[Double](v)(Math.ceil)

  def vectorFloor(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorUnop[Float](v)(f => Math.floor(f).toFloat)
      case LaneShape.F64 => vectorUnop[Double](v)(Math.floor)

  def vectorTrunc(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorUnop[Float](v)(f => if f.isNaN then f else f.toInt.toFloat)
      case LaneShape.F64 => vectorUnop[Double](v)(d => if d.isNaN then d else d.toLong.toDouble)

  def vectorNearest(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorUnop[Float](v)(f => Math.rint(f).toFloat)
      case LaneShape.F64 => vectorUnop[Double](v)(Math.rint)

  def vectorPopCount(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorUnop[Byte](v)(b => Integer.bitCount(b & 0xFF).toByte)

  // Binary operations
  def vectorAdd(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => (a + b).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => (a + b).toShort)
      case LaneShape.I32 => vectorBinop[Int](v1, v2)(_ + _)
      case LaneShape.I64 => vectorBinop[Long](v1, v2)(_ + _)
      case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => canonicalNaN(a + b))
      case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => canonicalNaN(a + b))

  def vectorSub(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => (a - b).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => (a - b).toShort)
      case LaneShape.I32 => vectorBinop[Int](v1, v2)(_ - _)
      case LaneShape.I64 => vectorBinop[Long](v1, v2)(_ - _)
      case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => canonicalNaN(a - b))
      case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => canonicalNaN(a - b))

  def vectorMul(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => (a * b).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => (a * b).toShort)
      case LaneShape.I32 => vectorBinop[Int](v1, v2)(_ * _)
      case LaneShape.I64 => vectorBinop[Long](v1, v2)(_ * _)
      case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => canonicalNaN(a * b))
      case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => canonicalNaN(a * b))

  def vectorDiv(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
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

  def vectorMin(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => canonicalNaN(Math.min(a, b)))
      case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => canonicalNaN(Math.min(a, b)))

  def vectorMax(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => canonicalNaN(Math.max(a, b)))
      case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => canonicalNaN(Math.max(a, b)))

  def vectorPMin(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => if (b < a) b else a)
      case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => if (b < a) b else a)

  def vectorPMax(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorBinop[Float](v1, v2)((a, b) => if (a < b) b else a)
      case LaneShape.F64 => vectorBinop[Double](v1, v2)((a, b) => if (a < b) b else a)

  def vectorMinU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.min(a & 0xFF, b & 0xFF).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.min(a & 0xFFFF, b & 0xFFFF).toShort)
      case LaneShape.I32 => vectorBinop[Int](v1, v2)((a, b) => Math.min(a.toLong & 0xFFFFFFFFL, b.toLong & 0xFFFFFFFFL).toInt)

  def vectorMinS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.min(a, b).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.min(a, b).toShort)
      case LaneShape.I32 => vectorBinop[Int](v1, v2)(Math.min)

  def vectorMaxU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.max(a & 0xFF, b & 0xFF).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.max(a & 0xFFFF, b & 0xFFFF).toShort)
      case LaneShape.I32 => vectorBinop[Int](v1, v2)((a, b) => Math.max(a.toLong & 0xFFFFFFFFL, b.toLong & 0xFFFFFFFFL).toInt)

  def vectorMaxS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.max(a, b).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.max(a, b).toShort)
      case LaneShape.I32 => vectorBinop[Int](v1, v2)(Math.max)

  def vectorAddSatU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.min(255, (a & 0xFF) + (b & 0xFF)).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.min(65535, (a & 0xFFFF) + (b & 0xFFFF)).toShort)

  def vectorAddSatS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => saturateSigned(a + b, -128, 127).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => saturateSigned(a + b, -32768, 32767).toShort)

  def vectorSubSatU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => Math.max(0, (a & 0xFF) - (b & 0xFF)).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => Math.max(0, (a & 0xFFFF) - (b & 0xFFFF)).toShort)

  def vectorSubSatS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => saturateSigned(a - b, -128, 127).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => saturateSigned(a - b, -32768, 32767).toShort)

  def vectorAvrgU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorBinop[Byte](v1, v2)((a, b) => (((a & 0xFF) + (b & 0xFF) + 1) / 2).toByte)
      case LaneShape.I16 => vectorBinop[Short](v1, v2)((a, b) => (((a & 0xFFFF) + (b & 0xFFFF) + 1) / 2).toShort)

  def vectorQ15MulrSatS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = ???

  private def vectorUnop[T: LaneCodec](v: Array[Byte])(op: T => T): Array[Byte] =
    val codec = summon[LaneCodec[T]]
    val lanes = extractLanes(v, codec).map(op)
    encodeLanes(lanes, codec)

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
    val lane1 = extractLanes(v1, codec)
    val lane2 = extractLanes(v2, codec)
    val lanes = lane1.zip(lane2).map(op.tupled)
    encodeLanes(lanes, codec)

  private def extractLanes[T](bytes: Array[Byte], codec: LaneCodec[T]): Seq[T] =
    val bb = ByteBuffer.wrap(bytes)
    val count = bytes.length / codec.bytes
    Seq.tabulate(count)(_ => codec.get(bb))

  private def encodeLanes[T](lanes: Seq[T], codec: LaneCodec[T]): Array[Byte] =
    val bb = ByteBuffer.allocate(lanes.size * codec.bytes)
    lanes.foreach(codec.put(bb, _))
    bb.array()
