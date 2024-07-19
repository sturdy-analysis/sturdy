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

given ConcreteConvertIntLong = ConcreteConvertIntLong(using new ConcreteFailure)
given ConcreteConvertFloatLong = ConcreteConvertFloatLong(using new ConcreteFailure)
given ConcreteConvertFloatInt = ConcreteConvertFloatInt(using new ConcreteFailure)
given ConcreteConvertDoubleLong = ConcreteConvertDoubleLong(using new ConcreteFailure)
given ConcreteConvertDoubleInt = ConcreteConvertDoubleInt(using new ConcreteFailure)
given ConcreteConvertLongFloat = ConcreteConvertLongFloat(using new ConcreteFailure)

class PolyhedraConvertTests extends RelationalConvertTests(Polka(true))
class OctagonConvertTests extends RelationalConvertTests(Octagon())

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
      RelationalConvertFloatInt,
      soundnessAFallible(using SoundnessIntApronExpr),
      implicitly
    )
  )
)

class RelationalConvertDoubleLongTest(using manager: Manager) extends ConvertTest[Double, Long, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Overflow && Bits](
  specials = specialFloatingIntegerNumbers[Double,Long](_.toDouble),
  makeConvert = withApronState(
    (
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
      RelationalConvertDoubleFloat,
      soundnessAFallible(using SoundnessFloatApronExpr),
      implicitly
    )
  )
)

class RelationalConvertFloatDoubleTest(using manager: Manager) extends ConvertTest[Float, Double, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type], NilCC.type](
  specials = List(Float.NaN),
  makeConvert = withApronState(
    (
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
      RelationalConvertIntFloat,
      soundnessAFallible(using SoundnessFloatApronExpr),
      implicitly
    )
  )
):
  test("convert(16777217, Bits.Signed) = 1.6777216E7f") {
    implicit val (convertOps, soundness, afailure) = _makeConvert
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