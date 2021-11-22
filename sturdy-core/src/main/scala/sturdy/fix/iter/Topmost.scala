package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.effect.TrySturdy
import sturdy.fix.Combinator
import sturdy.fix.Contextual
import sturdy.fix.Stack
import sturdy.values.Finite
import sturdy.values.Widen

import scala.collection.mutable
import scala.util.Try

def topmost[Dom, Codom, In, Out, All, Ctx]
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[In, Out, All])
  (using Widen[Codom], Widen[In], Widen[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Topmost[Dom, Codom, In, Out, All, Ctx] = new Topmost(state, context)

final class Topmost[Dom, Codom, In, Out, All, Ctx]
  (state: AnalysisState[In, Out, All], context: Contextual[Ctx, Dom, Codom])
  (using Widen[Codom], Widen[In], Widen[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  extends Combinator[Dom, Codom]:

  private val stack: Stack[Dom, Codom, In, Out, All, Ctx] = new Stack(state, context)
  private var hasLoop: Boolean = false

  /** Runs `f`. If this is the topmost call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    def apply_(dom: Dom): Codom =
      if (stack.height > 0) {
        step(f, dom).getOrThrow
      } else {
        // this is the topmost call
        stack.repeatUntilStable { () =>
          hasLoop = false
          val result = step(f, dom)
          (result, hasLoop)
        }.getOrThrow
      }
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior. */
  private def step(f: Dom => Codom, dom: Dom): TrySturdy[Codom] =
    val inState = state.getInState
    stack.push(dom, inState) match
      case Some(result) => 
        result
      case None =>
        val result = TrySturdy(f(dom))
        val (widenedResult, looping) = stack.pop(dom, inState, result)
        hasLoop = hasLoop || looping
        widenedResult
