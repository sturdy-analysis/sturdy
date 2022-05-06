package sturdy.fix.iter

import sturdy.*
import sturdy.fix.Combinator
import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.fix.Contextual
import sturdy.fix
import sturdy.report.Properties
import sturdy.values.{Widen, Finite, Join}

val Property = "iteration strategy"

enum Config:
  case Innermost
  case Outermost
  case Topmost

  override def toString: String = this match
    case Innermost => "innermost"
    case Outermost => "outermost"
    case Topmost => "topmost"
  
  def get[Dom, Codom, In, Out, All, Ctx]
  (using state: AnalysisState[Dom, In, Out, All])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom] = this match
    case Innermost => fix.iter.innermost
    case Outermost => fix.iter.outermost
    case Topmost => fix.iter.topmost
