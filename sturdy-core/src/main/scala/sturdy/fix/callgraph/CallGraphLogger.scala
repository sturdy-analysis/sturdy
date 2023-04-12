package sturdy.fix.callgraph

import org.eclipse.collections.api.factory.{Maps, Sets}
import org.eclipse.collections.api.map.MutableMap
import org.eclipse.collections.api.set.MutableSet
import sturdy.effect.TrySturdy
import sturdy.fix.context.CallSiteLogger
import sturdy.fix.{Contextual, Logger, State}
import sturdy.data.MutableMap.updateWith

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
 * A call graph where an edge indicates where a function was called from and with which input state.
 */
final class CallGraphLogger[Dom,Callee,CallSite,Ctx](callSiteLogger: CallSiteLogger[Dom,CallSite])
                                                    (getCallee: Dom => Option[Callee])
                                                    (using contextual: Contextual[Ctx,Dom])
                                                  extends Logger[Dom,Any]:
  val calledFrom: MutableMap[Callee, MutableSet[(Callee,CallSite,Ctx)]] = Maps.mutable.empty()
  val calls: MutableMap[Callee, MutableSet[(Callee,CallSite,Ctx)]] = Maps.mutable.empty()
  val entryPoints: MutableSet[Callee] = Sets.mutable.empty()
  var calleeStack: List[Callee] = List.empty

  override def enter(dom: Dom): Unit =
    getCallee(dom) match
      case Some(callee) =>
        (calleeStack, callSiteLogger.getCalls) match
          case (caller::_, callSite::_) =>
            val ctx = contextual.getCurrentContext

            calledFrom.updateWith(callee, Sets.mutable.empty(), callers =>
              callers.add((caller, callSite, ctx))
              callers
            )

            calls.updateWith(caller, Sets.mutable.empty(), callees =>
              callees.add((callee, callSite, ctx))
              callees
            )

          case _ =>

          if(calleeStack.isEmpty)
            entryPoints.add(callee)

          calleeStack = callee :: calleeStack
      case _ =>

  override def exit(dom: Dom, codom: TrySturdy[Any]): Unit =
    calleeStack = calleeStack.tail

  def getEntryPoints: Iterator[Callee] =
    entryPoints.iterator().asScala

  def callsTransitively(initialCaller: Callee): Iterable[Callee] =
    val callees: MutableSet[Callee] = Sets.mutable.empty()
    val callers: mutable.Queue[Callee] = mutable.Queue(initialCaller)

    while(callers.nonEmpty) {
      val caller = callers.dequeue()

      calls.get(caller) match
        case null =>
        case cs =>
          for((callee,_,_) <- cs.iterator().asScala)
            if(! callees.contains(callee))
              callers.enqueue(callee)
              callees.add(callee)
    }

    callees.asScala


  def calledFromTransitively(initialCallee: Callee): Iterable[Callee] =
    val callers: MutableSet[Callee] = Sets.mutable.empty()
    val callees: mutable.Queue[Callee] = mutable.Queue(initialCallee)

    while (callees.nonEmpty) {
      val caller = callees.dequeue()

      calledFrom.get(caller) match
        case null =>
        case cs =>
          for ((callee, _, _) <- cs.iterator().asScala)
            if (!callers.contains(callee))
              callees.enqueue(callee)
              callers.add(callee)
    }

    callers.asScala

final case class Iterations[A](initialIteration: A, latestIteration: A):
  def addIteration(iteration: A): Iterations[A] = this.copy(latestIteration = iteration)

object Iterations:
  def initial[A](initialIteration: A): Iterations[A] = new Iterations(initialIteration, initialIteration)