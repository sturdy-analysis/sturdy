package dwarfSupport.helperTests

import org.scalatest.funsuite.AnyFunSuite


/* Function to be tested */
import swam.binary.custom.dwarf.DwarfOperationExprParser.readSLEB128

class CanReadSLEB128 extends AnyFunSuite{
  /** to see if conversion of int -> sleb128 -> int results in the same number */
  private def toSLEB128(value: Int): List[Int] = {
    var v = value
    var bytes = List.empty[Byte]
    var more = true

    while (more) {
      var byte = (v & 0x7F).toByte
      v >>= 7

      // check if we need more bytes
      val signBitSet = (byte & 0x40) != 0
      more = !((v == 0 && !signBitSet) || (v == -1 && signBitSet))

      if (more) byte = (byte | 0x80).toByte

      bytes = bytes :+ byte
    }

    bytes.map(byte => byte & 0xFF)
  }

  test("SLEB128: int -> sleb128 -> int from -100k to 100k") {
    val step = 1
    for (i <- -100000 to 100000 by step) {
      val encoded = toSLEB128(i)
      val (decoded, remaining) = readSLEB128(encoded)
      assert(decoded == i, s"Failed round-trip for $i: decoded=$decoded")
      assert(remaining.isEmpty, s"Remaining bytes not empty for $i")
    }
  }

  test("SLEB128: simple positive numbers") {
    val (v0, r0) = readSLEB128(List(0x00.toByte))
    assert(v0 == 0)
    assert(r0.isEmpty)

    val (v1, r1) = readSLEB128(List(0x01.toByte))
    assert(v1 == 1)
    assert(r1.isEmpty)
  }

  test("SLEB128: negative one") {
    val (v, r) = readSLEB128(List(0x7f.toByte))
    assert(v == -1)
    assert(r.isEmpty)
  }

  test("SLEB128: multi-byte positive") {
    val (v, r) = readSLEB128(List(0x80.toByte, 0x01.toByte))
    assert(v == 128)
    assert(r.isEmpty)
  }

  test("SLEB128: multi-byte negative") {
    val (v, r) = readSLEB128(List(0x80.toByte, 0x7f.toByte))
    assert(v == -128)
    assert(r.isEmpty)
  }

  test("SLEB128: with remaining bytes") {
    val (v, r) = readSLEB128(List(0x01, 0x9f))
    assert(v == 1)
    assert(r == List(0x9f))  // remaining bytes not part of SLEB128
  }
}
