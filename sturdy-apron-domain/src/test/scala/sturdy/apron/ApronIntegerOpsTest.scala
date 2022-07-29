package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.*
import gmp.*
import sturdy.data.{CombineUnit, JOptionC, noJoin}
import sturdy.apron.JoinTexpr1Node
import sturdy.effect.{ComputationJoiner, EffectStack}
import sturdy.values.integer.{ApronIntegerOps, IntervalIntegerOps, StandardIntervalIntegerOps}
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.values.Join
import sturdy.values.Widen
import sturdy.values.{Topped, given}


class ApronIntegerOpsTest extends AnyFunSuite:

  class IntApronCallFrame[Data, Var](apron: Apron, initData: Data, initVars: Iterable[(Var, Texpr1Node)] = Iterable.empty)(using Join[Texpr1Node], Widen[Texpr1Node])
    extends ApronCallFrame[Data, Var, Texpr1Node](apron, initData, v => Some(v), _ => None, identity, identity, initVars)

  val manager = new Polka(false)
  implicit val apron: Apron = new Apron(manager)
  var callFrame: IntApronCallFrame[String, String] = null
  implicit val effects: EffectStack = new EffectStack(List(callFrame))
  implicit val intervalOps : IntervalIntegerOps[Int] = implicitly
  implicit val failure : Failure = implicitly
  var intOps = new ApronIntegerOps[Int]
  callFrame = new IntApronCallFrame(apron, "initial call frame")


  test("Minimum"){
    val xval = intOps.integerLit(5)
    val yval = intOps.add(intOps.integerLit(-1), intOps.integerLit(-1))
    val zval = intOps.min(xval, yval)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val z = callFrame.withNew("frame 1", vars) {
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(apron)

    assert(apron.getBound(z) == Interval(-2,-2))
  }

  test("Maximum") {
    val xval = intOps.integerLit(5)
    val yval = intOps.add(intOps.integerLit(1), intOps.integerLit(1))
    val zval = intOps.max(xval, yval)
    val vars = Iterable("x" -> xval, "y" -> yval, "z" -> zval)

    val z = callFrame.withNew("frame 1", vars) {
      callFrame.getLocalByName("z").getOrElse(throw new IllegalStateException("z not found"))
    }

    println(apron)

    assert(apron.getBound(z) == Interval(5, 5))
  }
