package sturdy.fix

import sturdy.fix.context.Sensitivity

trait Fixpoint[Dom, Codom]:
  final type Fixed = Dom => Codom

  def fixpoint(f: (Dom => Codom) ?=> (Dom => Codom)): Dom => Codom =
    computeFixpoint(fixed => phi(f(using fixed)))

  private def computeFixpoint(f: (Dom => Codom) => (Dom => Codom)): Dom => Codom =
    f(dom => computeFixpoint(f)(dom))

  private final lazy val phi: Combinator[Dom, Codom] =
    contextFree (
      sturdy.fix.contextSensitive(context, contextSensitive)
    )

  protected type Ctx
  protected def context: Sensitivity[Dom, Ctx]
  protected def contextFree: Combinator[Dom, Codom] => Combinator[Dom, Codom]
  protected def contextSensitive: Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom]

trait Concrete[Dom, Codom] extends Fixpoint[Dom, Codom]:
  protected override type Ctx = Unit
  protected override def context: Sensitivity[Dom, Ctx] = sturdy.fix.context.none
  protected override def contextFree: Combinator[Dom, Codom] => Combinator[Dom, Codom] = f => f
  protected override def contextSensitive: Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom] = identity

object Fixpoint:
  var DEBUG: Boolean = System.getProperty("STURDY_DEBUG_FIXPOINT", "true").toBoolean
  val DEBUG_CACHE_CHANGES = System.getProperty("STURDY_DEBUG_CACHE_CHANGES", "false").toBoolean
