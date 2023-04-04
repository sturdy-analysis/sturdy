package sturdy.fix.iter

import javafx.scene.CacheHint
import org.eclipse.collections.api.RichIterable
import org.eclipse.collections.api.factory.{Lists, Maps, Sets}
import org.eclipse.collections.api.list.ImmutableList
import org.eclipse.collections.api.list.primitive.ImmutableIntList
import org.eclipse.collections.api.map.{ImmutableMap, MutableMap}
import org.eclipse.collections.api.set.MutableSet
import org.eclipse.collections.api.tuple.Pair
import org.eclipse.collections.impl.factory.primitive.IntLists
import sturdy.data.given
import sturdy.effect.{EffectStack, RecurrentCall, TrySturdy}
import sturdy.fix.*
import sturdy.incremental.{Change, Changes, Delta, ListDelta}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.{Finite, Join, MaybeChanged, Widen}

import scala.annotation.tailrec
import scala.collection.{IterableOps, mutable}
import scala.util.Try
import scala.jdk.CollectionConverters.*

class IncrementalFixpoint[Dom, Codom]
                         (stackLogger: StackLogger[Dom, Codom])
                         (using val dstate: State)
                         (using inStateWidening: => InStateWidening[Dom, dstate.In])
                         (using Finite[Dom], Widen[Codom]):

  /** Cache that accumulates the differences between the old and new analysis result */
  val dstack: StackedStates[Dom, Codom] & Stack[Dom, Codom, dstate.In, dstate.Out] =
    new StackedStates(dstate)(inStateWidening, readPriorOutput = true)
      .asInstanceOf[StackedStates[Dom, Codom] & Stack[Dom, Codom, dstate.In, dstate.Out]]
  dstack.loadCache(
    stackLogger.cache.keyValuesView().asInstanceOf[RichIterable[Pair[(Dom, dstack.state.In), (TrySturdy[Codom],dstack.state.Out)]]])

  def update(changes: ListDelta[Dom], f: Dom => Codom): Unit =
    for(change <- changes.delta.values)
      change match
        case Change.Nil(_) =>

        case Change.Add(_) =>
          // There is nothing to do.
          // If the added function was used from somewhere else, then it will be analyzed then.
          // Otherwise, the added function is dead code and does not need to be analyzed.

        case Change.Remove(oldDom) =>
          dstack.outCache.select((k: (Dom, dstack.state.In), v: dstack.OutCacheEntry) =>
            oldDom != k._1
          )

        case Change.Replace(from, to) =>
          for (stack <- getStacksAndSetState(from)) {

            // Clear the cache for elements on the stack
            for((dom,in) <- stack)
              dstack.outCache.remove((dom, in.asInstanceOf[dstack.state.Out]))

            // Replace domains on the stack with new domains
            val newStack = changes.replace[(Dom,dstate.In)](f => {case (dom:Dom,in) => f(dom).iterator.map(newDom => (newDom,in))}, stack)

            // Update analysis results for each element on the stack in bottom-up order
            @tailrec
            def loop(st: Iterable[(Dom,dstate.In)]): Unit =
              st.headOption match
                case None =>
                case Some(dom,in) =>
                  val rest = st.tail
                  println(s"\nUPDATE $dom")
                  dstate.setInState(dom, in)
                  dstack.loadStack(rest.asInstanceOf[Iterable[(Dom, dstack.state.In)]])
                  val result = TrySturdy(f(dom))
                  val out = dstate.getOutState(dom)
                  dstack.outCache.put((dom,in.asInstanceOf[dstack.state.In]), dstack.OutCacheEntry(result, out.asInstanceOf[dstack.state.Out], dstack.Stability.Stable))
                  loop(rest)

            loop(newStack)
          }

  /** Get all stacks in which a `dom` appeared */
  private def getStacksAndSetState(dom: Dom): Iterator[CachedHashStack[(Dom,dstate.In)]] =
    stackLogger.stacks.get(dom) match
      case sts => sts.iterator().asScala.asInstanceOf[Iterator[CachedHashStack[(Dom,dstate.In)]]]
      case null => Iterator()

  extension (stack: StackedStates[Dom, Codom])
    private def getDependers: Iterable[Dom] =
      stack.stack.keysView().collect(_._1).toSet.asScala

    private def hasChanged(dom: Dom): Boolean =
      val (codomOld, outOld) = stackLogger.cache.get(dom)
      val outEntry = dstack.outCache.get(dom)
      val (codomNew, outNew) = (outEntry.result, outEntry.out)
      codomOld != codomNew || outOld != outNew

enum CachedHashStack[A] extends Iterable[A]:
  case Empty()
  private case Cons(x: A, rest: CachedHashStack[A], hash: Int)

  def push(x: A): CachedHashStack[A] =
    new Cons(x, this, (x,this).hashCode())

  def pop: (Option[A], CachedHashStack[A]) =
    this match
      case Empty() => (None,Empty())
      case Cons(head,tail,_) => (Some(head),tail)

  override def hashCode(): Int =
    this match
      case Empty() => 0
      case Cons(_,_,h) => h
  override def toString: String = toList.toString()

  override def headOption: Option[A] =
    this match
      case Empty() => None
      case Cons(h,_,_) => Some(h)

  override def tail: Iterable[A] =
    this match
      case Empty() => throw new UnsupportedOperationException
      case Cons(_,t,_) => t

  override def map[B](f: A => B): Iterable[B] =
    def _map(c: CachedHashStack[A]): CachedHashStack[B] =
      c match
        case Empty() => Empty()
        case Cons(h,t,_) =>
          _map(t).push(f(h))
    _map(this)

  override def iterator: Iterator[A] =
    val that = this
    new Iterator[A] {
      var pointer: CachedHashStack[A] = that
      override def hasNext: Boolean =
        pointer match
          case Empty() => false
          case Cons(_,_,_) => true

      override def next():A =
        pointer match
          case Empty() => throw new NoSuchElementException()
          case Cons(x,r,_) =>
            pointer = r
            x
    }

final class StackLogger[Dom, Codom: Widen](val state: State) extends Logger[Dom, Codom]:

  type Stack = CachedHashStack[(Dom, state.In)]
  var stack: Stack = CachedHashStack.Empty()

  /** A map of all stacks in which a `dom` appeared. */
  val stacks: MutableMap[Dom, MutableSet[Stack]] = Maps.mutable.empty()

  /** Cache that tracks the outputs for every input */
  val cache: MutableMap[(Dom, state.In), (TrySturdy[Codom], state.Out)] = Maps.mutable.empty()

  /** Log the stack on each recursive call, so we can later recompute an incremental update with it. */
  override def enter(dom: Dom): Unit =
    val in = state.getInState(dom)
    stack = stack.push((dom,in))
    stacks.compute(dom, (_dom, sts) =>
      val res = Option(sts).getOrElse(Sets.mutable.empty())
      res.add(stack)
      res
    )

  override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit =
    val (Some(_,in), rest) = stack.pop
    stack = rest

    val out = state.getOutState(dom)
    given widenOut: Widen[state.Out] = state.widenOut(dom)
    given widenTrySturdyCodom: Widen[TrySturdy[Codom]] = summon[Widen[TrySturdy[Codom]]]
    cache.merge((dom,in), (codom,out), Widen.apply(_,_).get)