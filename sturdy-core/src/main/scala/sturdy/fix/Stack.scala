package sturdy.fix

import sturdy.values.{Widen, Join}
import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.effect.TrySturdy
import sturdy.values.Finite
import sturdy.values.MaybeChanged

object Stack:
  def apply[Dom, Codom, Ctx](state: State)
                            (config: StackConfig, contextual: Contextual[Ctx, Dom, Codom])
                            (using Finite[Dom], Finite[Ctx], Widen[Codom])
                            : Stack[Dom, Codom, state.In, state.Out] = config match
    case StackConfig.StackedStates(readPriorOutput) =>
      StackedStates(state)(new ContextualInStateWidening(contextual)(using state.widenIn), readPriorOutput)
//    case StackConfig.StackedCfgNodes(readPriorOutput, onlyWriteInCacheWhenRecurrent) =>
//      StackedFrames(state)(contextual, readPriorOutput, onlyWriteInCacheWhenRecurrent)

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
//  case StackedCfgNodes(readPriorOutput: Boolean = true, onlyWriteInCacheWhenRecurrent: Boolean = true)

class FrameInstanceInfo(var frameIdWithInStateOfCache: Option[Int]):
  private var frameCounter: Int = 1

  def this(id: Int) =
    this(Some(id))

  def push(newID: Int): Unit =
    frameIdWithInStateOfCache = Some(newID)
    frameCounter += 1

  def popAndGetIsEmpty(id: Int): Boolean =
    frameIdWithInStateOfCache = None
    frameCounter -= 1
//    assert(frameCounter >= 0)
    frameCounter == 0