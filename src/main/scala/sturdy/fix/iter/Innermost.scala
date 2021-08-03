package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.fix.Combinator
import sturdy.fix.RecurrentCall
import sturdy.fix.Stack

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

def innermost[Dom, Codom, In, Out, Ctx](stack: Stack[Dom, Codom, In, Out, Ctx], state: AnalysisState[In, Out]): Innermost[Dom, Codom, In, Out, Ctx] = new Innermost(stack, state)

final class Innermost[Dom, Codom, In, Out, Ctx](stack: Stack[Dom, Codom, In, Out, Ctx], state: AnalysisState[In, Out]) extends Combinator[Dom, Codom]:
  /** Runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    def apply_(dom: Dom): Codom =
      state.repeatUntilStable { () =>
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
