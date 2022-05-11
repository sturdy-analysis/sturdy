package sturdy.fix

import sturdy.effect.{AnalysisState, CombineTrySturdy, RecurrentCall, TrySturdy}
import sturdy.values.Finite
import sturdy.values.Join
import sturdy.values.Widen
import sturdy.fix.iter.Config
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.MaybeChanged

import scala.collection.immutable.BitSet
import scala.language.implicitConversions


object KeidelFixpoint:
  case class Call[Dom, In](dom: Dom, in: In)
  given finiteCall[Dom, In](using Finite[Dom], Finite[In]): Finite[Call[Dom, In]] with {}

import KeidelFixpoint.*


trait KeidelStack[Dom, In]:
  def height: Int
  def pushLocal[A](c: Call[Dom, In])(f: => A): A
  def widenLocal[A](c: Call[Dom, In])(f: Call[Dom, In] => A): A

  def getPosition(c: Call[Dom, In]): Option[Int]

/** Assumes there are only finitely many Call[Dom, In] */
class FiniteStack[Dom, In]() extends KeidelStack[Dom, In]:
  private var stack: Map[Call[Dom, In], Int] = Map()

  def height: Int = stack.size
  def pushLocal[A](c: Call[Dom, In])(f: => A): A =
    val oldStack = stack
    stack += c -> stack.size
    try f finally {
      stack = oldStack
    }
  def widenLocal[A](c: Call[Dom, In])(f: Call[Dom, In] => A): A =
    f(c)

  def getPosition(c: Call[Dom, In]): Option[Int] =
    LinearStateOperationCounter.lookupStackCounter += 1
    stack.get(c)


/** Only considers Dom and widens In states when a Dom is recurrent */
class InsensitiveStack[Dom, In](using Widen[In]) extends SensitiveStack[Dom, Dom, In](_.dom)

class SensitiveStack[Ctx, Dom, In](getContext: Call[Dom, In] => Ctx)(using Widen[In]) extends KeidelStack[Dom, In]:
  private var stack: Map[Call[Dom, In], Int] = Map()
  private var contexts: Map[Ctx, Call[Dom, In]] = Map()
  def height: Int = stack.size
  def pushLocal[A](c: Call[Dom, In])(f: => A): A =
    val oldStack = stack
    val oldContexts = contexts
    if (Fixpoint.DEBUG)
      println(s"## push $c")
    stack += c -> stack.size
    val ctx = getContext(c)
    contexts += ctx -> c
    try f finally {
      if (Fixpoint.DEBUG)
        println(s"## pop  $c")
      stack = oldStack
      contexts = oldContexts
    }
  def widenLocal[A](c: Call[Dom, In])(f: Call[Dom, In] => A): A =
    val ctx = getContext(c)
    contexts.get(ctx) match
      case None => f(c)
      case Some(previousCall) =>
        LinearStateOperationCounter.wideningCounter += 1
        Profiler.addTime("widen"){Widen(previousCall.in, c.in)} match
          case MaybeChanged.Changed(widenedIn) =>
            val widenedCall = Call(c.dom, widenedIn)
            if (Fixpoint.DEBUG)
              println(s"## Widened call from $c to $widenedCall")
            f(widenedCall)
          case MaybeChanged.Unchanged(_) =>
            f(c)

  def getPosition(c: Call[Dom, In]): Option[Int] =
    Profiler.addTime("lookupStack"){
      LinearStateOperationCounter.lookupStackCounter += 1
      stack.get(c)
//      if (contexts.contains(getContext(c)))
//        stack.get(c)
//      else
//        None
    }

//class SensitiveStack[Dom, Ctx, In] extends KeidelStack[Dom, In]:
//  private var stack: Set[Call[Dom, In]] = Set()
//  private var contexts: Map[Ctx, Call[Dom, In]] = Map()
//
//  def joinByContext(ctx: Ctx, c: Call[Dom, In]): Call[Dom, In] = contexts.get(ctx) match {
//    case Some(previousCall) if c <= previousCall => previousCall
//    case Some(previousCall) =>
//      val widened = Widen(previousCall, c)
//      contexts += ctx -> widened.get
//      widened.get
//    case None =>
//      // insert
//      c
//  }

class KeidelFixpoint[Dom, Codom, In, Out, All]
  (chooseFun: Dom => Int, phiConfigs: Iterable[Config], stack: KeidelStack[Dom, In])
  (using state: AnalysisState[Dom, In, Out, All])
  (using Finite[Dom], Widen[Codom], Widen[Out], Widen[In])
  extends Fixpoint[Dom, Codom]:

  type SCC = Set[Call[Dom, In]]
  case class CacheEntry(var stability: Stability, codom: TrySturdy[Codom], out: Out)
  type Cache = Map[Call[Dom, In], CacheEntry]
  var cache: Cache = Map()
  var scc: BitSet = BitSet()


  enum Stability:
    case Stable
    case Unstable

    def isStable: Boolean = this match
      case Stable => true
      case Unstable => false

  enum CacheWasWidened:
    case Yes
    case No

  def indent: String = "  " * stack.height


  implicit def fromBoolean(bool: Boolean): CacheWasWidened =
    if (bool) CacheWasWidened.Yes else CacheWasWidened.No

  def apply(f: (Dom => Codom) ?=> (Dom => Codom)): Dom => Codom =
    val fToTrySturdy = (fixed: (Dom => Codom)) => (dom: Dom) => TrySturdy(f(using fixed)(dom))
    val phis: Iterable[Combinator[Dom, TrySturdy[Codom]]] = phiConfigs.map(c => c match
      case Config.Innermost => fixed => widenStack.compose(innermost)(fixed)
//      case Config.Topmost => fixed => outermost.compose(widenStack)(fixed)
    )
    val fixCombinator: (Dom => Codom) => Dom => TrySturdy[Codom] = fixed => dispatch(chooseFun, phis)(fToTrySturdy(fixed))
    Fixpoint.computeLeastFixpoint(
      (fixed: Dom => Codom) => (dom: Dom) => fixCombinator(fixed)(dom)
        .getOrThrow
    )

  def innermost: Combinator[Dom, TrySturdy[Codom]] = keidelInnermost(iterateInnermost)
//  def outermost: Combinator[Dom, TrySturdy[Codom]] = keidelEntryFunction(iterateOutermost)

  def keidelInnermost(iterateFunction: (Dom => TrySturdy[Codom]) => (Dom => TrySturdy[Codom]))(f: Dom => TrySturdy[Codom]): Dom => TrySturdy[Codom] =
    def keidelInnermost_(dom: Dom): TrySturdy[Codom] =
      val call = Call(dom, state.getInState(dom))
      val cacheLookup = cache.get(call)
      if (Fixpoint.DEBUG)
        println(s"${indent}cache lookup: $call -> $cacheLookup")
      LinearStateOperationCounter.lookupCounter += 1
      val cachedEntry: CacheEntry = Profiler.addTime("lookup"){cache.get(call)} match {
        case None => CacheEntry(Stability.Unstable, TrySturdy(throw RecurrentCall(call)), state.getOutState(dom))
        case Some(entry) => entry
      }

      if (cachedEntry.stability.isStable) {
        if (Fixpoint.DEBUG)
          println(s"${indent}Stable result")
        state.setOutState(cachedEntry.out)
        cachedEntry.codom
      } else {
        stack.getPosition(call) match
          case Some(pos) =>
            scc += pos
            if (Fixpoint.DEBUG)
              println(s"${indent}Recurrent call: $scc")
            state.setOutState(cachedEntry.out)
            cachedEntry.codom
          case None =>
            val result: TrySturdy[Codom] = iterateFunction(f)(dom)
            if (Fixpoint.DEBUG)
              println(s"${indent}result after iterating: $result")
            result
      }
    keidelInnermost_


  def iterateInnermost(f: Dom => TrySturdy[Codom]): Dom => TrySturdy[Codom] =
    def iterateInnermost_(dom: Dom): TrySturdy[Codom] =
      val call = Call(dom, state.getInState(dom))
      val originalState = state.getAllState
      val stackPosOfCallInTheCallOfF = stack.height
      if (Fixpoint.DEBUG)
        println(s"${indent}iterate: $call")
      val codomNew = stack.pushLocal(call) {
        f(dom)
      }
      if (Fixpoint.DEBUG)
        println(s"${indent}new result: $codomNew")

      if (!scc.contains(stackPosOfCallInTheCallOfF)) {
        if (Fixpoint.DEBUG)
          println(s"${indent}end iterate bc $call not in scc: $scc")
        codomNew
      } else {
        val (cacheWasWidened, codomWidened) = updateCacheAndGetUpdatedCodom(call, codomNew)
        if (cacheWasWidened == CacheWasWidened.Yes) {
          if (Fixpoint.DEBUG)
            println(s"${indent}unstable cache entry for: $call -> ${(codomNew, state.getOutState(dom))}")
          state.setAllState(originalState)
          iterateInnermost(f)(dom)
        } else {
          if (Fixpoint.DEBUG)
            println(s"${indent}stable cache entry for: $call -> ${(codomNew, state.getOutState(dom))}")
          if (scc.size == 1)
            makeCacheEntryOfCallStable(call)
          else
            if (Fixpoint.DEBUG)
              println(s"scc > 1: $scc")
          scc -= stackPosOfCallInTheCallOfF
          if (Fixpoint.DEBUG)
            println(s"${indent}end iterate cache was not widened")
          codomWidened
        }
      }
    iterateInnermost_

  def widenStack(f: Dom => TrySturdy[Codom]): Dom => TrySturdy[Codom] =
    def widenStack_(dom: Dom): TrySturdy[Codom] =
      stack.widenLocal(Call(dom, state.getInState(dom))) { callWidened =>
        state.setInState(callWidened.in)
        f(callWidened.dom)
      }
    widenStack_


  def makeCacheEntryOfCallStable(call: Call[Dom, In]): Unit =
    LinearStateOperationCounter.lookupCounter += 1
    val cachedEntry: CacheEntry = Profiler.addTime("lookup"){cache(call)}
    cachedEntry.stability = Stability.Stable

  def updateCacheAndGetUpdatedCodom(call: Call[Dom, In], codom: TrySturdy[Codom]): (CacheWasWidened, TrySturdy[Codom]) =
    val out = state.getOutState(call.dom)
    LinearStateOperationCounter.lookupCounter += 1
    Profiler.addTime("lookup"){cache.get(call)} match {
      case None =>
        cache += call -> CacheEntry(Stability.Unstable, codom, out)
        (CacheWasWidened.Yes, codom)
      case Some(oldCacheEntry) =>
        val widenedCodom = Widen(oldCacheEntry.codom, codom)
        LinearStateOperationCounter.wideningCounter += 1
        val widenedOut = Profiler.addTime("widen"){Widen(oldCacheEntry.out, out)}
        cache += call -> CacheEntry(Stability.Unstable, widenedCodom.get, widenedOut.get)
        state.setOutState(widenedOut.get)
        (widenedCodom.hasChanged || widenedOut.hasChanged, widenedCodom.get)
    }
