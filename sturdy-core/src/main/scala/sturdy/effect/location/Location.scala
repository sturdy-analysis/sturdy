package sturdy.effect.location

import sturdy.data.MayJoin
import sturdy.effect.{Effect, Stateless}
import sturdy.values
import sturdy.values.{Finite, Join, MaybeChanged, Widen}
import sturdy.data.JoinMap
import sturdy.data.CombineFiniteKeyMap

trait Location[Loc, J[_] <: MayJoin[_]] extends Effect:
  def withLoc[R](loc: Loc)(f: => R): R

class NoLocation[Loc, J[_] <: MayJoin[_]] extends Location[Loc, J], Stateless {
  override inline def withLoc[R](a: Loc)(f: => R): R = f
}

class WithLocation[Loc, J[_] <: MayJoin[_]] extends Location[Loc, J]:
  var c : Option[Loc] = Option.empty
  def getLoc : Option[Loc] = c

  override def withLoc[R](a: Loc)(f: => R): R = {
    val snapshot = c
    c = Some(a)
    try f finally c = snapshot
  }

  override type State = Option[Loc]
  override def getState: State = c
  override def setState(st: State): Unit = c = st
  override def join: Join[State] = (v1: State, v2: State) => {
    assert(v1 == v2)
    MaybeChanged.Unchanged(v1)
  }
  override def widen: Widen[State] = (v1: State, v2: State) => {
    assert(v1 == v2)
    MaybeChanged.Unchanged(v1)
  }