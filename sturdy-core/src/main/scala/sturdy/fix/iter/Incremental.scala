package sturdy.fix.iter

import org.eclipse.collections.api.RichIterable
import org.eclipse.collections.api.factory.{Maps, Sets}
import org.eclipse.collections.api.map.{ImmutableMap, MutableMap}
import org.eclipse.collections.api.set.MutableSet
import org.eclipse.collections.api.tuple.Pair
import sturdy.effect.{EffectStack, RecurrentCall, TrySturdy}
import sturdy.fix.*
import sturdy.incremental.{Change, Changes, Delta}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.{Finite, Join, MaybeChanged, Widen}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try
import scala.jdk.CollectionConverters.*

class IncrementalInnermost[Dom, Codom]
                          (changes: Iterator[Change[Dom]], stackLogger: StackLogger[Dom, Codom])
                          (using dstate: State)
                          (using inStateWidening: => InStateWidening[Dom, dstate.In])
                          (using Finite[Dom], Widen[Codom])
  extends Combinator[Dom, Codom]:

  var updated = false

  def update(change: Change[Dom], f: Dom => Codom): Unit =
    change match
      case Change.Nil(_) =>
      case Change.Add(newDom) =>
      case Change.Replace(from, to) =>
        for (stack <- getStacksAndSetState(from))
          val st = stack.asInstanceOf[Stack[Dom, Codom, dstate.In, dstate.Out]]
          innermost(st)(f)(to)
          if (stack.hasChanged(to))
            for (depender <- stack.getDependers)
              update(Change.Replace(from = depender, to = depender), f)
      case Change.Remove(oldDom) =>
        dstack.outCache.select((k: (Dom, dstack.state.In), v: dstack.OutCacheEntry) =>
          oldDom != k._1
        )

  def apply(f: Dom => Codom): Dom => Codom =
    if(! updated) {
      for(change <- changes) update(change, f)
      updated = true
    }

    dstack.clearStack
    innermost(dstack.asInstanceOf[Stack[Dom, Codom, dstate.In, dstate.Out]])(f)


  /** Cache that accumulates the differences between the old and new analysis result */
  private val dstack: StackedStates[Dom, Codom] & Stack[Dom, Codom, dstate.In, dstate.Out] =
    new StackedStates(dstate)(inStateWidening, readPriorOutput = true)
      .asInstanceOf[StackedStates[Dom, Codom] & Stack[Dom, Codom, dstate.In, dstate.Out]]
  dstack.loadCache(
    stackLogger.stack.outCache.keyValuesView().asInstanceOf[
      RichIterable[Pair[(Dom, dstack.state.In),dstack.OutCacheEntry]
    ]])

  /** Get all stacks in which a `dom` appeared */
  def getStacksAndSetState(dom: Dom): Iterator[StackedStates[Dom, Codom]] =
    stackLogger.stacks.get(dom) match
      case sts => sts.iterator().asScala.map((in, stack) =>
        dstate.setInState(dom, in.asInstanceOf[dstate.In])
        dstack.loadStack(stack.keyValuesView().asInstanceOf[
          RichIterable[Pair[(Dom, dstack.state.In), FrameInstanceInfo]]])
        dstack)
      case null =>
        dstack.clearStack
        Iterator(dstack)

  extension (stack: StackedStates[Dom, Codom])
    def getDependers: Iterator[Dom] =
      stack.stack.keysView().iterator().asScala.map(_._1)

    def hasChanged(dom: Dom): Boolean =
      stackLogger.stack.outCache.get(dom) != dstack.outCache.get(dom)

final class StackLogger[Dom, Codom](val stack: StackedStates[Dom, Codom]) extends Logger[Dom, Codom]:
  type LoggedStack = ImmutableMap[(Dom, stack.state.In), FrameInstanceInfo]
  /** A map of all stacks in which a `dom` appeared. */
  val stacks: MutableMap[Dom, MutableSet[(stack.state.In, LoggedStack)]] = Maps.mutable.empty()

  /** Log the stack on each recursive call, so we can later recompute an incremental update with it. */
  final override def enter(dom: Dom): Unit =
    stacks.compute(dom, (_dom, sts) =>
      val res = Option(sts).getOrElse(Sets.mutable.empty())
      val in = stack.state.getInState(dom)
      res.add(in, stack.stack.toImmutable)
      res
    )

  final override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit = {}