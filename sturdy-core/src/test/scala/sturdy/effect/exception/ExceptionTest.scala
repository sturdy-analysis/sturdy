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

class ExceptionTest extends AnyFunSuite:
  test("Exceptions join correct states") {
    val callFrame = new JoinableDecidableCallFrame[Unit, String, Topped[Int], Unit](initData = (), initVars = Iterable())
    val exceptions = new JoinedExcept[AbstractSturdyException.type, Unit]()
    given effectStack: EffectStack = EffectStack(callFrame, exceptions)

    callFrame.withNew((), List(("x", Some(Topped.Actual(1)))), ()) {
      exceptions.tryCatch {
        callFrame.setLocalByName("x", Topped.Actual(2))
        exceptions.throws(AbstractSturdyException)
        callFrame.setLocalByName("x", Topped.Actual(1))
        ()
      } {
        case AbstractSturdyException => ()
      }
    }

    callFrame.getLocalByName("x") shouldBe JOptionC.Some(Topped.Top)
  }

  given UnitExceptional: Exceptional[AbstractSturdyException.type, Unit, WithJoin] with
    override def exception(exc: AbstractSturdyException.type): Unit = ()
    override def handle[A](e: Unit)(f: AbstractSturdyException.type => A): WithJoin[A] ?=> A =
      f(AbstractSturdyException)
