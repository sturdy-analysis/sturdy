package sturdy.values.simd

import sturdy.effect.failure.Failure
import sturdy.values.config.BytesSize
import sturdy.values.convert.{&&, SomeCC}
import sturdy.values.integer.IntegerDivisionByZero

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

    def get(bb: ByteBuffer) = bb.get
    def put(bb: ByteBuffer, v: Byte) = bb.put(v)
    def allOnes = 0xFF.toByte
    def allZeroes = 0x00.toByte

  given shortCodec: LaneCodec[Short] with
    val bytes = 2

    def get(bb: ByteBuffer) = bb.getShort
    def put(bb: ByteBuffer, v: Short) = bb.putShort(v)
    def allOnes = 0xFFFF.toShort
    def allZeroes = 0x0000.toShort

  given intCodec: LaneCodec[Int] with
    val bytes = 4

    def get(bb: ByteBuffer) = bb.getInt
    def put(bb: ByteBuffer, v: Int) = bb.putInt(v)
    def allOnes = 0xFFFFFFFF
    def allZeroes = 0x00000000

  given longCodec: LaneCodec[Long] with
    val bytes = 8

    def get(bb: ByteBuffer) = bb.getLong
    def put(bb: ByteBuffer, v: Long) = bb.putLong(v)
    def allOnes = 0xFFFFFFFFFFFFFFFFL
    def allZeroes = 0x0000000000000000L

  given floatCodec: LaneCodec[Float] with
    val bytes = 4

    def get(bb: ByteBuffer) = bb.getFloat
    def put(bb: ByteBuffer, v: Float) = bb.putFloat(v)
    def allOnes = java.lang.Float.intBitsToFloat(0xFFFFFFFF)
    def allZeroes = 0.0f

  given doubleCodec: LaneCodec[Double] with
    val bytes = 8

    def get(bb: ByteBuffer) = bb.getDouble
    def put(bb: ByteBuffer, v: Double) = bb.putDouble(v)
    def allOnes = java.lang.Double.longBitsToDouble(0xFFFFFFFFFFFFFFFFL)
    def allZeroes = 0.0d
}

given ConcreteSIMDOps[V] (using f: Failure, liftI32: Int => V, liftI64: Long => V, liftF32: Float => V, liftF64: Double => V): SIMDOps [Array[Byte], Array[Byte], V, Byte] with
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

  // Relational operations

  def vectorEq(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)(_ == _)
      case LaneShape.I16 => vectorRelop[Short](v1, v2)(_ == _)
      case LaneShape.I32 => vectorRelop[Int](v1, v2)(_ == _)
      case LaneShape.I64 => vectorRelop[Long](v1, v2)(_ == _)
      case LaneShape.F32 => vectorRelop[Float](v1, v2)((a, b) => canonicalNaN(a) == canonicalNaN(b))
      case LaneShape.F64 => vectorRelop[Double](v1, v2)((a, b) => canonicalNaN(a) == canonicalNaN(b))

  def vectorNe(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)(_ != _)
      case LaneShape.I16 => vectorRelop[Short](v1, v2)(_ != _)
      case LaneShape.I32 => vectorRelop[Int](v1, v2)(_ != _)
      case LaneShape.I64 => vectorRelop[Long](v1, v2)(_ != _)
      case LaneShape.F32 => vectorRelop[Float](v1, v2)((a, b) => canonicalNaN(a) != canonicalNaN(b))
      case LaneShape.F64 => vectorRelop[Double](v1, v2)((a, b) => canonicalNaN(a) != canonicalNaN(b))

  def vectorLt(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorRelop[Float](v1, v2)(_ < _)
      case LaneShape.F64 => vectorRelop[Double](v1, v2)(_ < _)

  def vectorLtU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)((a, b) => (a & 0xFF) < (b & 0xFF))
      case LaneShape.I16 => vectorRelop[Short](v1, v2)((a, b) => (a & 0xFFFF) < (b & 0xFFFF))
      case LaneShape.I32 => vectorRelop[Int](v1, v2)((a, b) => Integer.compareUnsigned(a, b) < 0)

  def vectorLtS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)(_ < _)
      case LaneShape.I16 => vectorRelop[Short](v1, v2)(_ < _)
      case LaneShape.I32 => vectorRelop[Int](v1, v2)(_ < _)
      case LaneShape.I64 => vectorRelop[Long](v1, v2)(_ < _)

  def vectorGt(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorRelop[Float](v1, v2)(_ > _)
      case LaneShape.F64 => vectorRelop[Double](v1, v2)(_ > _)

  def vectorGtU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)((a, b) => (a & 0xFF) > (b & 0xFF))
      case LaneShape.I16 => vectorRelop[Short](v1, v2)((a, b) => (a & 0xFFFF) > (b & 0xFFFF))
      case LaneShape.I32 => vectorRelop[Int](v1, v2)((a, b) => Integer.compareUnsigned(a, b) > 0)

  def vectorGtS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)(_ > _)
      case LaneShape.I16 => vectorRelop[Short](v1, v2)(_ > _)
      case LaneShape.I32 => vectorRelop[Int](v1, v2)(_ > _)
      case LaneShape.I64 => vectorRelop[Long](v1, v2)(_ > _)

  def vectorLe(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorRelop[Float](v1, v2)(_ <= _)
      case LaneShape.F64 => vectorRelop[Double](v1, v2)(_ <= _)

  def vectorLeU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)((a, b) => (a & 0xFF) <= (b & 0xFF))
      case LaneShape.I16 => vectorRelop[Short](v1, v2)((a, b) => (a & 0xFFFF) <= (b & 0xFFFF))
      case LaneShape.I32 => vectorRelop[Int](v1, v2)((a, b) => Integer.compareUnsigned(a, b) <= 0)

  def vectorLeS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)(_ <= _)
      case LaneShape.I16 => vectorRelop[Short](v1, v2)(_ <= _)
      case LaneShape.I32 => vectorRelop[Int](v1, v2)(_ <= _)
      case LaneShape.I64 => vectorRelop[Long](v1, v2)(_ <= _)

  def vectorGe(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorRelop[Float](v1, v2)(_ >= _)
      case LaneShape.F64 => vectorRelop[Double](v1, v2)(_ >= _)

  def vectorGeU(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)((a, b) => (a & 0xFF) >= (b & 0xFF))
      case LaneShape.I16 => vectorRelop[Short](v1, v2)((a, b) => (a & 0xFFFF) >= (b & 0xFFFF))
      case LaneShape.I32 => vectorRelop[Int](v1, v2)((a, b) => Integer.compareUnsigned(a, b) >= 0)

  def vectorGeS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorRelop[Byte](v1, v2)(_ >= _)
      case LaneShape.I16 => vectorRelop[Short](v1, v2)(_ >= _)
      case LaneShape.I32 => vectorRelop[Int](v1, v2)(_ >= _)
      case LaneShape.I64 => vectorRelop[Long](v1, v2)(_ >= _)


  // Lane operations
  def extractLane(shape: LaneShape, v: Array[Byte], lane: Byte): V =
    shape match {
      case LaneShape.I32 => liftI32(extractLanes(v, summon[LaneCodec[Int]])(3 - (lane & 0x03)))
      case LaneShape.I64 => liftI64(extractLanes(v, summon[LaneCodec[Long]])(1 - (lane & 0x01)))
      case LaneShape.F32 => liftF32(extractLanes(v, summon[LaneCodec[Float]])(3 - (lane & 0x03)))
      case LaneShape.F64 => liftF64(extractLanes(v, summon[LaneCodec[Double]])(1 - (lane & 0x01)))
    }

  def extractLaneU(shape: LaneShape, v: Array[Byte], lane: Byte): V = shape match
    case LaneShape.I8 =>
      val b = extractLanes(v, summon[LaneCodec[Byte]])(15 - (lane & 0x0F))
      liftI32(b & 0xFF)
    case LaneShape.I16 =>
      val s = extractLanes(v, summon[LaneCodec[Short]])(7 - (lane & 0x07))
      liftI32(s & 0xFFFF)
    case _ => throw new IllegalArgumentException("Invalid shape for unsigned extract")

  def extractLaneS(shape: LaneShape, v: Array[Byte], lane: Byte): V = shape match
    case LaneShape.I8 =>
      val b = extractLanes(v, summon[LaneCodec[Byte]])(15 - (lane & 0x0F))
      liftI32(b.toInt)
    case LaneShape.I16 =>
      val s = extractLanes(v, summon[LaneCodec[Short]])(7 - (lane & 0x07))
      liftI32(s.toInt)
    case _ => throw new IllegalArgumentException("Invalid shape for signed extract")


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

  private def vectorRelop[T: LaneCodec](v1: Array[Byte], v2: Array[Byte])(op: (T, T) => Boolean): Array[Byte] =
    val codec = summon[LaneCodec[T]]
    val lane1 = extractLanes(v1, codec)
    val lane2 = extractLanes(v2, codec)
    val lanes = lane1.zip(lane2).map { case (a, b) => if op(a, b) then codec.allOnes else codec.allZeroes }
    encodeLanes(lanes, codec)

  private def extractLanes[T](bytes: Array[Byte], codec: LaneCodec[T]): Seq[T] =
    val bb = ByteBuffer.wrap(bytes)
    val count = bytes.length / codec.bytes
    Seq.tabulate(count)(_ => codec.get(bb))

  private def encodeLanes[T](lanes: Seq[T], codec: LaneCodec[T]): Array[Byte] =
    val bb = ByteBuffer.allocate(lanes.size * codec.bytes)
    lanes.foreach(codec.put(bb, _))
    bb.array()

given ConcreteConvertBytesVector: ConvertBytesVec[Seq[Byte], Array[Byte]] with
  override def apply(from: Seq[Byte], conf: BytesSize && SomeCC[ByteOrder]): Array[Byte] = {
    val arr = from.toArray
    if conf.c2.t == ByteOrder.LITTLE_ENDIAN then arr.reverse else arr
  }