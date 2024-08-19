package sturdy.values.convert

import apron.*
import org.scalacheck.Gen
import org.scalatest.{Assertion, Suites}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.IsSound
import sturdy.apron.ApronExpr.{cast, doubleLit}
import sturdy.apron.{*, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{*, given}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.effect.{EffectStack, Stateless}
import sturdy.util.{Lazy, lazily}
import sturdy.util.TestContexts.{*, given}
import sturdy.util.TestTypes.{*, given}
import sturdy.util.{*, given}
import sturdy.values.*
import sturdy.values.config.{*, given}
import sturdy.values.convert.ConvertTest
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{ConcreteConvertDoubleInt, ConcreteConvertDoubleLong, *, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]

final class WithNearestRoundingMode[From, To, VFrom, VTo, Config <: ConvertConfig[_]](convert: Convert[From, To, VFrom, VTo, Config]) extends Convert[From, To, VFrom, VTo, Config]:
  override def apply(from: VFrom, conf: Config): VTo =
    RoundingMode.withRoundingMode(RoundingDir.Nearest) {
      convert(from,conf)
    }

given ConvertIntLong[Int,Long] = WithNearestRoundingMode(ConcreteConvertIntLong(using new ConcreteFailure))
given ConvertFloatLong[Float,Long] = WithNearestRoundingMode(ConcreteConvertFloatLong(using new ConcreteFailure))
given ConvertFloatInt[Float,Int] = WithNearestRoundingMode(ConcreteConvertFloatInt(using new ConcreteFailure))
given ConvertDoubleLong[Double,Long] = WithNearestRoundingMode(ConcreteConvertDoubleLong(using new ConcreteFailure))
given ConvertDoubleInt[Double,Int] = WithNearestRoundingMode(ConcreteConvertDoubleInt(using new ConcreteFailure))
given ConvertLongFloat[Long,Float] = WithNearestRoundingMode(ConcreteConvertLongFloat(using new ConcreteFailure))

class RelationalConvertTest extends Suites(
  new PolyhedraConvertTest,
  new OctagonConvertTest,
  new BoxConvertTest,
)

class PolyhedraConvertTest extends RelationalConvertTests(Polka(true))
class OctagonConvertTest extends RelationalConvertTests(Octagon())
class BoxConvertTest extends RelationalConvertTests(Box())

class RelationalConvertTests(manager: Manager) extends Suites(
  RelationalConvertIntLongTest(using manager),
  RelationalConvertIntFloatTest(using manager),
  RelationalConvertIntDoubleTest(using manager),
  RelationalConvertLongIntTest(using manager),
  RelationalConvertLongFloatTest(using manager),
  RelationalConvertLongDoubleTest(using manager),
  RelationalConvertFloatIntTest(using manager),
  RelationalConvertFloatLongTest(using manager),
  RelationalConvertFloatDoubleTest(using manager),
  RelationalConvertDoubleIntTest(using manager),
  RelationalConvertDoubleLongTest(using manager),
  RelationalConvertDoubleFloatTest(using manager)
)

class RelationalConvertIntLongTest(using manager: Manager) extends ConvertTest[Int, Long, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Bits](
  specials = List(),
  makeConvert = withApronState(using manager) (
    (
      RelationalIntInterval,
      RelationalLongInterval,
      RelationalConvertIntLong,
      soundnessAFallible(using SoundnessLongApronExpr),
      implicitly
    )
  )
)

class RelationalConvertLongIntTest(using manager: Manager) extends ConvertTest[Long, Int, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], NilCC.type](
  specials = List(
    Int.MinValue.toLong - 1, Int.MinValue.toLong + 0, Int.MinValue.toLong + 1,
    Int.MaxValue.toLong - 1, Int.MaxValue.toLong + 0, Int.MaxValue.toLong + 1
  ),
  makeConvert = withApronState (
    (
      RelationalLongInterval,
      RelationalIntInterval,
      RelationalConvertLongInt,
      soundnessAFallible(using sturdy.values.integer.SoundnessIntApronExpr),
      implicitly
    )
  )
)

class RelationalConvertFloatLongTest(using manager: Manager) extends ConvertTest[Float, Long, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Overflow && Bits](
  specials = specialFloatingIntegerNumbers[Float,Long](_.toFloat),
  makeConvert = withApronState(
    (
      RelationalFloatIsInterval,
      RelationalLongInterval,
      RelationalConvertFloatLong,
      soundnessAFallible(using SoundnessLongApronExpr),
      implicitly
    )
  )
)

class RelationalConvertFloatIntTest(using manager: Manager) extends ConvertTest[Float, Int, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Overflow && Bits](
  specials = specialFloatingIntegerNumbers[Float,Int](_.toFloat),
  makeConvert = withApronState(
    (
      RelationalFloatIsInterval,
      RelationalIntInterval,
      RelationalConvertFloatInt,
      soundnessAFallible(using SoundnessIntApronExpr),
      implicitly
    )
  )
):
  test(s"convert(0.0,Overflow.Fail && Bits.Unsigned) = 0") {
    implicit val (fromIvOps, toIvOps, convertOps, soundness, afailure) = _makeConvert
    val actual = afailure.fallible(convertOps(fromIvOps.constant(0.0f), Overflow.Fail && Bits.Unsigned))
    val expected = cfailure.fallible(ConcreteConvertFloatInt(0.0f, Overflow.Fail && Bits.Unsigned))
    assertResult(IsSound.Sound, s"$actual does not overapproximate $expected")(soundness.isSound(expected, actual))
  }


class RelationalConvertDoubleLongTest(using manager: Manager) extends ConvertTest[Double, Long, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Overflow && Bits](
  specials = specialFloatingIntegerNumbers[Double,Long](_.toDouble),
  makeConvert = withApronState(
    (
      RelationalDoubleIsInterval,
      RelationalLongInterval,
      RelationalConvertDoubleLong,
      soundnessAFallible(using SoundnessLongApronExpr),
      implicitly
    )
  )
)

class RelationalConvertDoubleIntTest(using manager: Manager) extends ConvertTest[Double, Int, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Overflow && Bits](
  specials = specialFloatingIntegerNumbers[Double,Int](_.toDouble),
  makeConvert = withApronState(
    (
      RelationalDoubleIsInterval,
      RelationalIntInterval,
      RelationalConvertDoubleInt,
      soundnessAFallible(using SoundnessIntApronExpr),
      implicitly
    )
  )
)

class RelationalConvertDoubleFloatTest(using manager: Manager) extends ConvertTest[Double, Float, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type], NilCC.type](
  specials = List(
    Math.nextDown(Float.MinValue.toDouble), Float.MinValue.toDouble, Math.nextUp(Float.MinValue.toDouble),
    Math.nextDown(Float.MaxValue.toDouble), Float.MaxValue.toDouble, Math.nextUp(Float.MaxValue.toDouble),
    Double.NaN
  ),
  makeConvert = withApronState(
    (
      RelationalDoubleIsInterval,
      RelationalFloatIsInterval,
      RelationalConvertDoubleFloat,
      soundnessAFallible(using SoundnessFloatApronExpr),
      implicitly
    )
  )
)(using
  implicitly,
  implicitly,
  implicitly,
  implicitly,
  concreteConvert = WithNearestRoundingModeConvert(ConcreteConvertDoubleFloat)
)

class RelationalConvertFloatDoubleTest(using manager: Manager) extends ConvertTest[Float, Double, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type], NilCC.type](
  specials = List(Float.NaN),
  makeConvert = withApronState(
    (
      RelationalFloatIsInterval,
      RelationalDoubleIsInterval,
      RelationalConvertFloatDouble,
      soundnessAFallible(using SoundnessDoubleApronExpr),
      implicitly
    )
  )
)


class RelationalConvertIntFloatTest(using manager: Manager) extends ConvertTest[Int, Float, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type], Bits](
  specials = List(),
  makeConvert = withApronState(
    (
      RelationalIntInterval,
      RelationalFloatIsInterval,
      RelationalConvertIntFloat,
      soundnessAFallible(using SoundnessFloatApronExpr),
      implicitly
    )
  )
):
  test("convert(16777217, Bits.Signed) = 1.6777216E7f") {
    implicit val (fromIvOps, toIvOps, convertOps, soundness, afailure) = _makeConvert
    assertResult(IsSound.Sound)(
      soundness.isSound(
        cfailure.fallible(ConcreteConvertIntFloat(16777217, Bits.Unsigned)),
        afailure.fallible(convertOps(ApronExpr.doubleLit(16777217, Type.IntType), Bits.Unsigned))
      )
    )
  }

class RelationalConvertIntDoubleTest(using manager: Manager) extends ConvertTest[Int, Double, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type], Bits](
  specials = List(),
  makeConvert = withApronState(
    (
      RelationalIntInterval,
      RelationalDoubleIsInterval,
      RelationalConvertIntDouble,
      soundnessAFallible(using SoundnessDoubleApronExpr),
      implicitly
    )
  )
)

class RelationalConvertLongFloatTest(using manager: Manager) extends ConvertTest[Long, Float, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type], Bits](
  specials = List(),
  makeConvert = withApronState(
    (
      RelationalLongInterval,
      RelationalFloatIsInterval,
      RelationalConvertLongFloat,
      soundnessAFallible(using SoundnessFloatApronExpr),
      implicitly
    )
  )
)

class RelationalConvertLongDoubleTest(using manager: Manager) extends ConvertTest[Long, Double, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type], Bits](
  specials = List(),
  makeConvert = withApronState(
    (
      RelationalLongInterval,
      RelationalDoubleIsInterval,
      RelationalConvertLongDouble,
      soundnessAFallible(using SoundnessDoubleApronExpr),
      implicitly
    )
  )
)

def specialFloatingIntegerNumbers[F: Enumerable: Fractional, I: Bounded: Integral](toFloat: I => F): Seq[F] = {
  import math.Fractional.Implicits.infixFractionalOps
  val en: Enumerable[F] = implicitly
  List(
    en.nextDown(toFloat(Bounded[I].minValue)), toFloat(Bounded[I].minValue), en.nextUp(toFloat(Bounded[I].minValue)),
    en.nextDown(toFloat(Bounded[I].maxValue)), toFloat(Bounded[I].maxValue), en.nextUp(toFloat(Bounded[I].maxValue)),
    en.nextDown(-toFloat(Bounded[I].minValue)), -toFloat(Bounded[I].minValue), en.nextUp(-toFloat(Bounded[I].minValue)),
    en.nextDown(-toFloat(Bounded[I].minValue) * Fractional[F].fromInt(2)), -toFloat(Bounded[I].minValue) * Fractional[F].fromInt(2), en.nextUp(-toFloat(Bounded[I].minValue) * Fractional[F].fromInt(2)),
    en.nextDown(-Fractional[F].fromInt(1)), -Fractional[F].fromInt(1), en.nextUp(-Fractional[F].fromInt(1)),
    Fractional[F].zero / Fractional[F].zero // NaN
  )
}

final class WithNearestRoundingModeConvert[From, To, VFrom, VTo, Config <: ConvertConfig[_]]
  (convert: Convert[From, To, VFrom, VTo, Config])
  extends Convert[From, To, VFrom, VTo, Config]:
  override def apply(from: VFrom, conf: Config): VTo =
    RoundingMode.withRoundingMode(RoundingDir.Nearest) {
      convert(from, conf)
    }