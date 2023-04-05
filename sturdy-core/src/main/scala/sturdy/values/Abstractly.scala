package sturdy.values

trait Abstractly[C, A]:
  def apply(c: C): A

object Abstractly:
  def apply[C, A](c: C)(using abs: Abstractly[C, A]): A = abs.apply(c)

given concreteAbstractly[T: Structural]: Abstractly[T, T] with
  def apply(c: T): T = c

given EitherAly[C1, C2, A1, A2](using abs1: Abstractly[C1, A1], abs2: Abstractly[C2, A2]): Abstractly[Either[C1, C2], Either[A1, A2]] with
  override def apply(c: Either[C1, C2]): Either[A1, A2] = c match
    case Left(c1) => Left(abs1.apply(c1))
    case Right(c2) => Right(abs2.apply(c2))
