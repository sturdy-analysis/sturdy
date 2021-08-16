package sturdy.fix.context

import sturdy.effect.AnalysisState

trait Sensitivity[Dom, Ctx] extends Function[Dom, Ctx]:
  def emptyContext: Ctx

  /** Indicates a context switch may appear at `dom`, only then `apply` will be called. */
  def switchCall(dom: Dom): Boolean

  /** Returns `None` if the context is definitely unchanged, returns `Some(ctx)` if `ctx` may be new. */
  override def apply(dom: Dom): Ctx

final class Product[Dom, Ctx1, Ctx2](s1: Sensitivity[Dom, Ctx1], s2: Sensitivity[Dom, Ctx2]) extends Sensitivity[Dom, (Ctx1, Ctx2)]:
  override def emptyContext: (Ctx1, Ctx2) = (s1.emptyContext, s2.emptyContext)

  /** Indicates a context switch may appear at `dom`, only then `apply` will be called. */
  override def switchCall(dom: Dom): Boolean =
    val v1 = s1.switchCall(dom)
    val v2 = s2.switchCall(dom)
    v1 || v2 // must execute both switchCalls before running apply, hence no short-circuiting here

  /** Returns `None` if the context is definitely unchanged, returns `Some(ctx)` if `ctx` may be new. */
  override def apply(dom: Dom): (Ctx1, Ctx2) = (s1(dom), s2(dom))


def full[Dom, In](using state: AnalysisState[In, _]) = new Sensitivity[Dom, In] {
  override def emptyContext = null.asInstanceOf[In]
  /** Indicates a context switch may appear at `dom`, only then `apply` will be called. */
  override def switchCall(dom: Dom): Boolean = true
  override def apply(dom: Dom) = state.getInState()
}

def none[Dom] = new Sensitivity[Dom, Unit] {
  override def emptyContext: Unit = ()
  /** Indicates a context switch may appear at `dom`, only then `apply` will be called. */
  override def switchCall(dom: Dom): Boolean = false
  override def apply(dom: Dom) = ()
}