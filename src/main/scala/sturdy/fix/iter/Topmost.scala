package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.fix.{RecurrentCall, Combinator, Stack}

import scala.collection.mutable

def topmost[Dom, Codom, In, Out](stack: Stack[Dom, Codom, In, Out], state: AnalysisState[In, Out]): Topmost[Dom, Codom, In, Out] = new Topmost(stack, state)

final class Topmost[Dom, Codom, In, Out](stack: Stack[Dom, Codom, In, Out], state: AnalysisState[In, Out]) extends Combinator[Dom, Codom]:
  private var hasLoop: Boolean = false

  /** Runs `f`. If this is the topmost call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    def apply_(dom: Dom): Codom =
      if (stack.height > 0) {
        step(f, dom, state.getRelevantInState())
      } else {
        // this is the topmost call
        state.repeatUntilStable { () =>
          hasLoop = false
          val result = step(f, dom, state.getRelevantInState())
          if (!hasLoop)
            return result
          result
        }
      }
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior. */
  private def step(f: Dom => Codom, dom: Dom, inState: In): Codom =
    stack.push((dom, inState)) match
      case Some((result, outState)) =>
        state.setOutState(outState)
        result
      case None =>
        var throws: Option[() => Nothing] = None
        val result = try f(dom) catch {
          case ex =>
            throws = Some(() => throw ex)
            null.asInstanceOf[Codom]
        }
        hasLoop = stack.pop((dom, inState), result) || hasLoop
        result
