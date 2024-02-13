package sturdy.values.integer

import org.scalacheck.{Gen, Shrink}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.utils.GenInterval.{*,given}

trait IntervalIntegerOps[L,N] extends IntegerOps[L,N]:
  def integerLit(i: Int): N
  def interval(low: Int, high: Int): N
  def getBounds(n:N): (Int,Int)

class IntegerOpsTest[L,N](size: Int,
                          makeIntegerOps: => IntervalIntegerOps[L, N])
    extends AnyFunSuite with ScalaCheckPropertyChecks:


  test("integer literal") {
    forAll("n") { (n: Int) =>
      val integerOps = makeIntegerOps
      integerOps.getBounds(integerOps.integerLit(n)) shouldBe (n,n)
    }

  }

  binOpTest(
    testName = "add",
    precondition = (_, _) => true,
    testFun = _.add(_, _),
    expectedFun = ConcreteIntegerOps.add(_, _)
  )

  binOpTest(
    testName = "sub",
    precondition = (_, _) => true,
    testFun = _.sub(_, _),
    expectedFun = ConcreteIntegerOps.sub(_, _)
  )

  binOpTest(
    testName = "mul",
    precondition = (_, _) => true,
    testFun = _.mul(_, _),
    expectedFun = ConcreteIntegerOps.mul(_, _)
  )

  binOpTest(
    testName = "min",
    precondition = (_, _) => true,
    testFun = _.min(_, _),
    expectedFun = ConcreteIntegerOps.min(_, _)
  )

  test("min([0,0],[-1,0])") {
      val integerOps = makeIntegerOps
      integerOps.getBounds(integerOps.min(integerOps.interval(0, 0), integerOps.interval(-1, 0))) shouldBe (-1,0)
  }

  binOpTest(
    testName = "max",
    precondition = (_, _) => true,
    testFun = _.max(_, _),
    expectedFun = ConcreteIntegerOps.max(_, _)
  )

  unOpTest(
    testName = "absolute",
    precondition = _ => true,
    testFun = _.absolute(_),
    expectedFun = ConcreteIntegerOps.absolute(_)
  )

  binOpTest(
    testName = "div",
    precondition = (_, y) => y != 0,
    testFun = _.div(_, _),
    expectedFun = ConcreteIntegerOps.div(_, _)
  )

  test("divide by zero") {
    val integerOps = makeIntegerOps
    integerOps.getBounds(integerOps.div(integerOps.interval(1, 1), integerOps.interval(-1, 1))) should contain(ConcreteIntegerOps.div(1,-1),ConcreteIntegerOps.div(1,1))
  }

  test("divide [-1,1] / [-1,-1]") {
    val integerOps = makeIntegerOps
    val result = integerOps.getBounds(integerOps.div(integerOps.interval(-1, 1), integerOps.interval(-1, -1))) shouldBe (-1,1)
  }

  binOpTest(
    testName = "divUnsigned",
    precondition = (_, y) => y != 0,
    testFun = _.divUnsigned(_, _),
    expectedFun = ConcreteIntegerOps.divUnsigned(_, _)
  )

  binOpTest(
    testName = "remainder",
    precondition = (_, y) => y != 0,
    testFun = _.remainder(_, _),
    expectedFun = ConcreteIntegerOps.remainder(_, _)
  )

  binOpTest(
    testName = "remainderUnsigned",
    precondition = (_, y) => y != 0,
    testFun = _.remainderUnsigned(_, _),
    expectedFun = ConcreteIntegerOps.remainderUnsigned(_, _)
  )

  binOpTest(
    testName = "modulo",
    precondition = (_, y) => y != 0,
    testFun = _.modulo(_, _),
    expectedFun = ConcreteIntegerOps.modulo(_, _)
  )

  binOpTest(
    testName = "shiftLeft",
    precondition = (_, _) => true,
    testFun = _.shiftLeft(_, _),
    expectedFun = ConcreteIntegerOps.shiftLeft(_, _)
  )

  binOpTest(
    testName = "shiftRight",
    precondition = (_, _) => true,
    testFun = _.shiftRight(_, _),
    expectedFun = ConcreteIntegerOps.shiftRight(_, _)
  )

  binOpTest(
    testName = "shiftRightUnsigned",
    precondition = (_, _) => true,
    testFun = _.shiftRightUnsigned(_,_),
    expectedFun = ConcreteIntegerOps.shiftRightUnsigned(_,_)
  )

  unOpTest(
    testName = "countLeadingZeros",
    precondition = _ => true,
    testFun = _.countLeadingZeros(_),
    expectedFun = ConcreteIntegerOps.countLeadingZeros(_)
  )

  test("countLeadingZeros([1,4])") {
    val integerOps: IntervalIntegerOps[L, N] = makeIntegerOps
    integerOps.getBounds(integerOps.countLeadingZeros(integerOps.interval(2, 4))) shouldBe (ConcreteIntegerOps.countLeadingZeros(4), ConcreteIntegerOps.countLeadingZeros(2))
  }

  def binOpTest(testName: String, precondition: (Int,Int) => Boolean, testFun: (IntegerOps[L,N],N,N) => N, expectedFun: (Int,Int) => Int) =
    test(testName) {
      forAll((genInterval(size), "x ∈ [x1,x2]"), (genInterval(size), "y ∈ [y1,y2]")) {
        case (Interval(x1, x, x2), Interval(y1, y, y2)) =>
          whenever(precondition(x,y)) {
            val integerOps: IntervalIntegerOps[L,N] = makeIntegerOps
            integerOps.getBounds(testFun(integerOps, integerOps.interval(x1, x2), integerOps.interval(y1, y2))) should contain(expectedFun(x, y))
          }
      }
    }

  def unOpTest(testName: String, precondition: Int => Boolean, testFun: (IntegerOps[L,N],N) => N, expectedFun: Int => Int) =
    test(testName) {
      forAll((genInterval(size), "x ∈ [x1,x2]")) {
        case Interval(x1, x, x2) =>
          whenever(precondition(x)) {
            val integerOps: IntervalIntegerOps[L, N] = makeIntegerOps
            integerOps.getBounds(testFun(integerOps, integerOps.interval(x1, x2))) should contain(expectedFun(x))
          }
      }
    }
  def contain(expected: Int): Matcher[(Int,Int)] =
    contain(expected,expected)

  def contain(expected_low: Int, expected_high: Int): Matcher[(Int, Int)] =
    (actual: (Int, Int)) =>
      MatchResult(
        actual._1 <= expected_low && expected_high <= actual._2,
        s"interval $actual does not contain ${(expected_low,expected_high)}",
        s"interval $actual contains ${(expected_low,expected_high)}"
      )