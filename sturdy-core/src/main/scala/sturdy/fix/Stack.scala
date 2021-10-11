package sturdy.fix

import sturdy.effect.AnalysisState
import sturdy.effect.Effectful
import sturdy.effect.SturdyException
import sturdy.effect.TrySturdy
import sturdy.values.{Widen, MaybeChanged, Changed, Unchanged}

import scala.collection.mutable
import scala.util.Failure
import scala.util.Success
import scala.util.Try

case class RecurrentCall[Dom, Ctx](frame: Frame[Dom, Ctx]) extends SturdyException:
  override def toString: String = s"RecurrentCall $frame"

case class Frame[Dom, Ctx](dom: Dom, ctx: Ctx)

/** This class implements the central data structure for computing fixpoints.
 *
 *  Terminology:
 *   - Call: a (recursive) call of the abstract interpreter identified by the input value and state `(Dom, In)`.
 *   - Context: an abstraction of calls that ensures the stack has finite height at all times.
 *   - Recurrent and co-recurrent calls: When a call (identified by its context) is pushed while it is already on
 *       the stack, the newly pushed call is recurrent while the previously pushed call is co-recurrent.
 *
 *  Contexts must be assigned to calls in a way that the stack is guaranteed to be finite.
 *  This can be achieved by making sure that each call chain repeats a previous context after
 *  finitely many calls. This property holds in particular if the set of contexts is finite.
 */
final class Stack[Dom, Codom, In, Out, All, Ctx](state: AnalysisState[In, Out, All], contextual: Contextual[Ctx, Dom, Codom])
  (using widenCodom: Widen[Codom], widenIn: Widen[In], widenOut: Widen[Out], j: Effectful):

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private var stack: Map[Frame[Dom, Ctx], Int] = Map()
  private var stackHeight = 0

  /** Cache of the inputs of previously executed stack frames.
   */
  private var inCache: Map[Frame[Dom, Ctx], In] = Map()
  private var inCacheDirty: Boolean = false

  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  private var outCache: Map[Frame[Dom, Ctx], (TrySturdy[Codom], Out)] = Map()
  private var outCacheDirty: Boolean = false

  /** Set of _active_ stack frames that have recurred.
   *  When a stack frame becomes inactive, it is also removed from this set.
   */
  private val recurrentCalls: mutable.Set[Int] = mutable.BitSet()
  private val corecurrentCalls: mutable.Set[Frame[Dom, Ctx]] = mutable.Set()

  override def toString: String = stack.toList.sortBy(_._2).map(_._1).mkString("Stack(", ", ", ")")

  /** Current height of the stack. */
  def height: Int = stackHeight

  def stackHeightIndent: String = "  " * (stackHeight - 1)

  /** Iterates `f` until the outCache is stable. */
  def repeatUntilStable[A](f: () => A): A = {
    val originalState = state.getAllState()
    var iterationCount = 0

    var previousInCache = inCache
    var previousOutCache = outCache

    var outerInCacheDirty = inCacheDirty
    var outerOutCacheDirty = outCacheDirty
    inCacheDirty = false
    outCacheDirty = false

    while (true) {
      val result = f()

      if (!inCacheDirty && !outCacheDirty) {
        if (Fixpoint.DEBUG_CACHE_CHANGES) {
          if (inCache != previousInCache) throw new IllegalStateException(s"inChacheDirty was wrong")
          if (outCache != previousOutCache) throw new IllegalStateException(s"outChacheDirty was wrong")
        }
        inCacheDirty = outerInCacheDirty
        outCacheDirty = outerOutCacheDirty
        return result
      } else {
        if (Fixpoint.DEBUG_CACHE_CHANGES) {
          if (inCacheDirty && inCache == previousInCache) throw new IllegalStateException(s"inChacheDirty was wrong")
          if (outCacheDirty && outCache == previousOutCache) throw new IllegalStateException(s"outChacheDirty was wrong")
        }
        outerInCacheDirty |= inCacheDirty
        outerInCacheDirty |= outCacheDirty
        inCacheDirty = false
        outCacheDirty = false

        iterationCount += 1
        if (Fixpoint.DEBUG)
          println(s"## REPEAT (Iteration $iterationCount)")

        previousInCache = inCache
        previousOutCache = outCache
        state.setAllState(originalState)
        state.repeating()
      }
    }
    throw new IllegalStateException()
  }
  
  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is recurrent and has not been previously executed, throws a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields the previous result.
   */
  def push(dom: Dom, in: In): Option[TrySturdy[Codom]] =
    val ctx = contextual.getCurrentContext
    val frame = Frame(dom, ctx)
    stack.get(frame) match
      case None => // call is not recurrent
        // push call to stack
        stack += frame -> stackHeight
        stackHeight += 1
        if (corecurrentCalls.contains(frame)) {
          // load input state based on previous calls with the same context
          loadCorecurrentInput(frame, in)
        }
        if (Fixpoint.DEBUG)
          println(s"${stackHeightIndent}PUSH $frame -> ${state.getInState()}")
        None
      case Some(ix) =>
        // call is recurrent
        recurrentCalls += ix
        // store the input state so the co-recurrent call considers it
        storeRecurrentInput(frame, in)
        // load any previous output or throw RecurrentCall exception
        loadRecurrentOutput(frame, ix)


  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(dom: Dom, in: In, result: TrySturdy[Codom]): (TrySturdy[Codom], Boolean) =
    val ctx = contextual.getCurrentContext
    val frame = Frame(dom, ctx)
    val entry = stack(frame)
    val newStackHeight = stackHeight - 1
    val updatedResult = if (recurrentCalls.remove(newStackHeight)) {
      corecurrentCalls += frame
      val widenedResult = storeCorecurrentOutput(frame, result)
      (widenedResult, true)
    } else {
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame NOT RECURRENT <- $result, ${state.getOutState()}")
      (result, false)
    }
    stack -= frame
    stackHeight = newStackHeight
    updatedResult

  @inline private def storeRecurrentInput(frame: Frame[Dom, Ctx], in: In): Unit = inCache.get(frame) match
    case None =>
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}## RECURRENT $frame -> $in")
      inCache += frame -> in
      inCacheDirty = true
    case Some(previousIn) =>
      val newIn = widenIn(previousIn, in)
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}## RECURRENT $frame -> $newIn")
      newIn.ifChanged { changedIn =>
        inCache += frame -> changedIn
        inCacheDirty = true
      }

  @inline private def loadCorecurrentInput(frame: Frame[Dom, Ctx], in: In): Unit = inCache.get(frame) match
    case None => inCache += frame -> in
    case Some(recurrentIn) =>
      val newIn = widenIn(recurrentIn, in)
      newIn.ifChanged { changedIn =>
        inCache += frame -> changedIn
        inCacheDirty = true
      }
      state.setInState(newIn.get)

  @inline private def loadRecurrentOutput(frame: Frame[Dom, Ctx], ix: Int): Option[TrySturdy[Codom]] = outCache.get(frame) match
    case None =>
      throw RecurrentCall(frame)
    case Some((res, previousOut)) =>
      state.setOutState(previousOut)
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}## RECURRENT $frame <- $res, $previousOut")
      Some(res)

  @inline private def storeCorecurrentOutput(frame: Frame[Dom, Ctx], result: TrySturdy[Codom]): TrySturdy[Codom] = outCache.get(frame) match
    case None =>
      val out = state.getOutState()
      outCache += frame -> (result, out)
      outCacheDirty = true
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame <- ${Changed(result)}, ${Changed(out)}")
      result
    case Some((previousResult, previousOut)) =>
      val newResult: MaybeChanged[TrySturdy[Codom]] = (previousResult, result) match
        case (TrySturdy.Failure(ex1), TrySturdy.Failure(ex2)) => MaybeChanged(TrySturdy.Failure(j.joinThrowables(ex1, ex2)), previousResult)
        case (TrySturdy.Failure(_), TrySturdy.Success(v)) => Changed(TrySturdy.Success(v))
        case (TrySturdy.Success(v), TrySturdy.Failure(_)) => Changed(TrySturdy.Success(v))
        case (TrySturdy.Success(v1), TrySturdy.Success(v2)) => widenCodom(v1, v2).map(TrySturdy.Success.apply)
      val currentOut = state.getOutState()
      val newOut = widenOut(previousOut, currentOut)

      if (newResult.hasChanged || newOut.hasChanged) {
        outCache += frame -> (newResult.get, newOut.get)
        outCacheDirty = true
      }
      state.setOutState(newOut.get)

      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame <- $newResult, $newOut")
      newResult.get
