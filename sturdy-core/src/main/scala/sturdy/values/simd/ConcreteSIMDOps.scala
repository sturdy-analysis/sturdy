package sturdy.values.simd

import sturdy.effect.failure.Failure
import sturdy.values.config.BytePadding.None
import sturdy.values.config.{Bits, BytesSize, BytePadding}
import sturdy.values.convert.{&&, Bijection, SomeCC}
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

given ConcreteSIMDOps[V]
(using f: Failure, bijectionI32: Bijection[Int, V], bijectionI64: Bijection[Long, V], bijectionF32: Bijection[Float, V], bijectionF64: Bijection[Double, V]): SIMDOps [Array[Byte], Array[Byte], V, Byte] with
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
      case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(Math.ceil(f).toFloat))
      case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(Math.ceil(d)))

  def vectorFloor(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(Math.floor(f).toFloat))
      case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(Math.floor(d)))

  def vectorTrunc(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(if f.isNaN then f else if f > 0 then f.floor else f.ceil))
      case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(if d.isNaN then d else if d > 0 then d.floor else d.ceil))

  def vectorNearest(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.F32 => vectorUnop[Float](v)(f => canonicalNaN(Math.rint(f).toFloat))
      case LaneShape.F64 => vectorUnop[Double](v)(d => canonicalNaN(Math.rint(d)))

  def vectorPopCount(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 => vectorUnop[Byte](v)(b => Integer.bitCount(b & 0xFF).toByte)

  def vectorNot(shape: LaneShape, v: Array[Byte]): Array[Byte] =
    shape match {
      case LaneShape.V128 => vectorUnop[Byte](v)(b => (~b).toByte)
    }

  def vectorBitmask(shape: LaneShape, v: Array[Byte]): V = {
    def bitmask[T](laneCount: Int, codec: LaneCodec[T], signBit: T => Boolean): V = {
      val buf = ByteBuffer.wrap(v.reverse)
      var result = 0
      for (i <- 0 until laneCount) {
        val value = codec.get(buf)
        if (signBit(value)) result |= (1 << i)
      }
      bijectionI32.apply(result)
    }

    shape match {
      case LaneShape.I8 => bitmask(16, summon[LaneCodec[Byte]], (x: Byte) => (x & 0x80) != 0)
      case LaneShape.I16 => bitmask(8, summon[LaneCodec[Short]], (x: Short) => (x & 0x8000) != 0)
      case LaneShape.I32 => bitmask(4, summon[LaneCodec[Int]], (x: Int) => x < 0)
      case LaneShape.I64 => bitmask(2, summon[LaneCodec[Long]], (x: Long) => x < 0)
    }
  }

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

  def vectorQ15MulrSatS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] = 
    shape match
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

  def vectorDotS(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I16 =>
        val codecShort = summon[LaneCodec[Short]]
        val lane1 = extractLanes(v1, codecShort)
        val lane2 = extractLanes(v2, codecShort)
        val products = lane1.zip(lane2).map { case (a, b) => a.toInt * b.toInt }
        val summed = products.grouped(2).map(_.sum).toArray
        val codecInt = summon[LaneCodec[Int]]
        encodeLanes(summed, codecInt)

  // Ternary operations
  def vectorBitselect(shape: LaneShape, v1: Array[Byte], v2: Array[Byte], mask: Array[Byte]): Array[Byte] =
    shape match {
      case LaneShape.V128 =>
        val result = new Array[Byte](v1.length)
        for (i <- v1.indices) {
          val b1: Byte = v1(i)
          val b2: Byte = v2(i)
          val m: Byte = mask(i)
          result(i) = ((b1 ^ b2) & m ^ b2).toByte
        }
        result
    }

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
      case LaneShape.F32 => vectorRelop[Float](v1, v2)(_ < _)
      case LaneShape.F64 => vectorRelop[Double](v1, v2)(_ < _)

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
      case LaneShape.F32 => vectorRelop[Float](v1, v2)(_ > _)
      case LaneShape.F64 => vectorRelop[Double](v1, v2)(_ > _)

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
      case LaneShape.F32 => vectorRelop[Float](v1, v2)(_ <= _)
      case LaneShape.F64 => vectorRelop[Double](v1, v2)(_ <= _)

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
      case LaneShape.F32 => vectorRelop[Float](v1, v2)(_ >= _)
      case LaneShape.F64 => vectorRelop[Double](v1, v2)(_ >= _)

  def vectorAnd(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match {
      case LaneShape.V128 => vectorBinop[Byte](v1, v2)((a, b) => (a & b).toByte)
    }

  def vectorAndNot(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match {
      case LaneShape.V128 => vectorBinop[Byte](v1, v2)((a, b) => (a & ~b).toByte)
    }

  def vectorOr(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match {
      case LaneShape.V128 => vectorBinop[Byte](v1, v2)((a, b) => (a | b).toByte)
    }

  def vectorXor(shape: LaneShape, v1: Array[Byte], v2: Array[Byte]): Array[Byte] =
    shape match {
      case LaneShape.V128 => vectorBinop[Byte](v1, v2)((a, b) => (a ^ b).toByte)
    }

  // Shift operations
  def vectorShiftLeft(shape: LaneShape, v: Array[Byte], shiftV: V): Array[Byte] =
    genericShift(shape, v, shiftV, bijectionI32.unapply, shiftLeftOp)

  def vectorShiftRightU(shape: LaneShape, v: Array[Byte], shiftV: V): Array[Byte] =
    genericShift(shape, v, shiftV, bijectionI32.unapply, shiftRightUnsignedOp)

  def vectorShiftRightS(shape: LaneShape, v: Array[Byte], shiftV: V): Array[Byte] =
    genericShift(shape, v, shiftV, bijectionI32.unapply, shiftRightSignedOp)

  def genericShift(shape: LaneShape, v: Array[Byte], shiftV: V, extraction: V => Int, op: (Any, Int) => Any): Array[Byte] =
    shape match {
      case LaneShape.I8 => shift(v, extraction(shiftV) % 8, op)(using summon[LaneCodec[Byte]])
      case LaneShape.I16 => shift(v, extraction(shiftV) % 16, op)(using summon[LaneCodec[Short]])
      case LaneShape.I32 => shift(v, extraction(shiftV) % 32, op)(using summon[LaneCodec[Int]])
      case LaneShape.I64 => shift(v, extraction(shiftV) % 64, op)(using summon[LaneCodec[Long]])
    }

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
    var i = 0
    while (i < lanes) {
      outCodec.put(out, f(inCodec.get(in)))
      i += 1
    }
    out.array()
  }

  def vectorConvertU(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match {
    case LaneShape.I32 => transformLanes[Int, Float](v, 0, 4)(using summon[LaneCodec[Int]], summon[LaneCodec[Float]])(v => (v.toLong & 0xFFFFFFFFL).toFloat)
  }

  def vectorConvertS(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match {
    case LaneShape.I32 => transformLanes[Int, Float](v, 0, 4)(using summon[LaneCodec[Int]], summon[LaneCodec[Float]])(_.toFloat)
  }

  def vectorConvertLowU(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match {
    case LaneShape.I32 => transformLanes[Int, Double](v, 8, 2)(using summon[LaneCodec[Int]], summon[LaneCodec[Double]]) (v => (v.toLong & 0xFFFFFFFFL).toDouble)
  }

  def vectorConvertLowS(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match {
    case LaneShape.I32 => transformLanes[Int, Double](v, 8, 2)(using summon[LaneCodec[Int]], summon[LaneCodec[Double]])(_.toDouble)
  }

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
      var i = 0
      while (i < 2) {
        outCodec.put(out, 0); i += 1
      }
      val high = transformLanes[Double, Int](v, 0, 2)(using summon[LaneCodec[Double]], summon[LaneCodec[Int]]) { d =>
        clampFloatToI32(d, signed)
      }
      out.put(high)
      out.array()

  def vectorTruncSatU(shape: LaneShape, mode: TruncMode, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 | LaneShape.F64 => truncSatFloatToI32(v, signed = false, mode)

  def vectorTruncSatS(shape: LaneShape, mode: TruncMode, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 | LaneShape.F64 => truncSatFloatToI32(v, signed = true, mode)

  def vectorDemoteZero(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F64 =>
      val out = ByteBuffer.allocate(16)
      val outCodec = summon[LaneCodec[Float]]
      var i = 0
      while (i < 2) {
        outCodec.put(out, outCodec.allZeroes); i += 1
      }
      val low = transformLanes[Double, Float](v, 0, 2)(using summon[LaneCodec[Double]], summon[LaneCodec[Float]])(_.toFloat)
      out.put(low)
      out.array()

  def vectorPromoteLow(shape: LaneShape, v: Array[Byte]): Array[Byte] = shape match
    case LaneShape.F32 => transformLanes[Float, Double](v, 0, 2)(using summon[LaneCodec[Float]], summon[LaneCodec[Double]])(_.toDouble)

  private def narrow[I, O](a: Array[Byte], b: Array[Byte], lanes: Int)(using inCodec: LaneCodec[I], outCodec: LaneCodec[O])(f: I => O): Array[Byte] = {
    val inA = ByteBuffer.wrap(a)
    val inB = ByteBuffer.wrap(b)
    val out = ByteBuffer.allocate(lanes * 2 * outCodec.bytes)
    var i = 0
    while (i < lanes) {
      outCodec.put(out, f(inCodec.get(inA)))
      i += 1
    }
    i = 0
    while (i < lanes) {
      outCodec.put(out, f(inCodec.get(inB)))
      i += 1
    }
    out.array()
  }

  def vectorNarrowU(from: LaneShape, to: LaneShape, a: Array[Byte], b: Array[Byte]): Array[Byte] = (from, to) match
    case (LaneShape.I16, LaneShape.I8) =>
      narrow[Short, Byte](a, b, 8)(using summon[LaneCodec[Short]], summon[LaneCodec[Byte]]) { s =>
        Math.min(255, Math.max(0, s.toInt)).toByte
      }

    case (LaneShape.I32, LaneShape.I16) =>
      narrow[Int, Short](a, b, 4)(using summon[LaneCodec[Int]], summon[LaneCodec[Short]]) { i =>
        Math.min(65535, Math.max(0, i)).toShort
      }

  def vectorNarrowS(from: LaneShape, to: LaneShape, a: Array[Byte], b: Array[Byte]): Array[Byte] = (from, to) match
    case (LaneShape.I16, LaneShape.I8) =>
      narrow[Short, Byte](a, b, 8)(using summon[LaneCodec[Short]], summon[LaneCodec[Byte]]) { s =>
        saturateSigned(s.toInt, -128, 127).toByte
      }

    case (LaneShape.I32, LaneShape.I16) =>
      narrow[Int, Short](a, b, 4)(using summon[LaneCodec[Int]], summon[LaneCodec[Short]]) { i =>
        saturateSigned(i, -32768, 32767).toShort
      }

  def vectorExtendU(from: LaneShape, to: LaneShape, half: Half, v: Array[Byte]): Array[Byte] = (from, to, half) match {
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
  }

  def vectorExtendS(from: LaneShape, to: LaneShape, half: Half, v: Array[Byte]): Array[Byte] = (from, to, half) match {
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
  }

  private def extAddGeneric[A, B](v: Array[Byte], pairs: Int)(using inCodec: LaneCodec[A], outCodec: LaneCodec[B])(toInt: A => Int, fromInt: Int => B): Array[Byte] = {
    val in = ByteBuffer.wrap(v)
    val out = ByteBuffer.allocate(16)
    var i = 0
    while (i < pairs) {
      val a = toInt(inCodec.get(in))
      val b = toInt(inCodec.get(in))
      outCodec.put(out, fromInt(a + b))
      i += 1
    }
    out.array()
  }

  def vectorExtAddU(shape: LaneShape, v1: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => extAddGeneric[Byte, Short](v1, 8)(using summon[LaneCodec[Byte]], summon[LaneCodec[Short]])(b => b & 0xFF, s => s.toShort)
    case LaneShape.I16 => extAddGeneric[Short, Int](v1, 4)(using summon[LaneCodec[Short]], summon[LaneCodec[Int]])(s => s & 0xFFFF, i => i)

  def vectorExtAddS(shape: LaneShape, v1: Array[Byte]): Array[Byte] = shape match
    case LaneShape.I8 => extAddGeneric[Byte, Short](v1, 8)(using summon[LaneCodec[Byte]], summon[LaneCodec[Short]])(_.toInt, s => s.toShort)
    case LaneShape.I16 => extAddGeneric[Short, Int](v1, 4)(using summon[LaneCodec[Short]], summon[LaneCodec[Int]])(_.toInt, i => i)

  // Lane operations
  def extractLane(shape: LaneShape, v: Array[Byte], lane: Byte): V =
    shape match {
      case LaneShape.I32 => bijectionI32.apply(extractLanes(v, summon[LaneCodec[Int]])(3 - (lane & 0x03)))
      case LaneShape.I64 => bijectionI64.apply(extractLanes(v, summon[LaneCodec[Long]])(1 - (lane & 0x01)))
      case LaneShape.F32 => bijectionF32.apply(extractLanes(v, summon[LaneCodec[Float]])(3 - (lane & 0x03)))
      case LaneShape.F64 => bijectionF64.apply(extractLanes(v, summon[LaneCodec[Double]])(1 - (lane & 0x01)))
    }

  def extractLaneU(shape: LaneShape, v: Array[Byte], lane: Byte): V = shape match
    case LaneShape.I8 =>
      val b = extractLanes(v, summon[LaneCodec[Byte]])(15 - (lane & 0x0F))
      bijectionI32.apply(b & 0xFF)
    case LaneShape.I16 =>
      val s = extractLanes(v, summon[LaneCodec[Short]])(7 - (lane & 0x07))
      bijectionI32.apply(s & 0xFFFF)
    case _ => throw new IllegalArgumentException("Invalid shape for unsigned extract")

  def extractLaneS(shape: LaneShape, v: Array[Byte], lane: Byte): V = shape match
    case LaneShape.I8 =>
      val b = extractLanes(v, summon[LaneCodec[Byte]])(15 - (lane & 0x0F))
      bijectionI32.apply(b.toInt)
    case LaneShape.I16 =>
      val s = extractLanes(v, summon[LaneCodec[Short]])(7 - (lane & 0x07))
      bijectionI32.apply(s.toInt)
    case _ => throw new IllegalArgumentException("Invalid shape for signed extract")

  def replaceLane(shape: LaneShape, v: Array[Byte], lane: Byte, value: V): Array[Byte] =
    shape match {
      case LaneShape.I8 =>
        val codec = summon[LaneCodec[Byte]]
        val lanes = extractLanes(v, codec).updated(15 - (lane & 0x0F), bijectionI32.unapply(value).toByte)
        encodeLanes(lanes, codec)

      case LaneShape.I16 =>
        val codec = summon[LaneCodec[Short]]
        val lanes = extractLanes(v, codec).updated(7 - (lane & 0x07), bijectionI32.unapply(value).toShort)
        encodeLanes(lanes, codec)

      case LaneShape.I32 =>
        val codec = summon[LaneCodec[Int]]
        val lanes = extractLanes(v, codec).updated(3 - (lane & 0x03), bijectionI32.unapply(value))
        encodeLanes(lanes, codec)

      case LaneShape.I64 =>
        val codec = summon[LaneCodec[Long]]
        val lanes = extractLanes(v, codec).updated(1 - (lane & 0x01), bijectionI64.unapply(value))
        encodeLanes(lanes, codec)

      case LaneShape.F32 =>
        val codec = summon[LaneCodec[Float]]
        val lanes = extractLanes(v, codec).updated(3 - (lane & 0x03), bijectionF32.unapply(value))
        encodeLanes(lanes, codec)

      case LaneShape.F64 =>
        val codec = summon[LaneCodec[Double]]
        val lanes = extractLanes(v, codec).updated(1 - (lane & 0x01), bijectionF64.unapply(value))
        encodeLanes(lanes, codec)
    }

  def shuffleLanes(shape: LaneShape, a: Array[Byte], b: Array[Byte], lanes: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 =>
        (0 until 16).map { i =>
          val idx = lanes(i) & 0xFF
          if idx < 16 then a(15 - idx) else b(15 - (idx - 16))
        }.reverse.toArray

  def swizzleLanes(shape: LaneShape, a: Array[Byte], s: Array[Byte]): Array[Byte] =
    shape match
      case LaneShape.I8 =>
        (0 until 16).map { i =>
          val idx = s(i)
          if idx < 16 && idx >= 0 then a(15 - idx) else 0.toByte
        }.toArray

  def splat(shape: LaneShape, v: V): Array[Byte] =
    shape match {
      case LaneShape.I8 =>
        val codec = summon[LaneCodec[Byte]]
        val value = bijectionI32.unapply(v).toByte
        encodeLanes(Seq.fill(16)(value), codec)
      case LaneShape.I16 =>
        val codec = summon[LaneCodec[Short]]
        val value = bijectionI32.unapply(v).toShort
        encodeLanes(Seq.fill(8)(value), codec)
      case LaneShape.I32 =>
        val codec = summon[LaneCodec[Int]]
        val value = bijectionI32.unapply(v)
        encodeLanes(Seq.fill(4)(value), codec)
      case LaneShape.I64 =>
        val codec = summon[LaneCodec[Long]]
        val value = bijectionI64.unapply(v)
        encodeLanes(Seq.fill(2)(value), codec)
      case LaneShape.F32 =>
        val codec = summon[LaneCodec[Float]]
        val value = bijectionF32.unapply(v)
        encodeLanes(Seq.fill(4)(value), codec)
      case LaneShape.F64 =>
        val codec = summon[LaneCodec[Double]]
        val value = bijectionF64.unapply(v)
        encodeLanes(Seq.fill(2)(value), codec)
    }

  def zeroPad(shape: LaneShape, v: V): Array[Byte] =
    shape match {
      case LaneShape.I32 =>
        val codec = summon[LaneCodec[Int]]
        val value = bijectionI32.unapply(v)
        encodeLanes(Seq(0, 0, 0, value), codec)
      case LaneShape.I64 =>
        val codec = summon[LaneCodec[Long]]
        val value = bijectionI64.unapply(v)
        encodeLanes(Seq(0L, value), codec)
    }

  def vectorAllTrue(shape: LaneShape, v: Array[Byte]): V =
    shape match {
      case LaneShape.I8 =>
        val codec = summon[LaneCodec[Byte]]
        val lanes = extractLanes(v, codec)
        if !lanes.contains(codec.allZeroes) then bijectionI32.apply(1) else bijectionI32.apply(0)
      case LaneShape.I16 =>
        val codec = summon[LaneCodec[Short]]
        val lanes = extractLanes(v, codec)
        if !lanes.contains(codec.allZeroes) then bijectionI32.apply(1) else bijectionI32.apply(0)
      case LaneShape.I32 =>
        val codec = summon[LaneCodec[Int]]
        val lanes = extractLanes(v, codec)
        if !lanes.contains(codec.allZeroes) then bijectionI32.apply(1) else bijectionI32.apply(0)
      case LaneShape.I64 =>
        val codec = summon[LaneCodec[Long]]
        val lanes = extractLanes(v, codec)
        if !lanes.contains(codec.allZeroes) then bijectionI32.apply(1) else bijectionI32.apply(0)
    }

  def vectorAnyTrue(shape: LaneShape, v: Array[Byte]): V =
    shape match {
      case LaneShape.V128 =>
        val codec = summon[LaneCodec[Byte]]
        val lanes = extractLanes(v, codec)
        if lanes.exists(_ != codec.allZeroes) then bijectionI32.apply(1) else bijectionI32.apply(0)
    }

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
  override def apply(from: Seq[Byte], conf: BytesSize && BytePadding && Bits && SomeCC[ByteOrder]): Array[Byte] = {
    val padding = conf.c1.c1.c2
    val signed = conf.c1.c2 match {
      case Bits.Signed => true
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