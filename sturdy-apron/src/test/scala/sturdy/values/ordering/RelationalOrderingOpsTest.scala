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
import sturdy.values.{JoinToppedFlat, *}
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}


class PolyhedraOrderingOpsTest extends RelationalOrderingOpsTest(using Polka(true))
class OctagonOrderingOpsTest extends RelationalOrderingOpsTest(using Octagon())

class RelationalOrderingOpsTest(using Manager) extends OrderingOpsTest[Int, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  minValue = Integer.MIN_VALUE,
  maxValue = Integer.MAX_VALUE,
  makeOrderingOps = withApronState {
    val intType: Type = Type.IntType
    val apronState = summon[ApronState[VirtAddr, Type]]
    new RelationalOrderingOps[VirtAddr, Type] with TestingOrderingOps[Int, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]] {
      override def integerLit(i: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intLit(i, intType)
      override def interval(low: Int, high: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intInterval(low, high, intType)
      override def getBool(b: ApronCons[VirtAddr, Type]): Topped[Boolean] =
        apronState.ifThenElse(b) {
          Topped.Actual(true)
        } {
          Topped.Actual(false)
        }
    }
  }
)