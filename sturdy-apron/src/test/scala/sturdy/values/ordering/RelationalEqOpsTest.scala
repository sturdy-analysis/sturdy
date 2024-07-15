package sturdy.values.ordering

import apron.*
import org.scalatest.Suites
import sturdy.apron.{*, given}
import sturdy.effect.{EffectList, EffectStack, Stateless}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.util.{*, given}
import sturdy.util.TestContexts.{*, given}
import sturdy.util.TestTypes.{*, given}
import sturdy.values.{JoinToppedFlat, *}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}


given Structural[Int] with {}
given EqOps[Int,Boolean] = StructuralEqOps[Int]
given Structural[Boolean] with {}

class PolyhedraEqOpsTest extends RelationalEqOpsTest(using Polka(true))
class OctagonEqOpsTest extends RelationalEqOpsTest(using Octagon())

class RelationalEqOpsTest(using Manager) extends EqOpsTest[Int, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  minValue = Integer.MIN_VALUE,
  maxValue = Integer.MAX_VALUE,
  makeEqOps = withApronState {
    val intType: Type = Type.IntType
    val apronState = summon[ApronState[VirtAddr,Type]]
    new RelationalEqOps[VirtAddr, Type] with IntervalEqOps[Int, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]] {
      override def getBool(b: ApronCons[VirtAddr, Type]): Topped[Boolean] =
        apronState.getBoolean(b)
    }
  }
)