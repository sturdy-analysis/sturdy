package sturdy.values

trait PartialOrder[T] extends PartialOrdering[T] {
  def tryCompare(x: T, y: T): Option[Int] = {
    val lte = lteq(x, y)
    val gte = lteq(y, x)
    if (lte && !gte)
      Some(-1)
    else if (lte && gte)
      Some(0)
    else if (!lte && gte)
      Some(1)
    else
      None
  }
}

given PartialOrder[Boolean] with
  def lteq(c1: Boolean, c2: Boolean): Boolean = c1 == c2
