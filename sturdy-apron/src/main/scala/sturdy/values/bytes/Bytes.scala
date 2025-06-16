package sturdy.values.bytes

import sturdy.apron.ApronExpr
import sturdy.values.{*, given}
import sturdy.values.integer.{*, given}

import java.nio.ByteOrder

case class Bytes[Addr,Type](value: Topped[ApronExpr[Addr,Type]], numBytes: NumericInterval[Int], byteOrder: Topped[ByteOrder])

given CombineBytes[Addr, Type, W <: Widening](using combineExpr: Combine[ApronExpr[Addr,Type], W]): Combine[Bytes[Addr,Type], W] with
  override def apply(v1: Bytes[Addr,Type], v2: Bytes[Addr,Type]): MaybeChanged[Bytes[Addr,Type]] =
    (v1.value, v2.value) match
      case (Topped.Actual(expr1), Topped.Actual(expr2)) if expr1._type == expr2._type =>
        for {
          value <- Combine(v1.value, v2.value)
          numBytes <- Join(v1.numBytes, v2.numBytes)
          byteOrder <- Join(v1.byteOrder, v2.byteOrder)
        } yield(Bytes(value, numBytes, byteOrder))
      case _ =>
        for {
          numBytes <- Join(v1.numBytes, v2.numBytes)
          byteOrder <- Join(v1.byteOrder, v2.byteOrder)
        } yield (Bytes(Topped.Top, numBytes, byteOrder))