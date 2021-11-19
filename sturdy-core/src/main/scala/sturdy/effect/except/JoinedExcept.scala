package sturdy.effect.except

import sturdy.data.{*, given}
import sturdy.effect.{Effectful, SturdyException}
import sturdy.values.Join
import sturdy.values.exceptions.Exceptional

import scala.collection.mutable.ListBuffer
import scala.util.Success
import JoinedExcept.*
import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.SturdyThrowable
import sturdy.effect.TrySturdy

case object AbstractSturdyException extends SturdyException:
  override def toString: String = s"Abstract exception"


trait JoinedExcept[Exc, E](using val exceptional: Exceptional[Exc, E, WithJoin], eJoin: Join[E]) extends Except[Exc, E, WithJoin], Effectful:

  protected var exception: JOptionA[E] = JOptionA.none

  def getException: State[Exc, E] = exception

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
      case AbstractSturdyException =>
        exception match
          case JOptionA.None() => throw new IllegalStateException(s"exception cannot be None here")
          case JOptionA.NoneSome(ex) => JEitherA.Right(ex)
          case JOptionA.Some(ex) => JEitherA.Right(ex)
      case exc =>
        // Our exception may have been replaced by another exception, such as a Failure.
        // We need to resurface our exception here so that it can be handled.
        exception match
          case JOptionA.None() => throw exc
          case JOptionA.NoneSome(ex) => JEitherA.Right(ex)
          case JOptionA.Some(ex) => JEitherA.Right(ex)
    } finally {
      this.exception = originalException
    }

  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    val snapshot = exception
    var fExcept: JOptionA[E] = null
    
    override def inbetween_(): Unit =
      fExcept = exception
      exception = snapshot

    override def retainNone_(): Unit =
      exception = snapshot

    override def retainFirst_(fRes: TrySturdy[A]): Unit =
      exception = fExcept

    override def retainSecond_(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      exception = fExcept.joinDeep(exception)
  }
  
object JoinedExcept:
  type State[Exc, E] = JOptionA[E]