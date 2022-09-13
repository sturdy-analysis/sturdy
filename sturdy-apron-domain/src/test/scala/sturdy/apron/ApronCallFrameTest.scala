package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.*
import gmp.*
import sturdy.data.{JOptionC, CombineUnit, noJoin}
import sturdy.apron.JoinTexpr1Node
import sturdy.effect.EffectStack
import sturdy.effect.callframe.{ApronCallFrame, given}
import sturdy.values.Join
import sturdy.values.Widen
import sturdy.values.{Topped, given}

class ApronCallFrameTest extends AnyFunSuite:

  def integerLit(i: Int): Texpr1Node =
    new Texpr1CstNode(new DoubleScalar(i.toDouble))

  def add(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_ADD, v1, v2)

  def sub(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_SUB, v1, v2)

  def neg(v: Texpr1Node): Texpr1Node =
    new Texpr1UnNode(Texpr1UnNode.OP_NEG, v)

  def interval(from: Int, to: Int): Interval = new Interval(new MpqScalar(from), new MpqScalar(to))


  def createCallFrame(apron: Apron, initData: String, initVars: Iterable[(String, Texpr1Node)] = Iterable.empty)(using Join[Texpr1Node], Widen[Texpr1Node])
    : ApronCallFrame[String, String, Texpr1Node]
    = new ApronCallFrame(apron, initData, Some.apply, _ => None, identity, identity, initVars)

  test("ApronCallFrame bound vars after frame_push and pop") {
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    val callFrame = createCallFrame(apron, "initial call frame")
    implicit val effects: EffectStack = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val r = callFrame.withNew("frame_1", vars) {
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(apron)
    println(apron.getBound(r))

    // z is bound within the new frame
    assert(apron.getBound(r) == interval(-1, -1))
    // but now it is unbound
    assert(callFrame.getLocalByName("z") == JOptionC.none)
  }

  test("ApronCallFrame z = x + y") {
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    val callFrame = createCallFrame(apron, "initial call frame")
    implicit val effects: EffectStack = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val r = callFrame.withNew("frame_1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      callFrame.setLocalByName("z", add(x, y))
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(apron)
    println(apron.getBound(r))

    // z is x + y
    assert(apron.getBound(r) == interval(9, 9))
  }

  test("ApronCallFrame (z = x + y) join (z = x - y)") {
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    val callFrame = createCallFrame(apron, "initial call frame")
    implicit val effects: EffectStack = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val r = callFrame.withNew("frame_1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      println(apron)
      effects.joinComputations {
        callFrame.setLocalByName("z", add(x, y))
        ()
      } {
        callFrame.setLocalByName("z", sub(x, y))
        ()
      }
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(apron)
    println(apron.getBound(r))

    // z is (x + y) join (x - y)
    assert(apron.getBound(r) == interval(1, 9))
  }

  test("ApronCallFrame (z = x +- y); if (z < 1) unreachable else true") {
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    val callFrame = createCallFrame(apron, "initial call frame")
    implicit val effects: EffectStack = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val z = callFrame.withNew("frame_1", vars) {
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

    println(apron)
    println(apron.getBound(z))

    // z < 1   iff   z - 1 < 0  iff  -z + 1 > 0
    // z >= 1  iff   z - 1 >= 0
    val cond = apron.makeConstraint(add(neg(z), integerLit(1)), Tcons1.SUP)

    val r = apron.ifThenElse(cond) {
      throw new Exception("unreachable")
    } {
      Topped.Actual(true)
    }

    // r is true
    println(r)
    assert(r == Topped.Actual(true))
  }

  test("ApronCallFrame (z = x +- y); if (z > 20) unreachable else true") {
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    val callFrame = createCallFrame(apron, "initial call frame")
    implicit val effects: EffectStack = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val z = callFrame.withNew("frame_1", vars) {
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

    println(apron)
    println(apron.getBound(z))

    // z > 20   iff   z - 20 > 0
    val cond = apron.makeConstraint(sub(z, integerLit(20)), Tcons1.SUP)

    val r = apron.ifThenElse(cond) {
      throw new Exception("unreachable")
    } {
      Topped.Actual(true)
    }

    // r is true
    println(r)
    assert(r == Topped.Actual(true))
  }

  test("ApronCallFrame (z = x +- y); if (z > 5) false else true") {
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    val callFrame = createCallFrame(apron, "initial call frame")
    implicit val effects: EffectStack = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val z = callFrame.withNew("frame_1", vars) {
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

    println(apron)
    println(apron.getBound(z))

    // z > 5   iff   z - 5 > 0
    // z <= 5  iff   z - 5 <= 0  iff  -z + 5 > 0
    val cond = apron.makeConstraint(sub(z, integerLit(5)), Tcons1.SUP)

    println(apron)
    val r = apron.ifThenElse(cond) {
      println(apron)
      Topped.Actual(false)
    } {
      println(apron)
      Topped.Actual(true)
    }

    println(apron)

    // r is Top
    println(r)
    assert(r == Topped.Top)
  }

  test("ApronCallFrame frame_1:z = x + y, frame_2:z = x - y") {
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    val callFrame = createCallFrame(apron, "initial call frame")
    implicit val effects: EffectStack = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val (r1, r2) = callFrame.withNew("frame_1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      callFrame.setLocalByName("z", add(x, y))
      val r1 = callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
      val r2 = callFrame.withNew("frame_2", vars) {
        val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
        val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
        callFrame.setLocalByName("z", sub(x, y))
        callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
      }
      (r1, r2)
    }

    println(apron)
    println(apron.getBound(r1))
    println(apron.getBound(r2))

    assert(r1 != r2)
    assert(apron.getBound(r1) == interval(9, 9))
    assert(apron.getBound(r2) == interval(1, 1))
  }

  test("ApronCallFrame frame_1:z = x + y, frame_1:z = x - y") {
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    val callFrame = createCallFrame(apron, "initial call frame")
    implicit val effects: EffectStack = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val (r1, r2) = callFrame.withNew("frame_1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      callFrame.setLocalByName("z", add(x, y))
      val r1 = callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
      val r2 = callFrame.withNew("frame_1", vars) {
        val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
        val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
        callFrame.setLocalByName("z", sub(x, y))
        callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
      }
      (r1, r2)
    }

    println(apron)
    println(apron.getBound(r1))
    println(apron.getBound(r2))

    assert(r1 == r2)
    assert(apron.getBound(r1) == interval(-1, 9))
    assert(apron.getBound(r2) == interval(-1, 9))
  }

  test("ApronCallFrame frame_1:z = x + y => frame_1:z = x - y") {
    val manager = new Polka(false)
    val alloc = ApronAlloc.default(manager)
    implicit val apron: Apron = new Apron(manager, alloc)
    val callFrame = createCallFrame(apron, "initial call frame")
    implicit val effects: EffectStack = new EffectStack(List(callFrame))

    val xval = integerLit(5)
    val yval = add(integerLit(1), integerLit(3))
    val zval = integerLit(-1)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val (r2, r3) = callFrame.withNew("frame_1", vars) {
      val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
      val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
      val r2 = callFrame.withNew("frame_1", vars) {
        val x = callFrame.getLocalByName("x").getOrElse(throw new IllegalStateException("x not found"))
        val y = callFrame.getLocalByName("y").getOrElse(throw new IllegalStateException("y not found"))
        callFrame.setLocalByName("z", sub(x, y))
        callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
      }
      callFrame.setLocalByName("z", add(x, y))
      val r3 = callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
      (r2, r3)
    }

    println(apron)
    println(r2)
    println(apron.getBound(r2))
    println(r3)
    println(apron.getBound(r3))

//    assert(r2 == r3)
    assert(apron.getBound(r2) == interval(-1, 9))
    assert(apron.getBound(r3) == interval(9, 9))
  }
