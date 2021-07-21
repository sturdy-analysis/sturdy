package sturdy.values

trait Abstractly[C, A]:
  def abstractly(c: C): A

object Abstractly:
  def abstractly[C, A](c: C)(using abs: Abstractly[C, A]): A = abs.abstractly(c)

given concreteAbstractly[C]: Abstractly[C, C] with
  def abstractly(c: C): C = c

given eitherAbstractly[C1, C2, A1, A2](using abs1: Abstractly[C1, A1], abs2: Abstractly[C2, A2]): Abstractly[Either[C1, C2], Either[A1, A2]] with
  override def abstractly(c: Either[C1, C2]): Either[A1, A2] = c match
    case Left(c1) => Left(abs1.abstractly(c1))
    case Right(c2) => Right(abs2.abstractly(c2))
