package sturdy.values

enum Topped[+V]:
  case Top
  case Actual(v: V)

  def foreach[A](f: V => A): Unit = this match
    case Top => // nothing
    case Actual(v) => f(v)

  def filter(f: V => Boolean): Topped[V] = this match
    case Top => Top
    case Actual(v) => if (f(v)) this else Top

  def withFilter(f: V => Boolean): Topped[V] =
    filter(f)

  final def toString(suffix: String): String = this match
    case Top => s"Top$suffix"
    case Actual(v) => v.toString

  final def map[A](f: V => A): Topped[A] = this match
    case Top => Top
    case Actual(v) => Actual(f(v))

  final def flatMap[A](f: V => Topped[A]): Topped[A] = this match
    case Top => Top
    case Actual(v) => f(v)

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