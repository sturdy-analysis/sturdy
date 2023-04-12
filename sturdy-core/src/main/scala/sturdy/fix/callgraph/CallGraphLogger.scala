package sturdy.fix.callgraph

import org.eclipse.collections.api.factory.{Maps, Sets}
import org.eclipse.collections.api.map.MutableMap
import org.eclipse.collections.api.set.MutableSet
import sturdy.effect.TrySturdy
import sturdy.fix.context.CallSiteLogger
import sturdy.fix.{Contextual, Logger, State}
import sturdy.data.MutableMap.updateWith
import scala.jdk.CollectionConverters.*

/**
 * A call graph where an edge indicates where a function was called from and with which input state.
 */
final class CallGraphLogger[Dom,Callee,Ctx,CallSite](callSiteLogger: CallSiteLogger[Dom,CallSite])
                                                    (getCallee: Dom => Option[Callee])
                                                    (using val state: State, contextual: Contextual[Ctx,Dom])
                                                  extends Logger[Dom,Any]:
  val calledFrom: MutableMap[Callee, MutableMap[CallSite, MutableMap[Ctx,Iterations[state.In]]]] = Maps.mutable.empty()
  val calls: MutableMap[Callee, MutableSet[Callee]] = Maps.mutable.empty()
  var calleeStack: List[Callee] = List.empty

  override def enter(dom: Dom): Unit =
    getCallee(dom) match
      case Some(callee) =>
        callSiteLogger.getCalls match
          case caller::_ =>
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

        calls.putIfAbsent(callee, Sets.mutable.empty())
        calleeStack match
          case caller::_ =>
            calls.compute(caller, (_,callees) =>
              callees.add(callee)
              callees
            )
          case _ =>
        calleeStack = callee :: calleeStack
      case _ =>

  override def exit(dom: Dom, codom: TrySturdy[Any]): Unit =
    calleeStack = calleeStack.tail

  final def callsTransitively(caller: Callee): Iterator[Callee] =
    calls.get(caller) match
      case null => Iterator()
      case callees => callees.iterator().asScala ++ callees.iterator().asScala.flatMap(callsTransitively)

final case class Iterations[A](initialIteration: A, latestIteration: A):
  def addIteration(iteration: A): Iterations[A] = this.copy(latestIteration = iteration)

object Iterations:
  def initial[A](initialIteration: A): Iterations[A] = new Iterations(initialIteration, initialIteration)