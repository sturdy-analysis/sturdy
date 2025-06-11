package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import apron.*
import gmp.*
import sturdy.apron.ApronExpr.bottomInterval
import sturdy.values.floating.FloatSpecials
import sturdy.values.references.{*, given}
import sturdy.values.types.BaseType

class ApronLibraryTest extends AnyFunSuite:

  test("ApronLibrary can be loaded") {

    val inames = Array("x", "y", "z")
    val fnames: Array[String] = Array()
    val env = new Environment(inames, fnames)

    val polyManager: Manager = new Polka(false)
    val aState = new Abstract1(polyManager, env)

    val yz_add: Texpr1Intern =
      new Texpr1Intern(
        env,
        new Texpr1BinNode(
          Texpr1BinNode.OP_ADD,
          new Texpr1VarNode("y"),
          new Texpr1VarNode("z")
        )
      )

    val y_eq_zero =
      new Tcons1(
        Tcons1.EQ,
        new Texpr1Intern(
          env,
          new Texpr1VarNode("y")
        )
      )

    aState.assign(polyManager, "x", yz_add, null)
    println(aState)

    aState.meet(polyManager, y_eq_zero)
    println(aState)

    val asItvs = aState.toBox(polyManager)
    asItvs.foreach(println)
  }

  test("{x ∈ [1,2], y ∈ [3,4], z ∈ [6,7]}.fold([x,y,z]) = { x ∈ [1,7] }") {

    val x = ApronVar("x")
    val y = ApronVar("y")
    val z = ApronVar("z")
    val inames = Array[Var](x, y, z)
    val fnames: Array[Var] = Array()
    val env = new Environment(inames, fnames)
    val manager: Manager = new Polka(false)
    val aState = new Abstract1(manager, env)

    aState.assign(manager, x, ApronExpr.constant(Interval(1, 2), BaseType[Int]).toIntern(env), null)
    aState.assign(manager, y, ApronExpr.constant(Interval(3, 4), BaseType[Int]).toIntern(env), null)
    aState.assign(manager, z, ApronExpr.constant(Interval(6, 7), BaseType[Int]).toIntern(env), null)
    aState.fold(manager, Array[Var](x,y,z))

    aState.getBound(manager, x) shouldBe Interval(1,7)
    aState.getEnvironment.hasVar(y) shouldBe false
    aState.getEnvironment.hasVar(z) shouldBe false

    val cons = ApronCons.lt(ApronExpr.addr("x", BaseType[Int]), ApronExpr.floatConstant(bottomInterval, FloatSpecials.PosInfinity, BaseType[Int]))
    aState.satisfy(manager, cons.toApron(aState.getEnvironment)) shouldBe false
    aState.satisfy(manager, cons.negated.toApron(aState.getEnvironment)) shouldBe false
  }

  def compare(manager: Manager, a1: Abstract1, a2: Abstract1) =
    a1.isEqual(manager,a2) shouldBe true
    a2.isEqual(manager,a1) shouldBe true
    a1.hashCode(manager) shouldEqual (a2.hashCode(manager))
    a2.hashCode(manager) shouldEqual (a1.hashCode(manager))


  test("{x = y, y ∈ [0,100]}.assign(x, x+1) = { x = y+1, y ∈ [0,100] }") {

    val x = ApronVar("x")
    val y = ApronVar("y")
    val inames = Array[Var](x, y)
    val fnames: Array[Var] = Array()
    val env = new Environment(inames, fnames)
    val manager: Manager = new Polka(false)
    val aState = new Abstract1(manager, env)

    aState.assign(manager, y, ApronExpr.constant(Interval(0, 100), BaseType[Int]).toIntern(env), null)
    aState.assign(manager, x, ApronExpr.addr("y", BaseType[Int]).toIntern(env), null)
    aState.assign(manager, x, ApronExpr.intAdd(ApronExpr.addr("x", BaseType[Int]), ApronExpr.intLit(1, BaseType[Int]), BaseType[Int]).toIntern(env), null)

    aState.getBound(manager, x) shouldBe Interval(1, 101)
    aState.getBound(manager, y) shouldBe Interval(0, 100)
    aState.satisfy(manager,
      ApronCons.eq(
        ApronExpr.addr("x", BaseType[Int]),
        ApronExpr.intAdd(ApronExpr.addr("y", BaseType[Int]), ApronExpr.intLit(1, BaseType[Int]), BaseType[Int])
      ).toApron(env)
    ) shouldBe true
  }


  test("{x = y}.meet(x != y) == bottom") {
    val x = ApronVar("x")
    val y = ApronVar("y")
    val inames = Array[Var](x, y)
    val fnames: Array[Var] = Array()
    val env = new Environment(inames, fnames)
    val manager: Manager = new Polka(true)
    val aState = new Abstract1(manager, env)

    aState.assign(manager, x, ApronExpr.addr("y", BaseType[Int]).toIntern(env), null)
//    aState.meet(manager, ApronCons.neq(ApronExpr.addr("x", BaseType[Int]), ApronExpr.addr("y", BaseType[Int])).toApron(env))
    val state1 = aState.meetCopy(manager, ApronCons.lt(ApronExpr.addr("x", BaseType[Int]), ApronExpr.addr("y", BaseType[Int])).toApron(env))
    val state2 = aState.meetCopy(manager, ApronCons.lt(ApronExpr.addr("y", BaseType[Int]), ApronExpr.addr("x", BaseType[Int])).toApron(env))
    val stateJoined = state1.joinCopy(manager, state2)
    stateJoined.isBottom(manager) shouldBe true
  }

  test("modulo") {
    val env = new Environment()
    val manager: Manager = new Polka(false)
    val a1 = new Abstract1(manager, env)
    def modulo(n: Int, m: Int) =
      a1.getBound(manager, ApronExpr.intMod(ApronExpr.intLit(n, BaseType[Int]), ApronExpr.intLit(m, BaseType[Int]), BaseType[Int]).toIntern(env))

    modulo(5, 5) shouldBe Interval(0,0)
    modulo(6, 5) shouldBe Interval(1,1)
    modulo(-1, 5) shouldBe Interval(-1,-1)
    modulo(-5, 5) shouldBe Interval(0,0)
    modulo(-6, 5) shouldBe Interval(-1,-1)
  }

  test("Abstract1.hashcode()") {
    val x = "x"
    val y = "y"
    val z = "z"
    val inames = Array(x, y, z)
    val fnames: Array[String] = Array()
    val env = new Environment(inames, fnames)
    val manager: Manager = new Polka(false)

    for(i <- 1 until 100) {
      val a1 = new Abstract1(manager, env)
      val a2 = new Abstract1(manager, env)
      compare(manager, a1, a2)

      a1.assign(manager, x, ApronExpr.constant(Interval(1, 2), BaseType[Int]).toIntern(env), null)
      val a1_v1 = new Abstract1(manager, a1)
      val a1_v1_hashcode = a1.hashCode(manager)
      compare(manager, a1, a1_v1)

      a1.assign(manager, y, ApronExpr.constant(Interval(3, 4), BaseType[Int]).toIntern(env), null)
      val a1_v2 = new Abstract1(manager, a1)
      val a1_v2_hashcode = a1.hashCode(manager)
      compare(manager, a1, a1_v2)

      a1.assign(manager, z, ApronExpr.constant(Interval(5, 6), BaseType[Int]).toIntern(env), null)
      val a1_v3 = new Abstract1(manager, a1)
      val a1_v3_hashcode = a1.hashCode(manager)
      compare(manager, a1, a1_v3)

      a2.assign(manager, z, ApronExpr.constant(Interval(5, 6), BaseType[Int]).toIntern(env), null)
      val a2_v1 = new Abstract1(manager, a2)
      val a2_v1_hashcode = a2.hashCode(manager)

      a2.assign(manager, y, ApronExpr.constant(Interval(3, 4), BaseType[Int]).toIntern(env), null)
      val a2_v2 = new Abstract1(manager, a2)
      val a2_v2_hashcode = a2.hashCode(manager)

      a2.assign(manager, x, ApronExpr.constant(Interval(1, 2), BaseType[Int]).toIntern(env), null)
      val a2_v3 = new Abstract1(manager, a2)
      val a2_v3_hashcode = a2.hashCode(manager)

      a1_v1.hashCode(manager) shouldBe a1_v1_hashcode
      a1_v2.hashCode(manager) shouldBe a1_v2_hashcode
      a1_v3.hashCode(manager) shouldBe a1_v3_hashcode

      a2_v1.hashCode(manager) shouldBe a2_v1_hashcode
      a2_v2.hashCode(manager) shouldBe a2_v2_hashcode
      a2_v3.hashCode(manager) shouldBe a2_v3_hashcode

      compare(manager, a1, a2)
      a1_v3_hashcode shouldBe a2_v3_hashcode
    }
  }