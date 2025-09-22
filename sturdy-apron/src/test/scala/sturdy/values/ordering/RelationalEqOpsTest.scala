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

given RelationalIntervalEqOps[L](using apronState: ApronState[VirtAddr,Type]): RelationalEqOps[VirtAddr, Type] with IntervalEqOps[L, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]] with
  override def getBool(b: ApronCons[VirtAddr, Type]): Topped[Boolean] =
    apronState.getBoolean(b)(using ResolveState.Internal)

class RelationalIntEqOpsTest(using Manager) extends EqOpsTest[Int, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  specials = List(Int.MinValue, -1, 0, 1, Int.MaxValue),
  makeEqOps = withApronState (RelationalIntInterval, RelationalIntervalEqOps[Int])
)

class RelationalFloatEqOpsTest(using Manager) extends EqOpsTest[Float, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  specials = List(Float.MinValue, math.nextDown(0.0f), 0.0f, math.nextUp(0.0f), Float.MaxValue),
  makeEqOps = withApronState (RelationalFloatIsInterval, RelationalIntervalEqOps[Float])
)

class RelationalDoubleEqOpsTest(using Manager) extends EqOpsTest[Double, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](
  specials = List(Double.MinValue, math.nextDown(0.0d), 0.0d, math.nextUp(0.0d), Double.MaxValue),
  makeEqOps = withApronState (RelationalDoubleIsInterval, RelationalIntervalEqOps[Double])
)