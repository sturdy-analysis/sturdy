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
import sturdy.utils.TestTypes.{*, given}
import sturdy.values.*
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]

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
    val floatType: Type = Type.FloatType(BaseType[Float])
    new RelationalFloatOps[Float, VirtAddr, Type] with TestingFloatOps[Float, ApronExpr[VirtAddr, Type]] {
      override def floatLit(i: Float): ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(i, floatType)
      override def interval(low: Float, high: Float): ApronExpr[VirtAddr, Type] = ApronExpr.doubleInterval(low, high, floatType)
      override def shouldContain(expr: ApronExpr[VirtAddr, Type], m: Float): Assertion =
        val iv = apronState.getInterval(expr)
        if(Interval(DoubleScalar(m),DoubleScalar(m)).isLeq(iv))
          succeed
        else
          fail(s"$iv does not include $m")
      override def shouldEqual(expr: ApronExpr[VirtAddr, Type], l: Float, u: Float): Assertion =
        val iv = apronState.getInterval(expr)
        if(Interval(DoubleScalar(l), DoubleScalar(u)).isEqual(iv))
          succeed
        else
          fail(s"$iv does not equal [$l,$u]")

      override def NaN: ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(Float.NaN, floatType)
      override def posInfinity: ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(Float.PositiveInfinity, floatType)
      override def negInfinity: ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(Float.NegativeInfinity, floatType)
    }
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
    val floatType: Type = Type.DoubleType(BaseType[Double])
    new RelationalFloatOps[Double, VirtAddr, Type] with TestingFloatOps[Double, ApronExpr[VirtAddr, Type]] {
      override def floatLit(i: Double): ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(i, floatType)
      override def interval(low: Double, high: Double): ApronExpr[VirtAddr, Type] = ApronExpr.doubleInterval(low, high, floatType)

      override def shouldContain(expr: ApronExpr[VirtAddr, Type], m: Double): Assertion =
        val iv = apronState.getInterval(expr)
        if (Interval(DoubleScalar(m), DoubleScalar(m)).isLeq(iv))
          succeed
        else
          fail(s"[${iv.inf}:${iv.inf.getClass}, ${iv.sup}:${iv.inf.getClass}] does not include $m")

      override def shouldEqual(expr: ApronExpr[VirtAddr, Type], l: Double, u: Double): Assertion =
        val iv = apronState.getInterval(expr)
        if (Interval(DoubleScalar(l), DoubleScalar(u)).isEqual(iv))
          succeed
        else
          fail(s"[${iv.inf}:${iv.inf.getClass}, ${iv.sup}: ${iv.sup.getClass}] does not equal [$l,$u]")

      override def NaN: ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(Double.NaN, floatType)
      override def posInfinity: ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(Double.PositiveInfinity, floatType)
      override def negInfinity: ApronExpr[VirtAddr, Type] = ApronExpr.doubleLit(Double.NegativeInfinity, floatType)
    }
  }
)