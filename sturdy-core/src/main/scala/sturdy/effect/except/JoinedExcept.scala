package sturdy.effect.except

import sturdy.data.{*, given}
import sturdy.effect.Effectful
import sturdy.values.Join
import sturdy.values.exceptions.Exceptional

import scala.collection.mutable.ListBuffer
import scala.util.Success
import JoinedExcept.*
import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.SturdyException
import sturdy.effect.TrySturdy

case object AbstractSturdyException extends ExceptSturdyException:
  override def toString: String = s"Abstract exception"


trait JoinedExcept[Exc <: LanguageException, E](using val exceptional: Exceptional[Exc, E, WithJoin], eJoin: Join[E]) extends Except[Exc, E, WithJoin], Effectful:

  protected var exception: OptionA[E] = OptionA.none

  def getException: State[Exc, E] = exception

  override def throws(ex: Exc): Nothing =
    throwing(ex)
    val e = exceptional.exception(ex)
    this.exception = exception match
      case OptionA.None() => OptionA.Some(e)
      case OptionA.NoneSome(old) => OptionA.Some(Join(old, e).get)
      case OptionA.Some(old) => OptionA.Some(Join(old, e).get)
    throw AbstractSturdyException

  override protected def tries[A](f: => A): EitherA[A, E] =
    val originalException = this.exception
    this.exception = OptionA.None()
    try {
      val a = f
      exception match
        case OptionA.None() => EitherA.Left(a)
        case OptionA.Some(ex) => EitherA.LeftRight(a, ex)
        case OptionA.NoneSome(ex) => EitherA.LeftRight(a, ex)
    } catch {
      case AbstractSturdyException =>
        exception match
          case OptionA.None() => throw new IllegalStateException(s"exception cannot be None here")
          case OptionA.NoneSome(ex) => EitherA.Right(ex)
          case OptionA.Some(ex) => EitherA.Right(ex)
      case ex =>
        exception match
          case OptionA.None() => throw ex
          case OptionA.NoneSome(ex) => EitherA.Right(ex)
          case OptionA.Some(ex) => EitherA.Right(ex)
    } finally {
      this.exception = originalException
    }

  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    val snapshot = exception
    var fExcept: OptionA[E] = null
    
    override def inbetween_(): Unit =
      fExcept = exception
      exception = snapshot

    override def retainOnlyFirst_(fRes: TrySturdy[A]): Unit =
      exception = fExcept

    override def retainOnlySecond_(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      exception = fExcept.joinDeep(exception)
  }
  
object JoinedExcept:
  type State[Exc, E] = OptionA[E]