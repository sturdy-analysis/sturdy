package sturdy.fix.iter

import sturdy.effect.EffectStack
import sturdy.effect.TrySturdy
import sturdy.fix.{Combinator, Contextual, Fixpoint, HasFixpointCache, Stack, StackConfig, StackedFrames, State}
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.{Join, Widen}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

/** Fixpoint combinator [[Outermost]] iterates on the innermost strongly-connected subgraphs of the call graph of the abstract interpreter.
 * The combinator uses widening on the output of the abstract interpreter to avoid non-termination.
 * Furthermore, the combinator assumes that every recursive call chain of the abstract interpreter contains a recurrent call.
 */
def outermost[Dom, Codom, In, Out, All, Ctx]
  (config: StackConfig)
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: State)
  (using Finite[Dom], Finite[Ctx], Join[Codom], Widen[Codom])
  : Outermost[Dom, Codom, In, Out, All, Ctx] =
  new Outermost(config, state, context)

final class Outermost[Dom, Codom, In, Out, All, Ctx]
  (config: StackConfig, state: State, context: Contextual[Ctx, Dom, Codom])
  (using Finite[Dom], Finite[Ctx], Join[Codom], Widen[Codom])
  extends Combinator[Dom, Codom], HasFixpointCache[Dom, Codom]:

  override def equals(obj: Any): Boolean = super.equals(obj)

  private val stack: Stack[Dom, Codom, state.In, state.Out] = Stack(state)(config, context)
  private var someComponentIsLooping: Boolean = false
  private var iterationCount: Int = 1

  override def getCache: Map[Dom, TrySturdy[Codom]] = stack.getCache

  /** Runs `f`. If this is the outermost call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    @tailrec
    def apply_(dom: Dom): Codom = {
      val allState: state.All = state.getAllState
      val (result, isOutermost) = step(f, dom)
      if (isOutermost && someComponentIsLooping) {
        if (Fixpoint.DEBUG) {
          iterationCount += 1
          println(s"## REPEAT (Iteration $iterationCount) of $dom")
        }
        someComponentIsLooping = false
        state.setAllState(allState)
        apply_(dom)
      } else
        result.getOrThrow
    }
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior. */
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
        val wasRecurrent = stack.hasRecurrentCalls
        val popResult = stack.pop(dom, widenedIn.getOrElse(in), result, out)
        val isOutermost = wasRecurrent && !stack.hasRecurrentCalls
        popResult match
          case stack.PopResult.Stable =>
            (result, isOutermost)
          case stack.PopResult.Unstable(newresult, newout) =>
            newout.foreach(state.setOutState(dom, _))
            someComponentIsLooping = true
            (newresult, isOutermost)
