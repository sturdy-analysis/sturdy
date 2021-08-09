package sturdy.fix

import sturdy.effect.AnalysisState
import sturdy.fix.context.Sensitivity
import sturdy.values.JoinValue


def contextSensitive[Ctx, Dom, Codom, In, Out, Phi <: Combinator[Dom, Codom]](phi: Phi)(using contextual: Contextual[Ctx, Dom, Codom, In, Out]): ContextSensitive[Ctx, Dom, Codom, In, Out, Phi] =
  new ContextSensitive(contextual, phi)
def contextSensitive[Ctx, Dom, Codom, In, Out, Phi <: Combinator[Dom, Codom]](contextual: Contextual[Ctx, Dom, Codom, In, Out], phi: Phi): ContextSensitive[Ctx, Dom, Codom, In, Out, Phi] =
  new ContextSensitive(contextual, phi)
final class ContextSensitive[Ctx, Dom, Codom, In, Out, Phi <: Combinator[Dom, Codom]](context: Contextual[Ctx, Dom, Codom, In, Out], phi : Phi) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    context.withContext(phi(f), dom)
}

def contextual[Ctx, Dom, Codom, In, Out](sensitivity: Sensitivity[Dom, Ctx], switchCall: Dom => Boolean)(using state: AnalysisState[In, Out]): Contextual[Ctx, Dom, Codom, In, Out] =
  new Contextual(sensitivity, switchCall, state)

def noContextual[Dom, Codom, In, Out]: Contextual[Unit, Dom, Codom, In, Out] =
  new Contextual(context.none, _ => false, null)

final class Contextual[Ctx, Dom, Codom, In, Out](sensitivity: Sensitivity[Dom, Ctx], switchCall: Dom => Boolean, state: AnalysisState[In, Out]):
  private var currentContext: Ctx = null.asInstanceOf[Ctx]
  def getCurrentContext: Ctx = currentContext

  private var previousContexts: List[(Ctx, Out)] = List()

  def withContext(f: Dom => Codom, dom: Dom): Codom =
    if (!switchCall(dom))
      return f(dom)

    val maybeCtx = sensitivity(dom)
    val contextSwitch = maybeCtx match
      case Some(ctx) if ctx != currentContext =>
        previousContexts = (currentContext, state.getOutState()) :: previousContexts
        currentContext = ctx
        true
      case _ => false

    try f(dom) finally {
      if (contextSwitch) {
        val (previousContext, previousOut) = previousContexts.head
        currentContext = previousContext
//        state.setOutState(previousOut)
        previousContexts = previousContexts.tail
      }
    }
