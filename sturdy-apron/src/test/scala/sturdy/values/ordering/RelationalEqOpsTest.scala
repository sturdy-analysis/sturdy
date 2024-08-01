package sturdy.values.ordering

import apron.*
import org.scalatest.Suites
import sturdy.apron.{*, given}
import sturdy.effect.{EffectList, EffectStack, Stateless}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{*, given}
import sturdy.util.{*, given}
import sturdy.util.TestContexts.{*, given}
import sturdy.util.TestTypes.{*, given}
import sturdy.values.{JoinToppedFlat, *}
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

class RelationalEqOpsTest extends Suites(
  new PolyhedraEqOpsTest,
  new OctagonEqOpsTest,
  new BoxEqOpsTest
)

class PolyhedraEqOpsTest extends RelationalEqOpsTests(using Polka(true))
class OctagonEqOpsTest extends RelationalEqOpsTests(using Octagon())
class BoxEqOpsTest extends RelationalEqOpsTests(using Box())

class RelationalEqOpsTests(using Manager) extends Suites(
  new RelationalIntEqOpsTest,
  new RelationalFloatEqOpsTest,
  new RelationalDoubleEqOpsTest
)

given Structural[Float] with {}
given Structural[Int] with {}
given EqOps[Int,Boolean] = StructuralEqOps[Int]
given Structural[Boolean] with {}

class RelationalIntEqOpsTest(using Manager) extends EqOpsTest[Int, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  makeEqOps = withApronState {
    val apronState = summon[ApronState[VirtAddr,Type]]
    new RelationalEqOps[VirtAddr, Type] with IntervalEqOps[Int, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]] {
      override def getBool(b: ApronCons[VirtAddr, Type]): Topped[Boolean] =
        apronState.getBoolean(b)
    }
  }
)

class RelationalFloatEqOpsTest(using Manager) extends EqOpsTest[Float, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  makeEqOps = withApronState {
    val apronState = summon[ApronState[VirtAddr,Type]]
    new RelationalEqOps[VirtAddr, Type] with IntervalEqOps[Float, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]] {
      override def getBool(b: ApronCons[VirtAddr, Type]): Topped[Boolean] =
        apronState.getBoolean(b)
    }
  }
)

class RelationalDoubleEqOpsTest(using Manager) extends EqOpsTest[Double, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  makeEqOps = withApronState {
    val apronState = summon[ApronState[VirtAddr,Type]]
    new RelationalEqOps[VirtAddr, Type] with IntervalEqOps[Double, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]] {
      override def getBool(b: ApronCons[VirtAddr, Type]): Topped[Boolean] =
        apronState.getBoolean(b)
    }
  }
)