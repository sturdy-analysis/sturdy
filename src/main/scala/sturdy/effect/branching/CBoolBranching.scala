package sturdy.effect.branching

import reflect.Selectable.reflectiveSelectable

trait CBoolBranching[V <: {def asBoolean: Boolean}] extends BoolBranching[V]:
  type BoolBranchJoin[A] = Unit

  def boolBranch[A](v: V, thn: => A, els: => A): BoolBranchJoined[A] =
    if v.asBoolean then
      thn
    else
      els
