package sturdy.values.integer

import org.scalacheck.Gen
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

trait IntervalIntegerOps[L,N] extends IntegerOps[L,N]:
  def integerLit(i: Int): N
  def interval(low: Int, high: Int): N
  def getBounds(n:N): (Int,Int)

class IntegerOpsTest[L,N](size: Int,
                          makeIntegerOps: => IntervalIntegerOps[L, N])
    extends AnyFunSuite with ScalaCheckPropertyChecks:

  def genInterval =
    for(x <- Gen.choose(-size, size);
        y <- Gen.choose(x, x+size);
        included <- Gen.choose(x,y))
      yield (x,included,y)

  test("integer literal") {
    forAll("n") { (n: Int) =>
      val integerOps = makeIntegerOps
      integerOps.getBounds(integerOps.integerLit(n)) shouldBe (n,n)
    }

  }

  test("addition") {
    forAll(genInterval, genInterval) {
      case ((x1,x,x2), (y1,y,y2)) =>
        val integerOps = makeIntegerOps
        integerOps.getBounds(integerOps.add(integerOps.interval(x1, x2), integerOps.interval(y1, y2))) should contain(x+y)
    }
  }

  test("multiplication") {
    forAll(genInterval, genInterval) {
      case ((x1, x, x2), (y1, y, y2)) =>
        val integerOps = makeIntegerOps
        integerOps.getBounds(integerOps.mul(integerOps.interval(x1, x2), integerOps.interval(y1, y2))) should contain(x*y)
    }
  }

  test("min") {
    forAll(genInterval, genInterval) {
      case ((x1, x, x2), (y1, y, y2)) =>
        val integerOps = makeIntegerOps
        integerOps.getBounds(integerOps.min(integerOps.interval(x1, x2), integerOps.interval(y1, y2))) should contain (math.min(x,y))
    }
  }

  test("max") {
    forAll(genInterval, genInterval) {
      case ((x1, x, x2), (y1, y, y2)) =>
        val integerOps = makeIntegerOps
        integerOps.getBounds(integerOps.max(integerOps.interval(x1, x2), integerOps.interval(y1, y2))) should contain (math.max(x,y))
    }
  }

  test("absolute") {
    forAll(genInterval) {
      case (x1, x, x2) =>
        val integerOps = makeIntegerOps

        integerOps.getBounds(integerOps.absolute(integerOps.interval(x1, x2))) should contain (math.abs(x))
    }
  }

  test("div") {
    forAll(genInterval, genInterval) {
      case ((x1, x, x2), (y1, y, y2)) =>
        whenever(y != 0) {
          val integerOps = makeIntegerOps
          integerOps.getBounds(integerOps.div(integerOps.interval(x1, x2), integerOps.interval(y1, y2))) should contain (x/y)
        }
    }
  }

  def contain(expected: Int): Matcher[(Int,Int)] =
    (actual: (Int, Int)) =>
      MatchResult(
        actual._1 <= expected && expected <= actual._2,
        s"$actual contains $expected",
        s"$actual does not contain $expected"
      )
