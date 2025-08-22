package sturdy.language.wasm.abstractions

import sturdy.apron.ApronExpr.*
import sturdy.apron.{ApronBool, ApronExpr, ApronState, ApronVar}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.bytememory
import sturdy.effect.bytememory.Bytes.*
import sturdy.effect.failure.Failure
import sturdy.fix.DomLogger
import sturdy.language.wasm.analyses.RelationalAnalysis.VirtAddr
import sturdy.language.wasm.generic.{FixIn, FrameData, MemoryAddr}
import sturdy.values.config.{BitSign, BytePadding, BytesSize, unsupportedConfiguration}
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

  private trait ExtractFromVal:
    def unapply(v: List[(Value,Int)]): Option[(ApronExpr[VirtAddr,Type] | ApronBool[VirtAddr,Type],Int)]

  private final class ConvertBytesInteger[To, VTo]
      (top: VTo, extract: ExtractFromVal, inject: (ApronExpr[VirtAddr, Type] | ApronBool[VirtAddr, Type]) => VTo)
      (using failure: Failure, intOps: RelationalBaseIntegerOps[To, VirtAddr, Type])
    extends Convert[Seq[Byte], To, Bytes, VTo, BytesSize && SomeCC[ByteOrder] && BitSign]:
    override def apply(from: Bytes, conf: BytesSize && SomeCC[ByteOrder] && BitSign): VTo =
      val expectedNum && SomeCC(toByteOrder, _) && bits = conf
      from match
        case ReadBytes(Topped.Actual(extract((v: ApronExpr[VirtAddr,Type],readNumBytes))), Topped.Actual(fromByteOrder))
          if (fromByteOrder == toByteOrder) =>
            assert(readNumBytes == expectedNum.bytes)
            bits match
              case BitSign.Signed   => inject(v)
              case BitSign.Unsigned => inject(intOps.interpretSignedAsUnsigned(v, fromNumBytes = readNumBytes))
              case BitSign.Raw      => unsupportedConfiguration(from, conf)
        case ReadBytes(Topped.Actual(extract((b: ApronBool[VirtAddr,Type],readNumBytes))), Topped.Actual(fromByteOrder))
          if (fromByteOrder == toByteOrder) =>
            assert(readNumBytes == expectedNum.bytes)
            inject(b)
        case _ => top

  given ConvertBytesI32(using Failure, RelationalBaseIntegerOps[Int, VirtAddr, Type]): ConvertBytesInt[Bytes, I32] = ConvertBytesInteger[Int, I32](
    top = topI32,
    extract = {
      case List((Num(Int32(NumExpr(v))),4)) => Some((v, 4))
      case List((Num(Int32(NumExpr(b1))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Int](List(b1), Type.I32Type), 1))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Int](List(b1, b2), Type.I32Type), 2))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1), (Num(Int32(NumExpr(b3))),1), (Num(Int32(NumExpr(b4))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Int](List(b1, b2, b3, b4), Type.I32Type), 4))
      case List((Num(Int32(BoolExpr(b))), 4)) => Some((b, 4))
      case _ => None
    },
    inject = {
      case expr: ApronExpr[VirtAddr,Type] => NumExpr(expr)
      case bool: ApronBool[VirtAddr, Type] => BoolExpr(bool)
    }
  )

  given ConvertBytesI64(using ApronState[VirtAddr, Type], Failure, RelationalBaseIntegerOps[Long, VirtAddr, Type]): ConvertBytesLong[Bytes, I64] = ConvertBytesInteger[Long, I64](
    top = topI64,
    extract = {
      case List((Num(Int64(v)), 8)) => Some((v, 8))
      case List((Num(Int32(NumExpr(v))), 4)) => Some((v, 4))
      case List((Num(Int32(v: BoolExpr)), 4)) => Some((v.asNumExpr, 4))
      case List((Num(Int32(NumExpr(b1))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Long](List(b1), Type.I64Type), 1))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Long](List(b1, b2), Type.I64Type), 2))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1), (Num(Int32(NumExpr(b3))),1), (Num(Int32(NumExpr(b4))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Long](List(b1, b2, b3, b4), Type.I64Type), 4))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1), (Num(Int32(NumExpr(b3))),1), (Num(Int32(NumExpr(b4))),1),
                (Num(Int32(NumExpr(b5))),1), (Num(Int32(NumExpr(b6))),1), (Num(Int32(NumExpr(b7))),1), (Num(Int32(NumExpr(b8))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Long](List(b1, b2, b3, b4, b5, b6, b7, b8), Type.I64Type), 8))
      case _ => None
    },
    inject = {
      case expr : ApronExpr[VirtAddr, Type] => expr
      case bool : ApronBool[VirtAddr, Type] => BoolExpr(bool).asNumExpr
    }
  )

  private inline def unsignedLittleEndianBytesToIntExpr[L](bytes: List[ApronExpr[VirtAddr, Type]], tpe: Type)(using intOps: RelationalBaseIntegerOps[L, VirtAddr, Type]): ApronExpr[VirtAddr, Type] =
    intOps.interpretUnsignedAsSigned(
      cast(unsignedLittleEndianBytesToIntExpr(bytes, 0), tpe.roundingType, tpe.roundingDir, tpe),
      fromNumBytes = bytes.size
    )

  private def unsignedLittleEndianBytesToIntExpr(bytes: List[ApronExpr[VirtAddr, Type]], exponent: Int): ApronExpr[VirtAddr, Type] =
    bytes match
      case first :: Nil =>
        intMul(first, lit(math.pow(2, exponent).toInt, Type.I8Type), Type.I8Type)
      case first :: rest =>
        intAdd(
          intMul(first, lit(math.pow(2, exponent).toInt, Type.I8Type), Type.I8Type),
          unsignedLittleEndianBytesToIntExpr(rest, exponent + 8),
          Type.I8Type)
      case Nil => throw IllegalArgumentException("Cannot convert empty sequence of bytes to int")

  private final class ConvertBytesFloating[To, VTo](expectedByteSize: Int, top: VTo, extract: ExtractFromVal, inject: ApronExpr[VirtAddr, Type] => VTo)
    extends Convert[Seq[Byte], To, Bytes, VTo, SomeCC[ByteOrder]]:
    override def apply(from: Bytes, conf: SomeCC[ByteOrder]): VTo =
      val SomeCC(toByteOrder, _) = conf
      from match
        case ReadBytes(Topped.Actual(extract((v: ApronExpr[VirtAddr, Type],fromByteSize))), Topped.Actual(fromByteOrder))
          if (fromByteSize == expectedByteSize
            && fromByteOrder == toByteOrder) => inject(v)
        case _ => top


  given ConvertBytesF32: ConvertBytesFloat[Bytes, F32] = ConvertBytesFloating[Float, F32](
    expectedByteSize = 4,
    top = topF32,
    extract = {
      case List((Num(Float32(v)), 4)) => Some((v,4))
      case _ => None
    },
    inject = expr => expr
  )

  given ConvertBytesF64: ConvertBytesDouble[Bytes, F64] = ConvertBytesFloating[Double, F64](
    expectedByteSize = 8,
    top = topF64,
    extract = {
      case List((Num(Float64(v)), 8)) => Some((v, 8))
      case _ => None
    },
    inject = expr => expr
  )

  given ConvertBytesV128: ConvertBytesVec[Bytes, V128] with
    override def apply(from: Bytes, conf: BytesSize && BytePadding && BitSign && SomeCC[ByteOrder]): V128 =
      Topped.Top