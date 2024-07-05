package sturdy.values.ordering

import apron.*
import sturdy.apron.{*, given}
import sturdy.effect.{EffectList, EffectStack, Stateless}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.util.{Lazy, lazily}
import sturdy.util.TestContexts.{*, given}
import sturdy.util.TestTypes.{*, given}
import sturdy.util.{VirtAddr, withApronState}
import sturdy.values.*
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

class RelationalOrderingOpsTest extends OrderingOpsTest[Int, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type]](
  minValue = Integer.MIN_VALUE,
  maxValue = Integer.MAX_VALUE,
  makeOrderingOps = withApronState {
    val intType: Type = Type.IntType
    new RelationalOrderingOps[VirtAddr, Type] with TestingOrderingOps[Int, ApronExpr[VirtAddr, Type], ApronExpr[VirtAddr, Type]] {
      override def integerLit(i: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intLit(i, intType)
      override def interval(low: Int, high: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intInterval(low, high, intType)
      override def getBool(b: ApronExpr[VirtAddr, Type]): Topped[Boolean] =
        this.apronState.getIntInterval(b) match
          case (0,0) => Topped.Actual(false)
          case (1,1) => Topped.Actual(true)
          case (0,1) => Topped.Top
          case iv => throw new IllegalStateException(s"Not a valid boolean interval ${iv}")
    }
  }
)