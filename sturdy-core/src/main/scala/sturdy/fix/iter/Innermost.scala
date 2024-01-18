package sturdy.fix.iter

import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.effect.TrySturdy
import sturdy.fix.{Stack, Combinator, Contextual, StackConfig, Fixpoint, State}
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.{Widen, Join}

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
  (using Finite[Dom], Finite[Ctx], Widen[Codom])
  : Innermost[Dom, Codom, Ctx] =
  new Innermost(config, state, context)


final class Innermost[Dom, Codom, Ctx]
  (config: StackConfig, state: State, context: Contextual[Ctx, Dom, Codom])
  (using Finite[Dom], Finite[Ctx], Widen[Codom])
  extends Combinator[Dom, Codom]:

  private val stack: Stack[Dom, Codom, state.In, state.Out] = Stack(state)(config, context)
  private var iterationCounts: Map[Dom, Int] = Map()

  /** Runs `f` until a fixed point is reached as soon as something is looping. */
  override def apply(f: Dom => Codom): Dom => Codom =
    @tailrec
    def apply_(dom: Dom): Codom = {
      val allState: state.All = state.getAllState
      val (result, loop) = step(f, dom)
      if (loop) {
        if (Fixpoint.DEBUG) {
          val iterationCount = iterationCounts.getOrElse(dom, 2)
          iterationCounts += dom -> (iterationCount + 1)
          println(s"## REPEAT (Iteration $iterationCount) of $dom")
        }
        state.setAllState(allState)
        apply_(dom)
      } else {
        result.getOrThrow
      }
    }
    apply_

  private def step(f: Dom => Codom, dom: Dom): (TrySturdy[Codom], Boolean) =
    val in = state.getInState(dom)
    val outBefore = state.getOutState(dom)
    stack.push(dom, in, outBefore) match
      case stack.PushResult.Recurrent(result, widenedOut) =>
        widenedOut.foreach(state.setOutState(dom, _))
        (result, false)
      case stack.PushResult.Continue(widenedIn) =>
        widenedIn.foreach(state.setInState(dom, _))
        val result = TrySturdy(f(dom))
        val out = state.getOutState(dom)
        stack.pop(dom, widenedIn.getOrElse(in), result, out) match
          case stack.PopResult.Stable =>
            (result, false)
          case stack.PopResult.Unstable(newresult, newout) =>
            newout.foreach(state.setOutState(dom, _))
            (newresult, true)
