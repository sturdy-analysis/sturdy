package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.effect.JoinComputation
import sturdy.fix.{Combinator, Contextual}
import sturdy.fix.RecurrentCall
import sturdy.fix.Stack
import sturdy.fix.Widening
import sturdy.values.JoinValue

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

def innermost[Dom, Codom, In, Out, Ctx]
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[In, Out])
  (using joinCodom: JoinValue[Codom], joinIn: JoinValue[In], joinOut: JoinValue[Out])
  (using widenCodom: Widening[Codom], widenIn: Widening[In], widenOut: Widening[Out], j: JoinComputation)
  : Innermost[Dom, Codom, In, Out, Ctx] = new Innermost(state, context)

final class Innermost[Dom, Codom, In, Out, Ctx]
  (state: AnalysisState[In, Out], context: Contextual[Ctx, Dom, Codom])
  (using joinCodom: JoinValue[Codom], joinIn: JoinValue[In], joinOut: JoinValue[Out])
  (using widenCodom: Widening[Codom], widenIn: Widening[In], widenOut: Widening[Out], j: JoinComputation)
  extends Combinator[Dom, Codom]:

  private val stack: Stack[Dom, Codom, In, Out, Ctx] = new Stack(state, context)

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
