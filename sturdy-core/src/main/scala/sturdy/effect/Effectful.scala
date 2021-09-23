package sturdy.effect

import sturdy.effect.Effectful.StarvedJoin
import sturdy.effect.except.ExceptException
import sturdy.effect.failure.FailureException
import sturdy.fix.RecurrentCall
import sturdy.values.JoinValue

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait Effectful:
//  type JoinExceptions
  type Joined[A] = JoinValue[A] ?=> A

  final def joinThrowables(failA: Throwable, failB: Throwable): Throwable = (failA, failB) match
    case (failA: RuntimeException, _) => throw failA
    case (_, failB: RuntimeException) => throw failB
    case (RecurrentCall, _) => failB
    case (_, RecurrentCall) => failA
    case _ => if (failA == failB) failA else StarvedJoin(failA, failB)

  /* This is the default join for pure computations f and g.
   * Subclasses must override join to join effects and call super.join
   */
  def joinComputations[A](f: => A)(g: => A): Joined[A] = {
    val triedF = Try(f)
    val triedG = Try(g)

    (triedF, triedG) match
      case (Failure(failA: RuntimeException), _) => throw failA
      case (_, Failure(failB: RuntimeException)) => throw failB
      case (Failure(failA), Failure(failB)) => throw joinThrowables(failA, failB)
      case (Success(aF), Success(aG)) => summon[JoinValue[A]].joinValues(aF, aG)
      case (Success(aF), _) => aF
      case (_, Success(aG)) => aG
  }

  def joinWithFailure[A](f: => A)(g: => Nothing): A = {
    val triedF = Try(f)
    val failB = Try(g).failed.get

    triedF match
      case Success(aF) => aF
      case Failure(failA) => throw joinThrowables(failA, failB)
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
  def join[A](f: => A)(g: => A)(using j: Effectful): JoinValue[A] ?=> A =
    j.joinComputations(f)(g)
  case class StarvedJoin(ex1: Throwable, ex2: Throwable) extends Throwable(s"Starved Join with $ex1 and $ex2")
