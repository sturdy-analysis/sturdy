package sturdy.values.booleans

import sturdy.data.{joinComputations, WithJoin, NoJoin, unit}
import sturdy.values.{Topped, Join}
import sturdy.effect.failure.Failure
import sturdy.effect.Effectful

given ToppedBooleanWithJoinBranching[B](using ops: BooleanBranching[B, WithJoin]): BooleanBranching[Topped[B], WithJoin] with
  override def boolBranch[A](v: Topped[B], thn: => A, els: => A): WithJoin[A] ?=> A =
    v match
      case Topped.Top => joinComputations(thn)(els)
      case Topped.Actual(b) => ops.boolBranch(b, thn, els)

given ToppedBooleanNoJoinBranching[B](using ops: BooleanBranching[B, NoJoin]): BooleanBranching[Topped[B], WithJoin] with
  override def boolBranch[A](v: Topped[B], thn: => A, els: => A): WithJoin[A] ?=> A =
    v match
      case Topped.Top => joinComputations(thn)(els)
      case Topped.Actual(b) => ops.boolBranch(b, thn, els)
