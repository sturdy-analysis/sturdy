package sturdy.fix

import sturdy.effect.{AnalysisState, CombineTrySturdy, TrySturdy, RecurrentCall}
import sturdy.values.Finite
import sturdy.values.Join
import sturdy.values.Widen
import sturdy.fix.iter.Config
import sturdy.util.StackManager
import sturdy.values.MaybeChanged

import scala.language.implicitConversions

// Scc: BitSet


object KeidelFixpoint:
  case class Call[Dom, In](dom: Dom, in: In)
  given finiteCall[Dom, In](using Finite[Dom], Finite[In]): Finite[Call[Dom, In]] with {}

import KeidelFixpoint.*


trait KeidelStack[Dom, In]:
  def height: Int
  def contains(c: Call[Dom, In]): Boolean
  def pushLocal[A](c: Call[Dom, In])(f: => A): A
  def widenLocal[A](c: Call[Dom, In])(f: Call[Dom, In] => A): A

/** Assumes there are only finitely many Call[Dom, In] */
class FiniteStack[Dom, In]() extends KeidelStack[Dom, In]:
  private var stack: Map[Call[Dom, In], Int] = Map()

  def height: Int = stack.size
  def contains(c: Call[Dom, In]): Boolean = stack.contains(c)
  def pushLocal[A](c: Call[Dom, In])(f: => A): A =
    val oldStack = stack
    stack += c -> stack.size
    try f finally {
      stack = oldStack
    }
  def widenLocal[A](c: Call[Dom, In])(f: Call[Dom, In] => A): A =
    f(c)

/** Only considers Dom and widens In states when a Dom is recurrent */

class InsensitiveStack[Dom, In](using Widen[In]) extends SensitiveStack[Dom, Dom, In](_.dom)

class SensitiveStack[Ctx, Dom, In](getContext: Call[Dom, In] => Ctx)(using Widen[In]) extends KeidelStack[Dom, In]:
  private var stack: Set[Call[Dom, In]] = Set()
  private var contexts: Map[Ctx, Call[Dom, In]] = Map()
  def height: Int = stack.size
  def contains(c: Call[Dom, In]): Boolean = stack.contains(c)
  def pushLocal[A](c: Call[Dom, In])(f: => A): A =
    val oldStack = stack
    val oldContexts = contexts
    if (Fixpoint.DEBUG)
      println(s"## push c")
    stack += c
    val ctx = getContext(c)
    contexts += ctx -> c
    try f finally {
      stack = oldStack
      contexts = oldContexts
    }
  def widenLocal[A](c: Call[Dom, In])(f: Call[Dom, In] => A): A =
    val ctx = getContext(c)
    contexts.get(ctx) match
      case None => f(c)
      case Some(previousCall) =>
        Widen(previousCall.in, c.in) match
          case MaybeChanged.Changed(widenedIn) =>
            val widenedCall = Call(c.dom, widenedIn)
            if (Fixpoint.DEBUG)
              println(s"## Widened call from $c to $widenedCall")
            f(widenedCall)
          case MaybeChanged.Unchanged(_) =>
            f(c)

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
//  (using Join[Out], Finite[Dom], Widen[Codom], Widen[Out], Widen[In])
  (using Finite[Dom], Widen[Codom], Widen[Out], Widen[In])
  extends Fixpoint[Dom, Codom]:

  StackManager.keidelFixpoint = this

  type SCC = Set[Call[Dom, In]]
  case class CacheEntry(var stability: Stability, codom: TrySturdy[Codom], out: Out)
  type Cache = Map[Call[Dom, In], CacheEntry]
  var cache: Cache = Map()
  var results: Map[Call[Dom, In], (TrySturdy[Codom], Out)] = Map()
  var scc: SCC = Set()


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
//      case Config.Innermost => fixed => innermost(fixed)
//      case Config.Topmost => fixed => outermost(fixed)
      case Config.Innermost => fixed => widenStack.compose(innermost)(fixed)
//      case Config.Topmost => fixed => outermost.compose(widenStack)(fixed)
    )
    val fixCombinator: (Dom => Codom) => Dom => TrySturdy[Codom] = fixed => dispatch(chooseFun, phis)(fToTrySturdy(fixed))
    Fixpoint.computeLeastFixpoint(
      (fixed: Dom => Codom) => (dom: Dom) => fixCombinator(fixed)(dom)
        .getOrThrow
    )

  def innermost: Combinator[Dom, TrySturdy[Codom]] = keidelEntryFunction(iterateInnermost)
//  def outermost: Combinator[Dom, TrySturdy[Codom]] = keidelEntryFunction(iterateOutermost)

  def keidelEntryFunction(iterateFunction: (Dom => TrySturdy[Codom]) => (Dom => TrySturdy[Codom]))(f: Dom => TrySturdy[Codom]): Dom => TrySturdy[Codom] =
    def keidelEntryFunction_(dom: Dom): TrySturdy[Codom] =
      val call = Call(dom, state.getInState(dom))
      if (Fixpoint.DEBUG)
        println(s"${indent}call: $call")
//      println(s"${indent}cached in innermost:  ${cache.getOrElse(call, None)}    cache: $cache")
      val cachedEntry: CacheEntry = cache.get(call) match {
        case None => CacheEntry(Stability.Unstable, TrySturdy(throw RecurrentCall(call)), state.getOutState(dom))
        case Some(entry) => entry
      }

      if (cachedEntry.stability.isStable) {
        if (Fixpoint.DEBUG)
          println(s"${indent}Use stable cached: ${cachedEntry.codom}")
        state.setOutState(cachedEntry.out)
        cachedEntry.codom
      } else if (stack.contains(call)) {
        if (Fixpoint.DEBUG)
          println(s"${indent}Use unstable cached: ${cachedEntry.codom}")
        scc += call
        if (Fixpoint.DEBUG)
          println(s"${indent}added to scc: $scc")
          println(s"${indent}sccSize ${scc.size}")
        state.setOutState(cachedEntry.out)
        cachedEntry.codom
      } else {
        val result: TrySturdy[Codom] = iterateFunction(f)(dom)
        if (Fixpoint.DEBUG)
          println(s"${indent}result after iterating: $result")
        results += call -> (result, state.getOutState(dom))
        result
      }
    keidelEntryFunction_


  def iterateInnermost(f: Dom => TrySturdy[Codom]): Dom => TrySturdy[Codom] =
    def iterateInnermost_(dom: Dom): TrySturdy[Codom] =
      val call = Call(dom, state.getInState(dom))
      val originalState = state.getAllState
      if (Fixpoint.DEBUG)
        println(s"${indent}iterate: $call")
      //      state.setInState(call.in)
      val codomNew = stack.pushLocal(call){
        f(dom)
      }
      if (Fixpoint.DEBUG)
        println(s"${indent}new result: $codomNew")
      if (!scc.contains(call)) {
        if (Fixpoint.DEBUG)
          println(s"${indent}end iterate bc $call not in scc: $scc")
        codomNew
      } else {
        val (cacheWasWidened, codomWidened) = updateCacheAndGetUpdatedCodom(call, codomNew)
        if (Fixpoint.DEBUG)
          println(s"${indent}cache after${if (cacheWasWidened == CacheWasWidened.No) " NO" else ""} widening: $cache")
        if (cacheWasWidened == CacheWasWidened.Yes)
          state.setAllState(originalState)
          iterateInnermost(f)(dom)
        else
          if (scc.size == 1)
            makeCacheEntryOfCallStable(call)
          else
            if (Fixpoint.DEBUG)
              println(s"scc > 1: $scc")
          scc -= call
          if (Fixpoint.DEBUG)
            println(s"${indent}end iterate cache was not widened")
          codomWidened
      }
    iterateInnermost_

  def iterateOutermost(f: Dom => TrySturdy[Codom]): Call[Dom, In] => TrySturdy[Codom] =
    def iterateOutermost_(call: Call[Dom, In]): TrySturdy[Codom] =
      state.setInState(call.in)
      val codomNew = stack.pushLocal(call) {
        f(call.dom)
      }
      f(call.dom)
      if (scc.contains(call) && scc.size == 1)
        val (cacheWasWidened, codomWidened) = updateCacheAndGetUpdatedCodom(call, codomNew)
        if (cacheWasWidened == CacheWasWidened.Yes)
          iterateOutermost(f)(call)
        else
          scc -= call
          codomWidened
      else
        codomNew
    iterateOutermost_

/*
  def iterateInnermost(f: Dom => TrySturdy[Codom]): Call => TrySturdy[Codom] =
    def iterateInnermost_(call: Call): TrySturdy[Codom] =
      println(s"${indent}iterate: $call")
      state.setInState(call.in)
      val oldStack = stack
      height += 1


      val widenedCall = widenStack2(call.dom)
      state.setInState(widenedCall.in)
      val codomNew = f(widenedCall.dom)


      height -= 1
      println(s"${indent}new result: $codomNew")
      stack = oldStack
      if (!scc.contains(widenedCall))
        println(s"${indent}end iterate bc not in scc: $scc")
        codomNew
      else
        val (cacheWasWidened, codomWidened) = updateCacheAndGetUpdatedCodom(widenedCall, codomNew, state.getOutState(widenedCall.dom))
        println(s"${indent}cache after${if (cacheWasWidened == CacheWasWidened.No) " NO" else ""} widening: $cache")
        if (cacheWasWidened == CacheWasWidened.Yes)
          iterateInnermost(f)(widenedCall)
        else
          if (scc.size == 1)
            makeCacheEntryOfCallStable(widenedCall)
          else
            println(s"scc > 1: $scc")
          scc -= widenedCall
          println(s"${indent}end iterate cache was not widened")
          codomWidened
    iterateInnermost_
*/




/*
  def widenStack2(dom: Dom): Call =
    val in = state.getInState(dom)
    val call = Call(dom, in)
    if (stack.contains(dom))
      val oldIn: In = stack(dom)
      val widenedIn = Widen(oldIn, in)
      stack += dom -> widenedIn.get
      println(s"${indent}widened Stack: old $oldIn                $widenedIn        $cache")
      Call(dom, widenedIn.get)
    else
      stack += dom -> in
      println(s"${indent}added to Stack: $stack")
      Call(dom, state.getInState(dom))
*/



  def widenStack(f: Dom => TrySturdy[Codom]): Dom => TrySturdy[Codom] =
    def widenStack_(dom: Dom): TrySturdy[Codom] =
      if (Fixpoint.DEBUG)
        println(s"${indent}Widening the stack")
      stack.widenLocal(Call(dom, state.getInState(dom))) { callWidened =>
        state.setInState(callWidened.in)
        f(callWidened.dom)
      }
    widenStack_


//      val in = state.getInState(dom)
//      val call = Call(dom, in)
//      if (stack.contains(dom))
//        val oldIn: In = stack(dom)
//        val widenedIn = Widen(oldIn, in)
//        stack += dom -> widenedIn.get
//        state.setInState(widenedIn.get)
//        println(s"${indent}widened Stack: old $oldIn                $widenedIn        $cache")
//        val result = f(dom)
//        result
//      else
//        stack += dom -> in
//        println(s"${indent}added to Stack: $stack")
//        val result = f(dom)
//        result
//    widenStack_



  def makeCacheEntryOfCallStable(call: Call[Dom, In]): Unit =
    val cachedEntry: CacheEntry = cache(call)
    cachedEntry.stability = Stability.Stable
    results += call -> (cachedEntry.codom, cachedEntry.out)

  def updateCacheAndGetUpdatedCodom(call: Call[Dom, In], codom: TrySturdy[Codom]): (CacheWasWidened, TrySturdy[Codom]) =
    val out = state.getOutState(call.dom)
    cache.get(call) match {
      case None =>
        cache += call -> CacheEntry(Stability.Unstable, codom, out)
        (CacheWasWidened.Yes, codom)
      case Some(oldCacheEntry) =>
        val widenedCodom = Widen(oldCacheEntry.codom, codom)
        val widenedOut = Widen(oldCacheEntry.out, out)
        cache += call -> CacheEntry(Stability.Unstable, widenedCodom.get, widenedOut.get)
        state.setOutState(widenedOut.get)
        (widenedCodom.hasChanged || widenedOut.hasChanged, widenedCodom.get)
    }
