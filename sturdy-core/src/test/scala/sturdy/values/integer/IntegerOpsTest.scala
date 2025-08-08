package sturdy.values.integer

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.{IsSound, Soundness}
import sturdy.util.Bounded
import sturdy.util.GenInterval.{*, given}
import sturdy.util.{*, given}

import math.Ordering.Implicits.infixOrderingOps
import scala.util.Try

class IntegerOpsTest
  [
    L: Ordering: Arbitrary: Choose: Bounded,
    N
  ]
  (
    val specials: Seq[L],
    val makeIntegerOps: () => (IsInterval[L,N], IntegerOps[L, N], Soundness[L, N])
  )
  (using
   val integral: Integral[L],
   val concreteIntegerOps: IntegerOps[L, L])
  extends AnyFunSuite with ScalaCheckPropertyChecks:

  test("integer literal") {
    forAll("n") { (n: L) =>
      implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
      assertResult(IsSound.Sound)(soundness.isSound(concreteIntegerOps.integerLit(n), integerOps.integerLit(n)))
    }
  }

  binOpTest(
    testName = "add",
    precondition = (_, _) => true,
    testFun = _.add(_, _),
    expectedFun = concreteIntegerOps.add
  )

  binOpTest(
    testName = "sub",
    precondition = (_, _) => true,
    testFun = _.sub(_, _),
    expectedFun = concreteIntegerOps.sub
  )

  binOpTest(
    testName = "mul",
    precondition = (_, _) => true,
    testFun = _.mul(_, _),
    expectedFun = concreteIntegerOps.mul
  )

  binOpTest(
    testName = "max",
    precondition = (_, _) => true,
    testFun = _.max(_, _),
    expectedFun = concreteIntegerOps.max
  )

  binOpTest(
    testName = "min",
    precondition = (_, _) => true,
    testFun = _.min(_, _),
    expectedFun = concreteIntegerOps.min
  )

  unOpTest(
    testName = "absolute",
    precondition = _ => true,
    testFun = _.absolute(_),
    expectedFun = concreteIntegerOps.absolute
  )

  binOpTest(
    testName = "div",
    precondition = (_, y) => y != 0,
    testFun = _.div(_, _),
    expectedFun = concreteIntegerOps.div
  )

  binOpTest(
    testName = "divUnsigned",
    precondition = (_, y) => y != 0,
    testFun = _.divUnsigned(_, _),
    expectedFun = concreteIntegerOps.divUnsigned
  )

  binOpTest(
    testName = "remainder",
    precondition = (_, y) => y != 0,
    testFun = _.remainder(_, _),
    expectedFun = concreteIntegerOps.remainder
  )

  binOpTest(
    testName = "remainderUnsigned",
    precondition = (_, y) => y != 0,
    testFun = _.remainderUnsigned(_, _),
    expectedFun = concreteIntegerOps.remainderUnsigned
  )

  binOpTest(
    testName = "modulo",
    precondition = (_, y) => y != 0,
    testFun = _.modulo(_, _),
    expectedFun = concreteIntegerOps.modulo
  )

  binOpTest(
    testName = "gcd",
    precondition = (_,_) => true,
    testFun = _.gcd(_, _),
    expectedFun = concreteIntegerOps.gcd
  )

  binOpTest(
    testName = "bitAnd",
    precondition = (_,_) => true,
    testFun = _.bitAnd(_, _),
    expectedFun = concreteIntegerOps.bitAnd
  )

  binOpTest(
    testName = "bitOr",
    precondition = (_, _) => true,
    testFun = _.bitOr(_, _),
    expectedFun = concreteIntegerOps.bitOr
  )

  binOpTest(
    testName = "bitXor",
    precondition = (_, _) => true,
    testFun = _.bitXor(_, _),
    expectedFun = concreteIntegerOps.bitXor
  )

  binOpTest(
    testName = "shiftLeft",
    precondition = (_, _) => true,
    testFun = _.shiftLeft(_, _),
    expectedFun = concreteIntegerOps.shiftLeft
  )

  binOpTest(
    testName = "shiftRight",
    precondition = (_, _) => true,
    testFun = _.shiftRight(_, _),
    expectedFun = concreteIntegerOps.shiftRight
  )

  binOpTest(
    testName = "shiftRightUnsigned",
    precondition = (_, _) => true,
    testFun = _.shiftRightUnsigned(_,_),
    expectedFun = concreteIntegerOps.shiftRightUnsigned
  )

  binOpTest(
    testName = "rotateLeft",
    precondition = (_, _) => true,
    testFun = _.rotateLeft(_, _),
    expectedFun = concreteIntegerOps.rotateLeft
  )

  binOpTest(
    testName = "rotateRight",
    precondition = (_, _) => true,
    testFun = _.rotateRight(_, _),
    expectedFun = concreteIntegerOps.rotateRight
  )

  unOpTest(
    testName = "countLeadingZeros",
    precondition = _ => true,
    testFun = _.countLeadingZeros(_),
    expectedFun = concreteIntegerOps.countLeadingZeros
  )

  unOpTest(
    testName = "countTrailingZeros",
    precondition = _ => true,
    testFun = _.countTrailingZeros(_),
    expectedFun = concreteIntegerOps.countTrailingZeros
  )

  unOpTest(
    testName = "nonzeroBitCount",
    precondition = _ => true,
    testFun = _.nonzeroBitCount(_),
    expectedFun = concreteIntegerOps.nonzeroBitCount
  )

  unOpTest(
    testName = "invertBits",
    precondition = _ => true,
    testFun = _.invertBits(_),
    expectedFun = concreteIntegerOps.invertBits
  )


  def binOpTest(testName: String, precondition: (L,L) => Boolean, testFun: (IntegerOps[L,N],N,N) => N, expectedFun: (L,L) => L) = {
    test(testName + " constant") {
      forAll(
        (genConstant[L](Bounded[L].minValue, Bounded[L].maxValue, specials*), "x"),
        (genConstant[L](Bounded[L].minValue, Bounded[L].maxValue, specials*), "y")
      ) {
        case (x, y) =>
          whenever(precondition(x, y)) {
            implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
            assertResult(IsSound.Sound)(soundness.isSound(
              expectedFun(x, y),
              testFun(integerOps, ivOps.constant(x), ivOps.constant(y))
            ))
          }
      }
    }

    test(testName + " intervals") {
      forAll((genInterval[L](Bounded[L].minValue,Bounded[L].maxValue, specials*), "x"), (genInterval[L](Bounded[L].minValue,Bounded[L].maxValue, specials*), "y")) {
        case (Interval(x1, x, x2, xSpecials), Interval(y1, y, y2, ySpecials)) =>
          whenever(precondition(x,y)) {
            implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
            assertResult(IsSound.Sound)(soundness.isSound(
              expectedFun(x, y),
              testFun(integerOps, ivOps.interval(x1, x2, xSpecials), ivOps.interval(y1, y2, ySpecials))
            ))
          }
      }
    }
  }

  def unOpTest(testName: String, precondition: L => Boolean, testFun: (IntegerOps[L,N],N) => N, expectedFun: L => L) = {
    test(testName + " constant") {
      forAll((genConstant[L](Bounded[L].minValue,Bounded[L].maxValue, specials*), "x")) {
        case x =>
          whenever(precondition(x)) {
            implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
            assertResult(IsSound.Sound)(soundness.isSound(
              expectedFun(x),
              testFun(integerOps, ivOps.constant(x))
            ))
          }
      }
    }

    test(testName + " interval") {
      forAll((genInterval(Bounded[L].minValue,Bounded[L].maxValue, specials*), "x")) {
        case Interval(x1, x, x2, xSpecials) =>
          whenever(precondition(x)) {
            implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
            assertResult(IsSound.Sound)(soundness.isSound(
              expectedFun(x),
              testFun(integerOps, ivOps.interval(x1, x2, xSpecials))
            ))
          }
      }
    }
  }
