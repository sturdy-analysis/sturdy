package sturdy.effect

import sturdy.fix.Frame
import sturdy.values.Combine
import sturdy.values.MaybeChanged
import sturdy.values.Widening

import java.security.cert.TrustAnchor

/** Throwables of a Sturdy program. */
trait SturdyThrowable extends Throwable

/** Fatal error in interpreted program. */
trait SturdyFailure extends SturdyThrowable
/** Exceptional control-flow in interpreted program. */
trait SturdyException extends SturdyThrowable

case class RecurrentCall(frame: Any) extends SturdyThrowable:
  override def toString: String = s"RecurrentCall $frame"

/**
 * `TrySturdy` is the result of an effectful computation:
 *  - [[TrySturdy.Success]] is the result of a successful computation with a resulting value.
 *  - [[TrySturdy.Failure]] is the result of a computation that crashed due to a fatal error.
 *  - [[TrySturdy.Exception]] is the result of a computation that crashed due to an exception within the evaluated program.
 *  - [[TrySturdy.Recurrent]] is the result of a computation that encountered a recurrent recursive call (a recursive call that reappears further up the stack). 
 */
enum TrySturdy[+A]:
  case Success(a: A)
  case Failure(f: SturdyFailure)
  case Exception(e: SturdyException)
  case Recurrent(rc: RecurrentCall)

  def isSuccess: Boolean = this match
    case _: Success[_] => true
    case _ => false
  def isBottom: Boolean = this match
    case _: Failure[_] | _: Recurrent[_] => true
    // Exception is not bottom, because it represents an actual program execution
    case _ => false 
  def isRecurrent: Boolean = this match
    case _: Recurrent[_] => true
    case _ => false
  def get: Option[A] = this match
    case Success(a) => Some(a)
    case _ => None
  def getOrThrow: A = this match
    case Success(a) => a
    case Failure(f) =>
      throw f
    case Exception(e) => throw e
    case Recurrent(rc) => throw rc
  def throwable: SturdyThrowable = this match
    case Success(_) => throw new MatchError(this)
    case Failure(f) => f
    case Exception(e) => e
    case Recurrent(rc) => rc
  def map[B](f: A => B): TrySturdy[B] = this match
    case Success(a) => Success(f(a))
    case Failure(f) => Failure(f)
    case Exception(e) => Exception(e)
    case Recurrent(rc) => Recurrent(rc)
  def flatMap[B](f: A => TrySturdy[B]): TrySturdy[B] = this match
    case Success(a) => f(a)
    case Failure(f) => Failure(f)
    case Exception(e) => Exception(e)
    case Recurrent(rc) => Recurrent(rc)
object TrySturdy:
  inline def apply[A](f: => A): TrySturdy[A] =
    try Success(f) catch {
      case f: SturdyFailure => Failure(f)
      case e: SturdyException => Exception(e)
      case rc: RecurrentCall => Recurrent(rc)
      case ex => throw ex
    }

  inline def combine[A, W <: Widening](v1: TrySturdy[A], v2: TrySturdy[A])(using Combine[A, W]): MaybeChanged[TrySturdy[A]] = (v1, v2) match
    case (TrySturdy.Success(a1), TrySturdy.Success(a2)) => Combine(a1, a2).map(TrySturdy.Success.apply)
    case (TrySturdy.Success(_), _) => MaybeChanged.Unchanged(v1)
    case (_, TrySturdy.Success(_)) => MaybeChanged.Changed(v2)
    case (_, TrySturdy.Recurrent(_)) => MaybeChanged.Unchanged(v1)
    case (TrySturdy.Recurrent(_), _) => MaybeChanged.Changed(v2)
    case (TrySturdy.Exception(_), TrySturdy.Failure(_)) => MaybeChanged.Unchanged(v1)
    case (TrySturdy.Failure(_), TrySturdy.Exception(_)) => MaybeChanged.Changed(v2)
    case (TrySturdy.Exception(_), TrySturdy.Exception(_)) => MaybeChanged.Unchanged(v1)
    case (TrySturdy.Failure(_), TrySturdy.Failure(_)) => MaybeChanged.Unchanged(v1)
    case _ => throw new IllegalArgumentException(s"Cannot join $v1 and $v2")


given CombineTrySturdy[A, W <: Widening](using Combine[A, W]): Combine[TrySturdy[A], W] with
  override def apply(v1: TrySturdy[A], v2: TrySturdy[A]): MaybeChanged[TrySturdy[A]] = TrySturdy.combine(v1, v2)

object TrySturdyFinally:
  inline def apply[A](f: => A)(g: => Unit): A =
    try {val r = f; g; r} catch
      case e : SturdyThrowable => g; throw e