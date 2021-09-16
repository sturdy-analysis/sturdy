package sturdy.effect.branching

import sturdy.effect.Effectful
import sturdy.values.JoinValue
import sturdy.values.Topped

import reflect.Selectable.reflectiveSelectable

trait ABoolBranching[V <: {def asBoolean: Topped[Boolean]}] extends BoolBranching[V], Effectful:
  type BoolBranchJoin[A] = JoinValue[A]

  def boolBranch[A](v: V, thn: => A, els: => A): BoolBranchJoin[A] ?=> A =
    val bool = v.asBoolean
    bool match
      case Topped.Actual(true) => thn
      case Topped.Actual(false) => els
      case Topped.Top => joinComputations(thn)(els)
