package sturdy.effect.callframe

import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import apron.*
import gmp.*
import sturdy.apron.{ApronExpr, given}
import sturdy.data.{*, given}
import sturdy.effect.{EffectStack, Stateless, callframe}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.values.Finite
import sturdy.values.references.{AddressTranslation, PhysicalAddress, VirtualAddress}
import sturdy.values.integer.BaseTypeIntegerOps
import sturdy.values.types.{BaseType, given}

class RelationalCallFrameTest extends AnyFunSuite:

  import sturdy.util.TestTypes.*
  import sturdy.util.TestContexts.*

  type Data = Unit
  type Var = String
  type CallSite = String
  type Val = ApronExpr[VirtualAddress[Ctx], Type]
  type ValIntern = ApronExpr[PhysicalAddress[Ctx], Type]
  val intType: Type = Type.IntType

  given failure: Failure = new CollectedFailures[FailureKind]
  given effectState: EffectStack = EffectStack(failure)
  given Finite[FailureKind] with {}

  given Manager = new apron.Polka(true)

  test("getLocal") {
    val (callFrame, state) = RelationalCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.interval[VirtualAddress[Ctx],Type](0, 10, intType)))
    )

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    state.getInterval(xExpr) shouldBe Interval(0,10)
  }

  test("setLocal") {
    val (callFrame, state) = RelationalCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> None)
    )

    callFrame.setLocalByName("x", ApronExpr.interval[VirtualAddress[Ctx],Type](0, 10, intType))

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    state.getInterval(xExpr) shouldBe Interval(0, 10)
  }

  test("strong update") {
    val (callFrame, state) = RelationalCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> None)
    )

    callFrame.setLocalByName("x", ApronExpr.interval[VirtualAddress[Ctx],Type](0, 10, intType))
    callFrame.setLocalByName("x", ApronExpr.interval[VirtualAddress[Ctx],Type](15, 20, intType))

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    state.getInterval(xExpr) shouldBe Interval(15, 20)
  }

  test("weak update") {
    val (callFrame, state) = RelationalCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.interval[VirtualAddress[Ctx],Type](0, 2, intType)))
    )

    callFrame.withNew((), List("x" -> Some(ApronExpr.interval[VirtualAddress[Ctx],Type](4, 5, intType))), "f") {}
    callFrame.withNew((), List("x" -> Some(ApronExpr.interval[VirtualAddress[Ctx],Type](7, 8, intType))), "f") {}

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    state.getInterval(xExpr) shouldBe Interval(0, 5)
  }


  test("withNew") {
    val (callFrame, state) = RelationalCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.interval[VirtualAddress[Ctx],Type](0, 10, intType)))
    )

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    val xIv = state.getInterval(xExpr)

    callFrame.withNew((), List("y" -> Some(xExpr)), "f") {
      val yExpr = callFrame.getLocalByName("y").getOrElse(fail(s"Variable y not bound in ${callFrame}"))
      state.getInterval(yExpr) shouldBe xIv

      callFrame.getLocalByName("x") shouldBe JOptionC.None()
    }

    callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getLocalByName("y") shouldBe JOptionC.None()
  }

  test("join") {
    val (callFrame, state) = RelationalCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.interval[VirtualAddress[Ctx],Type](0, 10, intType)))
    )

    val state1 = (callFrame.getState, state.recencyStore.getState)

    callFrame.setLocalByName("x", ApronExpr.interval[VirtualAddress[Ctx],Type](5, 15, intType))

    val state2 = (callFrame.getState, state.recencyStore.getState)

    val joinedFrames = callFrame.join(state1._1, state2._1)
    val joinedStores = state.recencyStore.join(state1._2, state2._2)
    joinedFrames.hasChanged shouldBe false
    joinedStores.hasChanged shouldBe true

    callFrame.setState(joinedFrames.get)
    state.recencyStore.setState(joinedStores.get)

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    state.getInterval(xExpr) shouldBe Interval(0, 15)
  }

  test("join recent against old") {
    val (callFrame, state) = RelationalCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.interval[VirtualAddress[Ctx],Type](0, 2, intType)))
    )

    val state1 = callFrame.getState

    val state2 = callFrame.withNew((), List("x" -> Some(ApronExpr.interval[VirtualAddress[Ctx],Type](4, 5, intType))), "f") {
      callFrame.getState
    }

    val joinedStates = callFrame.join(state1, state2)
    joinedStates.hasChanged shouldBe true
    callFrame.setState(joinedStates.get)

    var xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    state.getInterval(xExpr) shouldBe Interval(0, 5)

    // Test that x is old
    callFrame.setLocalByName("x", ApronExpr.interval[VirtualAddress[Ctx],Type](7, 8, intType))
    xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    state.getInterval(xExpr) shouldBe Interval(0, 8)

  }