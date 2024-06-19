package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.*
import gmp.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.effect.EffectStack
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.values.{Finite, Join}
import sturdy.values.types.BaseType
import sturdy.values.integer.BaseTypeIntegerOps

class ApronJoinTest extends AnyFunSuite:

  val x = ApronVar("x")
  val y = ApronVar("y")
  val z = ApronVar("z")
  val env = new Environment(Array[Var](), Array[Var]())
  val env_x = env.add(Array[Var](x), Array[Var]())
  val env_y = env.add(Array[Var](y), Array[Var]())
  val env_xy = env.add(Array[Var](x,y), Array[Var]())
  val env_xz = env.add(Array[Var](x,z), Array[Var]())
  val manager: Manager = new Polka(false)
  val state1 = new Abstract1(manager, env)
  given failure: Failure = new CollectedFailures[FailureKind]
  given effectState: EffectStack = EffectStack(failure)
  given Finite[FailureKind] with {}

  test("{x ∈ [0,10]} ⊔ {x ∈ [10,20]} = {x ∈ [0,20]}") {
    val state2 = state1.changeEnvironmentCopy(manager, env_x, false)
                       .assignCopy(manager, x, ApronExpr.intInterval(0, 10, BaseType[Int]).toIntern(env_x), null)
    val state3 = state1.changeEnvironmentCopy(manager, env_x, false)
                       .assignCopy(manager, x, ApronExpr.intInterval(10, 20, BaseType[Int]).toIntern(env_x), null)
    val joined = Join(state2, state3)

    joined.hasChanged shouldBe true
    joined.get.getBound(manager, x) shouldBe Interval(0,20)
  }

  test("{x ∈ [0,20]} ⊔ {x ∈ [10,10]} = {x ∈ [0,20]}") {
    val state2 = state1.changeEnvironmentCopy(manager, env_x, false)
                       .assignCopy(manager, x, ApronExpr.Constant(Interval(0, 20), BaseType[Int]).toIntern(env_x), null)
    val state3 = state1.changeEnvironmentCopy(manager, env_x, false)
                       .assignCopy(manager, x, ApronExpr.Constant(Interval(10, 20), BaseType[Int]).toIntern(env_x), null)
    val joined = Join(state2, state3)

    joined.hasChanged shouldBe false
    joined.get.getBound(manager, x) shouldBe Interval(0, 20)
  }

  test("{x ∈ [0,20], y = x + 1} ⊔ {x ∈ [0,20], y = x + 2} = {x ∈ [0,20], x + 1 <= y <= x + 2}") {
    val state2 = state1.changeEnvironmentCopy(manager, env_xy, false)
                       .assignCopy(manager, x, ApronExpr.Constant(Interval(0, 20), BaseType[Int]).toIntern(env_xy), null)
                       .assignCopy(manager, y, ApronExpr.intAdd[Int,String,BaseType[Int]](ApronExpr.Addr(x, BaseType[Int]), ApronExpr.intLit(1, BaseType[Int])).toIntern(env_xy), null)
    val state3 = state1.changeEnvironmentCopy(manager, env_xy, false)
                       .assignCopy(manager, x, ApronExpr.Constant(Interval(0, 20), BaseType[Int]).toIntern(env_xy), null)
                       .assignCopy(manager, y, ApronExpr.intAdd[Int,String,BaseType[Int]](ApronExpr.Addr(x, BaseType[Int]), ApronExpr.intLit(2, BaseType[Int])).toIntern(env_xy), null)
    val joined = Join(state2, state3)

    joined.hasChanged shouldBe true
    joined.get.getBound(manager, x) shouldBe Interval(0, 20)
    joined.get.getBound(manager, y) shouldBe Interval(1, 22)
  }


  test("{x ∈ [10,20]} ⊔ {y ∈ [10,20]} = {x ∈ [10,20], y ∈ [10,20]}") {
    val state2 = state1.changeEnvironmentCopy(manager, env_x, false)
                       .assignCopy(manager, x, ApronExpr.Constant(Interval(10, 20), BaseType[Int]).toIntern(env_x), null)
    val state3 = state1.changeEnvironmentCopy(manager, env_y, false)
                       .assignCopy(manager, y, ApronExpr.Constant(Interval(10, 20), BaseType[Int]).toIntern(env_y), null)
    val joined = Join(state2, state3)

    joined.hasChanged shouldBe true
    joined.get.getBound(manager, x) shouldBe Interval(10, 20)
    joined.get.getBound(manager, y) shouldBe Interval(10, 20)
  }

  test("{x = 1, y = 2} ⊔ {x = 3, z = 4} = {x ∈ [1,3], y = 2, z = 4}") {
    val state2 = state1.changeEnvironmentCopy(manager, env_xy, false)
      .assignCopy(manager, x, ApronExpr.Constant(Interval(1, 1), BaseType[Int]).toIntern(env_xy), null)
      .assignCopy(manager, y, ApronExpr.Constant(Interval(2, 2), BaseType[Int]).toIntern(env_xy), null)
    val state3 = state1.changeEnvironmentCopy(manager, env_xz, false)
      .assignCopy(manager, x, ApronExpr.Constant(Interval(3, 3), BaseType[Int]).toIntern(env_xz), null)
      .assignCopy(manager, z, ApronExpr.Constant(Interval(4, 4), BaseType[Int]).toIntern(env_xz), null)
    val joined = Join(state2, state3)

    joined.hasChanged shouldBe true
    joined.get.getBound(manager, x) shouldBe Interval(1, 3)
    joined.get.getBound(manager, y) shouldBe Interval(2, 2)
    joined.get.getBound(manager, z) shouldBe Interval(4, 4)
  }