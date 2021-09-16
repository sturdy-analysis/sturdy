package sturdy.fix

import sturdy.effect.AnalysisState
import sturdy.effect.Effectful
import sturdy.values.JoinValue

import scala.collection.mutable
import scala.util.Failure
import scala.util.Success
import scala.util.Try

case object RecurrentCall extends Exception:
  override def toString: String = "RecurrentCall"

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
final class Stack[Dom, Codom, In, Out, Ctx](state: AnalysisState[In, Out], contextual: Contextual[Ctx, Dom, Codom])
  (using joinCodom: JoinValue[Codom], joinIn: JoinValue[In], joinOut: JoinValue[Out])
  (using widenCodom: Widening[Codom], widenIn: Widening[In], widenOut: Widening[Out], j: Effectful):

  case class Frame(dom: Dom, ctx: Ctx)

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private var stack: Map[Frame, Int] = Map()
  private var stackHeight = 0

  /** Cache of the inputs of previously executed stack frames.
   */
  private var inCache: Map[Frame, In] = Map()

  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  private var outCache: Map[Frame, (Try[Codom], Out)] = Map()

  /** Set of _active_ stack frames that have recurred.
   *  When a stack frame becomes inactive, it is also removed from this set.
   */
  private val recurrentCalls: mutable.Set[Int] = mutable.BitSet()
  private val corecurrentCalls: mutable.Set[Frame] = mutable.Set()

  override def toString: String = stack.toList.sortBy(_._2).map(_._1).mkString("Stack(", ", ", ")")

  /** Current height of the stack. */
  def height: Int = stackHeight

  def stackHeightIndent: String = "  " * (stackHeight - 1)

  /** Iterates `f` until the outCache is stable. */
  def repeatUntilStable[A](f: () => A): A = {
    val originalInState = state.getInState()
    val originalOutState = state.getOutState()
    var iterationCount = 0

    var previousOutCache = outCache

    while (true) {
      val result = f()
      if (previousOutCache == outCache)
        return result
      else {
        iterationCount += 1
        if (Fixpoint.DEBUG)
          println(s"## REPEAT (Iteration $iterationCount)")
        previousOutCache = outCache
        state.setInState(originalInState)
        state.setOutState(originalOutState)
      }
    }
    throw new IllegalStateException()
  }
  
  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is recurrent and has not been previously executed, throws a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields the previous result.
   */
  def push(dom: Dom, in: In): Option[Try[Codom]] =
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
  def pop(dom: Dom, in: In, result: Try[Codom]): (Try[Codom], Boolean) =
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
        println(s"${stackHeightIndent}POP  $frame <- $result, ${state.getOutState()}")
      (result, false)
    }
    stack -= frame
    stackHeight = newStackHeight
    updatedResult

  @inline private def storeRecurrentInput(frame: Frame, in: In): Unit = inCache.get(frame) match
    case None =>
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}## RECURRENT $frame -> $in")
      inCache += frame -> in
    case Some(previousIn) =>
      val joinedIn = widenIn.widen(previousIn, in)
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}## RECURRENT $frame -> $joinedIn")
      inCache += frame -> joinedIn

  @inline private def loadCorecurrentInput(frame: Frame, in: In): Unit = inCache.get(frame) match
    case None => inCache += frame -> in
    case Some(recurrentIn) =>
      val joinedIn = widenIn.widen(in, recurrentIn)
      inCache += frame -> joinedIn
      state.setInState(joinedIn)

  @inline private def loadRecurrentOutput(frame: Frame, ix: Int): Option[Try[Codom]] = outCache.get(frame) match
    case None =>
      throw RecurrentCall
    case Some((res, previousOut)) =>
      val joinedOut = widenOut.widen(previousOut, state.getOutState())
      state.setOutState(joinedOut)
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}## RECURRENT $frame <- $res, $joinedOut")
      Some(res)

  @inline private def storeCorecurrentOutput(frame: Frame, result: Try[Codom]): Try[Codom] = outCache.get(frame) match
    case None =>
      val out = state.getOutState()
      outCache += frame -> (result, out)
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame <- $result, $out")
      result
    case Some((previousResult, previousOut)) =>
      val joinedResult = (previousResult, result) match
        case (Failure(ex1), Failure(ex2)) => Failure(j.joinFailedComputations(ex1, ex2))
        case (Failure(_), Success(v)) => Success(v)
        case (Success(v), Failure(_)) => Success(v)
        case (Success(v1), Success(v2)) => Success(widenCodom.widen(v1, v2))
      val joinedOut = widenOut.widen(previousOut, state.getOutState())
      state.setOutState(joinedOut)
      outCache += frame -> (joinedResult, joinedOut)
      if (Fixpoint.DEBUG)
        println(s"${stackHeightIndent}POP  $frame <- $joinedResult, $joinedOut")
      joinedResult
