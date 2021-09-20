package sturdy.values

trait Top[T]:
  def top: T
object Top:
  def top[T](using t: Top[T]): T = t.top

trait PartialOrder[T] extends PartialOrdering[T]:
  def tryCompare(x: T, y: T): Option[Int] =
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

object PartialOrder:
  def apply[T](using po: PartialOrder[T]): PartialOrder[T] = po

given concretePO[T: Structural] : PartialOrder[T] with
  def lteq(c1: T, c2: T): Boolean = c1 == c2

given eitherPartialOrder[T1, T2](using po1: PartialOrder[T1], po2: PartialOrder[T2]): PartialOrder[Either[T1, T2]] with
  override def lteq(x: Either[T1, T2], y: Either[T1, T2]): Boolean = (x, y) match
    case (Left(x1), Left(y1)) => po1.lteq(x1, y1)
    case (Right(x2), Right(y2)) => po2.lteq(x2, y2)
    case _ => false
