package stateful


trait JoinVal[A] extends Function2[A, A, A] {
  def join(a1: A, a2: A): A = apply(a1, a2)

  def bottom: A
  def isBottom(a: A): Boolean = a == bottom
}

object JoinUnit extends JoinVal[Unit] {
  override def bottom: Unit = ()
  override def apply(a1: Unit, a2: Unit): Unit = ()
  implicit val unit: Unit = ()
  implicit val joinUnit: JoinVal[Unit] = this
}

trait JoinComputation {
  // This is the default join for pure computations f and g.
  // Subclasses must override join to join effects and call super.join
  def join[A](f: => A, g: => A)(implicit j: JoinVal[A]): A = {
    val a1 = f
    val a2 = g
    j.join(a1, a2)
  }
}
