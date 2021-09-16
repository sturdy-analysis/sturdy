package sturdy.effect

import sturdy.values.JoinValue

trait EitherCompute[J[_], A, B]:
  def either[C](f: A => C)(g: B => C): J[C] ?=> C

enum EitherComputeConcrete[A, B] extends EitherCompute[NoJoin, A, B]:
  case Left(a: A)
  case Right(b: B)

  override def either[C](f: A => C)(g: B => C): NoJoin[C] ?=> C = this match
    case Left(a) => f(a)
    case Right(b) => g(b)


enum EitherComputeAbstract[A, B] extends EitherCompute[Join, A, B]:
  case Left(a: A)
  case Right(b: B)
  case LeftRight(a: A, b: B)

  override def either[C](f: A => C)(g: B => C): Join[C] ?=> C = this match
    case Left(a) => f(a)
    case Right(b) => g(b)
    case LeftRight(a, b) => summon[Effectful].joinComputations(f(a))(g(b))

