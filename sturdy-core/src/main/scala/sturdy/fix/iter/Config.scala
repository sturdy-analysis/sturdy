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
  case Innermost(frames: Boolean = true)
  case Outermost(frames: Boolean = true)
  case Topmost(frames: Boolean = true)

  override def toString: String = this match
    case Innermost(frames) => s"innermost(${if (frames) "stacked frames" else "stacked states"})"
    case Outermost(frames) => s"outermost(${if (frames) "stacked frames" else "stacked states"})"
    case Topmost(frames) => s"topmost(${if (frames) "stacked frames" else "stacked states"})"
  
  def get[Dom, Codom, In, Out, All, Ctx]
  (using state: AnalysisState[Dom, In, Out, All])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom] = this match
    case Innermost(frames) => fix.iter.innermost(frames)
    case Outermost(frames) => fix.iter.outermost(frames)
    case Topmost(frames) => fix.iter.topmost(frames)
