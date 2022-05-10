package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.effect.TrySturdy
import sturdy.fix.Combinator
import sturdy.fix.Contextual
import sturdy.fix.Fixpoint
import sturdy.fix.Stack
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.{Widen, Join}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

def outermost[Dom, Codom, In, Out, All, Ctx]
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[Dom, In, Out, All])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Outermost[Dom, Codom, In, Out, All, Ctx] =
  new Outermost(state, context)

final class Outermost[Dom, Codom, In, Out, All, Ctx]
  (state: AnalysisState[Dom, In, Out, All], context: Contextual[Ctx, Dom, Codom])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  extends Combinator[Dom, Codom]:

  override def equals(obj: Any): Boolean = super.equals(obj)

  private val stack: Stack[Dom, Codom, In, Out, All, Ctx] = new Stack(state, context)
  private var someComponentIsLooping: Boolean = false
  private var iterationCount: Int = 1

  /** Runs `f`. If this is the outermost call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    @tailrec
    def apply_(dom: Dom): Codom = {
      val MaybeChanged(result, isOutermost) = step(f, dom)
      if (isOutermost && someComponentIsLooping) {
        if (Fixpoint.DEBUG) {
          iterationCount += 1
          println(s"## REPEAT (Iteration $iterationCount) of $dom")
        }
        someComponentIsLooping = false
        apply_(dom)
      } else
        result.getOrThrow
    }
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior. */
  private inline def step(f: Dom => Codom, dom: Dom): MaybeChanged[TrySturdy[Codom]] =
    val inState = state.getInState(dom)
    stack.push(dom, inState) match
      case Some(priorResult) =>
        MaybeChanged.Unchanged(priorResult)
      case None =>
        val result = TrySturdy(f(dom))
        val wasRecurrent = stack.hasRecurrentCalls
        val MaybeChanged(widenedResult, looping) = stack.pop(dom, inState, result)
        val isRecurrent = stack.hasRecurrentCalls
        val isOutermost = wasRecurrent && !isRecurrent
        someComponentIsLooping = someComponentIsLooping || looping
        MaybeChanged(widenedResult, isOutermost)