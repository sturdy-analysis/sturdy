package sturdy.fix.iter

import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.effect.TrySturdy
import sturdy.fix.{Combinator, Contextual, Fixpoint, HasFixpointCache, Stack, StackConfig, StackedFrames, StackedStates, State}
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.{Join, Widen}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

/**
 * Fixpoint combinator [[Innermost]] iterates on the innermost strongly-connected subgraphs of the call graph of the abstract interpreter.
 * The combinator uses widening on the output of the abstract interpreter to avoid non-termination.
 * Furthermore, the combinator assumes that every recursive call chain of the abstract interpreter contains a recurrent call.
 */
def innermost[Dom, Codom, Ctx]
  (config: StackConfig)
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: State)
  (using Finite[Dom], Finite[Ctx], Join[Codom], Widen[Codom])
  : Innermost[Dom, Codom, Ctx] =
  new Innermost(config, state, context)


final class Innermost[Dom, Codom, Ctx]
  (config: StackConfig, state: State, context: Contextual[Ctx, Dom, Codom])
  (using Finite[Dom], Finite[Ctx], Join[Codom], Widen[Codom])
  extends Combinator[Dom, Codom], HasFixpointCache[Dom, Codom]:

  private val stack: Stack[Dom, Codom, state.In, state.Out] = Stack(state)(config, context)
  private var iterationCounts: Map[Dom, Int] = Map()

  override def getCache: Map[Dom, TrySturdy[Codom]] = stack.getCache

  /** Runs `f` until a fixed point is reached as soon as something is looping. */
  override def apply(f: Dom => Codom): Dom => Codom =
    @tailrec
    def apply_(iterate: Boolean)(dom: Dom): Codom = {
      val allState: state.All = state.getAllState
      val (result, loop) = step(f, dom, iterate)
      if (loop) {
        if (Fixpoint.DEBUG) {
          val iterationCount = iterationCounts.getOrElse(dom, 2)
          iterationCounts += dom -> (iterationCount + 1)
          println(s"## REPEAT (Iteration $iterationCount) of $dom:${state.getInState(dom)}")
        }
        state.setAllState(allState)
        apply_(iterate = true)(dom)
      } else {
        result.getOrThrow
      }
    }
    apply_(stack.height == 0)

  private def step(f: Dom => Codom, dom: Dom, iterate: Boolean): (TrySturdy[Codom], Boolean) =
    val in = state.getInState(dom)
    val outBefore = state.getOutState(dom)
    stack.push(dom, in, outBefore, iterate) match
      case stack.PushResult.Skip(result, widenedOut) =>
        widenedOut.foreach(state.setOutState(dom, _))
        (result, false)
      case stack.PushResult.Continue(widenedIn) =>
        widenedIn.foreach(state.setInState(dom, _))
        val result = TrySturdy(f(dom))
        val out = state.getOutState(dom)
        stack.pop(dom, widenedIn.getOrElse(in), result, out) match
          case stack.PopResult.Stable(marker) =>
            if (!stack.hasRecurrentCalls)
              marker.markPermanentlyStable()
            (result, false)
          case stack.PopResult.Unstable(newresult, newout) =>
            newout.foreach(state.setOutStateNonMonotonically(dom, _))
            (newresult, true)
