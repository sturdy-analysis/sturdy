package sturdy.fix

import sturdy.control.{ControlObservable, FixpointControlEvent}
import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.effect.SturdyThrowable
import sturdy.effect.{CombineTrySturdy, TrySturdy}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.{Changed, Finite, Join, MaybeChanged, StackWidening, Unchanged, Widen}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try


/**
  *
  * @param state
  * @param inStateWidening
  * @param readPriorOutput if true, try to find prior cache entry and reuse it instead of recursing
  * @param storeNonrecursiveOutput if true, store the output of nonrecursive functions in the cache.
  *                                This may lead to additional cache hits during fixed-pointing, and it
  *                                can be useful when using the cache after the fixed-point is computed.
  * @param storeIntermediateOutput if true, store intermediate output in the cache, even when it is not
  *                                stable yet. This requires dependency tracking, which may negatively impact
  *                                the performance. This enables reading temporarily stable output when
  *                                `readPriorOutput` is true, which may positively impact the performance.
  * @param observers
  * @tparam Dom
  * @tparam Codom
  * @tparam In
  * @tparam Out
  */
final class StackedStates[Dom, Codom, In, Out](val state: StateT[In, Out])
                                              (val inStateWidening: InStateWidening[Dom, state.In],
                                               readPriorOutput: Boolean, storeNonrecursiveOutput: Boolean, storeIntermediateOutput: Boolean,
                                               observers: Iterable[Stack.FixEvent => Unit])
                                              (using Finite[Dom], Join[Codom], Widen[Codom])
  extends Stack[Dom, Codom, In, Out]:

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
//  private val stack: MutableMap[(Dom, state.In), FrameInstanceInfo] = Maps.mutable.empty()
  private val stack: mutable.Map[(Dom, state.In), FrameInstanceInfo] = mutable.Map()
  private var stackHeight: Int = 0

  private var dependencyStack: List[ListBuffer[OutCacheEntry]] = List(ListBuffer.empty)
  private inline def pushDependencyStack(): Unit = {
    if (storeIntermediateOutput)
      dependencyStack = ListBuffer.empty +: dependencyStack
  }
  private inline def popDependencyStack(): List[OutCacheEntry] =
    if (storeIntermediateOutput) {
      val depInfo = dependencyStack.head
      dependencyStack = dependencyStack.tail
      depInfo.toList
    } else {
      List.empty
    }
  private inline def addParentDependency(out: OutCacheEntry): Unit = {
    if (storeIntermediateOutput) {
      if (Fixpoint.DEBUG_PRIOR_OUTPUT)
        println(s"${stackHeightMinusOneIndent}REGISTER PARENT DEPENDENCY <- $out")
      dependencyStack.head += out
    }
  }


  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  private val outCache: mutable.Map[(Dom, state.In), OutCacheEntry] = mutable.Map()

  override def getCache: Map[Dom, TrySturdy[Codom]] = outCache.groupBy(_._1._1).view.mapValues { m =>
    m.values.map(_.result).reduce((r1,r2) => Join(r1,r2).get)
  }.toMap

  class OutCacheEntry(dom: Dom, in: state.In, var result: TrySturdy[Codom], var out: state.Out, var stability: Stability, val dependencies: mutable.Set[OutCacheEntry]) extends StableMaker:
    def isStable: Boolean = stability eq Stability.Stable
    def mayReadEntry: Boolean = (stability eq Stability.Stable) || storeIntermediateOutput && (stability eq Stability.Unstable)
    override def toString: String = s"OutCacheEntry($dom, $in, $stability) = $result:$out"
    override def markPermanentlyStable(): Unit =
      Profiler.addData("fix_stable", 1)(_ + 1)
      OutCacheEntry.this.stability = Stability.Stable

  object OutCacheEntry:
    def unapply(out: OutCacheEntry): (TrySturdy[Codom], state.Out) = (out.result, out.out)

  enum Stability:
    case Stable
    case Unstable
    case Invalid


  private def invalidateCache(out: OutCacheEntry): Unit =
    if (out.stability == Stability.Unstable) {
      if (Fixpoint.DEBUG_PRIOR_OUTPUT)
        println(s"${stackHeightIndent}  INVALIDATE PRIOR OUTPUT $out")
      out.stability = Stability.Invalid
      out.dependencies.foreach(invalidateCache)
    }

  /** Set of _active_ stack frames that have recurred.
   *  When a stack frame becomes inactive, it is also removed from this set.
   */
  private val corecurrentCalls: mutable.Set[Int] = mutable.BitSet()
  def hasRecurrentCalls: Boolean = corecurrentCalls.nonEmpty

  override def toString: String =
    s"Stack: ${stack.keys.map(k => k.hashCode()).toString()}\n"+
    s"Cache: ${outCache.toList.map(k => s"${k._1._1} @ ${k._1._2.hashCode()} -> ${k._2.result} @ ${k._2.out.hashCode()}, ${k._2.stability}").sorted.mkString("\n       ")}\n" +
    s"In:    ${outCache.toList.map(k => s"${k._1._2.hashCode()}: ${k._1._2}").sorted.mkString("\n       ")}\n" +
    s"Out:   ${outCache.toList.map(k => s"${k._2.out.hashCode()}: ${k._2.out}").sorted.mkString("\n       ")}\n"

  private def fire(ev: Stack.FixEvent): Unit = observers.foreach(_.apply(ev.asInstanceOf[Stack.FixEvent]))

  /** Current height of the stack. */
  def height: Int = stackHeight //stack.size()

  def stackHeightIndent: String = "  " * (height)
  def stackHeightMinusOneIndent: String = "  " * (height - 1)

  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is not recurrent, yields None.
   *  If the frame is recurrent and has not been previously executed, throws a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields the previous result.
   */
  def push(dom: Dom, in: state.In, currentOut: state.Out, iterate: Boolean): PushResult =
    if (Thread.currentThread().isInterrupted)
      throw new InterruptedException

    val widenedIn = inStateWidening.push(dom, in).get
    val stateFrame = (dom, widenedIn)
    if (storeIntermediateOutput && iterate) {
      Profiler.addData("fix_invalidate", 1)(_ + 1)
      outCache.get(stateFrame) match
        case Some(entry) => invalidateCache(entry)
        case None => dependencyStack.head.foreach(invalidateCache)
    }

    stack.get(stateFrame) match
      // call is not recurrent
      case None =>
        if (readPriorOutput) {
          val outEntry = outCache.get(stateFrame)
          if (Fixpoint.DEBUG_PRIOR_OUTPUT && outEntry.isDefined)
            println(s"${stackHeightIndent}FOUND PRIOR OUTPUT $stateFrame <- $outEntry")
          if (outEntry.exists(_.mayReadEntry)) {
            Profiler.addData("fix_read", 1)(_ + 1)
            // previous input subsumes current input and previous result still stable => return previous result
            val OutCacheEntry(result, out) = outEntry.get
            if (Fixpoint.DEBUG_PRIOR_OUTPUT)
              println(s"${stackHeightIndent}READ PRIOR OUTPUT $stateFrame <- $result:$out")
            fire(FixpointControlEvent.Recurrent(stateFrame))
            return PushResult.Skip(result, Some(out))
          }
        }

        // push call to stack
        Profiler.addData("fix_push", 1)(_ + 1)
        val info = new FrameInstanceInfo(stackHeight)
        stack.put(stateFrame, info)
        pushDependencyStack()
        if (Fixpoint.DEBUG)
          println(s"${stackHeightIndent}PUSH $stateFrame:$currentOut")
        stackHeight += 1
        fire(FixpointControlEvent.BeginFixpoint(stateFrame))
        PushResult.Continue(Some(widenedIn))

      case Some(info) =>
        // call is recurrent
        Profiler.addData("fix_recurrent", 1)(_ + 1)
        corecurrentCalls += info.frameIdWithInStateOfCache.get
        inStateWidening.pop(dom, in)
        if (Fixpoint.DEBUG)
          println(s"${stackHeightIndent}PUSH RECURRENT $stateFrame:$currentOut")
        outCache.get(stateFrame) match
          case None =>
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}POP RECURRENT  $stateFrame")
            fire(FixpointControlEvent.Recurrent(stateFrame))
            PushResult.Skip(TrySturdy(throw RecurrentCall(stateFrame)), None)
          case Some(out@OutCacheEntry(res, previousOut)) =>
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}POP RECURRENT  $stateFrame <- $res:$previousOut")
            fire(FixpointControlEvent.Recurrent(stateFrame))
            PushResult.Skip(res, Some(previousOut))

  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(dom: Dom, in: state.In, result: TrySturdy[Codom], out: state.Out): PopResult =
    Profiler.addData("fix_pop", 1)(_ + 1)
    val stateFrame = (dom, in)
    inStateWidening.pop(dom, in)
    val previousInfo = stack.remove(stateFrame)
    if (Fixpoint.DEBUG_INVARIANTS && previousInfo.isEmpty)
      throw new IllegalStateException(s"Pop must delete a previously pushed frame but did not $stateFrame")
    val deps = popDependencyStack()

    val newStackHeight = stackHeight - 1
    val isCorecurrent = corecurrentCalls.remove(newStackHeight)
    val updatedResult = if (isCorecurrent) {
      storeCorecurrentOutput(widen = true, stateFrame, result, out, deps)
    } else if (storeNonrecursiveOutput) {
      storeCorecurrentOutput(widen = false, stateFrame, result, out, deps) match {
        case s@PopResult.Stable(_) => s
        case PopResult.Unstable(_, _) => PopResult.Stable(StableMaker.empty)
      }
    } else {
      if (Fixpoint.DEBUG)
        println(s"${stackHeightMinusOneIndent}POP  $stateFrame:$in \n${stackHeightIndent}  <- $result:$out")
      if (storeIntermediateOutput) {
        val outCacheEntry = OutCacheEntry(dom, in, result, out, Stability.Unstable, mutable.Set() ++ deps)
        addParentDependency(outCacheEntry)
        PopResult.Stable(outCacheEntry)
      } else {
        PopResult.Stable(StableMaker.empty)
      }
    }

    stackHeight = newStackHeight
    fire(FixpointControlEvent.EndFixpoint())
    updatedResult

  private def storeCorecurrentOutput(widen: Boolean, frame: (Dom, state.In), result: TrySturdy[Codom], out: state.Out, deps: Iterable[OutCacheEntry]): PopResult = outCache.get(frame) match
    case None =>
      val outCacheEntry = OutCacheEntry(frame._1, frame._2, result, out, Stability.Unstable, mutable.Set() ++ deps)
      outCache.put(frame, outCacheEntry)
      if (Fixpoint.DEBUG)
        println(s"${stackHeightMinusOneIndent}POP  $frame \n${stackHeightMinusOneIndent}  <- Initial($result):$out")
      addParentDependency(outCacheEntry)
      PopResult.Unstable(result, None)
    case Some(outCacheEntry@OutCacheEntry(previousResult, previousOut)) =>
      val newResult: MaybeChanged[TrySturdy[Codom]] = if (widen) Widen(previousResult, result) else Join(previousResult, result)
      LinearStateOperationCounter.wideningCounter += 1
      val newOut = if (widen) state.widenOut(frame._1)(previousOut, out) else state.joinOut(frame._1)(previousOut, out)
      if (Fixpoint.DEBUG)
        println(s"${stackHeightMinusOneIndent}POP  $frame \n${stackHeightMinusOneIndent}  <- $newResult:$newOut")
      val changed = newResult.hasChanged || newOut.hasChanged
      if (changed) {
        if (Fixpoint.DEBUG_INVARIANTS && outCacheEntry.isStable) {
          throw new IllegalStateException(s"Stable out cache entry may not be written again. $frame ($changed) <- $outCacheEntry")
        }
        outCacheEntry.stability = Stability.Unstable
        outCacheEntry.result = newResult.get
        outCacheEntry.out = newOut.get
        if (storeIntermediateOutput) {
          outCacheEntry.dependencies ++= deps
          addParentDependency(outCacheEntry)
        }
        PopResult.Unstable(newResult.get, Some(newOut.get))
      } else {
        outCacheEntry.stability = Stability.Unstable // in case it was invalid
        if (storeIntermediateOutput) {
          // store entry but also track its dependencies to invalidate it when needed
          outCacheEntry.dependencies ++= deps
          addParentDependency(outCacheEntry)
        }
        PopResult.Stable(outCacheEntry)
      }

object StackedStates:
  def apply[Dom, Codom](state: State)
                       (inStateWidening: InStateWidening[Dom, state.In], readPriorOutput: Boolean, storeNonrecursiveOutput: Boolean, storeIntermediateOutput: Boolean, observers: Iterable[Stack.FixEvent => Unit])
                       (using Finite[Dom], Join[Codom], Widen[Codom]): Stack[Dom, Codom, state.In, state.Out] =
    new StackedStates(state)(inStateWidening, readPriorOutput, storeNonrecursiveOutput, storeIntermediateOutput, observers)

trait InStateWidening[Dom, In]:
  def push(dom: Dom, in: In): MaybeChanged[In]
  def pop(dom: Dom, in: In): Unit

class FiniteInStateWidening[Dom, In](using Finite[In]) extends InStateWidening[Dom, In]:
  def push(dom: Dom, in: In): MaybeChanged[In] = MaybeChanged.Unchanged(in)
  def pop(dom: Dom, in: In): Unit = ()

class ContextualInStateWidening[Ctx, Dom, In, Codom](contextual: Contextual[Ctx, Dom, Codom])(using widenIn: Dom => StackWidening[In]) extends InStateWidening[Dom, In]:
  class ContextEntry(var in: List[In]):
    override def toString: String = in.toString()
  private var contexts: Map[(Dom, Ctx), ContextEntry] = Map()

  override def toString: String = s"ContextualInWidening($contexts)"

  def push(dom: Dom, in: In): MaybeChanged[In] =
    val ctx =  contextual.getCurrentContext
    contexts.get((dom, ctx)) match
      case None =>
        contexts += ((dom, ctx) -> new ContextEntry(List(in)))
        MaybeChanged.Unchanged(in)
      case Some(ce: ContextEntry) =>
        val widenedIn = widenIn(dom)(ce.in, in)
        ce.in = widenedIn.get :: ce.in
        widenedIn

  def pop(dom: Dom, in: In): Unit =
    val ctx =  contextual.getCurrentContext
    contexts.get((dom, ctx)) match
      case None => throw new IllegalStateException()
      case Some(ce: ContextEntry) =>
        ce.in match
          case Nil => throw new IllegalStateException()
          case _::Nil => contexts -= ((dom, ctx))
          case _ => ce.in = ce.in.tail