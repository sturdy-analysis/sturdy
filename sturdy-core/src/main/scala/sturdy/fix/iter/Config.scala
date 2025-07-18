package sturdy.fix.iter

import sturdy.*
import sturdy.control.FixpointControlEvent
import sturdy.fix.{Combinator, Contextual, Stack, StackConfig}
import sturdy.effect.EffectStack
import sturdy.fix
import sturdy.report.Properties
import sturdy.values.{Finite, Join, Widen}

val Property = "iteration strategy"

enum Config:
  case Innermost
  case Outermost
  case Topmost

  override def toString: String = this match
    case Innermost => s"innermost"
    case Outermost => s"outermost"
    case Topmost => s"topmost"
  
  def get[Dom, Codom, Ctx](config: StackConfig)
  (using Join[Codom], Widen[Codom], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom] = this match
    case Innermost => fix.iter.innermost(config)
    case Outermost => fix.iter.outermost(config)
    case Topmost => fix.iter.topmost(config)
