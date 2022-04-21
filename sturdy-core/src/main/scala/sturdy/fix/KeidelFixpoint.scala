package sturdy.fix

import sturdy.effect.{AnalysisState, CombineTrySturdy, RecurrentCall, TrySturdy}
import sturdy.values.Finite
import sturdy.values.Join
import sturdy.values.Widen
import iter.Config
import sturdy.util.StackManager

import scala.language.implicitConversions
// Scc: BitSet
def identityStackWidening[Dom, In]: StackWidening[Dom, In] = scala.Predef.identity[(Stack[Dom, In], Call[Dom, In])]

type StackWidening[Dom, In] = (Stack[Dom, In], Call[Dom, In]) => (Stack[Dom, In], Call[Dom, In])

case class Call[Dom, In](dom: Dom, in: In)
type Stack[Dom, In] = Map[Call[Dom, In], Int]

class KeidelFixpoint[Dom, Codom, In, Out, All]
  (chooseFun: Dom => Int, phiConfigs: Iterable[Config])
  (stackWidening: StackWidening[Dom, In])
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
  var stack: Stack[Dom, In] = Map()

  enum Stability:
    case Stable
    case Unstable

    def isStable: Boolean = this match
      case Stable => true
      case Unstable => false

  enum CacheWasWidened:
    case Yes
    case No

  var height = 0
  def indent: String = "  " * height


  implicit def fromBoolean(bool: Boolean): CacheWasWidened =
    if (bool) CacheWasWidened.Yes else CacheWasWidened.No

  def apply(f: (Dom => Codom) ?=> (Dom => Codom)): Dom => Codom =
    val fToTrySturdy = (fixed: (Dom => Codom)) => (dom: Dom) => TrySturdy(f(using fixed)(dom))
    val phis: Iterable[Combinator[Dom, TrySturdy[Codom]]] = phiConfigs.map(c => c match
//      case Config.Innermost => fixed => innermost(fixed)
//      case Config.Topmost => fixed => outermost(fixed)
      case Config.Innermost => fixed => innermost.compose(widenStack)(fixed)
//      case Config.Topmost => fixed => outermost.compose(widenStack)(fixed)
    )
    val fixCombinator: (Dom => Codom) => Dom => TrySturdy[Codom] = fixed => dispatch(chooseFun, phis)(fToTrySturdy(fixed))
    Fixpoint.computeFixpoint(
      (fixed: Dom => Codom) => (dom: Dom) => fixCombinator(fixed)(dom)
        .getOrThrow
    )

  def innermost: Combinator[Dom, TrySturdy[Codom]] = keidelEntryFunction(iterateInnermost)
//  def outermost: Combinator[Dom, TrySturdy[Codom]] = keidelEntryFunction(iterateOutermost)

  def keidelEntryFunction(iterateFunction: (Dom => TrySturdy[Codom]) => (Dom => TrySturdy[Codom]))(f: Dom => TrySturdy[Codom]): Dom => TrySturdy[Codom] =
    def keidelEntryFunction_(dom: Dom): TrySturdy[Codom] =
      val call = Call(dom, state.getInState(dom))
      println(s"${indent}call: $call")
//      println(s"${indent}cached in innermost:  ${cache.getOrElse(call, None)}    cache: $cache")
      val cachedEntry: CacheEntry = cache.get(call) match {
        case None => CacheEntry(Stability.Unstable, TrySturdy(throw RecurrentCall(call)), state.getOutState(dom))
        case Some(entry) => entry
      }

      if (cachedEntry.stability.isStable) {
        println(s"${indent}Use stable cached: ${cachedEntry.codom}")
        state.setOutState(cachedEntry.out)
        cachedEntry.codom
      } else if (stack.contains(call)) {
        println(s"${indent}Use unstable cached: ${cachedEntry.codom}")
        scc += call
        println(s"${indent}added to scc: $scc")
        println(s"${indent}sccSize ${scc.size}")
        state.setOutState(cachedEntry.out)
        cachedEntry.codom
      } else {
        val result: TrySturdy[Codom] = iterateFunction(f)(dom)
        println(s"${indent}result after iterating: $result")
        results += call -> (result, state.getOutState(dom))
        result
      }
    keidelEntryFunction_

  def iterateOutermost(f: Dom => TrySturdy[Codom]): Call[Dom, In] => TrySturdy[Codom] =
    def iterateOutermost_(call: Call[Dom, In]): TrySturdy[Codom] =
      state.setInState(call.in)
      height += 1
      val codomNew = f(call.dom)
      height -= 1
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



  def iterateInnermost(f: Dom => TrySturdy[Codom]): Dom => TrySturdy[Codom] =
    def iterateInnermost_(dom: Dom): TrySturdy[Codom] =
      val call = Call(dom, state.getInState(dom))
      println(s"${indent}iterate: $call")
//      state.setInState(call.in)
      val oldStack = stack
      stack += (call -> height)
      height += 1
      val codomNew = f(dom)
      height -= 1
      println(s"${indent}new result: $codomNew")
      stack = oldStack
      if (!scc.contains(call))
        println(s"${indent}end iterate bc not in scc: $scc")
        codomNew
      else
        val (cacheWasWidened, codomWidened) = updateCacheAndGetUpdatedCodom(call, codomNew)
        println(s"${indent}cache after${if (cacheWasWidened == CacheWasWidened.No) " NO" else ""} widening: $cache")
        if (cacheWasWidened == CacheWasWidened.Yes)
          state.setInState(call.in)
          iterateInnermost(f)(dom)
        else
          if (scc.size == 1)
            makeCacheEntryOfCallStable(call)
          else
            println(s"scc > 1: $scc")
          scc -= call
          println(s"${indent}end iterate cache was not widened")
          codomWidened
    iterateInnermost_




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
      val (stackWidened, callWidened) = stackWidening(stack, Call(dom, state.getInState(dom)))
      val oldStack = stack
      stack = stackWidened
      state.setInState(callWidened.in)
      val result = f(callWidened.dom)
      stack = oldStack
      result
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
