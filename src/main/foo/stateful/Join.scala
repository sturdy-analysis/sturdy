package stateful

import scala.util.{Try, Success, Failure}


trait Join[A] extends Function2[A, A, A] {
  def join(a1: A, a2: A): A = apply(a1, a2)
}

object JoinUnit extends Join[Unit] {
  override def apply(a1: Unit, a2: Unit): Unit = ()
  implicit val unit: Unit = ()
  implicit val joinUnit: Join[Unit] = this
}

trait JoinComputation {
  // This is the default join for pure computations f and g.
  // Subclasses must override join to join effects and call super.join
  def join[A](f: => A, g: => A)(implicit j: Join[A]): A = {
    (Try(f), Try(g)) match {
      case (Success(a1), Success(a2)) => j.join(a1, a2)
      case (Success(a1), _) => a1
      case (_, Success(a2)) => a2
      case (Failure(fail), _) => throw fail
    }
  }
}
