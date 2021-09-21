package sturdy.effect.branching

import sturdy.effect.failure.Failure

import reflect.Selectable.reflectiveSelectable

trait CBoolBranching[V <: {def asBoolean(using Failure): Boolean}] extends BoolBranching[V] with Failure:
  type BoolBranchJoin[A] = Unit

  def boolBranch[A](v: V, thn: => A, els: => A): BoolBranchJoin[A] ?=> A =
    if v.asBoolean(using this) then
      thn
    else
      els
