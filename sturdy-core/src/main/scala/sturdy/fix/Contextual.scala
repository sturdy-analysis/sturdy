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

def contextual[Ctx, Dom, Codom, In, Out](sensitivity: Sensitivity[Dom, Ctx]): Contextual[Ctx, Dom, Codom, In, Out] =
  new Contextual(sensitivity)

def noContextual[Dom, Codom, In, Out]: Contextual[Unit, Dom, Codom, In, Out] = new Contextual(context.none)

final class Contextual[Ctx, Dom, Codom, In, Out](sensitivity: Sensitivity[Dom, Ctx]):
  private var currentContext: Ctx = sensitivity.emptyContext
  def getCurrentContext: Ctx = currentContext

  private var previousContexts: List[Ctx] = List()

  def withContext(f: Dom => Codom, dom: Dom): Codom =
    if (!sensitivity.switchCall(dom))
      return f(dom)

    val ctx = sensitivity(dom)
    val contextSwitch =
      if (ctx != currentContext) {
        previousContexts = currentContext :: previousContexts
        currentContext = ctx
        true
      } else {
        false
      }

    try f(dom) finally {
      if (contextSwitch) {
        currentContext = previousContexts.head
        previousContexts = previousContexts.tail
      }
    }
