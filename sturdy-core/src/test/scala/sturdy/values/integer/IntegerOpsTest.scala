package sturdy.values.integer

import org.scalacheck.Gen
import org.scalatest.funsuite.AnyFunSuite
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
        y <- Gen.choose(x, size))
      yield (x,y)

  test("integer literal") {
    forAll("n") { (n: Int) =>
      val integerOps = makeIntegerOps
      integerOps.getBounds(integerOps.integerLit(n)) shouldBe (n,n)
    }

  }

  test("addition") {
    forAll(genInterval, genInterval) {
      case ((x1,x2), (y1,y2)) =>
        val integerOps = makeIntegerOps
        integerOps.getBounds(integerOps.add(integerOps.interval(x1,x2), integerOps.interval(y1,y2))) shouldBe
          (x1+y1, x2+y2)
    }
  }

  test("multiplication") {
    forAll(genInterval, genInterval) {
      case ((x1, x2), (y1, y2)) =>
        val integerOps = makeIntegerOps
        val v1 = x1 * y1
        val v2 = x1 * y2
        val v3 = x2 * y1
        val v4 = x2 * y2
        integerOps.getBounds(integerOps.mul(integerOps.interval(x1, x2), integerOps.interval(y1, y2))) shouldBe
          (List(v1,v2,v3,v4).min, List(v1,v2,v3,v4).max)
    }
  }

  test("min") {
    forAll(genInterval, genInterval) {
      case ((x1, x2), (y1, y2)) =>
        val integerOps = makeIntegerOps
        integerOps.getBounds(integerOps.min(integerOps.interval(x1, x2), integerOps.interval(y1, y2))) shouldBe
          (math.min(x1, y1), math.min(x2, y2))
    }
  }

  test("max") {
    forAll(genInterval, genInterval) {
      case ((x1, x2), (y1, y2)) =>
        val integerOps = makeIntegerOps
        integerOps.getBounds(integerOps.max(integerOps.interval(x1, x2), integerOps.interval(y1, y2))) shouldBe
          (math.max(x1, y1), math.max(x2, y2))
    }
  }

  test("absolute") {
    forAll(genInterval) {
      case (x1, x2) =>
        val integerOps = makeIntegerOps

        integerOps.getBounds(integerOps.absolute(integerOps.interval(x1, x2))) shouldBe {
          if (x1 <= 0 && 0 <= x2 )
            (0, math.max(math.abs(x1), math.abs(x2)))
          else if(x1 <= 0 && x2 <= 0)
            (math.min(math.abs(x1), math.abs(x2)), math.max(math.abs(x1), math.abs(x2)))
          else
            (x1, x2)
        }
    }
  }