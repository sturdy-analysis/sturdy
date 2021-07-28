package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.fix.Combinator
import sturdy.fix.RecurrentCall
import sturdy.fix.Stack

import scala.annotation.tailrec
import scala.collection.mutable

def innermost[Dom, Codom, In, Out](stack: Stack[Dom, Codom, In, Out], state: AnalysisState[In, Out]): Innermost[Dom, Codom, In, Out] = new Innermost(stack, state)

final class Innermost[Dom, Codom, In, Out](stack: Stack[Dom, Codom, In, Out], state: AnalysisState[In, Out]) extends Combinator[Dom, Codom]:
  /** Runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    def apply_(dom: Dom): Codom =
      state.repeatUntilStable { () =>
        val (result, hasLoop) = step(f, dom, state.getRelevantInState())
        if (!hasLoop)
          return result
        result
      }
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior.
   *  
   *  @return the result of running `f` and a flag that indicates if `f` is looping and needs iterating.
   */
  private def step(f: Dom => Codom, dom: Dom, inState: In): (Codom, Boolean) =
    stack.push((dom, inState)) match
      case Some((result, outState)) =>
        state.setOutState(outState)
        (result, false)
      case None =>
        var throws: Option[() => Nothing] = None
        val result = try f(dom) catch {
          case ex =>
            throws = Some(() => throw ex)
            null.asInstanceOf[Codom]
        }
        val hasLoop = stack.pop((dom, inState), result)
        if (hasLoop)
          (result, true)
        else {
          throws.map(_())
          (result, false)
        }
