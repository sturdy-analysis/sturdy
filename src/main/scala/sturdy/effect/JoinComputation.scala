package sturdy.effect

import sturdy.effect.JoinComputation.StarvedJoin
import sturdy.effect.except.ExceptException
import sturdy.effect.failure.FailureException
import sturdy.fix.RecurrentCall
import sturdy.values.JoinValue

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait JoinComputation:
  type JoinExceptions
  type Join[A] = JoinValue[A] ?=> A

  def joinFailedComputations(failA: Throwable, failB: Throwable): Throwable = (failA, failB) match
    case (RecurrentCall, RecurrentCall) => failA
    case _ => StarvedJoin(failA, failB)

  /* This is the default join for pure computations f and g.
   * Subclasses must override join to join effects and call super.join
   */
  def joinComputations[A](f: => A)(g: => A): Join[A] = {
    val triedF = Try(f)
    val triedG = Try(g)

    (triedF, triedG) match
      case (Success(aF), Success(aG)) => summon[JoinValue[A]].joinValues(aF, aG)
      case (Success(aF), _) => aF
      case (_, Success(aG)) => aG
      case (Failure(failA), Failure(failB)) => throw joinFailedComputations(failA, failB)
  }

  final def joinComputationsIterable[A](as: IterableOnce[() => A]): Join[A] =
    joinComputationsIt(as.iterator)
    
  private final def joinComputationsIt[A](as: Iterator[() => A]): Join[A] =
    val next = as.next()
    if (as.isEmpty)
      next()
    else {
      joinComputations(next())(joinComputationsIt(as))
    }

object JoinComputation:
  case class StarvedJoin(ex1: Throwable, ex2: Throwable) extends Throwable(s"Starved Join with $ex1 and $ex2")
