package sturdy.values.integer

import sturdy.data.*
import sturdy.effect.EffectStack
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.values.*
import sturdy.values.booleans.*
import sturdy.values.config.{Bits, UnsupportedConfiguration}
import sturdy.values.convert.*
import sturdy.values.integer.AbstractBitVector.*
// import sturdy.values.relational.*
import sturdy.{AbstractlySound, IsSound, Soundness, given}

import java.lang.Math
import java.nio.file.{Path, Paths}
import java.nio.{ByteBuffer, ByteOrder}
import scala.annotation.tailrec
import scala.collection.immutable.{AbstractSeq, LinearSeq, TreeSet}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.math.*
import scala.math.Integral.Implicits.infixIntegralOps
import scala.math.Numeric.Implicits.infixNumericOps
import scala.math.Ordering.Implicits.infixOrderingOps
import scala.util.control.Breaks.{break, breakable}


object AbstractBitVectorTestMain:
  def main(args: Array[String]): Unit = {
    //abstractBitTest(args)
    //abstractBitVectorTest(args)
    abstractBitVectorIntOpsTest(args)
    //abstractOrderingTest(args)
  }
  def abstractBitTest(args: Array[String]): Unit = {
    import sturdy.values.integer.AbstractBit.*
    var firstBit = Bit.fromBoolean(Topped.Actual(false))
    var secondBit = Bit.fromBoolean(Topped.Top)
    println(s"False: $firstBit")
    println(s"Top: $secondBit")
    println(firstBit.toBoolean)
    println(secondBit.toBoolean)
    println(firstBit.and(secondBit))
    println(firstBit.or(secondBit))
    println(secondBit.not)
    println(firstBit.xor(secondBit))
    println(firstBit.add(secondBit))
    println(firstBit.add(One,One))
    println(Zer.subtypeOf(One))
    println(Zer.subtypeOf(Zer))
    println(Bit.subtypeOf(One))
    println(Zer.subtypeOf(Bit))
    println(One.joinWith(Zer))
    println(Zer.lt(One))
    println(One.lt(One))
    println(One.lt(Bit))
    println(Bit.lt(One))
    println(Zer.le(One))
    println(Zer.le(Zer))
    println(Bit.le(One))
    println(One.le(Zer))
    println(One.le(Bit))
    println(One.equ(Bit))
    println(One.equ(One))
    println(Bit.equ(Bit))
    println(One.neq(Zer))
    println(One.neq(Bit))
  }

  def abstractBitVectorTest(args: Array[String]): Unit = {
    import sturdy.values.integer.AbstractBit.*
    val numInt = constant(6)
    val numInt2 = constant(4)
    val numLong = constant((-50).toLong)
    println(numInt2)
    println(numInt2.containsNum(3))
    val joined = AbstractBitVectorJoin[Int](numInt,numInt2).get
    println(joined)
  }

  def abstractBitVectorIntOpsTest(args: Array[String]): Unit = {
    import sturdy.values.integer.AbstractBitVectorIntegerOpsTest
    val testObject: AbstractBitVectorIntegerOpsTest = new AbstractBitVectorIntegerOpsTest
      /*testObject.multipleJointest()
      testObject.integerLitTest()
      testObject.randomIntegerTest()
      testObject.addTest()
      testObject.subTest()*/
      //testObject.mulTest()
      /*testObject.maxTest()
      testObject.minTest()*/
      /*testObject.divTest()
      testObject.divUnsignedTest()
      testObject.remainderTest()
      testObject.remainderUnsignedTest()
      testObject.moduloTest()*/
      testObject.gcdTest()
      /*testObject.absoluteTest()
      testObject.bitAndTest()
      testObject.bitOrTest()
      /*testObject.bitXorTest()*/
      testObject.shiftLeftTest()
      testObject.shiftRightTest()*/
      /*testObject.shiftRightUnsignedTest()
      testObject.rotateLeftTest()
      testObject.rotateRightTest()
      testObject.countLeadingZerosTest()
      testObject.countTrailingZerosTest()
      testObject.nonzeroBitCountTest()
      testObject.invertBitsTest()
      testObject.getSignTest()
      testObject.copySignTest()*/
  }

  def abstractOrderingTest(strings: Array[String]): Unit = {
    import sturdy.values.integer.AbstractOrderingTest
    val testObject: AbstractOrderingTest = new AbstractOrderingTest
    /*testObject.lteqTest()
    testObject.ltTest()
    testObject.leTest()
    testObject.ltUnsignedTest()
    testObject.leUnsignedTest()
    testObject.equTest()
    testObject.neqTest()*/
  }

class AbstractBitVectorIntegerOpsTest:
  // TODO: somehow implement Failure and EffectStack
  given Failure = ConcreteFailure() //sturdy/effect/failure/concretefailure
  given EffectStack = EffectStack() //sturdy/effect/effectstack
  val con: ConcreteIntegerOps = new ConcreteIntegerOps()
  val abs: AbstractBitVectorIntegerOps[Int] = new AbstractBitVectorIntegerOps[Int]()
  val ord: AbstractBitVectorOrdering[Int] = new AbstractBitVectorOrdering[Int]()

  private def assertInList(abstractPairs: List[(AbstractBitVector[Int], AbstractBitVector[Int])]): Unit = {
    assert(abstractPairs.forall((x,y) => ord.lteq(x,y)))
  }

  private def assertSound(x: AbstractBitVector[Int], y: AbstractBitVector[Int]): Unit = {
    assert(ord.lteq(x,y))
  }

  def integerLitTest(): Unit = {
    println(f"14's integer lit is ${abs.integerLit(14)}")
  }

  def randomIntegerTest(): Unit = {
    println("Testing: randomInteger")
    println(abs.randomInteger())
    println("Finished: randomInteger ////")
  }

  def multipleJointest(): Unit = {
    val first = constant(1)
    val second = constant(5)
    val third = constant(13)
    val mix = abs.joinMultipleAbstractBitVectors(Seq(first,second,third))
    val trial = abs.AbstractBitVectorToList(mix)
    println(trial)
  }

  def addTest(): Unit = {
    //TODO: negative numbers don't seem to work
    val pairs = List((0,0), (0,1), (-1,0), (3,5), (-3,5), (-12,4), (2147483647,1))
    val abstractPairs = pairs.map((x,y) => {
      //println(f"$x + $y is concrete: ${abs.integerLit(con.add(x,y))} and abstract: ${abs.add(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.add(x,y)), abs.add(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def subTest(): Unit = {
    //TODO: 0-0 doesn't work as well as negatives
    val pairs = List((0, 0), (0, 1), (0, -1), (3, 5), (-3, 5), (12, 4), (2147483647, -1))
    val abstractPairs = pairs.map((x, y) => {
      //println(f"$x - $y is concrete: ${abs.integerLit(con.sub(x, y))} and abstract: ${abs.sub(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.sub(x, y)), abs.sub(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def mulTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(-14),abs.integerLit(-3)))
    val second = abs.integerLit(4)
    val res = abs.mul(first,second)
    val resConc = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.mul(-14,4)),abs.integerLit(con.mul(-3,4))))
    println(f"$first * $second is concrete: $resConc and abstract: $res")
    assertSound(resConc,res)
    val pairs = List((0, 1), (2, -1), (-1, 0), (3, 5), (-3, 5), (-12, -4), (2147483647, 2))
    val abstractPairs = pairs.map((x, y) => {
      //println(f"$x * $y is concrete: ${abs.integerLit(con.mul(x, y))} and abstract: ${abs.mul(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.mul(x, y)), abs.mul(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def maxTest(): Unit = {
    // seems to work fine
    val pairs = List((0, 0), (0, 1), (-1, 0), (3, 5), (-3, 5), (-12, 4), (-3, -1))
    val abstractPairs = pairs.map((x, y) => {
      println(f"max($x, $y) is concrete: ${abs.integerLit(con.max(x, y))} and abstract: ${abs.max(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.max(x, y)), abs.max(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def minTest(): Unit = {
    // seems to work fine
    val pairs = List((0, 0), (0, 1), (-1, 0), (3, 5), (-3, 5), (-12, 4), (-3, -1))
    val abstractPairs = pairs.map((x, y) => {
       println(f"min($x, $y) is concrete: ${abs.integerLit(con.min(x, y))} and abstract: ${abs.min(abs.integerLit(x), abs.integerLit(y))}")
       (abs.integerLit(con.min(x, y)), abs.min(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def divTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(14), abs.integerLit(229)))
    val second = abs.integerLit(4)
    val res = abs.div(first, second)
    val resConc = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.div(14, 4)), abs.integerLit(con.div(229, 4))))
    println(f"$first / $second is concrete: $resConc and abstract: $res")
    assertSound(resConc,res)
    val posPairs = List((0, 1), (3, 5), (-3, 5), (12, -4), (-2,-3), (2147483647, 2))
    val abstractPairs = posPairs.map((x, y) => {
      //println(f"$x / $y is concrete: ${abs.integerLit(con.div(x, y))} and abstract: ${abs.div(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.div(x, y)), abs.div(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def divTestFail(): Unit = {
    val negPairs = List((0, 0), (-1, 0))
    // TODO: implement failing tests
  }

  def divUnsignedTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(14), abs.integerLit(229)))
    val second = abs.integerLit(4)
    val res = abs.divUnsigned(first, second)
    val resConc = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.divUnsigned(14, 4)), abs.integerLit(con.divUnsigned(229, 4))))
    println(f"$first / $second unsigned is concrete: $resConc and abstract: $res")
    assertSound(resConc,res)
    val pairs = List((0, 1), (3, 5), (-3, 5), (12, -4), (-2,-3), (2147483647, 2))
    val abstractPairs = pairs.map((x, y) => {
      //println(f"$x / $y unsigned is concrete: ${abs.integerLit(con.divUnsigned(x, y))} and abstract: ${abs.divUnsigned(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.divUnsigned(x, y)), abs.divUnsigned(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def divUnsignedTestFail(): Unit = {
    val negPairs = List((0, 0), (-1, 0))
    // TODO: implement failing tests
  }

  def remainderTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(14), abs.integerLit(229)))
    val second = abs.integerLit(4)
    val res = abs.remainder(first, second)
    val resConc = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.remainder(14, 4)), abs.integerLit(con.remainder(229, 4))))
    println(f"$first rem $second is concrete: $resConc and abstract: $res")
    assertSound(resConc,res)
    val pairs = List((0, 1), (3, 5), (5, 3), (-12, 4), (2147483647, 2))
    val abstractPairs = pairs.map((x, y) => {
      //println(f"$x" + "%" + f"$y is concrete: ${abs.integerLit(con.remainder(x, y))} and abstract: ${abs.remainder(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.remainder(x, y)), abs.remainder(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def remainderTestFail(): Unit = {
    val negPairs = List((0, 0), (-1, 0))
    // TODO: implement failing tests
  }

  def remainderUnsignedTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(14), abs.integerLit(229)))
    val second = abs.integerLit(4)
    val res = abs.remainderUnsigned(first, second)
    val resConc = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.remainderUnsigned(14, 4)), abs.integerLit(con.remainderUnsigned(229, 4))))
    println(f"$first rem $second unsigned is concrete: $resConc and abstract: $res")
    assertSound(resConc,res)
    val pairs = List((0, 1), (3, 5), (5, 3), (-12, 4), (2147483647, 2))
    val abstractPairs = pairs.map((x, y) => {
      //println(f"$x" +  " % " + f"$y unsigned is concrete: ${abs.integerLit(con.remainderUnsigned(x, y))} and abstract: ${abs.remainderUnsigned(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.remainderUnsigned(x, y)), abs.remainderUnsigned(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def remainderUnsignedTestFail(): Unit = {
    val negPairs = List((0, 0), (-1, 0))
    // TODO: implement failing tests
  }

  def moduloTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(14), abs.integerLit(229)))
    val second = abs.integerLit(4)
    val res = abs.modulo(first, second)
    val resConc = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.modulo(14, 4)), abs.integerLit(con.remainderUnsigned(229, 4))))
    println(f"$first mod $second is concrete: $resConc and abstract: $res")
    assertSound(resConc,res)
    val pairs = List((0, 1), (3, -5), (25, 5), (-12, 4), (-13,-5), (2147483647, 13241241))
    val abstractPairs = pairs.map((x, y) => {
      //println(f"$x modulo $y is concrete: ${abs.integerLit(con.modulo(x, y))} and abstract: ${abs.modulo(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.modulo(x, y)), abs.modulo(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def gcdTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(54),abs.integerLit(62)))
    val second = abs.integerLit(24)
    val res = abs.gcd(first,second)
    val resC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.gcd(54,24)),abs.integerLit(con.gcd(62,24))))
    println(f"gcd($first,$second) is concrete: $resC and abstract: $res")
    assertSound(resC,res)
    val pairs = List((0, 1), (3, 5), (25, 5), (-12, 4), (2147483647, 13241241))
    val abstractPairs = pairs.map((x, y) => {
      //println(f"gcd($x,$y) is concrete: ${abs.integerLit(con.gcd(x, y))} and abstract: ${abs.gcd(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.gcd(x, y)), abs.gcd(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)
  }

  def absoluteTest(): Unit = {
    val test = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(25), abs.integerLit(-11118)))
    val absolute = abs.absolute(test)
    val absoluteC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.absolute(3)),abs.integerLit(con.absolute(25)),abs.integerLit(con.absolute(-11118))))
    println(f"$absolute")
    println(f"$absoluteC")
    assertSound(absoluteC,absolute)
    val pairs = List((0), (3), (-5), (-12), (-2147483647))
    val abstractPairs = pairs.map((x) => {
      //println(f"The absolute of $x is concrete: ${abs.integerLit(con.absolute(x))} and abstract: ${abs.absolute(abs.integerLit(x))}")
      (abs.integerLit(con.absolute(x)), abs.absolute(abs.integerLit(x)))
    })
    assertInList(abstractPairs)
  }

  def bitAndTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(25), abs.integerLit(-11118)))
    val second = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(1),abs.integerLit(5)))
    val and = abs.bitAnd(first,second)
    val andC = abs.joinMultipleAbstractBitVectors(Seq(
      abs.integerLit(con.bitAnd(3,1)), abs.integerLit(con.bitAnd(25,1)), abs.integerLit(con.bitAnd(-11118,1)),
      abs.integerLit(con.bitAnd(3,5)), abs.integerLit(con.bitAnd(25,5)), abs.integerLit(con.bitAnd(-11118,25))))
    println(f"$first, $second")
    println(f"$and")
    println(f"$andC")
    /*val pairs = List((0, 1), (3, 5), (25, 5), (-12, 4), (2147483647, 13241241))
    val abstractPairs = pairs.map((x, y) => {
      println(f"$x bit and $y is concrete: ${abs.integerLit(con.bitAnd(x, y))} and abstract: ${abs.bitAnd(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.bitAnd(x, y)), abs.bitAnd(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)*/
  }

  def bitOrTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(25), abs.integerLit(-11118)))
    val second = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(1), abs.integerLit(5)))
    val or = abs.bitOr(first, second)
    val orC = abs.joinMultipleAbstractBitVectors(Seq(
      abs.integerLit(con.bitOr(3, 1)), abs.integerLit(con.bitOr(25, 1)), abs.integerLit(con.bitOr(-11118, 1)),
      abs.integerLit(con.bitOr(3, 5)), abs.integerLit(con.bitOr(25, 5)), abs.integerLit(con.bitOr(-11118, 25))))
    println(f"$first, $second")
    println(f"$or")
    println(f"$orC")
    /*val pairs = List((0, 1), (3, 5), (25, 5), (-12, 4), (2147483647, 13241241))
    val abstractPairs = pairs.map((x, y) => {
      println(f"$x bit or $y is concrete: ${abs.integerLit(con.bitOr(x, y))} and abstract: ${abs.bitOr(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.bitOr(x, y)), abs.bitOr(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)*/
  }

  def bitXorTest(): Unit = {
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(25), abs.integerLit(-11118)))
    val second = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(1), abs.integerLit(5)))
    val xor = abs.bitXor(first, second)
    val xorC = abs.joinMultipleAbstractBitVectors(Seq(
      abs.integerLit(con.bitXor(3, 1)), abs.integerLit(con.bitXor(25, 1)), abs.integerLit(con.bitXor(-11118, 1)),
      abs.integerLit(con.bitXor(3, 5)), abs.integerLit(con.bitXor(25, 5)), abs.integerLit(con.bitXor(-11118, 25))))
    println(f"$first, $second")
    println(f"$xor")
    println(f"$xorC")
    /*val pairs = List((0, 1), (3, 5), (25, 5), (-12, 4), (2147483647, 13241241))
    val abstractPairs = pairs.map((x, y) => {
      println(f"$x bit xor $y is concrete: ${abs.integerLit(con.bitXor(x, y))} and abstract: ${abs.bitXor(abs.integerLit(x), abs.integerLit(y))}")
      (abs.integerLit(con.bitXor(x, y)), abs.bitXor(abs.integerLit(x), abs.integerLit(y)))
    })
    assertInList(abstractPairs)*/
  }

  def shiftLeftTest(): Unit = {
    println("Testing: shiftLeft")
    val first = abs.integerLit(-2)
    val shift = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3),abs.integerLit(1)))
    val shifted = abs.shiftLeft(first,shift)
    val shiftC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.shiftLeft(-2,3)),abs.integerLit(con.shiftLeft(-2,1))))
    println(f"$first, $shift:")
    println(f"abstract: $shifted")
    println(f"concrete: $shiftC")
    println("Finished: shiftLeft ////")
  }

  def shiftRightTest(): Unit = {
    println("Testing: shiftRight")
    val first = abs.integerLit(-2)
    val shift = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(1)))
    val shifted = abs.shiftRight(first, shift)
    val shiftC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.shiftRight(-2, 3)), abs.integerLit(con.shiftRight(-2, 1))))
    println(f"$first, $shift:")
    println(f"abstract: $shifted")
    println(f"concrete: $shiftC")
    println("Finished: shiftRight ////")
  }

  def shiftRightUnsignedTest(): Unit = {
    println("Testing: shiftRightUnsigned")
    val first = abs.integerLit(-2)
    val shift = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(1)))
    val shifted = abs.shiftRightUnsigned(first, shift)
    val shiftC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.shiftRightUnsigned(-2, 3)), abs.integerLit(con.shiftRightUnsigned(-2, 1))))
    println(f"$first, $shift:")
    println(f"abstract: $shifted")
    println(f"concrete: $shiftC")
    println("Finished: shiftRightUnsigned ////")
  }

  def rotateLeftTest(): Unit = {
    println("Testing: rotateLeft")
    val first = abs.integerLit(-2)
    val shift = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(1)))
    val shifted = abs.rotateLeft(first, shift)
    val shiftC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.rotateLeft(-2, 3)), abs.integerLit(con.rotateLeft(-2, 1))))
    println(f"$first, $shift:")
    println(f"abstract: $shifted")
    println(f"concrete: $shiftC")
    println("Finished: rotateLeft ////")
  }

  def rotateRightTest(): Unit = {
    println("Testing: rotateRight")
    val first = abs.integerLit(-2)
    val shift = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(1)))
    val shifted = abs.rotateRight(first, shift)
    val shiftC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.rotateRight(-2, 3)), abs.integerLit(con.rotateRight(-2, 1))))
    println(f"$first, $shift:")
    println(f"abstract: $shifted")
    println(f"concrete: $shiftC")
    println("Finished: rotateRight ////")
  }

  def countLeadingZerosTest(): Unit = {
    println("Testing: countLeadingZeros")
    val test = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3),abs.integerLit(25),abs.integerLit(11118)))
    val zeros = abs.countLeadingZeros(test)
    val zeroC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.countLeadingZeros(3)),abs.integerLit(con.countLeadingZeros(25)),abs.integerLit(con.countLeadingZeros(11118))))
    println(f"$test")
    println(f"$zeros")
    println(f"$zeroC")
    println("Finished: countLeadingZeros ////")
  }

  def countTrailingZerosTest(): Unit = {
    println("Testing: countTrailingZeros")
    val test = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(25), abs.integerLit(11118)))
    val zeros = abs.countTrailingZeros(test)
    val zeroC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.countTrailingZeros(3)), abs.integerLit(con.countTrailingZeros(25)), abs.integerLit(con.countTrailingZeros(11118))))
    println(f"$test")
    println(f"$zeros")
    println(f"$zeroC")
    println("Finished: countTrailingZeros ////")
  }

  def nonzeroBitCountTest(): Unit = {
    println("Testing: nonzeroBitCount")
    val test = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(25), abs.integerLit(11118)))
    val zeros = abs.nonzeroBitCount(test)
    val zeroC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.nonzeroBitCount(3)), abs.integerLit(con.nonzeroBitCount(25)), abs.integerLit(con.nonzeroBitCount(11118))))
    println(f"$test")
    println(f"$zeros")
    println(f"$zeroC")
    println("Finished: nonzeroBitCount ////")
  }

  def invertBitsTest(): Unit = {
    println("Testing: invertBits")
    val test = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(25), abs.integerLit(11118)))
    val zeros = abs.invertBits(test)
    val zeroC = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(con.invertBits(3)), abs.integerLit(con.invertBits(25)), abs.integerLit(con.invertBits(11118))))
    println(f"$test")
    println(f"$zeros")
    println(f"$zeroC")
    println("Finished: invertBits ////")
  }

  def getSignTest(): Unit = {
    val posOne = abs.getSign(abs.integerLit(3))
    val negOne = abs.getSign(abs.integerLit(-3))
    val posMul = abs.getSign(abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3),abs.integerLit(5))))
    val negMul = abs.getSign(abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(-3),abs.integerLit(-5))))
    val mixMul = abs.getSign(abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3),abs.integerLit(-5))))
    println(f"pos: $posOne, $posMul")
    println(f"neg: $negOne, $negMul")
    println(f"mix: $mixMul")
  }

  def copySignTest(): Unit = {
    val target = abs.integerLit(25)
    val posMul = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(5)))
    val negMul = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(-3), abs.integerLit(-5)))
    val mixMul = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(3), abs.integerLit(-5)))
    val posCopy = abs.copySign(target,posMul)
    val negCopy = abs.copySign(target,negMul)
    val mixCopy = abs.copySign(target,mixMul)
    println(f"pos: $target => $posCopy")
    println(f"neg: $target => $negCopy")
    println(f"mix: $target => $mixCopy")
  }

class AbstractOrderingTest:
  given Failure = ConcreteFailure() //sturdy/effect/failure/concretefailure
  given EffectStack = EffectStack() //sturdy/effect/effectstack

  val con: ConcreteIntegerOps = new ConcreteIntegerOps()
  val abs: AbstractBitVectorIntegerOps[Int] = new AbstractBitVectorIntegerOps[Int]()
  val ord: AbstractBitVectorOrdering[Int] = new AbstractBitVectorOrdering[Int]()
  val ordOps: AbstractBitVectorOrderingOps[Int] = new AbstractBitVectorOrderingOps[Int]()
  val ordUOps: AbstractBitVectorUnsignedOrderingOps[Int] = new AbstractBitVectorUnsignedOrderingOps[Int]()
  val eqOps: AbstractBitVectorEqOps[Int] = new AbstractBitVectorEqOps[Int]()

  def lteqTest(): Unit = {
    println("starting lteq test...")
    val first = abs.integerLit(3)
    val second = abs.joinMultipleAbstractBitVectors(Seq(first,abs.integerLit(13)))
    val in = abs.integerLit(5)
    val inlier = ord.lteq(in,second)
    val out = abs.integerLit(8)
    val outlier = ord.lteq(out,second)
    println(f"$second")
    println(f"$in: $inlier")
    println(f"$out: $outlier")
    println("-----")
  }

  def ltTest(): Unit = {
    println("starting lt test...")
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(5),abs.integerLit(13)))
    val smol = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(1),abs.integerLit(3)))
    val beeg = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(13),abs.integerLit(29)))
    val mix = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(8),abs.integerLit(11)))
    val smaller = ordOps.lt(smol,first)
    val bigger = ordOps.lt(beeg,first)
    val mixed = ordOps.lt(mix,first)
    println(f"$first")
    println(f"$smol, $smaller")
    println(f"$beeg, $bigger")
    println(f"$mix, $mixed")
    println("-----")
  }

  def leTest(): Unit = {
    println("starting le test...")
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(5), abs.integerLit(13)))
    val smol = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(1), abs.integerLit(3)))
    val beeg = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(13),abs.integerLit(29)))
    val mix = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(8), abs.integerLit(11)))
    val smaller = ordOps.le(smol, first)
    val bigger = ordOps.le(beeg, first)
    val mixed = ordOps.le(mix, first)
    println(f"$first")
    println(f"$smol, $smaller")
    println(f"$beeg, $bigger")
    println(f"$mix, $mixed")
    println("-----")
  }

  def ltUnsignedTest(): Unit = {
    println("starting ltunsigned test...")
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(-5), abs.integerLit(13)))
    val smol = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(1), abs.integerLit(3)))
    val beeg = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(13), abs.integerLit(29)))
    val mix = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(8), abs.integerLit(-11)))
    val smaller = ordUOps.ltUnsigned(smol, first)
    val bigger = ordUOps.ltUnsigned(beeg, first)
    val mixed = ordUOps.ltUnsigned(mix, first)
    println(f"$first")
    println(f"$smol, $smaller")
    println(f"$beeg, $bigger")
    println(f"$mix, $mixed")
    println("-----")
  }

  def leUnsignedTest(): Unit = {
    println("starting leunsigned test...")
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(-5), abs.integerLit(13)))
    val smol = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(1), abs.integerLit(3)))
    val beeg = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(13), abs.integerLit(29)))
    val mix = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(8), abs.integerLit(-11)))
    val smaller = ordUOps.leUnsigned(smol, first)
    val bigger = ordUOps.leUnsigned(beeg, first)
    val mixed = ordUOps.leUnsigned(mix, first)
    println(f"$first")
    println(f"$smol, $smaller")
    println(f"$beeg, $bigger")
    println(f"$mix, $mixed")
    println("-----")
  }

  def equTest(): Unit = {
    println("starting equ test...")
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(5), abs.integerLit(13)))
    val smol = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(1), abs.integerLit(3)))
    val beeg = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(13), abs.integerLit(29)))
    val mix = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(8), abs.integerLit(11)))
    val smaller = eqOps.equ(smol, first)
    val bigger = eqOps.equ(beeg, first)
    val mixed = eqOps.equ(mix, first)
    println(f"$first")
    println(f"$smol, $smaller")
    println(f"$beeg, $bigger")
    println(f"$mix, $mixed")
    println("-----")
  }

  def neqTest(): Unit = {
    println("starting neq test...")
    val first = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(5), abs.integerLit(13)))
    val smol = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(1), abs.integerLit(3)))
    val beeg = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(13), abs.integerLit(29)))
    val mix = abs.joinMultipleAbstractBitVectors(Seq(abs.integerLit(8), abs.integerLit(11)))
    val smaller = eqOps.neq(smol, first)
    val bigger = eqOps.neq(beeg, first)
    val mixed = eqOps.neq(mix, first)
    println(f"$first")
    println(f"$smol, $smaller")
    println(f"$beeg, $bigger")
    println(f"$mix, $mixed")
    println("-----")
  }
