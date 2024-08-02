package sturdy.values.ordering

import apron.*
import org.scalacheck.Gen
import org.scalatest.Suites
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.apron.{*, given}
import sturdy.effect.{EffectList, EffectStack, Stateless}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.util.{*, given}
import sturdy.util.TestContexts.{*, given}
import sturdy.util.TestTypes.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.{JoinToppedFlat, *}
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}
given Ordering[Float] = Ordering.Float.IeeeOrdering


class RelationalOrderingOpsTest extends Suites(
  new PolyhedraOrderingOpsTest,
  new OctagonOrderingOpsTest,
  new BoxOrderingOpsTest
)

class PolyhedraOrderingOpsTest extends RelationalOrderingOpsTests(using Polka(true))
class OctagonOrderingOpsTest extends RelationalOrderingOpsTests(using Octagon())
class BoxOrderingOpsTest extends RelationalOrderingOpsTests(using Box())

class RelationalOrderingOpsTests(using Manager) extends Suites(
  new RelationalIntOrderingOpsTest,
  new RelationalFloatOrderingOpsTest,
  new RelationalDoubleOrderingOpsTest
)

class RelationalIntOrderingOpsTest(using Manager) extends OrderingOpsTest[Int, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  specials = List(Int.MinValue, -1, 0, 1, Int.MaxValue),
  makeOrderingOps = withApronState {
    val intType: Type = Type.IntType
    val apronState = summon[ApronState[VirtAddr, Type]]
    new RelationalOrderingOps[VirtAddr, Type] with TestingOrderingOps[Int, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]] {
      override def getBool(b: ApronCons[VirtAddr, Type]): Topped[Boolean] =
        apronState.ifThenElse(b) {
          Topped.Actual(true)
        } {
          Topped.Actual(false)
        }
    }
  }
)

class RelationalFloatOrderingOpsTest(using Manager) extends OrderingOpsTest[Float, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  specials = List(Float.MinValue, math.nextDown(0.0f), 0.0f, math.nextUp(0.0f), Float.MaxValue),
  makeOrderingOps = withApronState {
    val apronState = summon[ApronState[VirtAddr, Type]]
    new RelationalOrderingOps[VirtAddr, Type] with TestingOrderingOps[Float, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]] {
      override def getBool(b: ApronCons[VirtAddr, Type]): Topped[Boolean] =
        apronState.ifThenElse(b) {
          Topped.Actual(true)
        } {
          Topped.Actual(false)
        }
    }
  }
)

class RelationalDoubleOrderingOpsTest(using Manager) extends OrderingOpsTest[Double, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  specials = List(Double.MinValue, math.nextDown(0.0d), 0.0d, math.nextUp(0.0d), Double.MaxValue),
  makeOrderingOps = withApronState {
    val apronState = summon[ApronState[VirtAddr, Type]]
    new RelationalOrderingOps[VirtAddr, Type] with TestingOrderingOps[Double, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]] {
      override def getBool(b: ApronCons[VirtAddr, Type]): Topped[Boolean] =
        apronState.ifThenElse(b) {
          Topped.Actual(true)
        } {
          Topped.Actual(false)
        }
    }
  }
)