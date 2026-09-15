package sturdy.values

enum Topped[+V] extends Iterable[V]:
  case Top
  case Actual(v: V)

  def isActual: Boolean = this match
    case Top => false
    case Actual(_) => true

  def isTop: Boolean = this match
    case Top => true
    case Actual(_) => false

  override def iterator: Iterator[V] = this match
    case Top => Iterator.empty
    case Actual(v) => Iterator.single(v)

  inline def get: V = this match
    case Top => throw new MatchError(this)
    case Actual(v) => v

  override def foreach[A](f: V => A): Unit = this match
    case Top => // nothing
    case Actual(v) => f(v)

  override def filter(f: V => Boolean): Topped[V] = this match
    case Top => Top
    case Actual(v) => if (f(v)) this else Top

  override def toString(): String = this match
    case Top => s"Top"
    case Actual(v) => v.toString

  final def toString(suffix: String): String = this match
    case Top => s"Top$suffix"
    case Actual(v) => v.toString

  override final def map[A](f: V => A): Topped[A] = this match
    case Top => Top
    case Actual(v) => Actual(f(v))

  inline final def flatMap[A](f: V => Topped[A]): Topped[A] = this match
    case Top => Top
    case Actual(v) => f(v)

  inline def binary[B, AA >: V](f: (V, AA) => B, other: Topped[AA]): Topped[B] =
    for (i1 <- this; i2 <- other) yield f(i1, i2)

  inline def unary[B](f: V => B): Topped[B] = map(f)

  def toOption: scala.Option[V] = this match
    case Top => scala.None
    case Actual(v) => scala.Some(v)

given toppedAbstractly[C, A](using abs: Abstractly[C, A]): Abstractly[C, Topped[A]] with
  override def apply(c: C): Topped[A] = Topped.Actual(abs.apply(c))

given toppedPartialOrder[A](using po: PartialOrder[A]): PartialOrder[Topped[A]] with
  override def lteq(x: Topped[A], y: Topped[A]): Boolean = (x, y) match
    case (_, Topped.Top) => true
    case (Topped.Top, _) => false
    case (Topped.Actual(a1), Topped.Actual(a2)) => po.lteq(a1, a2)

given TopTopped[V]: Top[Topped[V]] with
  override def top: Topped[V] = Topped.Top

given JoinToppedFlat[V, W <: Widening](using Structural[V]): Combine[Topped[V], W] with
  def apply(v1: Topped[V], v2: Topped[V]): MaybeChanged[Topped[V]] = (v1, v2) match
    case (Topped.Top, _) => Unchanged(Topped.Top)
    case (_, Topped.Top) => Changed(Topped.Top)
    case (Topped.Actual(x1), Topped.Actual(x2)) => if (x1 == x2) Unchanged(v1) else Changed(Topped.Top)

given CombineToppedDeep[V, W <: Widening](using j: Combine[V, W]): Combine[Topped[V], W] with
  def apply(v1: Topped[V], v2: Topped[V]): MaybeChanged[Topped[V]] = (v1, v2) match
    case (Topped.Top, _) => Unchanged(Topped.Top)
    case (_, Topped.Top) => Changed(Topped.Top)
    case (Topped.Actual(x1), Topped.Actual(x2)) => j(x1, x2).map(Topped.Actual.apply)

given OrderingTopped[V](using orderingV: Ordering[V]): Ordering[Topped[V]] with
  override def compare(x: Topped[V], y: Topped[V]): Int =
    (x,y) match
      case (Topped.Top, Topped.Top) => 0
      case (Topped.Actual(i), Topped.Actual(j)) => orderingV.compare(i,j)
      case _ => Ordering.by[Topped[V], Int] {
        case Topped.Top => 1
        case _: Topped.Actual[V] => 2
      }.compare(x,y)