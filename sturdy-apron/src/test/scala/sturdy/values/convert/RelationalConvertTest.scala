package sturdy.values.convert

import apron.*
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.apron.{*, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{*,given}
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
import sturdy.values.floating.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]

given ConcreteConvertIntLong = ConcreteConvertIntLong(using new ConcreteFailure)
given ConcreteConvertFloatLong = ConcreteConvertFloatLong(using new ConcreteFailure)


class RelationalConvertIntLongTest extends ConvertTest[Int, Long, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Bits](
  specials = List(),
  makeConvert = withApronState(
    (
      RelationalIntTestIntervalOps,
      RelationalLongTestIntervalOps,
      RelationalConvertIntLong,
      soundnessAFallible(using SoundnessLongApronExpr),
      implicitly
    )
  )
)

class RelationalConvertLongIntTest extends ConvertTest[Long, Int, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], NilCC.type](
  specials = List(
    Int.MinValue.toLong - 1, Int.MinValue.toLong + 0, Int.MinValue.toLong + 1,
    Int.MaxValue.toLong - 1, Int.MaxValue.toLong + 0, Int.MaxValue.toLong + 1
  ),
  makeConvert = withApronState(
    (
      RelationalLongTestIntervalOps,
      RelationalIntTestIntervalOps,
      RelationalConvertLongInt,
      soundnessAFallible(using sturdy.values.integer.SoundnessIntApronExpr),
      implicitly
    )
  )
)

class RelationalConvertFloatLongTest extends ConvertTest[Float, Long, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Overflow && Bits](
  specials = List(
    Math.nextDown(Long.MinValue.toFloat), Long.MinValue.toFloat, Math.nextUp(Long.MinValue.toFloat),
    Math.nextDown(Long.MaxValue.toFloat), Long.MaxValue.toFloat, Math.nextUp(Long.MaxValue.toFloat),
    Float.NaN
  ),
  makeConvert = withApronState(
    (
      RelationalFloatTestIntervalOps,
      RelationalLongTestIntervalOps ,
      RelationalConvertFloatLong,
      soundnessAFallible(using SoundnessLongApronExpr),
      implicitly
    )
  )
)