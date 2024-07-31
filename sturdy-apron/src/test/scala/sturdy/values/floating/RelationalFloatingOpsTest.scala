package sturdy.values.floating

import apron.*
import org.scalacheck.Gen
import org.scalatest.{Assertion, Suites}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sturdy.apron.{*, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.effect.{EffectList, EffectStack, Stateless}
import sturdy.util.{*, given}
import sturdy.util.TestContexts.{*, given}
import sturdy.util.TestTypes.{*, given}
import sturdy.values.*
import sturdy.values.ordering.*
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]

class ApronFloatingOpsTests extends Suites(
  new PolyhedraFloatingOpsTests,
  new OctagonFloatingOpsTests
)
class PolyhedraFloatingOpsTests extends RelationalFloatingOpsTests(Polka(true))
class OctagonFloatingOpsTests extends RelationalFloatingOpsTests(Octagon())

class RelationalFloatingOpsTests(manager: Manager) extends Suites(
  RelationalFloatOpsTest(using manager),
  RelationalDoubleOpsTest(using manager),
)

class RelationalFloatOpsTest(using Manager) extends FloatOpsTest[Float, ApronExpr[VirtAddr, Type]](
  makeFloatOps = withApronState {
    (RelationalFloatOps[Float, VirtAddr, Type], SoundnessFloatApronExpr[VirtAddr,Type])
  }
)(using
  implicitly,
  implicitly,
  implicitly,
  ivOps = implicitly,
  ord = implicitly,
  fractional = implicitly,
  concreteFloatOps = new WithNearestRoundingModeFloatingOps(ConcreteFloatOps)
)

class RelationalDoubleOpsTest(using Manager) extends FloatOpsTest[Double, ApronExpr[VirtAddr, Type]](
  makeFloatOps = withApronState {
    (RelationalFloatOps[Double, VirtAddr, Type], SoundnessDoubleApronExpr[VirtAddr,Type])
  }
)(using
  implicitly,
  implicitly,
  implicitly,
  ivOps = implicitly,
  ord = implicitly,
  fractional = implicitly,
  concreteFloatOps = new WithNearestRoundingModeFloatingOps(ConcreteDoubleOps)
)

given RelationalFloatIsInterval: IsInterval[Float, ApronExpr[VirtAddr, Type]] with
  val floatType: Type = Type.FloatType
  override def constant(i: Float): ApronExpr[VirtAddr, Type] = FloatingLit(i, floatType)
  override def interval(low: Float, high: Float): ApronExpr[VirtAddr, Type] = ApronExpr.doubleInterval(low, high, floatType)

given RelationalDoubleIsInterval: IsInterval[Double, ApronExpr[VirtAddr, Type]] with
  val floatType: Type = Type.DoubleType
  override def constant(i: Double): ApronExpr[VirtAddr, Type] = FloatingLit(i, floatType)

  override def interval(low: Double, high: Double): ApronExpr[VirtAddr, Type] = ApronExpr.doubleInterval(low, high, floatType)

final class WithNearestRoundingModeFloatingOps[B,V](floatOps: FloatOps[B,V]) extends FloatOps[B,V]:
  override def floatingLit(f: B): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.floatingLit(f) }
  override def randomFloat(): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.randomFloat() }
  override def add(v1: V, v2: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.add(v1,v2) }
  override def sub(v1: V, v2: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.sub(v1,v2) }
  override def mul(v1: V, v2: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.mul(v1,v2) }
  override def div(v1: V, v2: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.div(v1,v2) }
  override def min(v1: V, v2: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.min(v1,v2) }
  override def max(v1: V, v2: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.max(v1,v2) }
  override def absolute(v: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.absolute(v) }
  override def negated(v: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.negated(v) }
  override def sqrt(v: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.sqrt(v) }
  override def ceil(v: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.ceil(v) }
  override def floor(v: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.floor(v) }
  override def truncate(v: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.truncate(v) }
  override def nearest(v: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.nearest(v) }
  override def copysign(v: V, sign: V): V = RoundingMode.withRoundingMode(RoundingDir.Nearest) { floatOps.copysign(v, sign) }