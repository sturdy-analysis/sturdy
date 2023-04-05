package sturdy.fix.callgraph

import org.eclipse.collections.api.factory.Maps
import org.eclipse.collections.api.map.MutableMap
import sturdy.effect.TrySturdy
import sturdy.fix.context.CallSiteLogger
import sturdy.fix.{Contextual, Logger, State}


/**
 * A call graph where an edge indicates where a function was called from and with which input state.
 */
final class CallGraphLogger[Dom,Ctx,Caller,Callee](callSiteLogger: CallSiteLogger[Dom,Caller])
                                                  (getCallee: Dom => Option[Callee])
                                                  (using val state: State, contextual: Contextual[Ctx,Dom])
                                                  extends Logger[Dom,Any]:
  val calledFrom: MutableMap[Callee, MutableMap[Caller, MutableMap[Ctx,Iterations[state.In]]]] = Maps.mutable.empty()

  override def enter(dom: Dom): Unit =
    (getCallee(dom),callSiteLogger.getCalls) match
      case (Some(callee),caller::_) =>
        val in = state.getInState(dom)
        val ctx = contextual.getCurrentContext
        calledFrom.updateWith(callee, Maps.mutable.empty(), callers =>
          callers.updateWith(caller, Maps.mutable.empty(), ctxs =>
            ctxs.updateWith(ctx, Iterations.initial(in), _.addIteration(in))
            ctxs
          )
          callers
        )
      case _ =>

  override def exit(dom: Dom, codom: TrySturdy[Any]): Unit = {}

  extension[K, V] (m: MutableMap[K, V])
    private inline def updateWith(k: K, default: V, f: V => V): Unit =
      m.compute(k, (_, value) => if (value == null) f(default) else f(value))


final case class Iterations[A](initialIteration: A, latestIteration: A):
  def addIteration(iteration: A): Iterations[A] = this.copy(latestIteration = iteration)

object Iterations:
  def initial[A](initialIteration: A): Iterations[A] = new Iterations(initialIteration, initialIteration)