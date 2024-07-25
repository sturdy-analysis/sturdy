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

//    val state2 = state1.changeEnvironmentCopy(manager, env_xy, false)
//                       .assignCopy(manager, x, ApronExpr.Constant(Interval(0, 20), BaseType[Int]).toIntern(env_xy), null)
//                       .assignCopy(manager, y, ApronExpr.intAdd[Int,String,BaseType[Int]](ApronExpr.Addr(x, BaseType[Int]), ApronExpr.intLit(1, BaseType[Int])).toIntern(env_xy), null)
//    val state3 = state1.changeEnvironmentCopy(manager, env_xy, false)
//                       .assignCopy(manager, x, ApronExpr.Constant(Interval(0, 20), BaseType[Int]).toIntern(env_xy), null)
//                       .assignCopy(manager, y, ApronExpr.intAdd[Int,String,BaseType[Int]](ApronExpr.Addr(x, BaseType[Int]), ApronExpr.intLit(2, BaseType[Int])).toIntern(env_xy), null)
//    val joined = Join(state2, state3)
//
//    joined.hasChanged shouldBe true
//    joined.get.getBound(manager, x) shouldBe Interval(0, 20)
//    joined.get.getBound(manager, y) shouldBe Interval(1, 22)
  }
//
//
//  test("{x ∈ [10,20]} ⊔ {y ∈ [10,20]} = {x ∈ [10,20], y ∈ [10,20]}") {
//    val state2 = state1.changeEnvironmentCopy(manager, env_x, false)
//                       .assignCopy(manager, x, ApronExpr.Constant(Interval(10, 20), BaseType[Int]).toIntern(env_x), null)
//    val state3 = state1.changeEnvironmentCopy(manager, env_y, false)
//                       .assignCopy(manager, y, ApronExpr.Constant(Interval(10, 20), BaseType[Int]).toIntern(env_y), null)
//    val joined = Join(state2, state3)
//
//    joined.hasChanged shouldBe true
//    joined.get.getBound(manager, x) shouldBe Interval(10, 20)
//    joined.get.getBound(manager, y) shouldBe Interval(10, 20)
//  }
//
//  test("{x = 1, y = 2} ⊔ {x = 3, z = 4} = {x ∈ [1,3], y = 2, z = 4}") {
//    val state2 = state1.changeEnvironmentCopy(manager, env_xy, false)
//      .assignCopy(manager, x, ApronExpr.Constant(Interval(1, 1), BaseType[Int]).toIntern(env_xy), null)
//      .assignCopy(manager, y, ApronExpr.Constant(Interval(2, 2), BaseType[Int]).toIntern(env_xy), null)
//    val state3 = state1.changeEnvironmentCopy(manager, env_xz, false)
//      .assignCopy(manager, x, ApronExpr.Constant(Interval(3, 3), BaseType[Int]).toIntern(env_xz), null)
//      .assignCopy(manager, z, ApronExpr.Constant(Interval(4, 4), BaseType[Int]).toIntern(env_xz), null)
//    val joined = Join(state2, state3)
//
//    joined.hasChanged shouldBe true
//    joined.get.getBound(manager, x) shouldBe Interval(1, 3)
//    joined.get.getBound(manager, y) shouldBe Interval(2, 2)
//    joined.get.getBound(manager, z) shouldBe Interval(4, 4)
//  }