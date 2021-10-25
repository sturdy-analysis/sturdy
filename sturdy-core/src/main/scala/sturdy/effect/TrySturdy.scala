package sturdy.effect

trait SturdyException extends Exception:
  def isBottom: Boolean

enum TrySturdy[+A]:
  case Success(a: A)
  case Failure(ex: SturdyException)
  def isSuccess: Boolean = this match
    case Success(_) => true
    case Failure(_) => false
  def isBottom: Boolean = this match
    case Success(_) => false
    case Failure(ex) => ex.isBottom
  def get: A = this match
    case Success(a) => a
    case Failure(ex) => throw ex
  def exception: SturdyException = this match
    case Success(_) => throw new MatchError(this)
    case Failure(ex) => ex
object TrySturdy:
  inline def apply[A](f: => A) =
    try Success(f) catch {
      case ex: SturdyException => Failure(ex)
      case ex => throw ex
    }
