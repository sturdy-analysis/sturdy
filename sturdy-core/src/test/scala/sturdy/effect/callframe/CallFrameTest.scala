package sturdy.effect.callframe

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.data
import sturdy.data.MayJoin.WithJoin
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.{*, given}
import sturdy.util.{Lazy, lazily}
import sturdy.values.exceptions.Exceptional
import sturdy.values.{*, given}

class CallFrameTest extends AnyFunSuite:
  given Widen[Powerset[Int]] = null

  val callFrame = new JoinableDecidableCallFrame[Unit, String, Powerset[Int], Unit](initData = (), initVars = Iterable())
  given effectStack: EffectStack = EffectStack(callFrame)

  test("join correctly") {
    callFrame.withNew((), List(("x", Some(Powerset(1)))), ()) {
      effectStack.joinComputations {
        callFrame.setLocalByName("x", Powerset(2))
        ()
      } {
        callFrame.getLocalByName("x") shouldBe JOptionC.Some(Powerset(1))
        callFrame.setLocalByName("x", Powerset(3))
        ()
      }
      callFrame.getLocalByName("x") shouldBe JOptionC.Some(Powerset(2, 3))
    }
  }