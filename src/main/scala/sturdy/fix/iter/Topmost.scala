package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.fix.{RecurrentCall, Combinator, Stack}

import scala.collection.mutable
import scala.util.Try

def topmost[Dom, Codom, In, Out, Ctx](stack: Stack[Dom, Codom, In, Out, Ctx], state: AnalysisState[In, Out]): Topmost[Dom, Codom, In, Out, Ctx] = new Topmost(stack, state)

final class Topmost[Dom, Codom, In, Out, Ctx](stack: Stack[Dom, Codom, In, Out, Ctx], state: AnalysisState[In, Out]) extends Combinator[Dom, Codom]:
  private var hasLoop: Boolean = false

  /** Runs `f`. If this is the topmost call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    def apply_(dom: Dom): Codom =
      if (stack.height > 0) {
        step(f, dom, state.getInState()).get
      } else {
        // this is the topmost call
        state.repeatUntilStable { () =>
          hasLoop = false
          val result = step(f, dom, state.getInState())
          if (!hasLoop)
            return result.get
          result
        }.get
      }
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior. */
  private def step(f: Dom => Codom, dom: Dom, inState: In): Try[Codom] =
    stack.push(dom, inState) match
      case Some(result) => 
        result
      case None =>
        val result = Try(f(dom))
        hasLoop = stack.pop(dom, inState, result) || hasLoop
        result
