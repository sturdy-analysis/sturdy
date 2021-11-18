package sturdy.values.booleans

import sturdy.data.{joinComputations, WithJoin, NoJoin, unit}
import sturdy.values.{Topped, Join}
import sturdy.effect.failure.Failure
import sturdy.effect.Effectful

given ToppedBooleanBranching[B, R](using ops: BooleanBranching[B, R])(using WithJoin[R]): BooleanBranching[Topped[B], R] with
  override def boolBranch(v: Topped[B], thn: => R, els: => R): R =
    v match
      case Topped.Top => joinComputations(thn)(els)
      case Topped.Actual(b) => ops.boolBranch(b, thn, els)
