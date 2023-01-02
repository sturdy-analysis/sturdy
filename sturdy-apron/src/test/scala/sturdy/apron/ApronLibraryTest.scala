package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite

import apron.*
import gmp.*

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
