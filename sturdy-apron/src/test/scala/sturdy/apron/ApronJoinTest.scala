package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.*
import gmp.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen.Choose
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.Suites
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import sturdy.{IsSound, Soundness}
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.util.GenInterval.{Interval, genConstant, genInterval}
import sturdy.util.TestContexts.Ctx
import sturdy.util.TestTypes.Type
import sturdy.util.{*, given}
import sturdy.values.{*, given}
import sturdy.values.types.BaseType
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.references.{*, given}

class ApronJoinTest extends Suites(
  PolyhedraJoinTest(),
  OctagonJoinTest(),
  BoxJoinTest()
)

class PolyhedraJoinTest extends RelationalJoinTests(using new Polka(true))
class OctagonJoinTest extends RelationalJoinTests(using new Octagon)
class BoxJoinTest extends RelationalJoinTests(using new Box)

class RelationalJoinTests(using manager: apron.Manager) extends Suites(
  new RelationalIntJoinTest,
  new RelationalLongJoinTest,
  new RelationalFloatJoinTest,
  new RelationalDoubleJoinTest,
)

class RelationalIntJoinTest(using manager: apron.Manager) extends RelationalJoinTest[Int](
  specials = List(Int.MinValue, -1, 0, 1, Int.MaxValue),
  makeOps = (RelationalIntInterval, SoundnessIntApronExpr[VirtAddr,Type])
)
class RelationalLongJoinTest(using manager: apron.Manager) extends RelationalJoinTest[Long](
  specials = List(Long.MinValue, -1, 0, 1, Long.MaxValue),
  makeOps = (RelationalLongInterval, SoundnessLongApronExpr[VirtAddr,Type])
)
class RelationalFloatJoinTest(using manager: apron.Manager) extends RelationalJoinTest[Float](
  specials = List(Float.MinValue, -0.5f, 0.0f, 0.5f, Float.MaxValue),
  makeOps = (RelationalFloatIsInterval, SoundnessFloatApronExpr[VirtAddr,Type])
)
class RelationalDoubleJoinTest(using manager: apron.Manager) extends RelationalJoinTest[Double](
  specials = List(Double.MinValue, -0.5f, 0.0f, 0.5f, Double.MaxValue),
  makeOps = (RelationalDoubleIsInterval, SoundnessDoubleApronExpr[VirtAddr,Type])
)

class RelationalJoinTest
  [L: Numeric: Choose: Bounded]
  (
    specials: Seq[L],
    makeOps: ApronState[VirtAddr,Type] ?=> (IsInterval[L,ApronExpr[VirtAddr,Type]], Soundness[L, ApronExpr[VirtAddr,Type]])
  )
  (using
   manager: apron.Manager
  ) extends AnyFunSuite:

  val minValue = Bounded[L].minValue
  val maxValue = Bounded[L].maxValue

  combineTest("Join[ApronExpr]", Join[ApronExpr[VirtAddr,Type]](_,_))
  combineTest("Widen[ApronExpr]", Widen[ApronExpr[VirtAddr,Type]](_,_))

  def combineTest(combineName: String, combine: (ApronState[VirtAddr, Type],Lazy[ApronState[VirtAddr, Type]]) ?=> (e1: ApronExpr[VirtAddr,Type], e2: ApronExpr[VirtAddr,Type]) => MaybeChanged[ApronExpr[VirtAddr,Type]]) =
    test(combineName + " constant") {
      forAll((genConstant[L](minValue, maxValue, specials*), "x"), (genConstant[L](minValue, maxValue, specials*), "y")) {
        case (x, y) =>
          withApronState {
            val apronState = implicitly[ApronState[VirtAddr, Type]]
            given Lazy[ApronState[VirtAddr, Type]] = Lazy(apronState)

            val (ivOps, soundness) = makeOps
            val joined = combine(ivOps.constant(x), ivOps.constant(y))

            assertResult(IsSound.Sound)(soundness.isSound(x, joined.get))
            assertResult(IsSound.Sound)(soundness.isSound(y, joined.get))
          }
      }
    }

    test(combineName + " interval") {
      forAll((genInterval[L](minValue, maxValue, specials *), "x"), (genInterval[L](minValue, maxValue, specials *), "y")) {
        case (GenInterval.Interval(x1, x, x2, xSpecials), GenInterval.Interval(y1, y, y2, ySpecials)) =>
          withApronState {
            val apronState = implicitly[ApronState[VirtAddr, Type]]
            given Lazy[ApronState[VirtAddr, Type]] = Lazy(apronState)

            val (ivOps, soundness) = makeOps

            val xConst = ivOps.interval(x1, x2, xSpecials)
            val yConst = ivOps.interval(y1, y2, ySpecials)
            val joined = combine(xConst, yConst)

            assertResult(IsSound.Sound)(soundness.isSound(x, joined.get))
            assertResult(IsSound.Sound)(soundness.isSound(y, joined.get))
          }
      }
    }

  test("{x ∈ [0,10]} ⊔ {x ∈ [10,20]} = {x ∈ [0,20]}") {
    withApronState {
      val apronState = implicitly[ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]]]
      apronState.withTempVars(Type.IntType) {
        case (x, List()) =>
          apronState.assign(x, ApronExpr.interval(0, 10, Type.IntType))
          val state1 = apronState.effectStack.getState
          apronState.assign(x, ApronExpr.interval(10,20, Type.IntType))
          val state2 = apronState.effectStack.getState
          val joinedState = apronState.effectStack.join(state1, state2)
          joinedState.hasChanged shouldBe true
          apronState.effectStack.setState(joinedState.get)
          apronState.getIntInterval(ApronExpr.addr(x, Type.IntType)) shouldBe
            (0,20)
      }
    }
  }

  test("{x ∈ [0,20]} ⊔ {x ∈ [10,10]} = {x ∈ [0,20]}") {
    withApronState {
      val apronState = implicitly[ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]]]
      apronState.withTempVars(Type.IntType) {
        case (x, List()) =>
          apronState.assign(x, ApronExpr.interval(0, 20, Type.IntType))
          val state1 = apronState.effectStack.getState
          apronState.assign(x, ApronExpr.interval(10, 10, Type.IntType))
          val state2 = apronState.effectStack.getState
          val joinedState = apronState.effectStack.join(state1, state2)
          joinedState.hasChanged shouldBe false
          apronState.effectStack.setState(joinedState.get)
          apronState.getIntInterval(ApronExpr.addr(x, Type.IntType)) shouldBe
            (0, 20)
      }
    }
  }

  test("{x ∈ [0,20], y = x + 1} ⊔ {x ∈ [0,20], y = x + 2} = {x ∈ [0,20], x + 1 <= y <= x + 2}") {
    withApronState {
      val apronState = implicitly[ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]]]
      apronState.withTempVars(Type.IntType, ApronExpr.interval(0, 20, Type.IntType)) {
        case (y, List(x)) =>
          apronState.assign(y, ApronExpr.intAdd(x, ApronExpr.lit(1, Type.IntType), Type.IntType))
          val state1 = apronState.effectStack.getState
          apronState.assign(y, ApronExpr.intAdd(x, ApronExpr.lit(2, Type.IntType), Type.IntType))
          val state2 = apronState.effectStack.getState
          val joinedState = apronState.effectStack.join(state1, state2)
          joinedState.hasChanged shouldBe true
          apronState.effectStack.setState(joinedState.get)
          apronState.getIntInterval(x) shouldBe (0, 20)
          apronState.getIntInterval(ApronExpr.addr(y, Type.IntType)) shouldBe(1, 22)
      }
    }
  }

  test("{x ∈ [0,10], y = x} ⊔ {z ∈ [10,20], x = z} = {x ∈ [0,20], y ∈ [0,10], z ∈ [10,20], 0 <= x + y <= 30, 0 <= x - y <= 20, 10 <= z + x <= 40, 0 <= z - x <= 20, 10 <= z + y <= 30, 0 <= z - y <= 20}") {
    withApronState {
      val apronState = implicitly[ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]]]

      val x = apronState.recencyStore.alloc(Ctx.Var("x"))
      val xAddr = ApronExpr.addr(x, Type.IntType)

      val state0 = apronState.effectStack.getState

      val y = apronState.recencyStore.alloc(Ctx.Var("y"))
      val yAddr = ApronExpr.addr(y, Type.IntType)

      apronState.assign(x, ApronExpr.interval(0, 10, Type.IntType))
      apronState.assign(y, xAddr)

      val state1 = apronState.effectStack.getState

      apronState.effectStack.setState(state0)

      val z = apronState.recencyStore.alloc(Ctx.Var("z"))
      val zAddr = ApronExpr.addr(z, Type.IntType)

      apronState.assign(z, ApronExpr.interval(10, 20, Type.IntType))
      apronState.assign(x, zAddr)

      val state2 = apronState.effectStack.getState

      val joinedState = apronState.effectStack.join(state1, state2)
      joinedState.hasChanged shouldBe true
      apronState.effectStack.setState(joinedState.get)

      apronState.getIntInterval(xAddr) shouldBe(0, 20)
      apronState.getIntInterval(yAddr) shouldBe(0, 10)
      apronState.getIntInterval(zAddr) shouldBe(10, 20)

      apronState.getIntInterval(ApronExpr.intAdd(xAddr, yAddr, Type.IntType)) shouldBe(0, 30)
      if(manager.isInstanceOf[Box])
        apronState.getIntInterval(ApronExpr.intSub(xAddr, yAddr, Type.IntType)) shouldBe(-10, 20)
      else
        apronState.getIntInterval(ApronExpr.intSub(xAddr, yAddr, Type.IntType)) shouldBe(0, 20)

      apronState.getIntInterval(ApronExpr.intAdd(zAddr, xAddr, Type.IntType)) shouldBe(10, 40)
      if(manager.isInstanceOf[Box])
        apronState.getIntInterval(ApronExpr.intSub(zAddr, xAddr, Type.IntType)) shouldBe(-10, 20)
      else
        apronState.getIntInterval(ApronExpr.intSub(zAddr, xAddr, Type.IntType)) shouldBe(0, 20)

      apronState.getIntInterval(ApronExpr.intAdd(zAddr, yAddr, Type.IntType)) shouldBe(10, 30)
      apronState.getIntInterval(ApronExpr.intSub(zAddr, yAddr, Type.IntType)) shouldBe(0, 20)
    }
  }
