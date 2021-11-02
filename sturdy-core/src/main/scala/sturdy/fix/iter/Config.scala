package sturdy.fix.iter

import sturdy.effect.Effectful
import sturdy.fix.Combinator
import sturdy.effect.AnalysisState
import sturdy.fix.Contextual
import sturdy.fix
import sturdy.values.{Widen, Finite}

enum Config:
  case Innermost
  case Topmost

  def get[Dom, Codom, In, Out, All, Ctx]
  (using state: AnalysisState[In, Out, All])
  (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], j: Effectful)
  (using Finite[Dom], Finite[Ctx])
  : Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom] = this match
    case Innermost => fix.iter.innermost
    case Topmost => fix.iter.topmost
