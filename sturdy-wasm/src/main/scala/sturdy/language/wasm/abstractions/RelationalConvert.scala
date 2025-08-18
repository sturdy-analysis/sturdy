package sturdy.language.wasm.abstractions

import sturdy.apron.{ApronBool, ApronExpr, ApronState, ApronVar}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.bytememory
import sturdy.effect.bytememory.Bytes.*
import sturdy.fix.DomLogger
import sturdy.language.wasm.analyses.RelationalAnalysis.VirtAddr
import sturdy.language.wasm.generic.{FixIn, FrameData, MemoryAddr}
import sturdy.values.config.{Bits, BytePadding, BytesSize}
import sturdy.values.convert.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.references.VirtualAddress
import sturdy.values.simd.{*, given}
import sturdy.values.{Combine, MaybeChanged, Topped, Widening}

import java.nio.ByteOrder

trait RelationalConvert extends RelationalMemory:
  import RelI32.*
  import Value.*
  import NumValue.*
  import VecValue.*

  private final class ConvertToBytes[From, FromV](inject: FromV => Value)
    extends Convert[From, Seq[Byte], FromV, Bytes, BytesSize && SomeCC[ByteOrder]]:
    override def apply(from: FromV, conf: BytesSize && SomeCC[ByteOrder]): Bytes =
      val byteSize && SomeCC(byteOrder, _) = conf
      StoredBytes(
        value = List((inject(from),byteSize.bytes)),
        byteOrder = Topped.Actual(byteOrder)
      )

  given ConvertI32Bytes: ConvertIntBytes[I32, Bytes] = ConvertToBytes[Int, I32](n => Num(Int32(n)))
  given ConvertI64Bytes: ConvertLongBytes[I64, Bytes] = ConvertToBytes[Long, I64](n => Num(Int64(n)))
  given ConvertF32Bytes: ConvertFloatBytes[F32, Bytes] = ConvertToBytes[Float, F32](n => Num(Float32(n)))
  given ConvertF64Bytes: ConvertDoubleBytes[F64, Bytes] = ConvertToBytes[Double, F64](n => Num(Float64(n)))
  given ConvertV128Bytes: ConvertVecBytes[V128, Bytes] = ConvertToBytes[Seq[Byte], V128](n => Vec(Vec128(n)))

  private trait ExtractFromVal[VTo]:
    def unapply(v: List[(Value,Int)]): Option[(VTo,Int)]

  private final class ConvertBytesInteger[To, VTo](expectedByteSize: Int, top: VTo, extract: ExtractFromVal[VTo])
    extends Convert[Seq[Byte], To, Bytes, VTo, BytesSize && SomeCC[ByteOrder] && Bits]:
    override def apply(from: Bytes, conf: BytesSize && SomeCC[ByteOrder] && Bits): VTo =
      val toByteSize && SomeCC(toByteOrder, _) && bits = conf
      from match
        case ReadBytes(Topped.Actual(extract((v,fromByteSize))), Topped.Actual(fromByteOrder))
          if (fromByteSize == expectedByteSize
            && toByteSize.bytes == expectedByteSize
            && fromByteOrder == toByteOrder
            && bits == Bits.Signed) => v
        case _ => top

  given ConvertBytesI32: ConvertBytesInt[Bytes, I32] = ConvertBytesInteger[Int, I32](
    expectedByteSize = 4,
    top = topI32,
    extract = {
      case List((Num(Int32(v)),4)) => Some((v, 4))
      case List((Num(Int32(NumExpr(b4))),1), (Num(Int32(NumExpr(b3))),1), (Num(Int32(NumExpr(b2))),1), (Num(Int32(NumExpr(b1))),1)) =>
        import ApronExpr.{intAdd, intMul, lit}
        val res =
          intAdd(intMul(b1, lit(math.pow(2,24).toInt, Type.I32Type), Type.I32Type),
          intAdd(intMul(b2, lit(math.pow(2,16).toInt, Type.I32Type), Type.I32Type),
          intAdd(intMul(b3, lit(math.pow(2,8).toInt, Type.I32Type), Type.I32Type),
                 intMul(b4, lit(math.pow(2,0).toInt, Type.I32Type), Type.I32Type), Type.I32Type), Type.I32Type), Type.I32Type)
        Some((NumExpr(res), 4))
      case _ => None
    }
  )

  given ConvertBytesI64: ConvertBytesLong[Bytes, I64] = ConvertBytesInteger[Long, I64](
    expectedByteSize = 8,
    top = topI64,
    extract = {
      case List((Num(Int64(v)), 8)) => Some((v, 8))
      case _ => None
    }
  )

  private final class ConvertBytesFloating[To, VTo](expectedByteSize: Int, top: VTo, extract: ExtractFromVal[VTo])
    extends Convert[Seq[Byte], To, Bytes, VTo, SomeCC[ByteOrder]]:
    override def apply(from: Bytes, conf: SomeCC[ByteOrder]): VTo =
      val SomeCC(toByteOrder, _) = conf
      from match
        case ReadBytes(Topped.Actual(extract((v,fromByteSize))), Topped.Actual(fromByteOrder))
          if (fromByteSize == expectedByteSize
            && fromByteOrder == toByteOrder) => v
        case _ => top


  given ConvertBytesF32: ConvertBytesFloat[Bytes, F32] = ConvertBytesFloating[Float, F32](
    expectedByteSize = 4,
    top = topF32,
    extract = {
      case List((Num(Float32(v)), 4)) => Some((v,4))
      case _ => None
    }
  )

  given ConvertBytesF64: ConvertBytesDouble[Bytes, F64] = ConvertBytesFloating[Double, F64](
    expectedByteSize = 8,
    top = topF64,
    extract = {
      case List((Num(Float64(v)), 8)) => Some((v, 8))
      case _ => None
    }
  )

  given ConvertBytesV128: ConvertBytesVec[Bytes, V128] with
    override def apply(from: Bytes, conf: BytesSize && BytePadding && Bits && SomeCC[ByteOrder]): V128 =
      Topped.Top