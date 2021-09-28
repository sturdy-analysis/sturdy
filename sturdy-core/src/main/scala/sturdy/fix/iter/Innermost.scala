package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.effect.Effectful
import sturdy.fix.Combinator
import sturdy.fix.Contextual
import sturdy.fix.RecurrentCall
import sturdy.fix.Stack
import sturdy.values.Widen

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

def innermost[Dom, Codom, In, Out, All, Ctx]
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[In, Out, All])
  (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], j: Effectful)
  : Innermost[Dom, Codom, In, Out, All, Ctx] = new Innermost(state, context)

final class Innermost[Dom, Codom, In, Out, All, Ctx]
  (state: AnalysisState[In, Out, All], context: Contextual[Ctx, Dom, Codom])
  (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], j: Effectful)
  extends Combinator[Dom, Codom]:

  private val stack: Stack[Dom, Codom, In, Out, All, Ctx] = new Stack(state, context)

  /** Runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    def apply_(dom: Dom): Codom =
      stack.repeatUntilStable { () =>
        val (result, hasLoop) = step(f, dom, state.getInState())
        if (!hasLoop)
          return result.get
        result
      }.get
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior.
   *  
   *  @return the result of running `f` and a flag that indicates if `f` is looping and needs iterating.
   */
  private def step(f: Dom => Codom, dom: Dom, inState: In): (Try[Codom], Boolean) =
    stack.push(dom, inState) match
      case Some(result) =>
        (result, false)
      case None =>
        val result = Try(f(dom))
        stack.pop(dom, inState, result)
