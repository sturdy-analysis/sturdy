package sturdy.fix

import sturdy.values.{Widen, Join}
import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.effect.TrySturdy
import sturdy.values.Finite
import sturdy.values.MaybeChanged

object Stack:
  def apply[Dom, Codom, In, Out, Ctx](frames: Boolean, contextual: Contextual[Ctx, Dom, Codom])
                                     (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], joinOut: Join[Out], effectStack: EffectStack)
                                     (using Finite[Dom], Finite[Ctx])
                                     : Stack[Dom, Codom, In, Out] =
    if (frames)
      new StackedFrames(contextual)
    else
      new StackedStates(new ContextualInStateWidening(contextual))

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
