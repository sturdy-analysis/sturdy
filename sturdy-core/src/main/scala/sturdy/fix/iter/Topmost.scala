package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.effect.JoinComputation
import sturdy.fix.Widening
import sturdy.fix.{RecurrentCall, Combinator, Stack, Contextual}
import sturdy.values.JoinValue

import scala.collection.mutable
import scala.util.Try

def topmost[Dom, Codom, In, Out, Ctx]
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[In, Out])
  (using joinCodom: JoinValue[Codom], joinIn: JoinValue[In], joinOut: JoinValue[Out])
  (using widenCodom: Widening[Codom], widenIn: Widening[In], widenOut: Widening[Out], j: JoinComputation)
  : Topmost[Dom, Codom, In, Out, Ctx] = new Topmost(state, context)

final class Topmost[Dom, Codom, In, Out, Ctx]
  (state: AnalysisState[In, Out], context: Contextual[Ctx, Dom, Codom])
  (using joinCodom: JoinValue[Codom], joinIn: JoinValue[In], joinOut: JoinValue[Out])
  (using widenCodom: Widening[Codom], widenIn: Widening[In], widenOut: Widening[Out], j: JoinComputation)
  extends Combinator[Dom, Codom]:

  private val stack: Stack[Dom, Codom, In, Out, Ctx] = new Stack(state, context)
  private var hasLoop: Boolean = false

  /** Runs `f`. If this is the topmost call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    def apply_(dom: Dom): Codom =
      if (stack.height > 0) {
        step(f, dom, state.getInState()).get
      } else {
        // this is the topmost call
        stack.repeatUntilStable { () =>
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
        val (widenedResult, looping) = stack.pop(dom, inState, result)
        hasLoop = hasLoop || looping
        widenedResult
