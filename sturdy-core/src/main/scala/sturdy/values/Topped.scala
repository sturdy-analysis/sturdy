package sturdy.values

enum Topped[+V]:
  case Top
  case Actual(v: V)

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
  given flatToppedJoin[V]: JoinValue[Topped[V]] with
    def joinValues(v1: Topped[V], v2: Topped[V]): Topped[V] =
      if v1 == v2 then
        v1
      else
        Topped.Top

  given nestedToppedJoin[V](using j: JoinValue[V]): JoinValue[Topped[V]] with
    def joinValues(v1: Topped[V], v2: Topped[V]): Topped[V] = (v1, v2) match
      case (Top, _) => Top
      case (_, Top) => Top
      case (Actual(x1), Actual(x2)) => Actual(j.joinValues(x1, x2))