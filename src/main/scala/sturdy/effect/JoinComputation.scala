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
  def joinComputations[A](f: => A)(g: => A): Join[A] =
    (Try(f), Try(g)) match
      case (Success(a1), Success(a2)) => summon[JoinValue[A]].joinValues(a1, a2)
      case (Success(a1), _) => a1
      case (_, Success(a2)) => a2
      case (Failure(ex1), Failure(ex2)) =>
        throw StarvedJoin(ex1, ex2)

  final def joinComputationsIt[A](as: IterableOnce[() => A]): Join[A] =
    joinComputationsIt(as.iterator)
    
  final def joinComputationsIt[A](as: Iterator[() => A]): Join[A] =
    if (as.isEmpty) {
      throw new IllegalArgumentException
    } else {
      val a1 = as.next()
      if (as.isEmpty)
        a1()
      else {
        val a2 = as.next()
        if (as.isEmpty)
          joinComputations(a1())(a2())
        else
          joinComputations(a1())(joinComputationsIt(as))
      }
    }

object JoinComputation:
  case class StarvedJoin(ex1: Throwable, ex2: Throwable) extends Throwable(s"Starved Join with $ex1 and $ex2")
