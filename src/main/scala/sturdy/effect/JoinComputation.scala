package sturdy.effect

import sturdy.effect.JoinComputation.StarvedJoin
import sturdy.values.JoinValue

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait JoinComputation:
  type JoinExceptions
  type Join[A] = JoinValue[A] ?=> A

  /* This is the default join for pure computations f and g.
   * Subclasses must override join to join effects and call super.join
   */
  def joinComputations[A](f: => A)(g: => A): Join[A] = {
    val triedF = Try(f)
    val triedG = Try(g)
    (triedF, triedG) match
      case (Success(a1), Success(a2)) => summon[JoinValue[A]].joinValues(a1, a2)
      case (Success(a1), _) => a1
      case (_, Success(a2)) => a2
      case (Failure(ex1), Failure(ex2)) =>
        throw StarvedJoin(ex1, ex2)
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
