package sturdy.values.config

import org.scalacheck.{Arbitrary, Gen}
import sturdy.values.convert.{*,given}

given ArbitraryOverflow: Arbitrary[Overflow] = Arbitrary {
  Gen.oneOf(Overflow.Allow, Overflow.Fail, Overflow.JumpToBounds)
}

given ArbitraryBits: Arbitrary[Bits] = Arbitrary {
  Gen.oneOf(Bits.Signed, Bits.Unsigned, Bits.Raw)
}

given ArbitraryByteSize: Arbitrary[BytesSize] = Arbitrary {
  Gen.oneOf(BytesSize.Byte, BytesSize.Short, BytesSize.Int, BytesSize.Long)
}

given ArbitraryNilCC: Arbitrary[NilCC.type] = Arbitrary {
  Gen.const(NilCC)
}