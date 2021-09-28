package sturdy.effect.branching

import sturdy.effect.Effectful
import sturdy.effect.failure.Failure
import sturdy.values.Join
import sturdy.values.Topped

import reflect.Selectable.reflectiveSelectable

trait ABoolBranching[V <: {def asBoolean(using Failure): Topped[Boolean]}] extends BoolBranching[V], Effectful, Failure:
  type BoolBranchJoin[A] = Join[A]

  def boolBranch[A](v: V, thn: => A, els: => A): BoolBranchJoin[A] ?=> A =
    val bool = v.asBoolean(using this)
    bool match
      case Topped.Actual(true) => thn
      case Topped.Actual(false) => els
      case Topped.Top => joinComputations(thn)(els)
  