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

  private final class ConvertIntegerToBytes[From: Numeric, FromV]
    (typeByteSize: Int, inject: FromV => Value)
    (using intOps: IntegerOps[From, FromV],
           concreteIntOps: IntegerOps[From,From])
    extends Convert[From, Seq[Byte], FromV, Bytes, BytesSize && SomeCC[ByteOrder]]:
    override def apply(from: FromV, conf: BytesSize && SomeCC[ByteOrder]): Bytes =
      val toByteSize && SomeCC(byteOrder, _) = conf
      if(toByteSize.bytes < typeByteSize) {
        // res = remainderUnsigned(from, (2 << (toByteSize * 8 - 1)))
        val res = intOps.remainderUnsigned(
          from,
          intOps.integerLit(
            concreteIntOps.shiftLeft(
              concreteIntOps.integerLit(Numeric[From].fromInt(2)),
              concreteIntOps.integerLit(Numeric[From].fromInt(toByteSize.bytes * 8 - 1))
            )
          )
        )
        StoredBytes(
          value = List((inject(res), toByteSize.bytes)),
          byteOrder = Topped.Actual(byteOrder)
        )
      } else {
        StoredBytes(
          value = List((inject(from), toByteSize.bytes)),
          byteOrder = Topped.Actual(byteOrder)
        )
      }

  given ConvertI32Bytes(using intOps: IntegerOpsWithSignInterpretation[Int, I32], concreteIntOps: IntegerOps[Int, Int]): ConvertIntBytes[I32, Bytes] =
    ConvertIntegerToBytes[Int, I32](typeByteSize = 4, inject = n => Num(Int32(n)))

  given ConvertI64Bytes(using intOps: IntegerOpsWithSignInterpretation[Long, I64], concreteIntOps: IntegerOps[Long, Long]) : ConvertLongBytes[I64, Bytes] =
    ConvertIntegerToBytes[Long, I64](typeByteSize = 8, inject = n => Num(Int64(n)))

  private final class ConvertOtherValueToBytes[From, FromV]
    (inject: FromV => Value)
    extends Convert[From, Seq[Byte], FromV, Bytes, BytesSize && SomeCC[ByteOrder]]:
    override def apply(from: FromV, conf: BytesSize && SomeCC[ByteOrder]): Bytes =
      val toByteSize && SomeCC(byteOrder, _) = conf
      StoredBytes(
        value = List((inject(from), toByteSize.bytes)),
        byteOrder = Topped.Actual(byteOrder)
      )

  given ConvertF32Bytes: ConvertFloatBytes[F32, Bytes] = ConvertOtherValueToBytes[Float, F32](inject = n => Num(Float32(n)))
  given ConvertF64Bytes: ConvertDoubleBytes[F64, Bytes] = ConvertOtherValueToBytes[Double, F64](inject = n => Num(Float64(n)))
  given ConvertV128Bytes: ConvertVecBytes[V128, Bytes] = ConvertOtherValueToBytes[Seq[Byte], V128](inject = n => Vec(Vec128(n)))

  private trait ExtractFromVal[VTo]:
    def unapply(v: List[(Value,Int)]): Option[(VTo,Int)]

  private final class ConvertBytesInteger[To, VTo]
      (top: VTo, extract: ExtractFromVal[VTo])
      (using failure: Failure, intOps: IntegerOpsWithSignInterpretation[To, VTo])
    extends Convert[Seq[Byte], To, Bytes, VTo, BytesSize && SomeCC[ByteOrder] && BitSign]:
    override def apply(from: Bytes, conf: BytesSize && SomeCC[ByteOrder] && BitSign): VTo =
      val expectedNum && SomeCC(toByteOrder, _) && bits = conf
      from match
        case ReadBytes(Topped.Actual(extract((v,readNumBytes))), Topped.Actual(fromByteOrder))
          if (fromByteOrder == toByteOrder && readNumBytes == expectedNum.bytes) =>
            bits match
              case BitSign.Signed   => v
              case BitSign.Unsigned => intOps.interpretSignedAsUnsigned(v, fromNumBytes = readNumBytes)
              case BitSign.Raw      => unsupportedConfiguration(from, conf)
        case _ => top

  given ConvertBytesI32(using failure: Failure, intOps: IntegerOpsWithSignInterpretation[Int, I32]): ConvertBytesInt[Bytes, I32] = ConvertBytesInteger[Int, I32](
    top = topI32,
    extract = {
      case List((Num(Int32(v)), 4)) => Some((v, 4))
      case List((Num(Int32(NumExpr(b1))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Int,I32](List(b1), Type.I32Type, NumExpr(_)), 1))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Int,I32](List(b1, b2), Type.I32Type, NumExpr(_)), 2))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1), (Num(Int32(NumExpr(b3))),1), (Num(Int32(NumExpr(b4))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Int,I32](List(b1, b2, b3, b4), Type.I32Type, NumExpr(_)), 4))
      case _ => None
    }
  )

  given ConvertBytesI64(using failure: Failure, intOps: IntegerOpsWithSignInterpretation[Long, I64]): ConvertBytesLong[Bytes, I64] = ConvertBytesInteger[Long, I64](
    top = topI64,
    extract = {
      case List((Num(Int64(v)), 8)) => Some((v, 8))
      case List((Num(Int32(NumExpr(b1))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Long,I64](List(b1), Type.I64Type, x => x), 1))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Long,I64](List(b1, b2), Type.I64Type, x => x), 2))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1), (Num(Int32(NumExpr(b3))),1), (Num(Int32(NumExpr(b4))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Long,I64](List(b1, b2, b3, b4), Type.I64Type, x => x), 4))
      case List((Num(Int32(NumExpr(b1))),1), (Num(Int32(NumExpr(b2))),1), (Num(Int32(NumExpr(b3))),1), (Num(Int32(NumExpr(b4))),1),
                (Num(Int32(NumExpr(b5))),1), (Num(Int32(NumExpr(b6))),1), (Num(Int32(NumExpr(b7))),1), (Num(Int32(NumExpr(b8))),1)) =>
        Some((unsignedLittleEndianBytesToIntExpr[Long,I64](List(b1, b2, b3, b4, b5, b6, b7, b8), Type.I64Type, x => x), 8))
      case _ => None
    }
  )

  private inline def unsignedLittleEndianBytesToIntExpr[L,V](using intOps: IntegerOpsWithSignInterpretation[L,V])(bytes: List[ApronExpr[VirtAddr, Type]], tpe: Type, inject: ApronExpr[VirtAddr, Type] => V): V =
    intOps.interpretUnsignedAsSigned(
      inject(cast(unsignedLittleEndianBytesToIntExpr(bytes, 0), tpe.roundingType, tpe.roundingDir, tpe)),
      fromNumBytes = bytes.size
    )

  private def unsignedLittleEndianBytesToIntExpr(bytes: List[ApronExpr[VirtAddr, Type]], exponent: Int): ApronExpr[VirtAddr, Type] =
    bytes match
      case first :: Nil =>
        intMul(first, lit(BigInt(2).pow(exponent).toLong, Type.I8Type), Type.I8Type)
      case first :: rest =>
        intAdd(
          intMul(first, lit(BigInt(2).pow(exponent).toLong, Type.I8Type), Type.I8Type),
          unsignedLittleEndianBytesToIntExpr(rest, exponent + 8),
          Type.I8Type)
      case Nil => throw IllegalArgumentException("Cannot convert empty sequence of bytes to int")

  private final class ConvertBytesFloating[To, VTo](expectedByteSize: Int, top: VTo, extract: ExtractFromVal[VTo], inject: ApronExpr[VirtAddr, Type] => VTo)
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