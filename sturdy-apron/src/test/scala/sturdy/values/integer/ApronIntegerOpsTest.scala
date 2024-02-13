package sturdy.values.integer

import apron.*

import sturdy.apron.*

import sturdy.effect.Stateless
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{ApronRecencyStore, ApronStore, RecencyStore, given}
import sturdy.values.*
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}
import sturdy.utils.TestTypes.{*, given}
import sturdy.utils.TestContexts.{*, given}

type VirtAddr = VirtualAddress[Ctx]

class ApronIntegerOpsTest extends IntegerOpsTest[Int, ApronExpr[VirtAddr, Type]](
  minValue = -100,
  maxValue = 100,
  makeIntegerOps = {
    val apronManager: Manager = new apron.Polka(true)
    val (recencyStore, apronStore) = ApronRecencyStore[Ctx, Type](apronManager)
    given ApronState[VirtAddr, Type] = new ApronRecencyState(tempVariableAllocator, recencyStore, apronStore) {}
    new ApronIntegerOps[VirtAddr, Type] with IntervalIntegerOps[Int, ApronExpr[VirtAddr, Type]] {
      override def integerLit(i: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intLit(i)
      override def interval(low: Int, high: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intInterval(low, high)
      override def getBounds(n: ApronExpr[VirtAddr, Type]): (Int, Int) = apronState.getIntBound(n)
    }
  }
)