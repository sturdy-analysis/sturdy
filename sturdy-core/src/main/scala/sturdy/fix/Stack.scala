package sturdy.fix

import sturdy.effect.AnalysisState
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
final class Stack[Dom, Codom, In, Out, All, Ctx](state: AnalysisState[Dom, In, Out, All], contextual: Contextual[Ctx, Dom, Codom])
  (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], joinOut: Join[Out], effectStack: EffectStack)
  (using Finite[Dom], Finite[Ctx]):

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private var stack: Map[Frame[Dom, Ctx], FrameInstanceInfo[In]] = Map()
  private var stackHeight = 0

  /** Cache of the inputs of previously executed stack frames.
   */
  private var inCache: Map[Frame[Dom, Ctx], In] = Map()
  private var inCacheDirty: Boolean = false

  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  var outCache: Map[Frame[Dom, Ctx], OutCacheEntry] = Map()

  case class OutCacheEntry(result: TrySturdy[Codom], out: Out, var stability: Stability):
    def isStable: Boolean = stability eq Stability.Stable

  enum Stability:
    case Stable
    case Unstable

  private var outCacheDirty: Boolean = false

  /** Set of _active_ stack frames that have recurred.
   *  When a stack frame becomes inactive, it is also removed from this set.
   */
  private val recurrentCalls: mutable.Set[Int] = mutable.BitSet()
  def hasRecurrentCalls: Boolean = recurrentCalls.nonEmpty

  override def toString: String = stack.toList.map(_._1).mkString("Stack(", ", ", ")")

  /** Current height of the stack. */
  def height: Int = stackHeight

  def stackHeightIndent: String = "  " * (stackHeight - 1)

  /** Iterates `f` until the outCache is stable. */
  def repeatUntilStable[A](dom: Dom)(f: () => (A, Boolean)): A = {
    val originalState = state.getAllState
    var iterationCount = 0

    var previousInCache = inCache
    var previousOutCache = outCache

    var outerInCacheDirty = inCacheDirty
    var outerOutCacheDirty = outCacheDirty
    inCacheDirty = false
    outCacheDirty = false

    while (true) {
      val (result, looping) = f()
      if (!looping || !inCacheDirty && !outCacheDirty) {
        if (Fixpoint.DEBUG_INVARIANTS) {
          if (looping && inCache != previousInCache) throw new IllegalStateException(s"inChacheDirty was wrong")
          if (looping && outCache != previousOutCache) throw new IllegalStateException(s"outChacheDirty was wrong")
        }
        inCacheDirty |= outerInCacheDirty
        outCacheDirty |= outerOutCacheDirty
        return result
      } else {
        if (Fixpoint.DEBUG_INVARIANTS) {
          if (inCacheDirty && inCache == previousInCache) throw new IllegalStateException(s"inChacheDirty was wrong")
          if (outCacheDirty && outCache == previousOutCache) throw new IllegalStateException(s"outChacheDirty was wrong")
        }
        outerInCacheDirty |= inCacheDirty
        outerOutCacheDirty |= outCacheDirty
        inCacheDirty = false
        outCacheDirty = false

        iterationCount += 1
        if (Fixpoint.DEBUG)
          println(s"## REPEAT (Iteration $iterationCount)")

        previousInCache = inCache
        previousOutCache = outCache
        state.setAllState(originalState)
        effectStack.repeating()
      }
    }
    throw new IllegalStateException()
  }

  inline private def dropFrame(frame: Frame[Dom, Ctx]): Map[Frame[Dom, Ctx], FrameInstanceInfo[In]] =
    stack.get(frame) match
      case None => throw new MatchError(stack)
      case Some(info) =>
        if (info.popped())
          stack
        else
          stack - frame

  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is not recurrent, yields None.
   *  If the frame is recurrent and has not been previously executed, throws a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields the previous result.
   */
  def push(dom: Dom, in: In): Option[TrySturdy[Codom]] =
    val ctx = contextual.getCurrentContext
    val frame = Frame(dom, ctx)
    stack.get(frame) match
      case None =>
        // call is not recurrent
        // load input state based on previous calls with the same context
        val MaybeChanged(loadedIn, inHasChanged) = loadCorecurrentInput(frame, in)

        if (inHasChanged) {
          // previous input does not subsume current input => previous result is not valid anymore
          outCache.get(frame).foreach(_.stability = Stability.Unstable)
        } else {
          outCache.get(frame).filter(_.isStable).foreach { outCacheEntry =>
            // previous input subsume current input and previous result still stable => return previous result
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}  PUSH RECURRENT DIRECTLY $frame:$loadedIn <- ${outCacheEntry.result}")
            state.setOutState(outCacheEntry.out)
            return Some(outCacheEntry.result)
          }
        }

        // push call to stack
        val info = new FrameInstanceInfo(loadedIn, stackHeight)
        stack += frame -> info
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
            println(s"${stackHeightIndent}PUSH SEMI-RECURRENT (stack) $frame:$stackInWidened")

          info.pushed(stackInWidened, stackHeight)
          stackHeight += 1
          state.setInState(stackInWidened)
          outCache.get(frame).foreach(_.stability = Stability.Unstable)
          None

//
//          val tryNew = false
//          if (!tryNew)
//            updateSemiRecurrentFrame(frame, info, stackInWidened)
//          else
//            val stackCacheInWidened = loadCorecurrentInput(frame, stackInWidened)
//            updateSemiRecurrentFrame(frame, info, stackCacheInWidened.get)
        } else {
          LinearStateOperationCounter.wideningCounter += 1
          val cachedInWidened = inCache.get(frame).map(Profiler.addTime("widen"){widenIn(_, in)})
          if (cachedInWidened.nonEmpty && cachedInWidened.get.hasChanged) {
            // call is semi-recurrent (cache): output state occurs in the cache but for an incompatible input state
            val newIn = cachedInWidened.get.get
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}PUSH SEMI-RECURRENT (cache) $frame:$newIn")
            updateSemiRecurrentFrame(frame, info, newIn)
            inCache += frame -> newIn
            inCacheDirty = true
            None
          } else {
            // call is recurrent
            recurrentCalls += info.frameID
            if (Fixpoint.DEBUG)
              println(s"${stackHeightIndent}  PUSH RECURRENT $frame:$in")
            if (cachedInWidened.isEmpty) {
              // input is new
              inCache += frame -> stackInWidened
              inCacheDirty = true
              if (Fixpoint.DEBUG_INVARIANTS && outCache.contains(frame))
                throw new IllegalStateException(s"Found existing output for new input of $frame")
            } else {
              // input is unchanged, nothing to do
            }
            Some(loadRecurrentOutput(frame))
          }
        }

  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(dom: Dom, in: In, result: TrySturdy[Codom]): (TrySturdy[Codom], Boolean) =
    val ctx = contextual.getCurrentContext
    val frame = Frame(dom, ctx)
    val newStackHeight = stackHeight - 1
//    println(s"$newStackHeight in $recurrentCalls?")
    val updatedResult = if (recurrentCalls.remove(newStackHeight)) {
      if (!result.isRecurrent) {
        val (widenedResult, changed) = storeCorecurrentOutput(frame, result)
        (widenedResult, changed)
      } else {
        if (Fixpoint.DEBUG)
          println(s"${stackHeightIndent}POP  $frame:$in <- $result")
        (result, true)
      }
    } else {
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame:$in <- $result")
      (result, false)
    }
    stack = dropFrame(frame)
    stackHeight = newStackHeight
    updatedResult

  inline private def updateSemiRecurrentFrame(frame: Frame[Dom, Ctx], info: FrameInstanceInfo[In], newIn: In): Unit = {
    info.pushed(newIn, stackHeight)
    stackHeight += 1
    state.setInState(newIn)
  }

  inline private def loadCorecurrentInput(frame: Frame[Dom, Ctx], in: In): MaybeChanged[In] = inCache.get(frame) match
    case None =>
      inCache += frame -> in
      inCacheDirty = true
      Unchanged(in)
    case Some(recurrentIn) =>
      LinearStateOperationCounter.wideningCounter += 1
      val newIn = Profiler.addTime("widen"){widenIn(recurrentIn, in)}
      if (newIn.hasChanged)
        inCache += frame -> newIn.get
        inCacheDirty = true
      state.setInState(newIn.get)
      newIn

  inline private def loadRecurrentOutput(frame: Frame[Dom, Ctx]): TrySturdy[Codom] = outCache.get(frame) match
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

  inline private def storeCorecurrentOutput(frame: Frame[Dom, Ctx], result: TrySturdy[Codom]): (TrySturdy[Codom], Boolean)  = outCache.get(frame) match
    case None =>
      val out = state.getOutState(frame.dom)
      outCache += frame -> OutCacheEntry(result, out, Stability.Unstable)
//      println(s"write to outCache: $out")
      outCacheDirty = true
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame <- ${Changed(result)}")
      (result, true)
    case Some(outCacheEntry@OutCacheEntry(previousResult, previousOut, stability)) =>
      val newResult: MaybeChanged[TrySturdy[Codom]] = Widen(previousResult, result)
      val currentOut = state.getOutState(frame.dom)
      LinearStateOperationCounter.wideningCounter += 1
      val newOut = Profiler.addTime("widen"){widenOut(previousOut, currentOut)}

//      if (Widen(result, previousResult).hasChanged) {
//        throw new IllegalStateException(s"bla $result $previousResult")
//      }
//      if (widenOut(currentOut, previousOut).hasChanged) {
//        throw new IllegalStateException(s"bla $currentOut $previousOut $newOut")
//      }

      if (Fixpoint.DEBUG_INVARIANTS && outCacheEntry.isStable) {
        println(s"${stackHeightIndent}blaPOP  $frame <- $newResult")
        throw new IllegalStateException(s"Why can a stable out cache entry be written again?")
      }
      val changed = newResult.hasChanged || newOut.hasChanged
      if (changed) {
        outCache += frame -> OutCacheEntry(newResult.get, newOut.get, Stability.Unstable)
//        println(s"write to outCache: $newOut")
        outCacheDirty = true
      } else {
//        println(s"set stable in outCache: $newOut")
        outCacheEntry.stability = Stability.Stable
      }
      state.setOutState(newOut.get)

      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame <- $newResult")
      (newResult.get, changed)

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
    this.frameIDs.nonEmpty
