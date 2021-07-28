package sturdy.fix

import sturdy.effect.AnalysisState
import sturdy.values.JoinValue

import scala.collection.mutable

case class RecurrentCall(ix: Int) extends Exception

final class Stack[Dom, Codom, In, Out](state: AnalysisState[In, Out])(using j: JoinValue[Codom]):

  /** Set of active stack frames of type `(Dom, In)`. Each frame is identified by its stack position `Int`. */
  private var stack: Map[(Dom, In), Int] = Map()
  private var stackHeight = 0

  /** Cache of previously executed stack frames. Results have type `(Codom, Out)`. */
  private var cache: Map[(Dom, In), (Codom, Out)] = Map()

  /** Set of _active_ stack frames that have recurred.
   *  When a stack frame becomes inactive, it is also removed from this set.
   */
  private val recurrentCalls: mutable.Set[Int] = mutable.BitSet()

  override def toString: String = stack.toList.sortBy(_._2).map(_._1).mkString("Stack(", ", ", ")")

  /** Current height of the stack. */
  def height: Int = stackHeight

  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *  
   *  If the frame is recurrent and has not been previously executed, throws a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields the previous result.
   */
  def push(v: (Dom, In)): Option[(Codom, Out)] =
    stack.get(v) match
      case None =>
        stack += (v -> stackHeight)
        stackHeight += 1
        None
      case Some(ix) =>
        recurrentCalls += ix
        cache.get(v) match
          case None => throw RecurrentCall(ix)
          case found => found

  /** Pops a frame from the stack and detects if this frame recurred recursively.
   * 
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(v: (Dom, In), result: Codom): Boolean =
    stack -= v
    val newStackHeight = stackHeight - 1
    stackHeight = newStackHeight
    if (recurrentCalls.contains(newStackHeight)) {
      recurrentCalls -= newStackHeight
      cache.get(v) match
        case None =>
          val out = state.getRelevantOutState()
          cache += v -> (result, out)
        case Some((previousResult, previousOut)) =>
          val joinedResult = j.joinValues(previousResult, result)
          val joinedOut = state.getRelevantOutStateJoinedWith(previousOut)
          cache += v -> (joinedResult, joinedOut)
      true
    } else {
      false
    }

