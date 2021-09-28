package sturdy.effect.except

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.Join
import sturdy.values.exceptions.Exceptional

import scala.collection.mutable.ListBuffer
import scala.util.Success

import JoinedExcept.*

case object AbstractException extends ExceptException:
  override def toString: String = s"Exception (abstract)"


trait JoinedExcept[Exc, E](using val exceptional: Exceptional[Exc, E, WithJoin], eJoin: Join[E]) extends Except[Exc, E], Effectful:
  override type ExceptJoin[A] = WithJoin[A]

  protected var exception: OptionA[E] = OptionA.none

  def getException: State[Exc, E] = exception

  override def throws(ex: Exc): Nothing =
    val e = exceptional.exception(ex)
    this.exception = exception match
      case OptionA.None() => OptionA.Some(e::Nil)
      case OptionA.NoneSome(old::Nil) => OptionA.Some(Join(old, e)::Nil)
      case OptionA.Some(old::Nil) => OptionA.Some(Join(old, e)::Nil)
      case _ => throw new IllegalStateException()
    throw AbstractException

  override protected def tries[A](f: => A): EitherA[A, E] =
    val res = try {
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
      case ex => throw ex
    }
    // all exceptions are passed to the catch block, which must re-throw them if desired
    this.exception = OptionA.None()
    res

  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    val snapshot = this.exception
    super.joinComputations(f) {
      val fExcept = this.exception
      this.exception = snapshot
      try g finally {
        this.exception = fExcept.joinDeep(this.exception)
      }
    }

object JoinedExcept:
  type State[Exc, E] = OptionA[E]