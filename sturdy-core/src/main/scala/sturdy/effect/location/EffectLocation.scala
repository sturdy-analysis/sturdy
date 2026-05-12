package sturdy.effect.location

import sturdy.data.MayJoin
import sturdy.effect.{Effect, Monotone}
import sturdy.values.{Finite, Join, MaybeChanged, Widen}

class EffectLocation[Loc, J[_] <: MayJoin[_], Var, V](val eff: Effect)(using Finite[Loc]) extends Location[Loc, J], Monotone:
  var m : Map[Loc, eff.State] = Map.empty

  implicit val jState : Join[eff.State] = eff.join
  implicit val wState : Widen[eff.State] = eff.widen

  override def withLoc[R](a: Loc)(f: => R): R = {
    val r = f
    val j = if m.contains(a) then eff.join(m(a), eff.getState) else MaybeChanged.Changed(eff.getState)
    j match {
      case MaybeChanged.Changed(s) => m = m + (a -> s)
      case MaybeChanged.Unchanged(s) => ()
    }
    f
  }