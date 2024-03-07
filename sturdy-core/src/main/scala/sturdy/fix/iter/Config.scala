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
  case Innermost(config: StackConfig)
  case Outermost(config: StackConfig)
  case Topmost(config: StackConfig)

  def withObservers(newObservers: Iterable[Stack.FixEvent => Unit]): Config = this match
    case Innermost(config) => Innermost(config.withObservers(newObservers))
    case Outermost(config) => Outermost(config.withObservers(newObservers))
    case Topmost(config) => Topmost(config.withObservers(newObservers))

  override def toString: String = this match
    case Innermost(config) => s"innermost($config)"
    case Outermost(config) => s"outermost($config)"
    case Topmost(config) => s"topmost($config)"
  
  def get[Dom, Codom, Ctx]
  (using Widen[Codom], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom] = this match
    case Innermost(config) => fix.iter.innermost(config)
    case Outermost(config) => fix.iter.outermost(config)
    case Topmost(config) => fix.iter.topmost(config)
