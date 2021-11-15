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



enum TrySturdy[+A]:
  protected case Success(a: A)
  protected case Failure(f: SturdyFailure)
  protected case Exception(e: SturdyException)
  protected case Recurrent(rc: RecurrentCall)

  def isSuccess: Boolean = this match
    case _: Success[_] => true
    case _ => false
  def isBottom: Boolean = this match
    case _: Failure[_] | _: Recurrent[_] => true
    case _ => false
  def get: Option[A] = this match
    case Success(a) => Some(a)
    case _ => None
  def getOrThrow: A = this match
    case Success(a) => a
    case Failure(f) => throw f
    case Exception(e) => throw e
    case Recurrent(rc) => throw rc
  def throwable: SturdyThrowable = this match
    case Success(_) => throw new MatchError(this)
    case Failure(f) => f
    case Exception(e) => e
    case Recurrent(rc) => rc

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
    case (TrySturdy.Exception(e1), TrySturdy.Exception(e2)) =>
      if (e1 == e2) MaybeChanged.Unchanged(v1)
      else throw IllegalArgumentException(s"Cannot join conflicting exceptions $e1 and $e2")
    case (TrySturdy.Failure(f1), TrySturdy.Failure(f2)) =>
      if (f1 == f2) MaybeChanged.Unchanged(v1)
      else throw IllegalArgumentException(s"Cannot join conflicting failures $f1 and $f2")
    case _ => throw new IllegalArgumentException(s"Cannot join $v1 and $v2")


given CombineTrySturdy[A, W <: Widening](using Combine[A, W]): Combine[TrySturdy[A], W] with
  override def apply(v1: TrySturdy[A], v2: TrySturdy[A]): MaybeChanged[TrySturdy[A]] = TrySturdy.combine(v1, v2)