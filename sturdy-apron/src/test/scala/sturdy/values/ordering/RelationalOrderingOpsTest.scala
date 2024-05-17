package sturdy.values.ordering

import apron.*
import sturdy.apron.{*, given}
import sturdy.effect.{EffectList, EffectStack, Stateless}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.util.{Lazy, lazily}
import sturdy.utils.TestContexts.{*, given}
import sturdy.utils.TestTypes.{*, given}
import sturdy.values.*
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

class RelationalOrderingOpsTest extends OrderingOpsTest[Int, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type]](
  minValue = -100,
  maxValue = 100,
  makeOrderingOps = {
    given apronManager: Manager = new apron.Polka(true)
    var apronState: ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]] = null
    given effectStack: EffectStack = new EffectStack(
      RecencyClosure(apronState.recencyStore)
    )
    apronState = RecencyRelationalStore[Ctx, Type]
    given ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]] = apronState
    given lazyApronState: Lazy[ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]]] = lazily(apronState)
    new RelationalOrderingOps[VirtAddr, Type] with TestingOrderingOps[Int, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type]] {
      override def integerLit(i: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intLit(i)
      override def interval(low: Int, high: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intInterval(low, high)
      override def getBool(b: ApronExpr[VirtAddr, Type]): Topped[Boolean] =
        this.apronState.getIntBound(b) match
          case (0,0) => Topped.Actual(false)
          case (1,1) => Topped.Actual(true)
          case (0,1) => Topped.Top
          case iv => throw new IllegalStateException(s"Not a valid boolean interval ${iv}")
    }
  }
)