package sturdy.values.integer

import apron.*
import org.scalacheck.Gen
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.apron.{*, given}
import sturdy.effect.{EffectStack, Stateless}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.util.{Lazy, lazily}
import sturdy.values.*
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}
import sturdy.utils.TestTypes.{*, given}
import sturdy.utils.TestContexts.{*, given}

type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]

class RelationalIntOpsTest extends IntegerOpsTest[Int, ApronExpr[VirtAddr, Type]](
  minValue = Integer.MIN_VALUE,
  maxValue = Integer.MAX_VALUE,
  makeIntegerOps = {
    given apronManager: Manager = new apron.Polka(true)
    var apronState: ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]] = null
    given effectStack: EffectStack = new EffectStack(
      RecencyClosure(apronState.recencyStore)
    )
    apronState = RecencyRelationalStore[Ctx, Type]
    given ApronState[VirtAddr, Type] = apronState
    val lazyApronState: Lazy[ApronState[VirtAddr, Type]] = lazily(apronState)
    val intType: Type = Type.IntType(BaseType[Int])
    new RelationalIntOps[VirtAddr, Type] with TestingIntegerOps[Int, ApronExpr[VirtAddr, Type]] {
      override def integerLit(i: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intLit(i, intType)
      override def interval(low: Int, high: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intInterval(low, high, intType)
      override def getBounds(n: ApronExpr[VirtAddr, Type]): (Int, Int) =
        this.apronState.getBigIntInterval(n) match
          case (Some(l),Some(u)) if (Integer.MIN_VALUE <= l && u <= Integer.MAX_VALUE) => (l.intValue, u.intValue)
          case iv => throw new IllegalArgumentException(s"Interval $iv out of integer bounds ${(Integer.MIN_VALUE, Integer.MAX_VALUE)}")
    }
  }
)
//  test("-1 + -2147483648 = 2147483647") {
//    val integerOps = newIntegerOps
//    val x = integerOps.integerLit(-1)
//    val y = integerOps.integerLit(-2147483648)
//    val add = integerOps.add(x, y)
//    integerOps.getBounds(add) shouldBe (2147483647, 2147483647)
//  }
//
//  test("-1 mod 4294967296 = 4294967295") {
//    val integerOps = newIntegerOps
//    val x = integerOps.integerLit(-1)
//    val y = ApronExpr.bigIntLit[VirtAddr, Type](4294967296L, Type.IntType(BaseType[Int]))
//    val mod = integerOps.modulo(x, y)
//    val expected = 4294967295L
//    integerOps.getBounds(mod) shouldBe(expected, expected)
//  }
//
//  test("-1 mod 10 = 9") {
//    val integerOps = newIntegerOps
//    val x = integerOps.integerLit(-1)
//    val y = integerOps.integerLit(10)
//    val mod = integerOps.modulo(x, y)
//    val expected = java.lang.Math.floorMod(-1,10)
//    integerOps.getBounds(mod) shouldBe(expected, expected)
//  }


class RelationalLongOpsTest extends IntegerOpsTest[Long, ApronExpr[VirtAddr, Type]](
  minValue = Long.MinValue,
  maxValue = Long.MaxValue,
  makeIntegerOps = {
    given apronManager: Manager = new apron.Polka(true)
    var apronState: ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]] = null
    given effectStack: EffectStack = new EffectStack(
      RecencyClosure(apronState.recencyStore)
    )
    apronState = RecencyRelationalStore[Ctx, Type]
    given ApronState[VirtAddr, Type] = apronState
    val lazyApronState: Lazy[ApronState[VirtAddr, Type]] = lazily(apronState)
    val longType: Type = Type.LongType(BaseType[Long])
    new RelationalLongOps[VirtAddr, Type] with TestingIntegerOps[Long, ApronExpr[VirtAddr, Type]] {
      override def integerLit(i: Long): ApronExpr[VirtAddr, Type] = ApronExpr.longLit(i, longType)
      override def interval(low: Long, high: Long): ApronExpr[VirtAddr, Type] = ApronExpr.longInterval(low, high, longType)
      override def getBounds(n: ApronExpr[VirtAddr, Type]): (Long, Long) =
        this.apronState.getBigIntInterval(n) match
          case (Some(l), Some(u)) =>
            if (Long.MinValue <= l && u <= Long.MaxValue)
              (l.longValue, u.longValue)
            else
              throw new IllegalArgumentException(s"Interval ${(l,u)} out of integer bounds ${(Long.MinValue, Long.MaxValue)}")
          case iv =>
            throw new IllegalArgumentException(s"Unexpected infinite bounds ${iv}")
    }
  }
)

class RelationalIntOpsModelsTest extends AnyFunSuite with ScalaCheckPropertyChecks:
  def chooseInt: Gen[Int] = Gen.chooseNum(Integer.MIN_VALUE, Integer.MAX_VALUE)

  test("Model.add(x,y) = x + y") {
    forAll((chooseInt, "x"), (chooseInt, "y")) { (x: Int, y: Int) =>
      Model.add(x, y) shouldBe x + y
    }
  }

  test("Model.add(-1,-2147483648) = 2147483647") {
    val x = -1
    val y = -2147483648
    Model.add(x, y) shouldBe x + y
  }

  test("Model.sub(x,y) = x - y") {
    forAll((chooseInt, "x"), (chooseInt, "y")) { (x: Int, y: Int) =>
      Model.sub(x, y) shouldBe x - y
    }
  }

  test("Model.mul(x,y) = x * y") {
    forAll((chooseInt, "x"), (chooseInt, "y")) { (x: Int, y: Int) =>
      Model.mul(x, y) shouldBe x * y
    }
  }

  test("fromUnsigned(toUnsigned(x)) = x") {
    forAll((chooseInt,"x")) { (x: Int) =>
      Model.toSigned(Model.toUnsigned(x)) shouldBe x
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

  test("Model.shiftLeft(x,shift) = x << shift") {
    forAll((chooseInt, "x"), (chooseInt, "shift")) { (x: Int, shift: Int) =>
      Model.shiftLeft(x, shift) shouldBe x << shift
    }
  }

  test("Model.shiftRight(x,shift) = x >> shift") {
    forAll((chooseInt, "x"), (chooseInt, "shift")) { (x: Int, shift: Int) =>
      Model.shiftRight(x, shift) shouldBe x >> shift
    }
  }

  object Model:

    def toUnsigned(x: BigInt): BigInt =
      x - (-BigInt(2).pow(Integer.BYTES * 8 - 1))

    def toSigned(x: BigInt): BigInt =
      x + (-BigInt(2).pow(Integer.BYTES * 8 - 1))

    def toIntegerRange(x: BigInt): BigInt =
      val unsignedMaxValue = BigInt(2).pow(Integer.BYTES * 8)
      toSigned(toUnsigned(x).mod(unsignedMaxValue))

    def interpretSignedAsUnsigned(x: BigInt): BigInt =
      val unsignedMaxValue = math.pow(2, Integer.BYTES * 8).longValue()
      if (x < 0)
        x + unsignedMaxValue
      else
        x

    def interpretUnsignedAsUnsigned(x: BigInt): BigInt =
      val unsignedMaxValue = math.pow(2, Integer.BYTES * 8).longValue()
      val signedMaxValue = math.pow(2, Integer.BYTES * 8 - 1).longValue() - 1
      if (x > signedMaxValue)
        x - unsignedMaxValue
      else
        x.intValue()


    def add(x: BigInt, y: BigInt): BigInt =
      toIntegerRange(x + y)

    def sub(x: BigInt, y: BigInt): BigInt =
      toIntegerRange(x - y)

    def mul(x: BigInt, y: BigInt): BigInt =
      toIntegerRange(x * y)


    def divideUnsigned(x: BigInt, y: BigInt): BigInt =
      interpretUnsignedAsUnsigned(interpretSignedAsUnsigned(x) / interpretSignedAsUnsigned(y))

    def remainderUnsigned(x: BigInt, y: BigInt): BigInt =
      interpretUnsignedAsUnsigned(interpretSignedAsUnsigned(x) % interpretSignedAsUnsigned(y))

    def modulo(n: Int, m: Int): Int =
      val r = n % m
      if (r < 0)
        r + m
      else
        r

    def shiftLeft(n: BigInt, shift: Int): BigInt =
      toIntegerRange(n * BigInt(2).pow(modulo(shift, 32)))

    def shiftRight(n: BigInt, shift: Int): BigInt =
      n / BigInt(2).pow(modulo(shift, 32))


