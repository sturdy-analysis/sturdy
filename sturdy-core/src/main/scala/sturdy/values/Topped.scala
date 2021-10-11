package sturdy.values

enum Topped[+V]:
  case Top
  case Actual(v: V)

  inline def get: V = this match
    case Top => throw new MatchError(this)
    case Actual(v) => v

  inline def foreach[A](f: V => A): Unit = this match
    case Top => // nothing
    case Actual(v) => f(v)

  inline def filter(f: V => Boolean): Topped[V] = this match
    case Top => Top
    case Actual(v) => if (f(v)) this else Top

  inline def withFilter(f: V => Boolean): Topped[V] =
    filter(f)

  final def toString(suffix: String): String = this match
    case Top => s"Top$suffix"
    case Actual(v) => v.toString

  inline final def map[A](f: V => A): Topped[A] = this match
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
  override def abstractly(c: C): Topped[A] = Topped.Actual(abs.abstractly(c))

given toppedPartialOrder[A](using po: PartialOrder[A]): PartialOrder[Topped[A]] with
  override def lteq(x: Topped[A], y: Topped[A]): Boolean = (x, y) match
    case (_, Topped.Top) => true
    case (Topped.Top, _) => false
    case (Topped.Actual(a1), Topped.Actual(a2)) => po.lteq(a1, a2)

given TopTopped[V]: Top[Topped[V]] with
  override def top: Topped[V] = Topped.Top

object Topped:
  given CombineToppedFlat[V, W <: Widening]: Combine[Topped[V], W] with
    def apply(v1: Topped[V], v2: Topped[V]): MaybeChanged[Topped[V]] = (v1, v2) match
      case (Top, _) => Unchanged(Top)
      case (_, Top) => Changed(Top)
      case (Actual(x1), Actual(x2)) => if (x1 == x2) Unchanged(v1) else Changed(Topped.Top)

  given CombineToppedDeep[V, W <: Widening](using j: Combine[V, W]): Combine[Topped[V], W] with
    def apply(v1: Topped[V], v2: Topped[V]): MaybeChanged[Topped[V]] = (v1, v2) match
      case (Top, _) => Unchanged(Top)
      case (_, Top) => Changed(Top)
      case (Actual(x1), Actual(x2)) => j(x1, x2).map(Actual.apply)

