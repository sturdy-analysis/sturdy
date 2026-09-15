package sturdy.values.integer

import apron.*
import org.scalacheck.Gen
import org.scalatest.{Assertion, Suites}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.IsSound
import sturdy.apron.{*, given}
import sturdy.effect.{EffectStack, Stateless}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.util.{Lazy, lazily}
import sturdy.values.*
import sturdy.values.config.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}
import sturdy.values.integer.{*, given}
import sturdy.util.{*, given}
import sturdy.util.TestTypes.{*, given}
import sturdy.util.TestContexts.{*, given}
import sturdy.util.IsInterval
import sturdy.values.convert.{*, given}
import sturdy.values.floating.FloatSpecials

type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]

class RelationalIntegerOpsTest extends Suites(
  new PolyhedraIntegerOpsTest,
  new OctagonIntegerOpsTest,
  new BoxIntegerOpsTest
)

//class ElinaPolyhedraIntegerOpsTest extends RelationalIntegerOpsTests(elina.OptPoly(false))
class PolyhedraIntegerOpsTest extends RelationalIntegerOpsTests(Polka(true))
class OctagonIntegerOpsTest extends RelationalIntegerOpsTests(Octagon())
class BoxIntegerOpsTest extends RelationalIntegerOpsTests(Box())

class RelationalIntegerOpsTests(manager: Manager) extends Suites(
  RelationalIntOpsTest(using manager),
  RelationalLongOpsTest(using manager)
)

class RelationalIntOpsTest(using Manager) extends IntegerOpsTest[Int, ApronExpr[VirtAddr, Type]](
  specials = List(Int.MinValue, -1, 0, 1, Int.MaxValue),
  makeIntegerOps = () =>
    withApronState {
      (RelationalIntInterval, new RelationalIntOps[VirtAddr, Type], implicitly)
    }
):
  test("add([233886076,2147483647],[-40746874,323133476])") {
    implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
    val actual = integerOps.add(
      ivOps.interval(integral.fromInt(233886076), integral.fromInt(2147483647)),
      ivOps.interval(integral.fromInt(-40746874), integral.fromInt(323133476))
    )
    assertResult(IsSound.Sound)(soundness.isSound(concreteIntegerOps.add(integral.fromInt(233886076), integral.fromInt(1)), actual))
  }

  test("max([-2147483648,2147483647],[-1853936221,1863658670])") {
    implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
    val actual = integerOps.max(
      ivOps.interval(integral.fromInt(-2147483648), integral.fromInt(2147483647)),
      ivOps.interval(integral.fromInt(-1853936221), integral.fromInt(1863658670))
    )
    assertResult(IsSound.Sound)(soundness.isSound(concreteIntegerOps.max(integral.fromInt(2147483647), integral.fromInt(-1635461115)), actual))
  }

  test("div([1,1],[-1,1])") {
    implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
    val actual = integerOps.div(
      ivOps.interval(integral.fromInt(1), integral.fromInt(1)),
      ivOps.interval(integral.fromInt(-1), integral.fromInt(1))
    )
    assertResult(IsSound.Sound)(soundness.isSound(concreteIntegerOps.div(integral.fromInt(1), integral.fromInt(-1)), actual))
    assertResult(IsSound.Sound)(soundness.isSound(concreteIntegerOps.div(integral.fromInt(1), integral.fromInt(1)), actual))
  }

  test("div([-1,1],[-1,-1])") {
    implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
    val actual = integerOps.div(
      ivOps.interval(integral.fromInt(-1), integral.fromInt(1)),
      ivOps.interval(integral.fromInt(-1), integral.fromInt(-1))
    )
    assertResult(IsSound.Sound)(soundness.isSound(concreteIntegerOps.div(integral.fromInt(1), integral.fromInt(-1)), actual))
    assertResult(IsSound.Sound)(soundness.isSound(concreteIntegerOps.div(integral.fromInt(-1), integral.fromInt(-1)), actual))
  }

  test("shiftLeft(1, -1)") {
    val (ivOps, integerOps,soundness) = makeIntegerOps()
    val actual = integerOps.shiftLeft(
      ivOps.constant(integral.fromInt(1)),
      ivOps.constant(integral.fromInt(-1))
    )
    val expected = concreteIntegerOps.shiftLeft(integral.fromInt(1),integral.fromInt(-1))
    assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
  }

  test("shiftRight(-1, 1) == -1") {
    val (ivOps, integerOps, soundness) = makeIntegerOps()
    val actual = integerOps.shiftRight(
      ivOps.constant(integral.fromInt(-1)),
      ivOps.constant(integral.fromInt(1))
    )
    val expected = concreteIntegerOps.shiftRight(integral.fromInt(-1), integral.fromInt(1))
    assertResult(IsSound.Sound)(soundness.isSound(expected, actual))
  }


  test("countLeadingZeros([1,4])") {
    implicit val (ivOps, integerOps, soundness) = makeIntegerOps()
    val actual = integerOps.countLeadingZeros(ivOps.interval(integral.fromInt(1), integral.fromInt(4)))
    assertResult(IsSound.Sound)(soundness.isSound(concreteIntegerOps.countLeadingZeros(integral.fromInt(4)), actual))
    assertResult(IsSound.Sound)(soundness.isSound(concreteIntegerOps.countLeadingZeros(integral.fromInt(1)), actual))
  }

class RelationalLongOpsTest(using Manager) extends IntegerOpsTest[Long, ApronExpr[VirtAddr, Type]](
  specials = List(Long.MinValue, -1, 0, 1, Long.MaxValue),
  makeIntegerOps = () =>
    withApronState {
      (RelationalLongInterval, new RelationalLongOps[VirtAddr, Type], implicitly)
    }
)

given RelationalIntInterval(using apronState: ApronState[VirtAddr,Type]): IsInterval[Int, ApronExpr[VirtAddr, Type]] with
  override def constant(i: Int): ApronExpr[VirtAddr, Type] =
    apronState.assignTempVar(ApronExpr.lit(i, Type.IntType))

  override def interval(low: Int, high: Int, floatSpecials: FloatSpecials): ApronExpr[VirtAddr, Type] =
    apronState.assignTempVar(ApronExpr.interval(low, high, Type.IntType))

given RelationalLongInterval(using apronState: ApronState[VirtAddr, Type]): IsInterval[Long, ApronExpr[VirtAddr, Type]] with
  override def constant(i: Long): ApronExpr[VirtAddr, Type] =
    apronState.assignTempVar(ApronExpr.lit(i, Type.LongType))

  override def interval(low: Long, high: Long, floatSpecials: FloatSpecials): ApronExpr[VirtAddr, Type] =
    apronState.assignTempVar(ApronExpr.interval(low, high, Type.LongType))

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
      if(n >= 0)
        n / BigInt(2).pow(modulo(shift, 32))
      else
        ((n + 1) / BigInt(2).pow(modulo(shift, 32)) - 1)