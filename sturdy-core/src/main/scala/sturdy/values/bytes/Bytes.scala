package sturdy.values.bytes

import sturdy.values.{*, given}
import sturdy.values.integer.{*, given}

import java.nio.ByteOrder

case class Bytes[Val](value: Val, numBytes: NumericInterval[Int], byteOrder: Topped[ByteOrder])

given CombineBytes[Val, W <: Widening](using combineVal: Combine[Val, W]): Combine[Bytes[Val], W] with
  given Structural[ByteOrder] with {}
  override def apply(v1: Bytes[Val], v2: Bytes[Val]): MaybeChanged[Bytes[Val]] =
    for {
      value <- combineVal(v1.value, v2.value)
      numBytes <- Join(v1.numBytes, v2.numBytes)
      byteOrder <- Join(v1.byteOrder, v2.byteOrder)
    } yield(Bytes(value, numBytes, byteOrder))
