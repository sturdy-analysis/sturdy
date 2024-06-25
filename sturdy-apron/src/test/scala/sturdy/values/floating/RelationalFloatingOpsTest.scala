package sturdy.values.floating

import apron.*
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.apron.{*, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.effect.{EffectStack, Stateless}
import sturdy.util.{Lazy, lazily}
import sturdy.utils.TestContexts.{*, given}
import sturdy.utils.TestIntervalOps
import sturdy.utils.TestTypes.{*, given}
import sturdy.values.*
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]

trait RelationalFloatTestIntervalOps(using apronState: ApronState[VirtAddr, Type]) extends TestIntervalOps[Float, ApronExpr[VirtAddr, Type]]:
  val floatType: Type = Type.FloatType(BaseType[Float])
  override def constant(i: Float): ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(i, floatType)
  override def interval(low: Float, high: Float): ApronExpr[VirtAddr, Type] = ApronExpr.doubleInterval(low, high, floatType)
  override def contains(expr: ApronExpr[VirtAddr, Type], m: Float): Boolean =
    val iv = this.apronState.getInterval(expr)
    Interval(DoubleScalar(m),DoubleScalar(m)).isLeq(iv)

  override def equals(expr: ApronExpr[VirtAddr, Type], l: Float, u: Float): Boolean =
    val iv = this.apronState.getInterval(expr)
    Interval(DoubleScalar(l), DoubleScalar(u)).isEqual(iv)

trait RelationalDoubleTestIntervalOps(using apronState: ApronState[VirtAddr, Type]) extends TestIntervalOps[Double, ApronExpr[VirtAddr, Type]]:
  val floatType: Type = Type.DoubleType(BaseType[Double])
  override def constant(i: Double): ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(i, floatType)

  override def interval(low: Double, high: Double): ApronExpr[VirtAddr, Type] = ApronExpr.doubleInterval(low, high, floatType)

  override def contains(expr: ApronExpr[VirtAddr, Type], m: Double): Boolean =
    val iv = this.apronState.getInterval(expr)
    Interval(DoubleScalar(m), DoubleScalar(m)).isLeq(iv)

  override def equals(expr: ApronExpr[VirtAddr, Type], l: Double, u: Double): Boolean =
    val iv = this.apronState.getInterval(expr)
    Interval(DoubleScalar(l), DoubleScalar(u)).isEqual(iv)

  class RelationalFloatOpsTest extends FloatOpsTest[Float, ApronExpr[VirtAddr, Type]](
  minValue = Float.MinValue,
  maxValue = Float.MaxValue,
  makeFloatOps = {
    given apronManager: Manager = new Octagon
    var apronState: ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]] = null
    given effectStack: EffectStack = new EffectStack(
      RecencyClosure(apronState.recencyStore)
    )
    apronState = RecencyRelationalStore[Ctx, Type]
    given ApronState[VirtAddr, Type] = apronState
    val lazyApronState: Lazy[ApronState[VirtAddr, Type]] = lazily(apronState)
    new RelationalFloatOps[Float, VirtAddr, Type] with RelationalFloatTestIntervalOps {}
  }
)

class RelationalDoubleOpsTest extends FloatOpsTest[Double, ApronExpr[VirtAddr, Type]](
  minValue = Double.MinValue,
  maxValue = Double.MaxValue,
  makeFloatOps = {
    given apronManager: Manager = new Octagon
    var apronState: ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]] = null
    given effectStack: EffectStack = new EffectStack(
      RecencyClosure(apronState.recencyStore)
    )
    apronState = RecencyRelationalStore[Ctx, Type]
    given ApronState[VirtAddr, Type] = apronState
    val lazyApronState: Lazy[ApronState[VirtAddr, Type]] = lazily(apronState)
    new RelationalFloatOps[Double, VirtAddr, Type] with RelationalDoubleTestIntervalOps {}
  }
)