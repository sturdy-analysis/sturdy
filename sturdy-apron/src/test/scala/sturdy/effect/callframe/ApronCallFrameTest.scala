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
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.effect.store.RecencyClosure
import sturdy.values.Finite
import sturdy.values.references.{AddressTranslation, VirtualAddress}
import sturdy.values.integer.TypeIntegerOps
import sturdy.values.types.{BaseType, given}

class ApronCallFrameTest extends AnyFunSuite:

  type Data = Unit
  type Var = String
  type CallSite = String
  type Ctx = Unit
  type Context = LocalVariableContext[Var, Ctx]
  type Type = BaseType[Int]
  val intType: Type = BaseType[Int]

  given Finite[Context] with {}
  given failure: Failure = new CollectedFailures[FailureKind]
  given effectState: EffectStack = EffectStack(List(failure))
  given Finite[FailureKind] with {}

  val manager = new apron.Polka(true)

  given contextInsensitive: HasContext[Ctx] with
    override def currentContext: Ctx = ()


  test("getLocal") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.Constant(Interval(0, 10), intType))),
      manager
    )

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0,10)
  }

  test("setLocal") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> None),
      manager
    )

    callFrame.setLocalByName("x", ApronExpr.intInterval(0, 10))

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0, 10)
  }

  test("strong update") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> None),
      manager
    )

    callFrame.setLocalByName("x", ApronExpr.intInterval(0, 10))
    callFrame.setLocalByName("x", ApronExpr.intInterval(15, 20))

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(15, 20)
  }

  test("weak update") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.intInterval(0, 2))),
      manager
    )

    callFrame.withNew((), List("x" -> Some(ApronExpr.intInterval(4, 5))), "f") {}
    callFrame.withNew((), List("x" -> Some(ApronExpr.intInterval(7, 8))), "f") {}

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0, 5)
  }


  test("withNew") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.intInterval(0, 10))),
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

  test("join") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.intInterval(0, 10))),
      manager
    )

    val state1 = callFrame.getState

    callFrame.setLocalByName("x", ApronExpr.intInterval(5, 15))

    val state2 = callFrame.getState

    val joinedStates = callFrame.join(state1, state2)

    joinedStates.hasChanged shouldBe true

    callFrame.setState(joinedStates.get)

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0, 15)
  }

  test("join recent against old") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.intInterval(0, 2))),
      manager
    )

    val state1 = callFrame.getState

    val state2 = callFrame.withNew((), List("x" -> Some(ApronExpr.intInterval(4, 5))), "f") {
      callFrame.getState
    }

    val joinedStates = callFrame.join(state1, state2)
    joinedStates.hasChanged shouldBe true
    callFrame.setState(joinedStates.get)

    var xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0, 5)

    // Test that x is old
    callFrame.setLocalByName("x", ApronExpr.intInterval(7, 8))
    xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0, 8)

  }