package sturdy.data

import sturdy.effect.Effect

trait JEither[J[_] <: MayJoin[_], A, B]:
  def either[C](f: A => C)(g: B => C): J[C] ?=> C
  def left: J[A] ?=> A = either(identity)(throw new MatchError(this))
  def right: J[B] ?=> B = either(throw new MatchError(this))(identity)

enum JEitherC[A, B] extends JEither[NoJoin, A, B]:
  case Left(a: A)
  case Right(b: B)

  override def either[C](f: A => C)(g: B => C): NoJoin[C] ?=> C = this match
    case Left(a) => f(a)
    case Right(b) => g(b)


enum JEitherA[A, B] extends JEither[WithJoin, A, B]:
  case Left(as: A)
  case Right(bs: B)
  case LeftRight(as: A, bs: B)

  override def either[C](f: A => C)(g: B => C): WithJoin[C] ?=> C = this match
    case Left(a) => f(a)
    case Right(b) => g(b)
    case LeftRight(a, b) => joinComputations(f(a))(g(b))

given EitherOrdering[A: Ordering, B: Ordering]: Ordering[Either[A, B]] = {
  case (Left(l1), Left(l2)) => Ordering[A].compare(l1, l2)
  case (Right(r1), Right(r2)) => Ordering[B].compare(r1, r2)
  case (Left(_), Right(_)) => 1
  case (Right(_), Left(_)) => -1
}