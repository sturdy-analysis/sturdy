package sturdy.fix.iter

import sturdy.effect.AnalysisState
import sturdy.effect.Effectful
import sturdy.fix.Combinator
import sturdy.fix.Contextual
import sturdy.fix.RecurrentCall
import sturdy.fix.Stack
import sturdy.values.Widen

import scala.collection.mutable
import scala.util.Try

def topmost[Dom, Codom, In, Out, All, Ctx]
  (using context: Contextual[Ctx, Dom, Codom])
  (using state: AnalysisState[In, Out, All])
  (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], j: Effectful)
  : Topmost[Dom, Codom, In, Out, All, Ctx] = new Topmost(state, context)

final class Topmost[Dom, Codom, In, Out, All, Ctx]
  (state: AnalysisState[In, Out, All], context: Contextual[Ctx, Dom, Codom])
  (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], j: Effectful)
  extends Combinator[Dom, Codom]:

  private val stack: Stack[Dom, Codom, In, Out, All, Ctx] = new Stack(state, context)
  private var hasLoop: Boolean = false

  /** Runs `f`. If this is the topmost call, runs `f` until a fixed point is reached. */
  override def apply(f: Dom => Codom): Dom => Codom =
    def apply_(dom: Dom): Codom =
      if (stack.height > 0) {
        step(f, dom).get
      } else {
        // this is the topmost call
        stack.repeatUntilStable { () =>
          hasLoop = false
          val result = step(f, dom)
          if (!hasLoop)
            return result.get
          result
        }.get
      }
    apply_

  /** Runs `f` by pushing and popping a frame to the stack and handling recurrent behavior. */
  private def step(f: Dom => Codom, dom: Dom): Try[Codom] =
    val inState = state.getInState()
    stack.push(dom, inState) match
      case Some(result) => 
        result
      case None =>
        val result = Try(f(dom))
        val (widenedResult, looping) = stack.pop(dom, inState, result)
        hasLoop = hasLoop || looping
        widenedResult
