package sturdy.effect

import sturdy.effect.Effectful.StarvedJoin
import sturdy.effect.except.ExceptException
import sturdy.effect.failure.FailureException
import sturdy.fix.RecurrentCall
import sturdy.values.Join

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait SturdyException extends Exception

enum TrySturdy[A]:
  case Success(a: A)
  case Failure(ex: SturdyException)
  def get: A = this match
    case Success(a) => a
    case Failure(ex) => throw ex
  def exception: SturdyException = this match
    case Success(_) => throw new MatchError(this)
    case Failure(ex) => ex
object TrySturdy:
  def apply[A](f: => A) =
    try Success(f) catch {
      case ex: SturdyException => Failure(ex)
      case ex => throw ex
    }

trait Effectful extends ObservableJoin:
//  type JoinExceptions
  type Joined[A] = Join[A] ?=> A

  final def joinThrowables(failA: SturdyException, failB: SturdyException): SturdyException = (failA, failB) match
    case (_: RecurrentCall[_, _], _) => failB
    case (_, _: RecurrentCall[_, _]) => failA
    case _ => if (failA == failB) failA else StarvedJoin(failA, failB)

  /* This is the default join for pure computations f and g.
   * Subclasses must override join to join effects and call super.join
   */
  def joinComputations[A](f: => A)(g: => A): Joined[A] = {
    this.joinStart()
    val triedF = TrySturdy(f)
    this.joinSwitch()
    val triedG = TrySturdy(g)
    this.joinEnd()

    (triedF, triedG) match
      case (TrySturdy.Failure(failA), TrySturdy.Failure(failB)) => throw joinThrowables(failA, failB)
      case (TrySturdy.Success(aF), TrySturdy.Success(aG)) => Join(aF, aG).get
      case (TrySturdy.Success(aF), _) => aF
      case (_, TrySturdy.Success(aG)) => aG
  }

  def joinWithFailure[A](f: => A)(g: => Nothing): A = {
    val triedF = TrySturdy(f)
    val failB = TrySturdy(g).exception

    triedF match
      case TrySturdy.Success(aF) => aF
      case TrySturdy.Failure(failA) => throw joinThrowables(failA, failB)
  }

  final def joinComputationsIterable[A](as: IterableOnce[() => A]): Joined[A] =
    joinComputationsIt(as.iterator)

  private final def joinComputationsIt[A](as: Iterator[() => A]): Joined[A] =
    val next = as.next()
    if (as.isEmpty)
      next()
    else {
      joinComputations(joinComputationsIt(as))(next())
    }

object Effectful:
  def join[A](f: => A)(g: => A)(using j: Effectful): Join[A] ?=> A =
    j.joinComputations(f)(g)
  case class StarvedJoin(ex1: Throwable, ex2: Throwable) extends Exception(s"Starved Join with $ex1 and $ex2") with SturdyException
