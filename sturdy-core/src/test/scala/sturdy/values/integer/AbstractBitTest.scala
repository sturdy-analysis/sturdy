package sturdy.values.integer

import org.scalatest.funsuite.AnyFunSuite

import sturdy.values.integer.AbstractBit.*
import sturdy.values.*


class AbstractBitTest extends AnyFunSuite:

  test("testFalse") {
    assertResult(Zer)(Bit.fromBoolean(Topped.Actual(false)))
  }

  test("testBit") {
    assertResult(Bit)(Bit.fromBoolean(Topped.Top))
  }

  test("testFalseToBool"){
    assertResult(Topped.Actual(false))(Zer.toBoolean)
  }

  test("testBitToBool") {
    assertResult(Topped.Top)(Bit.toBoolean)
  }

  test("testZerAndBit") {
    assertResult(Zer)(Zer.and(Bit))
  }

  test("testZerOrOr") {
    assertResult(Bit)(Zer.or(Bit))
  }

  test("testBitNot") {
    assertResult(Bit)(Bit.not)
  }

  test("testZerXorBit") {
    assertResult(Bit)(Zer.xor(Bit))
  }

  test("testZerAddBitWithoutCarry") {
    assertResult((Zer, Bit))(Zer.add(Bit))
  }

  test("testZerAddOneWithCarryOne") {
    assertResult((One, Zer))(Zer.add(One, One))
  }

  test("testZerSubtypeOfOne") {
    assertResult(false)(Zer.subtypeOf(One))
  }

  test("testZerSubtypeOfZer") {
    assertResult(true)(Zer.subtypeOf(Zer))
  }

  test("testBitSubtypeOfOne") {
    assertResult(false)(Bit.subtypeOf(One))
  }

  test("testZerSubtypeOfBit") {
    assertResult(true)(Zer.subtypeOf(Bit))
  }

  test("testOneJoinWithZer") {
    assertResult(Changed(Bit))(One.joinWith(Zer))
  }

  test("testZerLessThanOne") {
    assertResult(Topped.Actual(true))(Zer.lt(One))
  }

  test("testOneLessThanOne") {
    assertResult(Topped.Actual(false))(One.lt(One))
  }

  test("testOneLessThanBit") {
    assertResult(Topped.Actual(false))(One.lt(Bit))
  }

  test("testBitLessThanOne") {
    assertResult(Topped.Top)(Bit.lt(One))
  }

  test("testZerLessOrEqualOne") {
    assertResult(Topped.Actual(true))(Zer.le(One))
  }

  test("testZerLessOrEqualZer") {
    assertResult(Topped.Actual(true))(Zer.le(Zer))
  }

  test("testBitLessOrEqualOne") {
    assertResult(Topped.Actual(true))(Bit.le(One))
  }

  test("testOneLessOrEqualZer") {
    assertResult(Topped.Actual(false))(One.le(Zer))
  }

  test("testOneLessOrEqualBit") {
    assertResult(Topped.Top)(One.le(Bit))
  }

  test("testOneEqualBit") {
    assertResult(Topped.Top)(One.equ(Bit))
  }

  test("testOneEqualOne") {
    assertResult(Topped.Actual(true))(One.equ(One))
  }

  test("testBitEqualBit") {
    assertResult(Topped.Top)(Bit.equ(Bit))
  }

  test("testOneNegatedZer") {
    assertResult(Topped.Actual(true))(One.neq(Zer))
  }

  test("testOneNegatedBit") {
    assertResult(Topped.Top)(One.neq(Bit))
  }
