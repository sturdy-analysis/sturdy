package sturdy.effect.callframe

import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

import scala.math.Ordering.*
import math.Ordered.orderingToOrdered
import apron.*
import gmp.*
import sturdy.apron.{ApronExpr, given}
import sturdy.data.{*, given}
import sturdy.effect.{EffectStack, Stateless, callframe}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
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
  type Type = BaseType[Int]
  val intType: Type = BaseType[Int]

  enum Ctx:
    case Var(name: String)
    case TempVar(tpe: Type, n: Int)

  given Finite[Ctx] with {}
  given Ordering[Ctx] = {
    case (Ctx.Var(n1), Ctx.Var(n2)) => n1.compare(n2)
    case (Ctx.TempVar(t1, n1), Ctx.TempVar(t2,n2)) => (t1,n1).compare((t2,n2))
    case _ => -1
  }
  given failure: Failure = new CollectedFailures[FailureKind]
  given effectState: EffectStack = EffectStack(List(failure))
  given Finite[FailureKind] with {}
  given variableAllocator: Allocator[Ctx,Var] =
    AAllocatorFromContext(ctx => Ctx.Var(ctx))

  // Unused
  given tempVariableAllocator: Allocator[Ctx,Type] = new Allocator[Ctx,Type] with Stateless:
    var n = 0
    override def alloc(ctx: Type): Ctx =
      n += 1
      Ctx.TempVar(ctx, n)

  given Manager = new apron.Polka(true)

  test("getLocal") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.Constant(Interval(0, 10), intType)))
    )

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0,10)
  }

  test("setLocal") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> None)
    )

    callFrame.setLocalByName("x", ApronExpr.intInterval(0, 10))

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0, 10)
  }

  test("strong update") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> None)
    )

    callFrame.setLocalByName("x", ApronExpr.intInterval(0, 10))
    callFrame.setLocalByName("x", ApronExpr.intInterval(15, 20))

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(15, 20)
  }

  test("weak update") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.intInterval(0, 2)))
    )

    callFrame.withNew((), List("x" -> Some(ApronExpr.intInterval(4, 5))), "f") {}
    callFrame.withNew((), List("x" -> Some(ApronExpr.intInterval(7, 8))), "f") {}

    val xExpr = callFrame.getLocalByName("x").getOrElse(fail(s"Variable x not bound in ${callFrame}"))
    callFrame.getBound(xExpr) shouldBe Interval(0, 5)
  }


  test("withNew") {
    val callFrame = ApronCallFrame[Data, Var, CallSite, Ctx, Type](
      initData = (),
      initVars = List("x" -> Some(ApronExpr.intInterval(0, 10)))
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
      initVars = List("x" -> Some(ApronExpr.intInterval(0, 10)))
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
      initVars = List("x" -> Some(ApronExpr.intInterval(0, 2)))
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