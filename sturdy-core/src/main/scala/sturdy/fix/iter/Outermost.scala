package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.effect.TrySturdy
import sturdy.fix.Combinator
import sturdy.fix.Contextual
import sturdy.fix.Stack
import sturdy.values.Finite
import sturdy.values.{Widen, Join}

import scala.collection.mutable
import scala.util.Try

def outermost[Dom, Codom, In, Out, All, Ctx]
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[Dom, In, Out, All])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Outermost[Dom, Codom, In, Out, All, Ctx] =
  new Outermost(state, context)

object OutermostCounter:
  var instanceCounter = 0

final class Outermost[Dom, Codom, In, Out, All, Ctx]
  (state: AnalysisState[Dom, In, Out, All], context: Contextual[Ctx, Dom, Codom])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  extends Combinator[Dom, Codom]:

  private val id = OutermostCounter.instanceCounter
  OutermostCounter.instanceCounter += 1

  override def equals(obj: Any): Boolean = super.equals(obj)

  private val stack: Stack[Dom, Codom, In, Out, All, Ctx] = new Stack(state, context)

  /** Runs `f`. If this is the outermost corecurrent call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    def apply_(dom: Dom): Codom =
      stack.repeatUntilStable(dom)(() => step(f, dom)).getOrThrow
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior. */
  private def step(f: Dom => Codom, dom: Dom): (TrySturdy[Codom], Boolean) =
    val inState = state.getInState(dom)
    stack.push(dom, inState) match
      case Some(result) =>
        (result, false)
      case None =>
        val result = TrySturdy(f(dom))
        val wasRecurrent = stack.hasRecurrentCalls
        val (widenedResult, looping) = stack.pop(dom, inState, result)
        val isRecurrent = stack.hasRecurrentCalls
        val isOutermost = wasRecurrent && !isRecurrent
        (widenedResult, isOutermost)
