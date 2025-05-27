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
                            (using Finite[Dom], Finite[Ctx], Join[Codom], Widen[Codom])
                            : Stack[Dom, Codom, state.In, state.Out] = config match
    case StackConfig.StackedStates(readPriorOutput, storeNonrecursiveOutput, observers) =>
      StackedStates(state)(new ContextualInStateWidening(contextual)(using state.stackWiden), readPriorOutput, storeNonrecursiveOutput, observers)
    case StackConfig.StackedCfgNodes(readPriorOutput, onlyWriteInCacheWhenRecurrent, observers) =>
      StackedFrames(state)(contextual, readPriorOutput, onlyWriteInCacheWhenRecurrent) // TODO pass observers

  type FixEvent = FixpointControlEvent[Nothing,Nothing,Nothing,Any]

trait Stack[Dom, Codom, In, Out] extends HasFixpointCache[Dom, Codom]:
  enum PushResult:
    case Recurrent(result: TrySturdy[Codom], widenedOut: Option[Out])
    case Continue(widenedIn: Option[In])

  enum PopResult:
    case Stable
    case Unstable(codom: TrySturdy[Codom], widenedOut: Option[Out])

  def push(dom: Dom, in: In, currentOut: Out, invalidate: Boolean): PushResult
  def pop(dom: Dom, in: In, codom: TrySturdy[Codom], out: Out): PopResult

  def height: Int
  def hasRecurrentCalls: Boolean

enum StackConfig:
  case StackedStates(readPriorOutput: Boolean = true, storeNonrecursiveOutput: Boolean = true, observers: Iterable[Stack.FixEvent => Unit] = Seq())
  case StackedCfgNodes(readPriorOutput: Boolean = true, onlyWriteInCacheWhenRecurrent: Boolean = true, observers: Iterable[Stack.FixEvent => Unit] = Seq())

  def withObservers[Fx](newObservers: Iterable[FixpointControlEvent[Nothing,Nothing,Nothing,Fx] => Unit]): StackConfig = this match
    case ss: StackedStates => StackedStates(ss.readPriorOutput, ss.storeNonrecursiveOutput, observers = ss.observers ++ newObservers.map(_.asInstanceOf[Stack.FixEvent => Unit]))
    case ss: StackedCfgNodes => StackedCfgNodes(ss.readPriorOutput, observers = ss.observers ++ newObservers.map(_.asInstanceOf[Stack.FixEvent => Unit]))


trait HasFixpointCache[Dom, Codom]:
  def getCache: Map[Dom, TrySturdy[Codom]]