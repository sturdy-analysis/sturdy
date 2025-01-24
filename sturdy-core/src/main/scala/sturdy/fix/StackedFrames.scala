package sturdy.fix

import org.eclipse.collections.api.factory.Maps
import org.eclipse.collections.api.map.MutableMap
import sturdy.effect.EffectStack
import sturdy.effect.RecurrentCall
import sturdy.effect.SturdyThrowable
import sturdy.effect.{CombineTrySturdy, TrySturdy}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Finite
import sturdy.values.{Changed, Join, MaybeChanged, Unchanged, Widen}

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
final class StackedFrames[Dom, Codom, Ctx](val state: State)
                                          (contextual: Contextual[Ctx, Dom, Codom], readPriorOutput: Boolean, onlyWriteInCacheWhenRecurrent: Boolean)
                                          (using Finite[Dom], Finite[Ctx], Join[Codom], Widen[Codom])
  extends Stack[Dom, Codom, state.In, state.Out]:

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private val stack: mutable.Map[Frame[Dom, Ctx], FrameInstanceInfo] = mutable.Map.empty
  private var stackHeight: Int = 0

  /** Cache of the inputs of previously executed stack frames.
   */
  private val inCache: mutable.Map[Frame[Dom, Ctx], state.In] = mutable.Map.empty

  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  private val outCache: mutable.Map[Frame[Dom, Ctx], OutCacheEntry] = mutable.Map.empty

  override def getCache: Map[Dom, TrySturdy[Codom]] = outCache.groupBy(_._1._1).view.mapValues { m =>
    m.values.map(_.result).reduce((r1,r2) => Join(r1,r2).get)
  }.toMap

  case class OutCacheEntry(result: TrySturdy[Codom], out: state.Out, var stability: Stability):
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

  override def toString: String = stack.keys.toString

  /** Current height of the stack. */
  def height: Int = stackHeight //stack.size()

  def stackHeightIndent: String = "  " * (height)
  def stackHeightMinusOneIndent: String = "  " * (height - 1)

  inline private def dropFrame(frame: Frame[Dom, Ctx], id: Int): Boolean = {
    val info = stack.getOrElse(frame, throw new MatchError(stack))
    val isEmpty = info.popAndGetIsEmpty(id)
    if (isEmpty)
      stack.remove(frame)
    isEmpty
  }

  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is not recurrent, yields None.
   *  If the frame is recurrent and has not been previously executed, throws a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields the previous result.
   */
  def push(dom: Dom, in: state.In, currentOut: state.Out): PushResult =
    if (Thread.currentThread().isInterrupted)
      throw new InterruptedException

    val ctx = contextual.getCurrentContext
    val frame = Frame(dom, ctx)
    stack.get(frame) match
      case None =>
        // call is not recurrent
        // load input state based on previous calls with the same context
        val MaybeChanged(loadedIn, inHasChanged) = loadStateFromInCache(frame, in)
        val outEntry = outCache.get(frame)

        if (readPriorOutput && !inHasChanged) {
          val outEntry = outCache.get(frame)
          if (outEntry.exists(_.isStable)) {
            // previous input subsumes current input and previous result still stable => return previous result
            val OutCacheEntry(result, out, _) = outEntry.get
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}READ PRIOR OUTPUT $frame:$loadedIn <- $result:$out")
            return PushResult.Recurrent(result, Some(out))
          }
        }

        if (inHasChanged) {
          val outEntry = outCache.get(frame)
          outEntry.foreach(_.setUnstable())
        }

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

      case Some(info) =>
        inCache.get(frame) match {
          case None =>
            if (!onlyWriteInCacheWhenRecurrent)
              throw new IllegalStateException("inCache(frame) should have been created when frame was pushed to stack")
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}PUSH SEMI-RECURRENT (new) $frame:$in")
            inCache.put(frame, in)
            info.push(stackHeight)
            stackHeight += 1
            PushResult.Continue(None)
          case Some(cachedIn) =>
            LinearStateOperationCounter.wideningCounter += 1
            val inWidenedWithCache = Profiler.addTime("widen"){state.widenIn(dom)(cachedIn, in)}
            if (inWidenedWithCache.hasChanged || info.frameIdWithInStateOfCache.isEmpty) {
              // call is semi-recurrent: output state occurs in the cache but for an incompatible input state
              val newIn = inWidenedWithCache.get
              if (Fixpoint.DEBUG)
                println(s"${stackHeightIndent}PUSH SEMI-RECURRENT $frame:$newIn")

              info.push(stackHeight)
              stackHeight += 1
              inCache.put(frame, newIn)
              outCache.get(frame).foreach(_.stability = Stability.Unstable)
              PushResult.Continue(Some(newIn))
            } else {
              // call is recurrent
              corecurrentCalls += info.frameIdWithInStateOfCache.get
              if (Fixpoint.DEBUG)
                println(s"${stackHeightIndent}PUSH RECURRENT $frame:${inWidenedWithCache.get}")
              loadRecurrentOutput(frame, currentOut)
            }
        }

  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(dom: Dom, in: state.In, codom: TrySturdy[Codom], out: state.Out): PopResult =
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
    if (dropFrame(frame, newStackHeight) && false) {
      inCache.remove(frame)
      outCache.remove(frame)
    }
    stackHeight = newStackHeight
    updatedResult


  inline private def loadStateFromInCache(frame: Frame[Dom, Ctx], in: state.In): MaybeChanged[state.In] = inCache.get(frame) match
    case None =>
      if (!onlyWriteInCacheWhenRecurrent)
        inCache.put(frame, in)
      Unchanged(in)
    case Some(recurrentIn) =>
      LinearStateOperationCounter.wideningCounter += 1
      val newIn = Profiler.addTime("widen"){state.widenIn(frame.dom)(recurrentIn, in)}
      if (newIn.hasChanged)
        inCache.put(frame, newIn.get)
      newIn

  inline private def loadRecurrentOutput(frame: Frame[Dom, Ctx], currentOut: state.Out): PushResult = outCache.get(frame) match
    case None =>
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP RECURRENT  $frame")
      PushResult.Recurrent(TrySturdy(throw RecurrentCall(frame)), None)
    case Some(OutCacheEntry(result, out, _)) =>
      LinearStateOperationCounter.wideningCounter += 1
      val joinedOut = Profiler.addTime("widen"){state.joinOut(frame.dom)(currentOut, out).get}
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP RECURRENT  $frame <- $result:$joinedOut")
      PushResult.Recurrent(result, Some(joinedOut))

  inline private def storeCorecurrentOutput(frame: Frame[Dom, Ctx], result: TrySturdy[Codom], out: state.Out): PopResult = outCache.get(frame) match
    case None =>
      outCache.put(frame, OutCacheEntry(result, out, Stability.Unstable))
      if (Fixpoint.DEBUG)
        println(s"${stackHeightMinusOneIndent}POP  $frame <- ${Changed(result)}")
      PopResult.Unstable(result, None)
    case Some(outCacheEntry@OutCacheEntry(previousResult, previousOut, stability)) =>
      val newResult: MaybeChanged[TrySturdy[Codom]] = Widen(previousResult, result)
      LinearStateOperationCounter.wideningCounter += 1
      val newOut = Profiler.addTime("widen"){state.widenOut(frame.dom)(previousOut, out)}

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
  def apply[Dom, Codom, Ctx](state: State)
                            (contextual: Contextual[Ctx, Dom, Codom], readPriorOutput: Boolean, onlyWriteInCacheWhenRecurrent: Boolean)
                            (using Finite[Dom], Finite[Ctx], Join[Codom], Widen[Codom]): Stack[Dom, Codom, state.In, state.Out] =
    new StackedFrames(state)(contextual, readPriorOutput, onlyWriteInCacheWhenRecurrent).asInstanceOf[Stack[Dom, Codom, state.In, state.Out]]

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
