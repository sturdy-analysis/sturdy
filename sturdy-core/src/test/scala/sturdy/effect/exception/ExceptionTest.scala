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
  var effectStack: EffectStack = null

  given Lazy[EffectStack] = lazily(effectStack)

  given Widen[Powerset[Int]] = null

  val callFrame = new JoinableDecidableCallFrame[Unit, String, Powerset[Int], Unit](initData = (), initVars = Iterable())
  val exceptions = new JoinedExcept[AbstractSturdyException.type, ExceptionState]()
  effectStack = EffectStack(callFrame, exceptions)

  given EffectStack = effectStack

  test("Exceptions join correct states") {
    callFrame.withNew((), List(("x", Some(Powerset(1)))), ()) {
      exceptions.tryCatch {
        callFrame.setLocalByName("x", Powerset(2))
        effectStack.joinComputations {

        } {
          exceptions.throws(AbstractSturdyException)
        }
        callFrame.setLocalByName("x", Powerset(3))
        ()
      } {
        case AbstractSturdyException => ()
      }

      callFrame.getLocalByName("x") shouldBe JOptionC.Some(Powerset(2,3))
    }
  }

  test("Nested try-catch blocks") {
    callFrame.withNew((), List(("x", Some(Powerset(1)))), ()) {
      exceptions.tryCatch {
        callFrame.setLocalByName("x", Powerset(2))
        exceptions.tryCatch {
          callFrame.setLocalByName("x", Powerset(3))
          effectStack.joinComputations {

          } {
            exceptions.throws(AbstractSturdyException)
          }
          callFrame.setLocalByName("x", Powerset(4))
          ()
        } {
          case (ex:AbstractSturdyException.type) => exceptions.throws(ex)
        }
        callFrame.setLocalByName("x", Powerset(5))
        ()
      } {
        case AbstractSturdyException => ()
      }

      callFrame.getLocalByName("x") shouldBe JOptionC.Some(Powerset(3, 5))
    }
  }



  case class ExceptionState(state: Any)

  given JoinAbsExc(using lazyEffectStack: Lazy[EffectStack]): Join[ExceptionState] with
    override def apply(v1: ExceptionState, v2: ExceptionState): MaybeChanged[ExceptionState] = {
      val effectStack = lazyEffectStack.value
      for {
        joinedState <- effectStack.join(v1.state, v2.state)
      } yield ExceptionState(joinedState)
    }

  given WidenAbsExc(using lazyEffectStack: Lazy[EffectStack]): Widen[ExceptionState] with
    override def apply(v1: ExceptionState, v2: ExceptionState): MaybeChanged[ExceptionState] = {
      val effectStack = lazyEffectStack.value
      for {
        joinedState <- effectStack.widen(v1.state, v2.state)
      } yield ExceptionState(joinedState)
    }

  given StateExceptional(using lazyEffectStack: Lazy[EffectStack]): Exceptional[AbstractSturdyException.type, ExceptionState, WithJoin] with
    override def exception(exc: AbstractSturdyException.type): ExceptionState = {
      val effectStack = lazyEffectStack.value
      ExceptionState(effectStack.getState)
    }

    override def handle[A](exceptionState: ExceptionState)(f: AbstractSturdyException.type => A): WithJoin[A] ?=> A = {
      val effectStack = lazyEffectStack.value
      effectStack.setState(exceptionState.state)
      f(AbstractSturdyException)
    }
