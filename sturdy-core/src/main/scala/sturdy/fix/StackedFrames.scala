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

/** This class implements the central Stack data structure for computing fixpoints in Sturdy.
 *  The function to be fixed has purified type `f: (Dom, In) => (Codom, Out)`.
 *  The stack is intended to be used by iteration strategies, which push and pop calls to the stack and
 *  invoke `repeatUntilStable` to compute fixpoints.
 *
 *  Terminology:
 *   - Call: a call of function `f(dom,in)` identified by its input value `Dom` and input state `In`. Type `Dom` must be finite.
 *   - Context: a finite abstraction of input states, denoted `Ctx`.
 *   - Frame: a Call whose input state is abstracted to a context yielding `(Dom,Ctx)`, which is finite.
 *   - FrameInfo: information associated with a frame, including its input state
 *
 *   Big-step fixpoint algorithms must identify and prevent recurrent calls to interrupt unbounded recursion.
 *   Recurrent calls occur when pushing to the stack:
 *   - Recurrent calls: A call `f(dom,in)` is recurrent if `fr = Frame(dom,context(in))` already exists on the stack and
 *     `fr.in >= in`, that is, we have seen a call with the same `dom`, same context, and a larger `fr.in` before.
 *   - Co-recurrent calls: When `f(dom,in)` is a recurrent call due to frame `fr = Frame(dom,context(in))` on the stack,
 *     then `f(dom,fr.in)` is the co-recurrent call belonging to `f(from,in)`.
 *   - Semi-recurrent calls: A call `f(dom,in)` is semi-recurrent if `fr = Frame(dom,context(in))` is on the stack but
 *     `fr.in < in`, that is, the current input `in` is larger than the previous `fr.in`.
 *
 */
final class StackedFrames[Dom, Codom, In, Out, Ctx](contextual: Contextual[Ctx, Dom, Codom])
                                                   (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], joinOut: Join[Out], effectStack: EffectStack)
                                                   (using Finite[Dom], Finite[Ctx])
  extends Stack[Dom, Codom, In, Out]:

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private val stack: MutableMap[Frame[Dom, Ctx], FrameInstanceInfo] = Maps.mutable.empty()
  private var stackHeight: Int = 0

  /** Cache of the inputs of previously executed stack frames.
   */
  private val inCache: MutableMap[Frame[Dom, Ctx], In] = Maps.mutable.empty()

  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  private val outCache: MutableMap[Frame[Dom, Ctx], OutCacheEntry] = Maps.mutable.empty()

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

  inline private def dropFrame(frame: Frame[Dom, Ctx], id: Int): Unit = {
    val info = stack.getIfAbsent(frame, () => throw new MatchError(stack))
    val isEmpty = info.popAndGetIsEmpty(id)
    if (isEmpty)
      stack.remove(frame)
  }

  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is not recurrent, yields None.
   *  If the frame is recurrent and has not been previously executed, throws a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields the previous result.
   */
  def push(dom: Dom, in: In): PushResult =
    if (Thread.currentThread().isInterrupted)
      throw new InterruptedException

    val ctx = contextual.getCurrentContext
    val frame = Frame(dom, ctx)
    Option(stack.get(frame)) match
      case None =>
        // call is not recurrent
        // load input state based on previous calls with the same context
        val MaybeChanged(loadedIn, inHasChanged) = loadStateFromInCache(frame, in)
        val outEntry = Option(outCache.get(frame))

        if (!inHasChanged && outEntry.exists(_.isStable)) {
          // previous input subsumes current input and previous result still stable => return previous result
          val OutCacheEntry(result, out, _) = outEntry.get
          if (Fixpoint.DEBUG)
            println(s"${stackHeightIndent}READ PRIOR OUTPUT $frame:$loadedIn <- $result:$out")
          PushResult.Recurrent(result, Some(out))
        } else {
          if (inHasChanged)
            outEntry.foreach(_.setUnstable())

          // push call to stack
          val info = new FrameInstanceInfo(stackHeight)
          stack.put(frame, info)
          if (Fixpoint.DEBUG)
            println(s"${stackHeightIndent}PUSH $frame:$in")
          stackHeight += 1
          if (inHasChanged)
            PushResult.Continue(Some(loadedIn))
          else
            PushResult.Continue(None)
        }

      case Some(info) =>
        Option(inCache.get(frame)) match {
          case None =>
            if (StackedFrames.writeInCacheWhenPushToStack)
              throw new IllegalStateException("inCache(frame) should have been created when frame was pushed to stack")
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}PUSH SEMI-RECURRENT (new) $frame:$in")
            inCache.put(frame, in)
            info.push(stackHeight)
            stackHeight += 1
            PushResult.Continue(None)
          case Some(cachedIn) =>
            LinearStateOperationCounter.wideningCounter += 1
            val inWidenedWithCache = Profiler.addTime("widen"){widenIn(cachedIn, in)}
            if (inWidenedWithCache.hasChanged || info.frameIdWithInStateOfCache.isEmpty) {
              // call is semi-recurrent: output state occurs in the cache but for an incompatible input state
              val newIn = inWidenedWithCache.get
              if (Fixpoint.DEBUG)
                println(s"${stackHeightIndent}PUSH SEMI-RECURRENT $frame:$newIn")

              info.push(stackHeight)
              stackHeight += 1
              inCache.put(frame, newIn)
              Option(outCache.get(frame)).foreach(_.stability = Stability.Unstable)
              PushResult.Continue(Some(newIn))
            } else {
              // call is recurrent
              corecurrentCalls += info.frameIdWithInStateOfCache.get
              if (Fixpoint.DEBUG)
                println(s"${stackHeightIndent}PUSH RECURRENT $frame:${inWidenedWithCache.get}")
              loadRecurrentOutput(frame)
            }
        }

  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(dom: Dom, in: In, codom: TrySturdy[Codom], out: Out): PopResult =
    val ctx = contextual.getCurrentContext
    val frame = Frame(dom, ctx)
    val newStackHeight = stackHeight - 1
    val updatedResult = if (corecurrentCalls.remove(newStackHeight)) {
      storeCorecurrentOutput(frame, codom, out)
    } else {
      if (Fixpoint.DEBUG)
        println(s"${stackHeightMinusOneIndent}POP  $frame:$in <- $codom:$out")
      PopResult.Stable
    }
    dropFrame(frame, newStackHeight)
    stackHeight = newStackHeight
    updatedResult


  inline private def loadStateFromInCache(frame: Frame[Dom, Ctx], in: In): MaybeChanged[In] = Option(inCache.get(frame)) match
    case None =>
      if (StackedFrames.writeInCacheWhenPushToStack)
        inCache.put(frame, in)
      Unchanged(in)
    case Some(recurrentIn) =>
      LinearStateOperationCounter.wideningCounter += 1
      val newIn = Profiler.addTime("widen"){widenIn(recurrentIn, in)}
      if (newIn.hasChanged)
        inCache.put(frame, newIn.get)
      newIn

  inline private def loadRecurrentOutput(frame: Frame[Dom, Ctx]): PushResult = Option(outCache.get(frame)) match
    case None =>
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP RECURRENT  $frame")
      PushResult.Recurrent(TrySturdy(throw RecurrentCall(frame)), None)
    case Some(OutCacheEntry(result, out, _)) =>
      LinearStateOperationCounter.wideningCounter += 1
//      val joinedOut = Profiler.addTime("widen"){joinOut(state.getOutState(frame.dom), previousOut).get}
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP RECURRENT  $frame <- $result:$out")
      PushResult.Recurrent(result, Some(out))

  inline private def storeCorecurrentOutput(frame: Frame[Dom, Ctx], result: TrySturdy[Codom], out: Out): PopResult = Option(outCache.get(frame)) match
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

object StackedFrames:
  private val writeInCacheWhenPushToStack: Boolean = false

case class Frame[Dom, Ctx](dom: Dom, ctx: Ctx)
class FrameInstanceInfo(var frameIdWithInStateOfCache: Option[Int]):
  private var frameCounter: Int = 1

  def this(id: Int) =
    this(Some(id))

  def push(newID: Int): Unit =
    frameIdWithInStateOfCache = Some(newID)
    frameCounter += 1

  def popAndGetIsEmpty(id: Int): Boolean =
    frameIdWithInStateOfCache = None
    frameCounter -= 1
//    assert(frameCounter >= 0)
    frameCounter == 0
