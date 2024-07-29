package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.*
import gmp.*
import org.scalatest.Suites
import org.scalatest.matchers.should.Matchers.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.util.TestContexts.Ctx
import sturdy.util.TestTypes.Type
import sturdy.util.{*, given}
import sturdy.values.{Finite, Join}
import sturdy.values.types.BaseType
import sturdy.values.integer.BaseTypeIntegerOps
import sturdy.values.references.{*, given}

class ApronJoinTests extends Suites(
  PolyhedraJoinTest(),
  OctagonJoinTest()
)

class PolyhedraJoinTest extends ApronJoinTest(using new Polka(true))
class OctagonJoinTest extends ApronJoinTest(using new Octagon)

class ApronJoinTest(using manager: apron.Manager) extends AnyFunSuite:

  test("{x ∈ [0,10]} ⊔ {x ∈ [10,20]} = {x ∈ [0,20]}") {
    withApronState {
      val apronState = implicitly[ApronRecencyState[Ctx, Type, ApronExpr[VirtAddr, Type]]]
      apronState.withTempVars(Type.IntType) {
        case (x, List()) =>
          apronState.assign(x, ApronExpr.intInterval(0, 10, Type.IntType))
          val state1 = apronState.effectStack.getState
          apronState.assign(x, ApronExpr.intInterval(10,20, Type.IntType))
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
          apronState.assign(x, ApronExpr.intInterval(0, 20, Type.IntType))
          val state1 = apronState.effectStack.getState
          apronState.assign(x, ApronExpr.intInterval(10, 10, Type.IntType))
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
      apronState.withTempVars(Type.IntType, ApronExpr.intInterval(0, 20, Type.IntType)) {
        case (y, List(x)) =>
          apronState.assign(y, ApronExpr.intAdd(x, ApronExpr.intLit(1, Type.IntType), Type.IntType))
          val state1 = apronState.effectStack.getState
          apronState.assign(y, ApronExpr.intAdd(x, ApronExpr.intLit(2, Type.IntType), Type.IntType))
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

      apronState.assign(x, ApronExpr.intInterval(0, 10, Type.IntType))
      apronState.assign(y, xAddr)

      val state1 = apronState.effectStack.getState

      apronState.effectStack.setState(state0)

      val z = apronState.recencyStore.alloc(Ctx.Var("z"))
      val zAddr = ApronExpr.addr(z, Type.IntType)

      apronState.assign(z, ApronExpr.intInterval(10, 20, Type.IntType))
      apronState.assign(x, zAddr)

      val state2 = apronState.effectStack.getState

      val joinedState = apronState.effectStack.join(state1, state2)
      joinedState.hasChanged shouldBe true
      apronState.effectStack.setState(joinedState.get)

      apronState.getIntInterval(xAddr) shouldBe(0, 20)
      apronState.getIntInterval(yAddr) shouldBe(0, 10)
      apronState.getIntInterval(zAddr) shouldBe(10, 20)

      apronState.getIntInterval(ApronExpr.intAdd(xAddr, yAddr, Type.IntType)) shouldBe(0, 30)
      apronState.getIntInterval(ApronExpr.intSub(xAddr, yAddr, Type.IntType)) shouldBe(0, 20)

      apronState.getIntInterval(ApronExpr.intAdd(zAddr, xAddr, Type.IntType)) shouldBe(10, 40)
      apronState.getIntInterval(ApronExpr.intSub(zAddr, xAddr, Type.IntType)) shouldBe(0, 20)

      apronState.getIntInterval(ApronExpr.intAdd(zAddr, yAddr, Type.IntType)) shouldBe(10, 30)
      apronState.getIntInterval(ApronExpr.intSub(zAddr, yAddr, Type.IntType)) shouldBe(0, 20)
    }
  }
