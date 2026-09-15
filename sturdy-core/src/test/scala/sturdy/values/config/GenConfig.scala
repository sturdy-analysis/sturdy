package sturdy.values.config

import org.scalacheck.{Arbitrary, Gen}
import sturdy.values.convert.{*,given}

trait AllConfigs[C <: ConvertConfig[_]]:
  def allConfigs: Seq[C]

object AllConfigs:
  def apply[C <: ConvertConfig[_]: AllConfigs]: Seq[C] =
    summon[AllConfigs[C]].allConfigs

given AllConfigsOverflow: AllConfigs[Overflow] with
  def allConfigs: Seq[Overflow] = List(Overflow.Allow, Overflow.Fail, Overflow.JumpToBounds)


given AllConfigsBitSign: AllConfigs[BitSign] with
  override def allConfigs: Seq[BitSign] = List(BitSign.Signed, BitSign.Unsigned, BitSign.Raw)

given AllConfigsByteSize: AllConfigs[BytesSize] with
  override def allConfigs: Seq[BytesSize] = List(BytesSize.Byte, BytesSize.Short, BytesSize.Int, BytesSize.Long)


given AllConfigsNilCC: AllConfigs[NilCC.type] with
  override def allConfigs: Seq[NilCC.type] = List(NilCC)

given AllConfigsAnd[A <: ConvertConfig[_],B <: ConvertConfig[_]](using allConfigsA: AllConfigs[A], allConfigsB: AllConfigs[B]): AllConfigs[&&[A,B]] with
  override def allConfigs: Seq[&&[A,B]] =
    for((a:A) <- allConfigsA.allConfigs;
        (b:B) <- allConfigsB.allConfigs)
      yield &&(a, b)
