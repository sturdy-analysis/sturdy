package sturdy.effect.callframe

import apron.*
import gmp.*
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import sturdy.apron.ApronExpr
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.callframe.{HasContext, LocalVariableContext}
import sturdy.effect.store.RecencyClosure
import sturdy.values.Finite
import sturdy.values.references.{AddressTranslation, VirtualAddress}

class ApronCallFrameTest extends AnyFunSuite:

  type Data = Unit
  type Var = String
  type CallSite = String
  type Ctx = Unit
  type Context = LocalVariableContext[Var, Ctx]

  given Finite[Context] with {}

  val manager = new apron.Polka(true)

  given contextInsensitive: HasContext[Ctx] with
    override def currentContext: Ctx = ()


  test("getLocal") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.Constant(Interval(0, 10)))),
      manager
    )

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0,10)

    println(callFrame.getState)
  }

  test("setLocal") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx](
      initData = (),
      initVars = List("x" -> None),
      manager
    )

    callFrame.setLocalByName("x", ApronExpr.Constant(Interval(0, 10)))

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0, 10)
  }

  test("strong update") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx](
      initData = (),
      initVars = List("x" -> None),
      manager
    )

    callFrame.setLocalByName("x", ApronExpr.Constant(Interval(0, 10)))
    callFrame.setLocalByName("x", ApronExpr.Constant(Interval(15, 20)))

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(15, 20)
  }

  test("weak update") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.Constant(Interval(0, 2)))),
      manager
    )

    callFrame.withNew((), List("x" -> Some(ApronExpr.Constant(Interval(4, 5)))), "f") {}
    callFrame.withNew((), List("x" -> Some(ApronExpr.Constant(Interval(7, 8)))), "f") {}

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0, 5)
  }


  test("withNew") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.Constant(Interval(0, 10)))),
      manager
    )

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    val xIv = callFrame.getBound(xExpr)

    callFrame.withNew((), List("y" -> Some(xExpr)), "f") {
      val yExpr = callFrame.getLocalByName("y").getOrElse(fail(s"Variable y not bound in ${callFrame}"))
      callFrame.getBound(yExpr) shouldBe xIv

      callFrame.getLocalByName("x") shouldBe JOptionC.None()
    }

    callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getLocalByName("y") shouldBe JOptionC.None()
  }

//  test("EffectStack.join") {
//    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx](
//      initData = (),
//      initVars = List("x" -> Some(ApronExpr.Constant(Interval(0, 10)))),
//      manager
//    )
//    val closure = new RecencyClosure[Ctx](callFrame.getAddressTranslation, callFrame)(using callFrame.closedEquality)
//    val effectStack: EffectStack = new EffectStack(List(closure))
//
//
//    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
//    val xIv = callFrame.getBound(xExpr)
//
//    callFrame.withNew((), List("y" -> Some(xExpr)), "f") {
//      val yExpr = callFrame.getLocalByName("y").getOrElse(fail(s"Variable y not bound in ${callFrame}"))
//      callFrame.getBound(yExpr) shouldBe xIv
//
//      callFrame.getLocalByName("x") shouldBe JOptionC.None()
//    }
//
//    callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
//    callFrame.getLocalByName("y") shouldBe JOptionC.None()
//  }