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

def innermost[Dom, Codom, In, Out, All, Ctx]
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[Dom, In, Out, All])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  : Innermost[Dom, Codom, In, Out, All, Ctx] =
  new Innermost(state, context)

final class Innermost[Dom, Codom, In, Out, All, Ctx]
  (state: AnalysisState[Dom, In, Out, All], context: Contextual[Ctx, Dom, Codom])
  (using Widen[Codom], Widen[In], Widen[Out], Join[Out], EffectStack)
  (using Finite[Dom], Finite[Ctx])
  extends Combinator[Dom, Codom]:

  private val stack: Stack[Dom, Codom, In, Out, All, Ctx] = new Stack(state, context)
  private var iterationCounts: Map[Dom, Int] = Map()

  /** Runs `f` until a fixed point is reached as soon as something is looping. */
  override def apply(f: Dom => Codom): Dom => Codom =
    @tailrec
    def apply_(dom: Dom): Codom = {
      val MaybeChanged(result, loop) = step(f, dom)
      if (loop) {
        if (Fixpoint.DEBUG) {
          val iterationCount = iterationCounts.getOrElse(dom, 2)
          iterationCounts += dom -> (iterationCount + 1)
          println(s"## REPEAT (Iteration $iterationCount) of $dom")
        }
        apply_(dom)
      } else {
        result.getOrThrow
      }
    }
    apply_

  private inline def step(f: Dom => Codom, dom: Dom): MaybeChanged[TrySturdy[Codom]] =
    val inState = state.getInState(dom)
    stack.push(dom, inState) match
      case Some(priorResult) =>
        MaybeChanged.Unchanged(priorResult)
      case None =>
        val result = TrySturdy(f(dom))
        stack.pop(dom, inState, result)
