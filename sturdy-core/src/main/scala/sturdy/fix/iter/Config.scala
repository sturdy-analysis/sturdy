package sturdy.fix.iter

import sturdy.*
import sturdy.fix.{Combinator, Contextual, StackConfig}
import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.fix
import sturdy.report.Properties
import sturdy.values.{Finite, Join, Widen}

val Property = "iteration strategy"

enum Config:
  case Innermost(config: StackConfig)
  case Outermost(config: StackConfig)
  case Topmost(config: StackConfig)

  override def toString: String = this match
    case Innermost(config) => s"innermost($config)"
    case Outermost(config) => s"outermost($config)"
    case Topmost(config) => s"topmost($config)"
  
  def get[Dom, Codom, In, Out, All, Ctx]
  (using state: AnalysisState[Dom, In, Out, All])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom] = this match
    case Innermost(config) => fix.iter.innermost(config)
    case Outermost(config) => fix.iter.outermost(config)
    case Topmost(config) => fix.iter.topmost(config)
