package sturdy.language.wasm.abstractions

import sturdy.apron.{ApronBool, ApronExpr, ApronState, ApronVar}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.bytememory
import sturdy.effect.bytememory.Bytes.*
import sturdy.fix.DomLogger
import sturdy.language.wasm.analyses.RelationalAnalysis.VirtAddr
import sturdy.language.wasm.generic.{FixIn, FrameData, MemoryAddr}
import sturdy.values.config.{Bits, BytesSize}
import sturdy.values.convert.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.references.VirtualAddress
import sturdy.values.{Combine, MaybeChanged, Topped, Widening}

import java.nio.ByteOrder

trait RelationalConvert extends RelationalMemory:
  import RelI32.*

  private final class ConvertToBytes[From, FromV](inject: FromV => Value)
    extends Convert[From, Seq[Byte], FromV, Bytes, BytesSize && SomeCC[ByteOrder]]:
    override def apply(from: FromV, conf: BytesSize && SomeCC[ByteOrder]): Bytes =
      val byteSize && SomeCC(byteOrder, _) = conf
      StoredBytes(
        value = inject(from),
        storedBytes = Topped.Actual(byteSize.bytes),
        storedByteOrder = Topped.Actual(byteOrder)
      )

  given ConvertI32Bytes: ConvertIntBytes[I32, Bytes] = ConvertToBytes[Int, I32](Value.Int32(_))
  given ConvertI64Bytes: ConvertLongBytes[I64, Bytes] = ConvertToBytes[Long, I64](Value.Int64(_))
  given ConvertF32Bytes: ConvertFloatBytes[F32, Bytes] = ConvertToBytes[Float, F32](Value.Float32(_))
  given ConvertF64Bytes: ConvertDoubleBytes[F64, Bytes] = ConvertToBytes[Double, F64](Value.Float64(_))

  private trait ExtractFromVal[VTo]:
    def unapply(v: Value): Option[VTo]

  private final class ConvertBytesInteger[To, VTo](expectedByteSize: Int, top: VTo, extract: ExtractFromVal[VTo])
    extends Convert[Seq[Byte], To, Bytes, VTo, BytesSize && SomeCC[ByteOrder] && Bits]:
    override def apply(from: Bytes, conf: BytesSize && SomeCC[ByteOrder] && Bits): VTo =
      val toByteSize && SomeCC(toByteOrder, _) && bits = conf
      from match
        case ReadBytes(extract(v), Topped.Actual(storedBytes), Topped.Actual(fromByteOrder), Topped.Actual(true), Topped.Actual(readBytes))
          if (storedBytes == expectedByteSize
            && readBytes == expectedByteSize
            && toByteSize.bytes == expectedByteSize
            && fromByteOrder == toByteOrder
            && bits == Bits.Signed) => v
        case _ => top

  given ConvertBytesI32: ConvertBytesInt[Bytes, I32] = ConvertBytesInteger[Int, I32](
    expectedByteSize = 4,
    top = topI32,
    extract = {
      case Value.Int32(v) => Some(v)
      case _ => None
    }
  )

  given ConvertBytesI64: ConvertBytesLong[Bytes, I64] = ConvertBytesInteger[Long, I64](
    expectedByteSize = 8,
    top = topI64,
    extract = {
      case Value.Int64(v) => Some(v)
      case _ => None
    }
  )

  private final class ConvertBytesFloating[To, VTo](expectedByteSize: Int, top: VTo, extract: ExtractFromVal[VTo])
    extends Convert[Seq[Byte], To, Bytes, VTo, SomeCC[ByteOrder]]:
    override def apply(from: Bytes, conf: SomeCC[ByteOrder]): VTo =
      val SomeCC(toByteOrder, _) = conf
      from match
        case ReadBytes(extract(v), Topped.Actual(storedBytes), Topped.Actual(fromByteOrder), Topped.Actual(true), Topped.Actual(readBytes))
          if (storedBytes == expectedByteSize
            && readBytes == expectedByteSize
            && fromByteOrder == toByteOrder) => v
        case _ => top


  given ConvertBytesF32: ConvertBytesFloat[Bytes, F32] = ConvertBytesFloating[Float, F32](
    expectedByteSize = 4,
    top = topF32,
    extract = {
      case Value.Float32(v) => Some(v)
      case _ => None
    }
  )

  given ConvertBytesF64: ConvertBytesDouble[Bytes, F64] = ConvertBytesFloating[Double, F64](
    expectedByteSize = 8,
    top = topF64,
    extract = {
      case Value.Float64(v) => Some(v)
      case _ => None
    }
  )