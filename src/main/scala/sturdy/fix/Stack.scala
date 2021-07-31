package sturdy.fix

import sturdy.effect.AnalysisState
import sturdy.effect.JoinComputation
import sturdy.values.JoinValue

import scala.collection.mutable
import scala.util.Failure
import scala.util.Success
import scala.util.Try

case object RecurrentCall extends Exception {
  override def toString: String = "RecurrentCall"
}

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
final class Stack[Dom, Codom, In, Out, Ctx](state: AnalysisState[In, Out], context: ContextSensitive[Dom, In, Ctx])
  (using joinIn: JoinValue[In], joinOut: JoinValue[Out])
  (using widenCodom: Widening[Codom], widenIn: Widening[In], j: JoinComputation):

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private var stack: Map[Ctx, Int] = Map()
  private var stackHeight = 0

  /** Cache of the inputs of previously executed stack frames.
   *  Inputs are joined upon write, that is,
   */
  private var inCache: Map[Ctx, In] = Map()

  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  private var outCache: Map[Ctx, (Try[Codom], Out)] = Map()

  /** Set of _active_ stack frames that have recurred.
   *  When a stack frame becomes inactive, it is also removed from this set.
   */
  private val recurrentCalls: mutable.Set[Int] = mutable.BitSet()
//  private val corecurrentCalls: mutable.Set[Ctx] = mutable.Set()

  override def toString: String = stack.toList.sortBy(_._2).map(_._1).mkString("Stack(", ", ", ")")

  /** Current height of the stack. */
  def height: Int = stackHeight

  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is recurrent and has not been previously executed, throws a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields the previous result.
   */
  def push(dom: Dom, in: In): Option[Try[Codom]] =
    val ctx = context(dom, in)
    stack.get(ctx) match
      case None => // call is not recurrent
        // push call to stack
        stack += ctx -> stackHeight
        stackHeight += 1
//        if (corecurrentCalls.contains(ctx)) {
          // store and join with input state based on previous calls with the same context
          storeAndSetJoinedInState(ctx, in)
//        }
        if (Fixpoint.DEBUG)
          println(("  " * (stackHeight)) + s"PUSH $ctx -> ${state.getInState()}")
        None
      case Some(ix) =>
        // call is recurrent
        recurrentCalls += ix
        // store the input state so the co-recurrent call considers it
        storeWidenedInState(ctx, in)
        // load any previous output or throw RecurrentCall exception
        loadOutputOfRecurrentCall(ctx, ix)


  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(dom: Dom, in: In, result: Try[Codom]): Boolean =
    val ctx = context(dom, in)
    val entry = stack(ctx)
    stack -= ctx
    val newStackHeight = stackHeight - 1
    stackHeight = newStackHeight
    if (recurrentCalls.remove(newStackHeight)) {
//      corecurrentCalls += ctx
      storeOutput(ctx, result)
      true
    } else {
      if (Fixpoint.DEBUG)
        println(("  " * stackHeight) + s"  POP  $ctx -> $result, ${state.getOutState()}")
      false
    }

  @inline private def storeWidenedInState(ctx: Ctx, in: In): Unit = inCache.get(ctx) match
    case None =>
      if (Fixpoint.DEBUG)
        println(("  " * stackHeight) + s"  ## RECURRENT $ctx -> $in")
      inCache += ctx -> in
    case Some(previousIn) =>
      val joinedIn = widenIn.widen(previousIn, in)
      if (Fixpoint.DEBUG)
        println(("  " * stackHeight) + s"  ## RECURRENT $ctx -> $joinedIn")
      inCache += ctx -> joinedIn

  @inline private def storeAndSetJoinedInState(ctx: Ctx, in: In): Unit = inCache.get(ctx) match
    case None => inCache += ctx -> in
    case Some(previousIn) =>
      val joinedIn = joinIn.joinValues(previousIn, in)
      inCache += ctx -> joinedIn
      state.setInState(joinedIn)

  @inline private def loadOutputOfRecurrentCall(ctx: Ctx, ix: Int): Option[Try[Codom]] = outCache.get(ctx) match
    case None =>
      throw RecurrentCall
    case Some((res, previousOut)) =>
      val joinedOut = joinOut.joinValues(previousOut, state.getOutState())
      state.setOutState(joinedOut)
      if (Fixpoint.DEBUG)
        println(("  " * stackHeight) + s"  ## RECURRENT $ctx <- $res, $joinedOut")
      Some(res)

  @inline private def storeOutput(ctx: Ctx, result: Try[Codom]): Unit = outCache.get(ctx) match
    case None =>
      val out = state.getOutState()
      outCache += ctx -> (result, out)
      if (Fixpoint.DEBUG)
        println(("  " * stackHeight) + s"  POP  $ctx <- $result, $out")
    case Some((previousResult, previousOut)) =>
      val joinedResult = (previousResult, result) match
        case (Failure(ex1), Failure(ex2)) => Failure(j.joinFailedComputations(ex1, ex2))
        case (Failure(_), Success(v)) => Success(v)
        case (Success(v), Failure(_)) => Success(v)
        case (Success(v1), Success(v2)) => Success(widenCodom.widen(v1, v2))
      val joinedOut = joinOut.joinValues(previousOut, state.getOutState())
      // should we do this here?
//      state.setOutState(joinedOut)
      outCache += ctx -> (joinedResult, joinedOut)
      if (Fixpoint.DEBUG)
        println(("  " * stackHeight) + s"  POP  $ctx <- $joinedResult, $joinedOut")

object Stack:
  class StackBuilder[Dom, Codom, In, Out]
    (state: AnalysisState[In, Out])
    (using joinIn: JoinValue[In], joinOut: JoinValue[Out])
    (using wCodom: Widening[Codom], wIn: Widening[In], jComp: JoinComputation) {
    def apply[Ctx](context: ContextSensitive[Dom, In, Ctx]) = new Stack(state, context)(using joinIn, joinOut)(using wCodom, wIn, jComp)
  }
  def apply[Dom, Codom, In, Out]
    (state: AnalysisState[In, Out])
    (using joinIn: JoinValue[In], joinOut: JoinValue[Out])
    (using wCodom: Widening[Codom], wIn: Widening[In], jComp: JoinComputation)
      : StackBuilder[Dom, Codom, In, Out] =
    new StackBuilder(state)(using joinIn, joinOut)(using wCodom, wIn, jComp)