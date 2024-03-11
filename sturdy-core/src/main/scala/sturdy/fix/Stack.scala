package sturdy.fix

import sturdy.control.FixpointControlEvent
import sturdy.values.{Join, Widen}
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
    case StackConfig.StackedStates(readPriorOutput, observers) =>
      StackedStates(state)(new ContextualInStateWidening(contextual)(using state.widenIn), readPriorOutput, observers)
    case StackConfig.StackedCfgNodes(readPriorOutput, onlyWriteInCacheWhenRecurrent, observers) =>
      StackedFrames(state)(contextual, readPriorOutput, onlyWriteInCacheWhenRecurrent) // TODO pass observers

  type FixEvent = FixpointControlEvent[Nothing,Nothing,Nothing,Any]

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
  case StackedStates(readPriorOutput: Boolean = true, observers: Iterable[Stack.FixEvent => Unit] = Seq())
  case StackedCfgNodes(readPriorOutput: Boolean = true, onlyWriteInCacheWhenRecurrent: Boolean = true, observers: Iterable[Stack.FixEvent => Unit] = Seq())

  def withObservers[Fx](newObservers: Iterable[FixpointControlEvent[Nothing,Nothing,Nothing,Fx] => Unit]): StackConfig = this match
    case ss: StackedStates => StackedStates(false, observers = ss.observers ++ newObservers.map(_.asInstanceOf[Stack.FixEvent => Unit]))
    case ss: StackedCfgNodes => StackedCfgNodes(false, observers = ss.observers ++ newObservers.map(_.asInstanceOf[Stack.FixEvent => Unit]))

