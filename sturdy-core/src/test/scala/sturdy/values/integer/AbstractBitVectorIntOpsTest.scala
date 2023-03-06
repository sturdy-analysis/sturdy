package sturdy.values.integer

import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ArrayBuffer
import sturdy.effect.EffectStack
import sturdy.effect.failure.{CFailureException, ConcreteFailure, Failure}
import sturdy.values.integer.AbstractBit.*
import sturdy.values.integer.AbstractBitVector.*


class AbstractBitVectorIntOpsTest extends AnyFunSuite:

  given Failure = ConcreteFailure() //sturdy/effect/failure/concretefailure
  given EffectStack = EffectStack(List()) //sturdy/effect/effectstack

  val con: ConcreteIntegerOps = new ConcreteIntegerOps()
  val abs: AbstractBitVectorIntegerOps[Int] = new AbstractBitVectorIntegerOps[Int]()
  val ord: AbstractBitVectorOrdering[Int] = new AbstractBitVectorOrdering[Int]()

  private def assertContains(abstractPairs: List[(AbstractBitVector[Int], AbstractBitVector[Int])]): Unit = {
    assert(abstractPairs.forall((x, y) =>
      if (ord.lteq(x, y)) true
      else fail(f"$x is not subtype of $y")
    ))
  }

  private def testSoundnessOfFunctionDoubleArg(concreteMethod: (Int, Int) => Int, abstractMethod: (AbstractBitVector[Int], AbstractBitVector[Int]) => AbstractBitVector[Int], withZero: Boolean, shortened: Boolean): Unit = {
    val pairs: List[(Set[Int], Set[Int])] = if (shortened) (if (withZero) tuplesWithZeroShort else tuplesWithoutZeroShort) else (if (withZero) tuplesWithZero else tuplesWithoutZero)
    assert(pairs.forall((x: Set[Int], y: Set[Int]) =>
      val conV: AbstractBitVector[Int] = abs.joinMultipleAbstractBitVectors(for(a <- x; b <- y) yield abs.integerLit(concreteMethod(a, b)))
      val absV: AbstractBitVector[Int] = abstractMethod(abs.joinMultipleAbstractBitVectors(x.map(a => abs.integerLit(a))), abs.joinMultipleAbstractBitVectors(y.map(abs.integerLit)))
      if (ord.lteq(conV, absV)) true
      else fail(f"$conV is not subtype of $absV for $x and $y")
    ))
  }

  private def testSoundnessOfFunctionSingleArg(concreteMethod: Int => Int, abstractMethod: AbstractBitVector[Int] => AbstractBitVector[Int]): Unit = {
    val pairs: List[Set[Int]] = monoWithZero
    assert(pairs.forall((x: Set[Int]) =>
      val conV: AbstractBitVector[Int] = abs.joinMultipleAbstractBitVectors(for (a <- x) yield abs.integerLit(concreteMethod(a)))
      val absV: AbstractBitVector[Int] = abstractMethod(abs.joinMultipleAbstractBitVectors(x.map(a => abs.integerLit(a))))
      if (ord.lteq(conV, absV)) true
      else fail(f"$conV is not subtype of $absV for $x")
    ))
  }

  private val mix: AbstractBitVector[Int] = abs.joinMultipleAbstractBitVectors(Seq(constant(2), constant(6), constant(12)))

  test("testMultipleJoin") {
    assertResult(AbstractBitVector(Array(Zer, Bit, Bit, Bit, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer, Zer)))(mix)
  }

  test("testCountOfNums") {
    assertResult(8)(mix.countOfNums)
  }

  test("testAbstractBitVectorToList") {
    assertResult(ArrayBuffer(0, 2, 4, 6, 8, 10, 12, 14))(abs.AbstractBitVectorToList(mix))
  }

  private val tuplesWithoutZero: List[(Set[Int], Set[Int])] = constructPairsDoubleArg(false, false)
  private val tuplesWithoutZeroShort: List[(Set[Int], Set[Int])] = constructPairsDoubleArg(false, true)
  private val tuplesWithZero: List[(Set[Int], Set[Int])] = constructPairsDoubleArg(true, false)
  private val tuplesWithZeroShort: List[(Set[Int], Set[Int])] = constructPairsDoubleArg(true, true)
  private val monoWithZero: List[Set[Int]] = constructPairsSingleArg(true, false)

  private def constructPairsSingleArg(withZero: Boolean, shortened: Boolean): List[Set[Int]] = {
    val nonZero: Set[Int] = if (shortened) Set(-2147483647, -2048, -2, 1, 13241241) else Set(-2147483647, -13241241, -2048, -2, -1, 1, 2, 2048, 13241241, 2147483647)
    val values: Set[Int] = if(withZero) nonZero ++ Set(0) else nonZero
    val pairs = for(x <- values; y <- values) yield Set(x, y)
    val triple = for(x <- values; y <- values; z <- values) yield Set(x, y, z)
    (pairs ++ triple).toList
  }

  private def constructPairsDoubleArg(withZero: Boolean, shortened: Boolean): List[(Set[Int], Set[Int])] = {
    val values = constructPairsSingleArg(withZero, shortened)
    for(x <- values; y <- values) yield (x,y)
  }

  test("testAdd") {
    testSoundnessOfFunctionDoubleArg(con.add, abs.add, true, false)
  }

  test("testSub") {
    testSoundnessOfFunctionDoubleArg(con.sub, abs.sub, true,false )
  }

  test("testMul") {
    testSoundnessOfFunctionDoubleArg(con.mul, abs.mul, true, false)
  }

  test("testMax") {
    testSoundnessOfFunctionDoubleArg(con.max, abs.max, true, false)
  }

  test("testMin") {
    testSoundnessOfFunctionDoubleArg(con.min, abs.min, true, false)
  }

  test("testDiv") {
    testSoundnessOfFunctionDoubleArg(con.div, abs.div, false, false)
  }

  test("testDivFail") {
    assertThrows[CFailureException](testSoundnessOfFunctionDoubleArg(con.div, abs.div, true, false))
  }

  test("testDivUnsigned") {
    testSoundnessOfFunctionDoubleArg(con.divUnsigned, abs.divUnsigned, false, true)
  }

  test("testDivUnsignedFail") {
    assertThrows[CFailureException](testSoundnessOfFunctionDoubleArg(con.divUnsigned, abs.divUnsigned, true, true))
  }

  test("testRemainder") {
    testSoundnessOfFunctionDoubleArg(con.remainder, abs.remainder, false, false)
  }

  test("testRemainderFail") {
    assertThrows[CFailureException](testSoundnessOfFunctionDoubleArg(con.remainder, abs.remainder, true, false))
  }

  test("testRemainderUnsigned") {
    testSoundnessOfFunctionDoubleArg(con.remainderUnsigned, abs.remainderUnsigned, false, false)
  }

  test("testRemainderUnsignedFail") {
    assertThrows[CFailureException](testSoundnessOfFunctionDoubleArg(con.remainderUnsigned, abs.remainderUnsigned, true, false))
  }

  test("testModulo") {
    testSoundnessOfFunctionDoubleArg(con.modulo, abs.modulo, false, true)
  }

  test("testModuloFail") {
    assertThrows[CFailureException](testSoundnessOfFunctionDoubleArg(con.modulo, abs.modulo, true, true))
  }

  /**test("testGCD") {
    testSoundnessOfFunctionDoubleArg(con.gcd, abs.gcd, true, false)
  }**/

  test("testAbsolute") {
    testSoundnessOfFunctionSingleArg(con.absolute, abs.absolute)
  }

  test("testBitAnd") {
    testSoundnessOfFunctionDoubleArg(con.bitAnd, abs.bitAnd, true, false)
  }

  test("testBitOr") {
    testSoundnessOfFunctionDoubleArg(con.bitOr, abs.bitOr, true, false)
  }

  test("testBitXor") {
    testSoundnessOfFunctionDoubleArg(con.bitXor, abs.bitXor, true, false)
  }

  test("testShiftLeft") {
    testSoundnessOfFunctionDoubleArg(con.shiftLeft, abs.shiftLeft, true, false)
  }

  test("testShiftRight") {
    testSoundnessOfFunctionDoubleArg(con.shiftRight, abs.shiftRight, true, false)
  }

  test("testRotateLeft") {
    testSoundnessOfFunctionDoubleArg(con.rotateRight, abs.rotateRight, true, false)
  }

  test("testRotateRight") {
    testSoundnessOfFunctionDoubleArg(con.rotateLeft, abs.rotateLeft, true, false)
  }

  test("testCountLeadingZeros") {
    testSoundnessOfFunctionSingleArg(con.countLeadingZeros, abs.countLeadingZeros)
  }

  test("testCountTrailingZeros") {
    testSoundnessOfFunctionSingleArg(con.countTrailingZeros, abs.countTrailingZeros)
  }

  test("testNonzeroBitCount") {
    testSoundnessOfFunctionSingleArg(con.nonzeroBitCount, abs.nonzeroBitCount)
  }

  test("testInvertBits") {
    testSoundnessOfFunctionSingleArg(con.invertBits, abs.invertBits)
  }
