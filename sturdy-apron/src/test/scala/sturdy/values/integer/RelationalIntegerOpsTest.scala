package sturdy.values.integer

import apron.*
import org.scalacheck.Gen
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.apron.{*, given}
import sturdy.effect.Stateless
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{RecencyRelationalStore, RelationalStore, RecencyStore, given}
import sturdy.values.*
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}
import sturdy.utils.TestTypes.{*, given}
import sturdy.utils.TestContexts.{*, given}

type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]

class RelationalIntegerOpsTest extends IntegerOpsTest[Int, ApronExpr[VirtAddr, Type]](
  minValue = -100,
  maxValue = 100,
  makeIntegerOps = {
    given apronManager: Manager = new apron.Polka(true)
    val (recencyStore, apronStore) = RecencyRelationalStore[Ctx, Type]
    given ApronState[VirtAddr, Type] = new ApronRecencyState(tempVariableAllocator, recencyStore, apronStore) {}
    new RelationalIntegerOps[VirtAddr, Type] with TestingIntegerOps[Int, ApronExpr[VirtAddr, Type]] {
      override def integerLit(i: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intLit(i)
      override def interval(low: Int, high: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intInterval(low, high)
      override def getBounds(n: ApronExpr[VirtAddr, Type]): (Int, Int) = apronState.getIntBound(n)
    }
  }
)

class RelationalIntegerOpsModelsTest extends AnyFunSuite with ScalaCheckPropertyChecks:
  def chooseInt: Gen[Int] = Gen.chooseNum(Integer.MIN_VALUE, Integer.MAX_VALUE)

  test("fromUnsigned(toUnsigned(x)) = x") {
    forAll((chooseInt,"x")) { (x: Int) =>
      Model.fromUnsigned(Model.toUnsigned(x)) shouldBe x
    }
  }

  test("Model.divideUnsigned(x,y) = Integer.divideUnsigned(x,y)") {
    forAll((chooseInt, "x"), (chooseInt, "y")) { (x: Int, y: Int) =>
      whenever(y != 0) {
        Model.divideUnsigned(x, y) shouldBe Integer.divideUnsigned(x, y)
      }
    }
  }

  test("Model.remainderUnsigned(x,y) = Integer.remainderUnsigned(x,y)") {
    forAll((chooseInt, "x"), (chooseInt, "y")) { (x: Int, y: Int) =>
      whenever(y != 0) {
        Model.remainderUnsigned(x, y) shouldBe Integer.remainderUnsigned(x, y)
      }
    }
  }

  test("Model.shiftLeft(x,y) = x << y") {
    forAll((chooseInt, "x"), (chooseInt, "y")) { (x: Int, y: Int) =>
      Model.shiftLeft(x, y) shouldBe x << y
    }
  }

  test("Model.shiftLeft(1,-1) = 1 << -1") {
    Model.shiftLeft(1, -1) shouldBe 1 << -1
  }

  test("Model.shiftRight(x,y) = x >> y") {
    forAll((chooseInt, "x"), (chooseInt, "y")) { (x: Int, y: Int) =>
      Model.shiftRight(x, y) shouldBe x >> y
    }
  }

  object Model:

    def toUnsigned(x: Int): Long =
      val unsignedMaxValue = math.pow(2, Integer.BYTES * 8).longValue()
      if (x < 0)
        x + unsignedMaxValue
      else
        x

    def fromUnsigned(x: Long): Int =
      val unsignedMaxValue = math.pow(2, Integer.BYTES * 8).longValue()
      val signedMaxValue = math.pow(2, Integer.BYTES * 8 - 1).longValue() - 1
      if (x > signedMaxValue)
        (x - unsignedMaxValue).intValue()
      else
        x.intValue()

    def divideUnsigned(x: Int, y: Int): Long =
      fromUnsigned(toUnsigned(x) / toUnsigned(y))

    def remainderUnsigned(x: Int, y: Int): Long =
      fromUnsigned(toUnsigned(x) % toUnsigned(y))

    def modulo(n: Int, m: Int): Int =
      val r = n % m
      if (r < 0)
        r + m
      else
        r

    def shiftLeft(n: Int, shift: Int): Int =
//      fromUnsigned(n * math.pow(2, modulo(shift, 32)).longValue())
      fromUnsigned(n * math.pow(2, modulo(shift, 32)).longValue())

    def shiftRight(n: Int, shift: Int): Int =
      (n / math.pow(2, modulo(shift, 32))).floor.intValue()


