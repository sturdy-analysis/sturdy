package sturdy.fix.iter

import org.eclipse.collections.api.RichIterable
import org.eclipse.collections.api.factory.{Lists, Maps, Sets}
import org.eclipse.collections.api.list.ImmutableList
import org.eclipse.collections.api.list.primitive.ImmutableIntList
import org.eclipse.collections.api.map.{ImmutableMap, MutableMap}
import org.eclipse.collections.api.set.MutableSet
import org.eclipse.collections.api.tuple.Pair
import org.eclipse.collections.impl.factory.primitive.IntLists
import sturdy.data.given
import sturdy.effect.{EffectStack, RecurrentCall, TrySturdy, given}
import sturdy.fix.*
import sturdy.fix.callgraph.CallGraphLogger
import sturdy.fix.summary.SummaryLogger
import sturdy.incremental.{Change, Changes, Delta, ListDelta}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.{Finite, Join, MaybeChanged, Widen}

import scala.annotation.tailrec
import scala.collection.{IterableOps, mutable}
import scala.util.Try
import scala.jdk.CollectionConverters.*

class IncrementalFixpoint[Dom, Codom, Callee, Ctx, CallSite]
                         (callGraph: CallGraphLogger[Dom, Callee, Ctx, CallSite],
                          summaryLogger: SummaryLogger[Dom, Codom])
                         (using val dstate: State)
                         (using inStateWidening: => InStateWidening[Dom, dstate.In])
                         (using Finite[Dom], Widen[Codom]):

  /** Cache that accumulates the differences between the old and new analysis result */
  val dstack: StackedStates[Dom, Codom] & Stack[Dom, Codom, dstate.In, dstate.Out] =
    new StackedStates(dstate)(inStateWidening, readPriorOutput = true)
      .asInstanceOf[StackedStates[Dom, Codom] & Stack[Dom, Codom, dstate.In, dstate.Out]]
//  dstack.loadCache(
//    inputOutput.cache.keyValuesView().asInstanceOf[RichIterable[Pair[(Dom, dstack.state.In), (TrySturdy[Codom],dstack.state.Out)]]])

  def update(changes: ListDelta[Callee], f: Dom => Codom): Unit =
    for((dom,in) <- updateOrder(changes))
      print(dom,in)
//    for(change <- changes.delta.values)
//      change match
//        case Change.Nil(_) =>
//
//        case Change.Add(_) =>
//          // There is nothing to do.
//          // If the added function was used from somewhere else, then it will be analyzed then.
//          // Otherwise, the added function is dead code and does not need to be analyzed.
//
//        case Change.Remove(oldDom) =>
//          dstack.outCache.select((k: (Dom, dstack.state.In), v: dstack.OutCacheEntry) =>
//            oldDom != k._1
//          )
//
//        case Change.Replace(from, to) =>
//          for (stack <- getStacksAndSetState(from)) {
//
//            // Clear the cache for elements on the stack
//            for((dom,in) <- stack)
//              dstack.outCache.remove((dom, in.asInstanceOf[dstack.state.Out]))
//
//            // Replace domains on the stack with new domains
//            val newStack = changes.replace[(Dom,dstate.In)](f => {case (dom:Dom,in) => f(dom).iterator.map(newDom => (newDom,in))}, stack)
//
//            // Update analysis results for each element on the stack in bottom-up order
//            @tailrec
//            def loop(st: Iterable[(Dom,dstate.In)]): Unit =
//              st.headOption match
//                case None =>
//                case Some(dom,in) =>
//                  val rest = st.tail
//                  println(s"\nUPDATE $dom")
//                  dstate.setInState(dom, in)
//                  dstack.loadStack(rest.asInstanceOf[Iterable[(Dom, dstack.state.In)]])
//                  val result = TrySturdy(f(dom))
//                  val out = dstate.getOutState(dom)
//                  dstack.outCache.put(
//                    (dom,in.asInstanceOf[dstack.state.In]),
//                    dstack.OutCacheEntry(result, out.asInstanceOf[dstack.state.Out], dstack.Stability.Stable))
//                  loop(rest)
//
//            loop(newStack)
//          }

  private def updateOrder(changes: ListDelta[Callee]): Iterable[Callee] =
    val replaced: Iterable[Callee] = changes.delta.values.flatMap {
      case c@Change.Replace(from, _to) => Iterable(from)
      case _ => Iterable()
    }
    val worklist: mutable.Queue[Callee] = mutable.Queue.from(replaced)
    ???

  /** Get all stacks in which a `dom` appeared */
  private def getStacksAndSetState(dom: Dom): Iterator[Iterable[(Dom,dstate.In)]] = ???
//    stackLogger.stacks.get(dom) match
//      case sts =>
//        sts.keyValuesView().iterator().asScala.map(p =>
//          val sites = p.getOne
//          val in = p.getTwo.latestIteration
//          sites.zip(in).map((site,in) => (site.call,in.asInstanceOf[dstate.In]))
//        )
//      case null => Iterator()

  extension (stack: StackedStates[Dom, Codom])
    private def getDependers: Iterable[Dom] = ???
//      stack.stack.keysView().collect(_._1).toSet.asScala

    private def hasChanged(dom: Dom): Boolean = ???
//      val (codomOld, outOld) = inputOutput.cache.get(dom)
//      val outEntry = dstack.outCache.get(dom)
//      val (codomNew, outNew) = (outEntry.result, outEntry.out)
//      codomOld != codomNew || outOld != outNew

