package sturdy.effect.exception

import sturdy.data.{*, given}
import sturdy.values.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.EffectStack
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.{*, given}
import sturdy.values.exceptions.Exceptional
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.data
import sturdy.util.{Lazy, lazily}

class ExceptionTest extends AnyFunSuite:
  test("Exceptions join correct states") {
    var effectStack: EffectStack = null
    given Lazy[EffectStack] = lazily(effectStack)
    val callFrame = new JoinableDecidableCallFrame[Unit, String, Topped[Int], Unit](initData = (), initVars = Iterable())
    val exceptions = new JoinedExcept[AbstractSturdyException.type, AbsExc]()
    effectStack = EffectStack(callFrame, exceptions)
    given EffectStack = effectStack

    callFrame.withNew((), List(("x", Some(Topped.Actual(1)))), ()) {
      exceptions.tryCatch {
        callFrame.setLocalByName("x", Topped.Actual(2))
        effectStack.joinComputations {

        } {
          exceptions.throws(AbstractSturdyException)
        }
        callFrame.setLocalByName("x", Topped.Actual(1))
        ()
      } {
        case AbstractSturdyException => ()
      }

      callFrame.getLocalByName("x") shouldBe JOptionC.Some(Topped.Top)
    }

  }

  case class AbsExc(state: Any)

  given JoinAbsExc(using lazyEffectStack: Lazy[EffectStack]): Join[AbsExc] with
    override def apply(v1: AbsExc, v2: AbsExc): MaybeChanged[AbsExc] = {
      val effectStack = lazyEffectStack.value
      for {
        joinedState <- effectStack.join(v1.state, v2.state)
      } yield AbsExc(joinedState)
    }

  given WidenAbsExc(using lazyEffectStack: Lazy[EffectStack]): Widen[AbsExc] with
    override def apply(v1: AbsExc, v2: AbsExc): MaybeChanged[AbsExc] = {
      val effectStack = lazyEffectStack.value
      for {
        joinedState <- effectStack.widen(v1.state, v2.state)
      } yield AbsExc(joinedState)
    }

  given StateExceptional(using lazyEffectStack: Lazy[EffectStack]): Exceptional[AbstractSturdyException.type, AbsExc, WithJoin] with
    override def exception(exc: AbstractSturdyException.type): AbsExc = {
      val effectStack = lazyEffectStack.value
      AbsExc(effectStack.getState)
    }

    override def handle[A](exceptionState: AbsExc)(f: AbstractSturdyException.type => A): WithJoin[A] ?=> A = {
      val effectStack = lazyEffectStack.value
      effectStack.setState(exceptionState.state)
      f(AbstractSturdyException)
    }
