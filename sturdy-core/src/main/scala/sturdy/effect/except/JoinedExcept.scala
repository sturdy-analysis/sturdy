package sturdy.effect.except

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.Join
import sturdy.values.exceptions.Exceptional

import scala.collection.mutable.ListBuffer
import scala.util.Success
import JoinedExcept.*
import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.TrySturdy

case object AbstractException extends ExceptException:
  override def toString: String = s"Exception (abstract)"


trait JoinedExcept[Exc, E](using val exceptional: Exceptional[Exc, E, WithJoin], eJoin: Join[E]) extends Except[Exc, E, WithJoin], Effectful:

  protected var exception: OptionA[E] = OptionA.none

  def getException: State[Exc, E] = exception

  override def throws(ex: Exc): Nothing =
    val e = exceptional.exception(ex)
    this.exception = exception match
      case OptionA.None() => OptionA.Some(e::Nil)
      case OptionA.NoneSome(old::Nil) => OptionA.Some(Join(old, e).get::Nil)
      case OptionA.Some(old::Nil) => OptionA.Some(Join(old, e).get::Nil)
      case _ => throw new IllegalStateException()
    throw AbstractException

  override protected def tries[A](f: => A): EitherA[A, E] =
    val originalException = this.exception
    this.exception = OptionA.None()
    try {
      val a = f
      exception match
        case OptionA.None() => EitherA.Left(Iterable.single(a))
        case OptionA.Some(exs) => EitherA.LeftRight(Iterable.single(a), exs)
        case OptionA.NoneSome(exs) => EitherA.LeftRight(Iterable.single(a), exs)
    } catch {
      case AbstractException =>
        exception match
          case OptionA.None() => throw new IllegalStateException(s"exception cannot be None here")
          case OptionA.NoneSome(exs) => EitherA.Right(exs)
          case OptionA.Some(exs) => EitherA.Right(exs)
      case ex =>
        exception match
          case OptionA.None() => throw ex
          case OptionA.NoneSome(exs) => EitherA.Right(exs)
          case OptionA.Some(exs) => EitherA.Right(exs)
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