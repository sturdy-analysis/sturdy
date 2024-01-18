package sturdy.fix.iter

import sturdy.effect.EffectStack
import sturdy.effect.TrySturdy
import sturdy.fix.{Combinator, Contextual, Fixpoint, Stack, StackConfig, State}
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.{Join, Widen}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

/**
 * Fixpoint combinator [[Topmost]] iterates on the entire program.
 * The combinator uses widening on the output of the abstract interpreter to avoid non-termination.
 * Furthermore, the combinator assumes that every recursive call chain of the abstract interpreter contains a recurrent call.
 */
def topmost[Dom, Codom, In, Out, All, Ctx]
  (config: StackConfig)
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: State)
  (using Finite[Dom], Finite[Ctx], Widen[Codom])
  : Topmost[Dom, Codom, In, Out, All, Ctx] =
  new Topmost(config, state, context)

final class Topmost[Dom, Codom, In, Out, All, Ctx]
  (config: StackConfig, state: State, context: Contextual[Ctx, Dom, Codom])
  (using Finite[Dom], Finite[Ctx], Widen[Codom])
  extends Combinator[Dom, Codom]:

  override def equals(obj: Any): Boolean = super.equals(obj)

  private val stack: Stack[Dom, Codom, state.In, state.Out] = Stack(state)(config, context)
  private var someComponentIsLooping: Boolean = false
  private var iterationCount: Int = 1

  /** Runs `f`. If this is the topmost call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    @tailrec
    def apply_(dom: Dom): Codom =
      if (stack.height == 0) {
        val allState: state.All = state.getAllState
        val result = step(f, dom)
        if (someComponentIsLooping) {
          if (Fixpoint.DEBUG) {
            iterationCount += 1
            println(s"## REPEAT (Iteration $iterationCount) of $dom")
          }
          someComponentIsLooping = false
          state.setAllState(allState)
          apply_(dom)
        } else
          result.getOrThrow
      } else {
        step(f, dom).getOrThrow
      }
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior. */
  private def step(f: Dom => Codom, dom: Dom): TrySturdy[Codom] =
    val in = state.getInState(dom)
    val outBefore = state.getOutState(dom)
    stack.push(dom, in, outBefore) match
      case stack.PushResult.Recurrent(result, widenedOut) =>
        widenedOut.foreach(state.setOutState(dom, _))
        result
      case stack.PushResult.Continue(widenedIn) =>
        widenedIn.foreach(state.setInState(dom, _))
        val result = TrySturdy(f(dom))
        val out = state.getOutState(dom)
        stack.pop(dom, widenedIn.getOrElse(in), result, out) match
          case stack.PopResult.Stable =>
            result
          case stack.PopResult.Unstable(newresult, newout) =>
            newout.foreach(state.setOutState(dom, _))
            someComponentIsLooping = true
            newresult
