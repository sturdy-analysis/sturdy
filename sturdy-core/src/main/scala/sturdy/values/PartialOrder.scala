package sturdy.values

trait Top[T]:
  def top: T
object Top:
  def top[T](using t: Top[T]): T = t.top

given Top[Unit] with
  def top: Unit = ()

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
  def lteq[T](x: T, y: T)(using po: PartialOrder[T]): Boolean = po.lteq(x, y)

given concretePO[T: Structural] : PartialOrder[T] with
  def lteq(c1: T, c2: T): Boolean = c1 == c2

given eitherPartialOrder[A, B](using poA: PartialOrder[A], poB: PartialOrder[B]): PartialOrder[Either[A, B]] with
  override def lteq(x: Either[A, B], y: Either[A, B]): Boolean = (x, y) match
    case (Left(x1), Left(y1)) => poA.lteq(x1, y1)
    case (Right(x2), Right(y2)) => poB.lteq(x2, y2)
    case _ => false

given pairPartialOrder[A,B](using poA: PartialOrder[A], poB: PartialOrder[B]): PartialOrder[(A,B)] with
  override def lteq(x: (A, B), y: (A, B)): Boolean =
    poA.lteq(x._1, y._1) && poB.lteq(x._2, y._2)

given tripplePartialOrder[A,B,C](using poA: PartialOrder[A], poB: PartialOrder[B], poC: PartialOrder[C]): PartialOrder[(A,B,C)] with
  override def lteq(x: (A, B, C), y: (A, B, C)): Boolean =
    poA.lteq(x._1, y._1) && poB.lteq(x._2, y._2) && poC.lteq(x._3, y._3)

given quadruppelPartialOrder[A,B,C,D](using poA: PartialOrder[A], poB: PartialOrder[B], poC: PartialOrder[C], poD: PartialOrder[D]): PartialOrder[(A,B,C,D)] with
  override def lteq(x: (A, B, C, D), y: (A, B, C, D)): Boolean =
    poA.lteq(x._1, y._1) && poB.lteq(x._2, y._2) && poC.lteq(x._3, y._3) && poD.lteq(x._4, y._4)

given listPartialOrder[A](using poA: PartialOrder[A]): PartialOrder[List[A]] with
  override def lteq(xs: List[A], ys: List[A]): Boolean =
    if(xs.size == ys.size) {
      xs.zip(ys).forall(poA.lteq)
    } else {
      false
    }