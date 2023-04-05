package sturdy.effect.environment

import sturdy.data.MayJoin

enum Box[V]:
  case Eager(v: V)
  case Lazy(v: () => V)

  lazy val value: V = this match
    case Eager(v) => v
    case Lazy(v) => v()

trait CyclicEnvironment[Var, V, J[_] <: MayJoin[_]] extends Environment[Var, V, J]:
  def bindLazy(x: Var, v: => V): Unit