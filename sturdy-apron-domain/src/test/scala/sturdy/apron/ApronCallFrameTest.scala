package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.*
import gmp.*
import sturdy.data.{noJoin, JOptionC, CombineUnit}
import sturdy.effect.EffectStack
import sturdy.effect.callframe.ApronCallFrame
import sturdy.values.{Topped, given}

class ApronCallFrameTest extends AnyFunSuite:

  def integerLit(i: Int): Texpr1Node = new Texpr1CstNode(new DoubleScalar(i.toDouble))

  def add(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_ADD, v1, v2)

  def sub(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_SUB, v1, v2)

  def neg(v: Texpr1Node): Texpr1Node =
    new Texpr1UnNode(Texpr1UnNode.OP_NEG, v)

  def interval(from: Int, to: Int): Interval = new Interval(new MpqScalar(from), new MpqScalar(to))


  test("ApronCallFrame bound vars after frame push and pop") {
    val manager = new Polka(false)
    val callFrame = new ApronCallFrame[String, String](manager, "initial call frame")

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val r = callFrame.withNew("frame 1", vars) {
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(callFrame)
    println(callFrame.getBound(r))

    // z is bound within the new frame
    assert(callFrame.getBound(r) == interval(-1, -1))
    // but now it is unbound
    assert(callFrame.getLocalByName("z") == JOptionC.none)
  }

  test("ApronCallFrame z = x + y") {
    val manager = new Polka(false)
    val callFrame = new ApronCallFrame[String, String](manager, "initial call frame")

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val r = callFrame.withNew("frame 1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      callFrame.setLocalByName("z", add(x, y))
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(callFrame)
    println(callFrame.getBound(r))

    // z is x + y
    assert(callFrame.getBound(r) == interval(9, 9))
  }

  test("ApronCallFrame (z = x + y) join (z = x - y)") {
    val manager = new Polka(false)
    val callFrame = new ApronCallFrame[String, String](manager, "initial call frame")
    val effects = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val r = callFrame.withNew("frame 1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      effects.joinComputations {
        callFrame.setLocalByName("z", add(x, y))
        ()
      } {
        callFrame.setLocalByName("z", sub(x, y))
        ()
      }
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(callFrame)
    println(callFrame.getBound(r))

    // z is (x + y) join (x - y)
    assert(callFrame.getBound(r) == interval(1, 9))
  }

  test("ApronCallFrame (z = x +- y); if (z < 1) unreachable else true") {
    val manager = new Polka(false)
    val callFrame = new ApronCallFrame[String, String](manager, "initial call frame")
    val effects = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val z = callFrame.withNew("frame 1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      effects.joinComputations {
        callFrame.setLocalByName("z", add(x, y))
        ()
      } {
        callFrame.setLocalByName("z", sub(x, y))
        ()
      }
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(callFrame)
    println(callFrame.getBound(z))

    // z < 1   iff   z - 1 < 0  iff  -z + 1 > 0
    // z >= 1  iff   z - 1 >= 0
    val trueCond = (add(neg(z), integerLit(1)), Tcons1.SUP)
    val falseCond = (sub(z, integerLit(1)), Tcons1.SUPEQ)

    val r = effects.joinComputations {
      callFrame.constrain(trueCond._1, trueCond._2)
      throw new Exception("unreachable")
    } {
      callFrame.constrain(falseCond._1, falseCond._2)
      Topped.Actual(true)
    }

    // r is true
    println(r)
    assert(r == Topped.Actual(true))
  }

  test("ApronCallFrame (z = x +- y); if (z > 20) unreachable else true") {
    val manager = new Polka(false)
    val callFrame = new ApronCallFrame[String, String](manager, "initial call frame")
    val effects = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val z = callFrame.withNew("frame 1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      effects.joinComputations {
        callFrame.setLocalByName("z", add(x, y))
        ()
      } {
        callFrame.setLocalByName("z", sub(x, y))
        ()
      }
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(callFrame)
    println(callFrame.getBound(z))

    // z > 20   iff   z - 20 > 0
    // z <= 20  iff   z - 20 <= 0  iff  -z + 20 > 0
    val trueCond = (sub(z, integerLit(20)), Tcons1.SUP)
    val falseCond = (add(neg(z), integerLit(20)), Tcons1.SUPEQ)

    val r = effects.joinComputations {
      callFrame.constrain(trueCond._1, trueCond._2)
      throw new Exception("unreachable")
    } {
      callFrame.constrain(falseCond._1, falseCond._2)
      Topped.Actual(true)
    }

    // r is true
    println(r)
    assert(r == Topped.Actual(true))
  }

  test("ApronCallFrame (z = x +- y); if (z > 5) false else true") {
    val manager = new Polka(false)
    val callFrame = new ApronCallFrame[String, String](manager, "initial call frame")
    val effects = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val z = callFrame.withNew("frame 1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      effects.joinComputations {
        callFrame.setLocalByName("z", add(x, y))
        ()
      } {
        callFrame.setLocalByName("z", sub(x, y))
        ()
      }
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(callFrame)
    println(callFrame.getBound(z))

    // z > 5   iff   z - 5 > 0
    // z <= 5  iff   z - 5 <= 0  iff  -z + 5 > 0
    val trueCond = (sub(z, integerLit(5)), Tcons1.SUP)
    val falseCond = (add(neg(z), integerLit(5)), Tcons1.SUPEQ)

    val r = effects.joinComputations {
      callFrame.constrain(trueCond._1, trueCond._2)
      Topped.Actual(false)
    } {
      callFrame.constrain(falseCond._1, falseCond._2)
      Topped.Actual(true)
    }

    // r is Top
    println(r)
    assert(r == Topped.Top)
  }
