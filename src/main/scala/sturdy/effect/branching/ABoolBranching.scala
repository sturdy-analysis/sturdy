package sturdy.effect.branching

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue
import sturdy.values.Topped

import reflect.Selectable.reflectiveSelectable

trait ABoolBranching[V <: {def asBoolean: Topped[Boolean]}] extends BoolBranching[V], JoinComputation:
  type BoolBranchJoin[A] = JoinValue[A]

  def boolBranch[A](v: V, thn: => A, els: => A): BoolBranchJoined[A] = v.asBoolean match
    case Topped.Actual(true) => thn
    case Topped.Actual(false) => els
    case Topped.Top => joinComputations(thn)(els)
