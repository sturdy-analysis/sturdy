
package sturdy.fix

import sturdy.fix.context.Sensitivity
import sturdy.values.Join

val ContextProperty = "context"

def contextSensitive
  [Ctx, Dom, Codom]
  (sensitivity: Sensitivity[Dom, Ctx], phi: Contextual[Ctx, Dom] ?=> Combinator[Dom, Codom])
  : ContextSensitive[Ctx, Dom, Codom] =
    val contextual = new Contextual[Ctx, Dom](sensitivity)
    new ContextSensitive(contextual, phi(using contextual))

def notContextSensitive
  [Dom, Codom, Phi <: Combinator[Dom, Codom]]
  (phi: Contextual[Unit, Dom] ?=> Phi)
  : ContextSensitive[Unit, Dom, Codom] =
    val contextual = new Contextual[Unit, Dom](context.none)
    new ContextSensitive(contextual, phi(using contextual))

final class ContextSensitive
  [Ctx, Dom, Codom]
  (contextual: Contextual[Ctx, Dom], phi : Combinator[Dom, Codom])
  extends Combinator[Dom, Codom]:
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    contextual.withContext(phi(f), dom)

trait HasContext[Ctx]:
  def getCurrentContext: Ctx

final class Contextual[Ctx, Dom](sensitivity: Sensitivity[Dom, Ctx]) extends HasContext[Ctx]:
  private var currentContext: Ctx = sensitivity.emptyContext
  def getCurrentContext: Ctx = currentContext

  private var previousContexts: List[Ctx] = List()

  def withContext[Codom](f: Dom => Codom, dom: Dom): Codom =
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
