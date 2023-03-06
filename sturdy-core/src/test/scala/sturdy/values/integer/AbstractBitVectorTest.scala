package sturdy.values.integer

import org.scalatest.funsuite.AnyFunSuite

import sturdy.values.integer.AbstractBit.*
import sturdy.values.integer.AbstractBitVector.*
import sturdy.values.*


class AbstractBitVectorTest extends AnyFunSuite:

  test("testConstant") {
    assertResult(AbstractBitVector(Array(Zer, Zer, One, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer)))(constant(4))
  }

  test("test4Contains3") {
    assertResult(false)(constant(4).containsNum(3))
  }

  test("testJoin4And6") {
    assertResult(Changed(AbstractBitVector(Array(Zer, Bit, One, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer))))(AbstractBitVectorJoin[Int](constant(4), constant(6)))
  }
