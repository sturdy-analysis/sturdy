package sturdy.data

import sturdy.effect.Effectful

trait Either[J[_], A, B]:
  def either[C](f: A => C)(g: B => C): J[C] ?=> C
  def left: J[A] ?=> A = either(identity)(throw new MatchError(this))
  def right: J[B] ?=> B = either(throw new MatchError(this))(identity)

enum EitherC[A, B] extends Either[NoJoin, A, B]:
  case Left(a: A)
  case Right(b: B)

  override def either[C](f: A => C)(g: B => C): NoJoin[C] ?=> C = this match
    case Left(a) => f(a)
    case Right(b) => g(b)


enum EitherA[A, B] extends Either[WithJoin, A, B]:
  case Left(as: Iterable[A])
  case Right(bs: Iterable[B])
  case LeftRight(as: Iterable[A], bs: Iterable[B])

  override def either[C](f: A => C)(g: B => C): WithJoin[C] ?=> C = this match
    case Left(as) => mapJoin(as, f)
    case Right(bs) => mapJoin(bs, g)
    case LeftRight(as, bs) => joinComputations(mapJoin(as, f))(mapJoin(bs, g))

