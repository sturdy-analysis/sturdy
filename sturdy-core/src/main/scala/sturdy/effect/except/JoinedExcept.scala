package sturdy.effect.except

import sturdy.data.{*, given}
import sturdy.effect.{Effect, SturdyException}
import sturdy.values.{Join, Widen}
import sturdy.values.exceptions.Exceptional

import scala.collection.mutable.ListBuffer
import scala.util.Success
import sturdy.effect.ComputationJoiner
import sturdy.effect.SturdyThrowable
import sturdy.effect.TrySturdy

case object AbstractSturdyException extends SturdyException:
  override def toString: String = s"Abstract exception"


class JoinedExcept[Exc, E](using val exceptional: Exceptional[Exc, E, WithJoin])(using Join[E], Widen[E]) extends Except[Exc, E, WithJoin]:

  protected var exception: JOptionA[E] = JOptionA.none

  override def throws(ex: Exc): Nothing =
    throwing(ex)
    val e = exceptional.exception(ex)
    this.exception = exception match
      case JOptionA.None() => JOptionA.Some(e)
      case JOptionA.NoneSome(old) => JOptionA.Some(Join(old, e).get)
      case JOptionA.Some(old) => JOptionA.Some(Join(old, e).get)
    throw AbstractSturdyException

  override protected def tries[A](f: => A): JEitherA[A, E] =
    val originalException = this.exception
    this.exception = JOptionA.None()
    try {
      val a = f
      exception match
        case JOptionA.None() => JEitherA.Left(a)
        case JOptionA.Some(ex) => JEitherA.LeftRight(a, ex)
        case JOptionA.NoneSome(ex) => JEitherA.LeftRight(a, ex)
    } catch {
      case exc@AbstractSturdyException =>
        exception match
          case JOptionA.None() => throw new IllegalStateException(s"exception cannot be None here", exc)
          case JOptionA.NoneSome(ex) => JEitherA.Right(ex)
          case JOptionA.Some(ex) => JEitherA.Right(ex)
      case exc: SturdyThrowable =>
        // Our exception may have been replaced by another exception, such as a Failure.
        // We need to resurface our exception here so that it can be handled.
        exception match
          case JOptionA.None() => throw exc
          case JOptionA.NoneSome(ex) => JEitherA.Right(ex)
          case JOptionA.Some(ex) => JEitherA.Right(ex)
    } finally {
      this.exception = originalException
    }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ExceptJoiner)
  private class ExceptJoiner[A] extends ComputationJoiner[A] {
    val snapshot = exception
    var fExcept: JOptionA[E] = null
    
    override def inbetween(): Unit =
      fExcept = exception
      exception = snapshot

    override def retainNone(): Unit =
      exception = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      exception = fExcept

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      exception = fExcept.joinDeep(exception)
  }

  override type State = JOptionA[E]
  override def getState: State = exception
  override def setState(s: State): Unit = exception = s
  override def join: Join[State] = implicitly
  override def widen: Widen[State] = implicitly
