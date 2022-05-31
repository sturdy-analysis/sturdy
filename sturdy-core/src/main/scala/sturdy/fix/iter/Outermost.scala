package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.effect.TrySturdy
import sturdy.fix.Combinator
import sturdy.fix.Contextual
import sturdy.fix.Fixpoint
import sturdy.fix.Stack
import sturdy.fix.StackedFrames
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.{Widen, Join}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

def outermost[Dom, Codom, In, Out, All, Ctx]
  (frames: Boolean = true)  
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[Dom, In, Out, All])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Outermost[Dom, Codom, In, Out, All, Ctx] =
  new Outermost(frames, state, context)

final class Outermost[Dom, Codom, In, Out, All, Ctx]
  (frames: Boolean, state: AnalysisState[Dom, In, Out, All], context: Contextual[Ctx, Dom, Codom])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  extends Combinator[Dom, Codom]:

  override def equals(obj: Any): Boolean = super.equals(obj)

  private val stack: Stack[Dom, Codom, In, Out] = Stack(frames, context)
  private var someComponentIsLooping: Boolean = false
  private var iterationCount: Int = 1

  /** Runs `f`. If this is the outermost call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    @tailrec
    def apply_(dom: Dom): Codom = {
      val allState = state.getAllState
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
    stack.push(dom, in) match
      case stack.PushResult.Recurrent(result, widenedOut) =>
        widenedOut.foreach(state.setOutState)
        (result, false)
      case stack.PushResult.Continue(widenedIn) =>
        widenedIn.foreach(state.setInState)
        val result = TrySturdy(f(dom))
        val out = state.getOutState(dom)
        val wasRecurrent = stack.hasRecurrentCalls
        val popResult = stack.pop(dom, in, result, out)
        val isOutermost = wasRecurrent && !stack.hasRecurrentCalls
        popResult match
          case stack.PopResult.Stable =>
            (result, isOutermost)
          case stack.PopResult.Unstable(newresult, newout) =>
            newout.foreach(state.setOutState)
            someComponentIsLooping = true
            (newresult, isOutermost)
