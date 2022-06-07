package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.effect.TrySturdy
import sturdy.fix.Combinator
import sturdy.fix.Contextual
import sturdy.fix.Fixpoint
import sturdy.fix.Stack
import sturdy.fix.StackedFrames
import sturdy.fix.StackedStates
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.{Widen, Join}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

def innermost[Dom, Codom, In, Out, All, Ctx]
  (frames: Boolean = true)
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[Dom, In, Out, All])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Innermost[Dom, Codom, In, Out, All, Ctx] =
  new Innermost(frames, state, context)

final class Innermost[Dom, Codom, In, Out, All, Ctx]
  (frames: Boolean, state: AnalysisState[Dom, In, Out, All], context: Contextual[Ctx, Dom, Codom])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  extends Combinator[Dom, Codom]:

  private val stack: Stack[Dom, Codom, In, Out] = Stack(frames, context)
  private var iterationCounts: Map[Dom, Int] = Map()

  /** Runs `f` until a fixed point is reached as soon as something is looping. */
  override def apply(f: Dom => Codom): Dom => Codom =
    @tailrec
    def apply_(dom: Dom): Codom = {
      val allState = state.getAllState
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
        widenedOut.foreach(state.setOutState)
        (result, false)
      case stack.PushResult.Continue(widenedIn) =>
        widenedIn.foreach(state.setInState)
        val result = TrySturdy(f(dom))
        val out = state.getOutState(dom)
        stack.pop(dom, widenedIn.getOrElse(in), result, out) match
          case stack.PopResult.Stable =>
            (result, false)
          case stack.PopResult.Unstable(newresult, newout) =>
            newout.foreach(state.setOutState)
            (newresult, true)
