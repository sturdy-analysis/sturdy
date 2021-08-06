package sturdy.fix.context

import sturdy.effect.AnalysisState

trait Sensitivity[Dom, Ctx] extends Function[Dom, Option[Ctx]]:
  def emptyContext: Ctx
  /** Returns `None` if the context is definitely unchanged, returns `Some(ctx)` if `ctx` may be new. */
  override def apply(dom: Dom): Option[Ctx]

def full[Dom, In](using state: AnalysisState[In, _]) = new Sensitivity[Dom, (Dom, In)] {
  override def emptyContext = null.asInstanceOf[(Dom,In)]
  override def apply(dom: Dom) = Some(dom -> state.getInState())
}

def none[Dom] = new Sensitivity[Dom, Unit] {
  override def emptyContext: Unit = ()
  override def apply(dom: Dom) = None
}