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
final class Stack[Dom, Codom, In, Out, All, Ctx](state: AnalysisState[Dom, In, Out, All], contextual: Contextual[Ctx, Dom, Codom])
  (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], joinOut: Join[Out], effectStack: EffectStack)
  (using Finite[Dom], Finite[Ctx]):

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private val stack: MutableMap[Frame[Dom, Ctx], FrameInstanceInfo[In]] = Maps.mutable.empty()
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
  private val recurrentCalls: mutable.Set[Int] = mutable.BitSet()
  def hasRecurrentCalls: Boolean = recurrentCalls.nonEmpty

  override def toString: String = stack.keysView().makeString("Stack(", ", ", ")")

  /** Current height of the stack. */
  def height: Int = stack.size()

  def stackHeightIndent: String = "  " * (height - 1)

  inline private def dropFrame(frame: Frame[Dom, Ctx]): Unit = {
    val info = stack.getIfAbsent(frame, () => throw new MatchError(stack))
    val isEmpty = info.popped()
    if (isEmpty)
      stack.remove(frame)
  }

  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is not recurrent, yields None.
   *  If the frame is recurrent and has not been previously executed, throws a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields the previous result.
   */
  def push(dom: Dom, in: In): Option[TrySturdy[Codom]] =
    val ctx = contextual.getCurrentContext
    val frame = Frame(dom, ctx)
    Option(stack.get(frame)) match
      case None =>
        // call is not recurrent
        // load input state based on previous calls with the same context
        val MaybeChanged(loadedIn, inHasChanged) = loadCorecurrentInput(frame, in)

        val outEntry = Option(outCache.get(frame))
        if (inHasChanged) {
          // previous input does not subsume current input => previous result is not valid anymore
          outEntry.foreach(_.setUnstable())
        } else {
          outEntry.filter(_.isStable).foreach { outCacheEntry =>
            // previous input subsume current input and previous result still stable => return previous result
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}  READ PRIOR OUTPUT $frame:$loadedIn <- ${outCacheEntry.result}")
            state.setOutState(outCacheEntry.out)
            return Some(outCacheEntry.result)
          }
        }

        // push call to stack
        val info = new FrameInstanceInfo(loadedIn, stackHeight)
        stack.put(frame, info)
        stackHeight += 1
        if (Fixpoint.DEBUG)
          println(s"${stackHeightIndent}PUSH $frame:${state.getInState(dom)}")
        None
      case Some(info) =>
        LinearStateOperationCounter.wideningCounter += 1
        val MaybeChanged(stackInWidened, stackWasWider) = Profiler.addTime("widen"){widenIn(info.inState, in)}
        if (stackWasWider) {
          // call is semi-recurrent (stack): the frame occurs on the stack but with a different state
          if (Fixpoint.DEBUG)
            println(s"${stackHeightIndent}PUSH SEMI-RECURRENT $frame:$stackInWidened")

          info.pushed(stackInWidened, stackHeight)
          stackHeight += 1
          state.setInState(stackInWidened)
          Option(outCache.get(frame)).foreach(_.stability = Stability.Unstable)
          None
        } else {
          // call is recurrent
          recurrentCalls += info.frameID
          if (Fixpoint.DEBUG)
            println(s"${stackHeightIndent}  PUSH RECURRENT $frame:$in")

          Option(inCache.get(frame)) match
            case None =>
              // input is new
              inCache.put(frame, stackInWidened)
              if (Fixpoint.DEBUG_INVARIANTS && outCache.contains(frame))
                throw new IllegalStateException(s"Found existing output for new input of $frame")
            case Some(inCached) =>
              LinearStateOperationCounter.wideningCounter += 1
              val MaybeChanged(inCachedWidened, inCacheHasChanged) = Profiler.addTime("widen"){widenIn(inCached, in)}
              if (inCacheHasChanged) {
                inCache.put(frame, inCachedWidened)
              }
          Some(loadRecurrentOutput(frame))
        }

  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(dom: Dom, in: In, result: TrySturdy[Codom]): MaybeChanged[TrySturdy[Codom]] =
    val ctx = contextual.getCurrentContext
    val frame = Frame(dom, ctx)
    val newStackHeight = stackHeight - 1
    val updatedResult = if (recurrentCalls.remove(newStackHeight)) {
      storeCorecurrentOutput(frame, result)
    } else {
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame:$in <- $result")
      MaybeChanged.Unchanged(result)
    }
    dropFrame(frame)
    stackHeight = newStackHeight
    updatedResult


  inline private def loadCorecurrentInput(frame: Frame[Dom, Ctx], in: In): MaybeChanged[In] = Option(inCache.get(frame)) match
    case None =>
      inCache.put(frame, in)
      Unchanged(in)
    case Some(recurrentIn) =>
      LinearStateOperationCounter.wideningCounter += 1
      val newIn = Profiler.addTime("widen"){widenIn(recurrentIn, in)}
      if (newIn.hasChanged) {
        inCache.put(frame, newIn.get)
      }
      state.setInState(newIn.get)
      newIn

  inline private def loadRecurrentOutput(frame: Frame[Dom, Ctx]): TrySturdy[Codom] = Option(outCache.get(frame)) match
    case None =>
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}  POP RECURRENT  $frame")
      TrySturdy(throw RecurrentCall(frame))
    case Some(OutCacheEntry(res, previousOut, _)) =>
      LinearStateOperationCounter.wideningCounter += 1
      state.setOutState(Profiler.addTime("widen"){joinOut(state.getOutState(frame.dom), previousOut).get})
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}  POP RECURRENT  $frame <- $res")
      res

  inline private def storeCorecurrentOutput(frame: Frame[Dom, Ctx], result: TrySturdy[Codom]): MaybeChanged[TrySturdy[Codom]] = Option(outCache.get(frame)) match
    case None =>
      val out = state.getOutState(frame.dom)
      outCache.put(frame, OutCacheEntry(result, out, Stability.Unstable))
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame <- ${Changed(result)}")
      MaybeChanged.Changed(result)
    case Some(outCacheEntry@OutCacheEntry(previousResult, previousOut, stability)) =>
      val newResult: MaybeChanged[TrySturdy[Codom]] = Widen(previousResult, result)
      val currentOut = state.getOutState(frame.dom)
      LinearStateOperationCounter.wideningCounter += 1
      val newOut = Profiler.addTime("widen"){widenOut(previousOut, currentOut)}

      val changed = newResult.hasChanged || newOut.hasChanged
      if (changed) {
        outCache.put(frame, OutCacheEntry(newResult.get, newOut.get, Stability.Unstable))
        if (Fixpoint.DEBUG_INVARIANTS && outCacheEntry.isStable) {
          throw new IllegalStateException(s"Stable out cache entry may not be written again. $frame ($changed) <- $outCacheEntry")
        }
      } else {
        outCacheEntry.stability = Stability.Stable
      }
      state.setOutState(newOut.get)

      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame <- $newResult")
      MaybeChanged(newResult.get, changed)

case class Frame[Dom, Ctx](dom: Dom, ctx: Ctx)
class FrameInstanceInfo[In](var inStates: List[In], var frameIDs: List[Int]):
  def this(in: In, id: Int) =
    this(in::Nil, id::Nil)
  def inState: In = inStates.head
  def frameID: Int = frameIDs.head
  def pushed(newIn: In, newID: Int): Unit =
    this.inStates = newIn :: this.inStates
    this.frameIDs = newID :: this.frameIDs
  /** Yields true if frame is non-empty after pop. */
  def popped(): Boolean =
    this.inStates = this.inStates.tail
    this.frameIDs = this.frameIDs.tail
    this.frameIDs.isEmpty
