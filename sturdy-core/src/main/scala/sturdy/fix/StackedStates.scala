package sturdy.fix

import org.eclipse.collections.api.factory.Maps
import org.eclipse.collections.api.map.MutableMap
import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.effect.SturdyThrowable
import sturdy.effect.{CombineTrySturdy, TrySturdy}
import sturdy.util.{Profiler, LinearStateOperationCounter}
import sturdy.values.Finite
import sturdy.values.{MaybeChanged, Unchanged, Join, Changed, Widen}

import scala.collection.mutable
import scala.util.Failure
import scala.util.Success
import scala.util.Try

final class StackedStates[Dom, Codom, In, Out](inStateWidening: InStateWidening[Dom, In])
                                              (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], joinOut: Join[Out], effectStack: EffectStack)
                                              (using Finite[Dom])
  extends Stack[Dom, Codom, In, Out]:

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private val stack: MutableMap[(Dom, In), FrameInstanceInfo] = Maps.mutable.empty()
  private var stackHeight: Int = 0

  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  private val outCache: MutableMap[(Dom, In), OutCacheEntry] = Maps.mutable.empty()

  case class OutCacheEntry(result: TrySturdy[Codom], out: Out, var stability: Stability):
    def isStable: Boolean = stability eq Stability.Stable
    def setStable(): Unit = this.stability = Stability.Stable
    def setUnstable(): Unit = this.stability = Stability.Unstable

  enum Stability:
    case Stable
    case Unstable

  /** Set of _active_ stack frames that have recurred.
   *  When a stack frame becomes inactive, it is also removed from this set.
   */
  private val corecurrentCalls: mutable.Set[Int] = mutable.BitSet()
  def hasRecurrentCalls: Boolean = corecurrentCalls.nonEmpty

  override def toString: String = stack.keysView().makeString("Stack(", ", ", ")")

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
  def push(dom: Dom, in: In): PushResult =
    if (Thread.currentThread().isInterrupted)
      throw new InterruptedException

    val widenedIn = inStateWidening.push(dom, in).get
    val stateFrame = (dom, widenedIn)
    Option(stack.get(stateFrame)) match
      case None =>
        // call is not recurrent
        Option(outCache.get(stateFrame)) match
          case Some(OutCacheEntry(result, out, Stability.Stable)) =>
            // stable output available
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}READ PRIOR OUTPUT $stateFrame <- $result:$out")
            PushResult.Recurrent(result, Some(out))
          case _ =>
            // push call to stack
            val info = new FrameInstanceInfo(stackHeight)
            stack.put(stateFrame, info)
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}PUSH $stateFrame")
            stackHeight += 1
            PushResult.Continue(Some(widenedIn))
      case Some(info) =>
        // call is recurrent
        corecurrentCalls += info.frameIdWithInStateOfCache.get
        if (Fixpoint.DEBUG)
          println(s"${stackHeightIndent}PUSH RECURRENT $stateFrame")
        Option(outCache.get(stateFrame)) match
          case None =>
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}POP RECURRENT  $stateFrame")
            PushResult.Recurrent(TrySturdy(throw RecurrentCall(stateFrame)), None)
          case Some(OutCacheEntry(res, previousOut, _)) =>
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}POP RECURRENT  $stateFrame <- $res")
            PushResult.Recurrent(res, Some(previousOut))

  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(dom: Dom, in: In, result: TrySturdy[Codom], out: Out): PopResult =
    val stateFrame = (dom, in)
    inStateWidening.pop(dom, in)
    val newStackHeight = stackHeight - 1
    val updatedResult = if (corecurrentCalls.remove(newStackHeight)) {
      storeCorecurrentOutput(stateFrame, result, out)
    } else {
      if (Fixpoint.DEBUG)
        println(s"${stackHeightMinusOneIndent}POP  $stateFrame:$in <- $result")
      PopResult.Stable
    }
    val previousInfo = stack.remove(stateFrame)
    if (Fixpoint.DEBUG_INVARIANTS && previousInfo == null)
      throw new IllegalStateException(s"Pop must delete a previously pushed frame but did not $stateFrame")
    stackHeight = newStackHeight
    updatedResult


  inline private def storeCorecurrentOutput(frame: (Dom, In), result: TrySturdy[Codom], out: Out): PopResult = Option(outCache.get(frame)) match
    case None =>
      outCache.put(frame, OutCacheEntry(result, out, Stability.Unstable))
      if (Fixpoint.DEBUG)
        println(s"${stackHeightMinusOneIndent}POP  $frame <- ${Changed(result)}")
      PopResult.Unstable(result, None)
    case Some(outCacheEntry@OutCacheEntry(previousResult, previousOut, stability)) =>
      val newResult: MaybeChanged[TrySturdy[Codom]] = Widen(previousResult, result)
      LinearStateOperationCounter.wideningCounter += 1
      val newOut = Profiler.addTime("widen"){widenOut(previousOut, out)}

      if (Fixpoint.DEBUG)
        println(s"${stackHeightMinusOneIndent}POP  $frame <- $newResult")
      val changed = newResult.hasChanged || newOut.hasChanged
      if (changed) {
        outCache.put(frame, OutCacheEntry(newResult.get, newOut.get, Stability.Unstable))
        if (Fixpoint.DEBUG_INVARIANTS && outCacheEntry.isStable) {
          throw new IllegalStateException(s"Stable out cache entry may not be written again. $frame ($changed) <- $outCacheEntry")
        }
        PopResult.Unstable(newResult.get, Some(newOut.get))
      } else {
        outCacheEntry.stability = Stability.Stable
        PopResult.Stable
      }

trait InStateWidening[Dom, In]:
  def push(dom: Dom, in: In): MaybeChanged[In]
  def pop(dom: Dom, in: In): Unit

class FiniteInStateWidening[Dom, In](using Finite[In]) extends InStateWidening[Dom, In]:
  def push(dom: Dom, in: In): MaybeChanged[In] = MaybeChanged.Unchanged(in)
  def pop(dom: Dom, in: In): Unit = ()

class ContextualInStateWidening[Ctx, Dom, In, Codom](contextual: Contextual[Ctx, Dom, Codom])(using widenIn: Widen[In]) extends InStateWidening[Dom, In]:
  class ContextEntry(var in: List[In])
  private var contexts: Map[(Dom, Ctx), ContextEntry] = Map()

  def push(dom: Dom, in: In): MaybeChanged[In] =
    val ctx =  contextual.getCurrentContext
    contexts.get((dom, ctx)) match
      case None =>
        contexts += ((dom, ctx) -> new ContextEntry(List(in)))
        MaybeChanged.Unchanged(in)
      case Some(ce: ContextEntry) =>
        val widenedIn = widenIn(ce.in.head, in)
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