//package sturdy.effect.failure
//
//import sturdy.effect.Stateless
//import sturdy.effect.SturdyFailure
//
//import scala.util.control.NonFatal
//
//case class CFailureException(kind: FailureKind, msg: String) extends SturdyFailure:
//  override def toString: String = s"Failure $kind: $msg"
//
//trait CFailure extends Failure, Stateless:
//  override def fail(kind: FailureKind, msg: String): Nothing =
//    throw CFailureException(kind, msg)
//
//  def fallible[A](f: => A): AFallible[A] =
//    try {
//      val res = f
//      AFallible.Unfailing(res)
//    } catch {
//      case CFailureException(kind, msg) => AFallible.Failing(kind, msg)
//      case ex => throw ex
//    }
