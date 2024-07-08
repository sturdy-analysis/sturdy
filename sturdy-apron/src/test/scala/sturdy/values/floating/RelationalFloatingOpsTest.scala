package sturdy.values.floating

import apron.*
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.apron.{*, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.effect.{EffectList, EffectStack, Stateless}
import sturdy.util.{*,given}
import sturdy.util.TestContexts.{*, given}
import sturdy.util.TestTypes.{*, given}
import sturdy.values.*
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]

given RelationalFloatIsInterval: IsInterval[Float, ApronExpr[VirtAddr, Type]] with
  val floatType: Type = Type.FloatType
  override def constant(i: Float): ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(i, floatType)
  override def interval(low: Float, high: Float): ApronExpr[VirtAddr, Type] = ApronExpr.doubleInterval(low, high, floatType)

given RelationalDoubleIsInterval: IsInterval[Double, ApronExpr[VirtAddr, Type]] with
  val floatType: Type = Type.DoubleType
  override def constant(i: Double): ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(i, floatType)

  override def interval(low: Double, high: Double): ApronExpr[VirtAddr, Type] = ApronExpr.doubleInterval(low, high, floatType)


class RelationalFloatOpsTest extends FloatOpsTest[Float, ApronExpr[VirtAddr, Type]](
  makeFloatOps = withApronState {
    (RelationalFloatOps[Float, VirtAddr, Type], SoundnessFloatApronExpr[VirtAddr,Type])
  }
)

class RelationalDoubleOpsTest extends FloatOpsTest[Double, ApronExpr[VirtAddr, Type]](
  makeFloatOps = withApronState {
    (RelationalFloatOps[Double, VirtAddr, Type], SoundnessDoubleApronExpr[VirtAddr,Type])
  }
)