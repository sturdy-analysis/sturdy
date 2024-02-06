package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import apron.*
import gmp.*
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

    val x = "x"
    val y = "y"
    val z = "z"
    val inames = Array(x, y, z)
    val fnames: Array[String] = Array()
    val env = new Environment(inames, fnames)
    val manager: Manager = new Polka(false)
    val aState = new Abstract1(manager, env)

    aState.assign(manager, x, ApronExpr.Constant(Interval(1, 2), BaseType[Int]).toIntern(env), null)
    aState.assign(manager, y, ApronExpr.Constant(Interval(3, 4), BaseType[Int]).toIntern(env), null)
    aState.assign(manager, z, ApronExpr.Constant(Interval(6, 7), BaseType[Int]).toIntern(env), null)
    aState.fold(manager, Array(x,y,z))

    aState.getBound(manager, x) shouldBe Interval(1,7)
    aState.getEnvironment.hasVar(y) shouldBe false
    aState.getEnvironment.hasVar(z) shouldBe false
  }