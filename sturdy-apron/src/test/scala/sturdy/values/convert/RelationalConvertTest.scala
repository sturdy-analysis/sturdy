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
import sturdy.utils.TestContexts.{*, given}
import sturdy.utils.TestTypes.{*, given}
import sturdy.utils.{*, given}
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

class RelationalConvertIntLongTest extends ConvertTest[Int, Long, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Bits](
  withApronState(
    (RelationalIntTestIntervalOps, RelationalLongTestIntervalOps, RelationalConvertIntLong, soundnessAFallible(using SoundnessLongApronExpr))
  )
)

class RelationalConvertLongIntTest extends ConvertTest[Long, Int, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], NilCC.type](
  withApronState(
    (RelationalLongTestIntervalOps, RelationalIntTestIntervalOps , RelationalConvertLongInt, soundnessAFallible(using sturdy.values.integer.SoundnessIntApronExpr))
  )
)

class RelationalConvertFloatLongTest extends ConvertTest[Float, Long, ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type], Overflow && Bits](
  withApronState(
    (RelationalFloatTestIntervalOps, RelationalLongTestIntervalOps , RelationalConvertFloatLong, soundnessAFallible(using SoundnessLongApronExpr))
  )
)