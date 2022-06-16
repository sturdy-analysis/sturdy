package sturdy.fix

import sturdy.values.{Widen, Join}
import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.effect.TrySturdy
import sturdy.values.Finite
import sturdy.values.MaybeChanged

object Stack:
  def apply[Dom, Codom, In, Out, Ctx](config: StackConfig, contextual: Contextual[Ctx, Dom, Codom])
                                     (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], joinOut: Join[Out], effectStack: EffectStack)
                                     (using Finite[Dom], Finite[Ctx])
                                     : Stack[Dom, Codom, In, Out] = config match
    case StackConfig.StackedStates(readPriorOutput) =>
      new StackedStates(new ContextualInStateWidening(contextual), readPriorOutput)
    case StackConfig.StackedCfgNodes(readPriorOutput, onlyWriteInCacheWhenRecurrent) =>
      new StackedFrames(contextual, readPriorOutput, onlyWriteInCacheWhenRecurrent)

trait Stack[Dom, Codom, In, Out]:
  enum PushResult:
    case Recurrent(result: TrySturdy[Codom], widenedOut: Option[Out])
    case Continue(widenedIn: Option[In])

  enum PopResult:
    case Stable
    case Unstable(codom: TrySturdy[Codom], widenedOut: Option[Out])

  def push(dom: Dom, in: In, currentOut: Out): PushResult
  def pop(dom: Dom, in: In, codom: TrySturdy[Codom], out: Out): PopResult

  def height: Int
  def hasRecurrentCalls: Boolean

enum StackConfig:
  case StackedStates(readPriorOutput: Boolean = true)
  case StackedCfgNodes(readPriorOutput: Boolean = true, onlyWriteInCacheWhenRecurrent: Boolean = true)
