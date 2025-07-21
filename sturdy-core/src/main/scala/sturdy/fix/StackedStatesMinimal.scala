package sturdy.fix

import sturdy.effect.{CombineTrySturdy, RecurrentCall, TrySturdy}
import sturdy.values.*

import scala.collection.mutable


final class StackedStatesMinimal[Dom, Codom, In, Out](val state: StateT[In, Out])
                                              (val inStateWidening: InStateWidening[Dom, state.In])
                                              (using Finite[Dom], Join[Codom], Widen[Codom])
  extends Stack[Dom, Codom, In, Out]:

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private val stack: mutable.Map[(Dom, state.In), Int] = mutable.Map()

  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  private val outCache: mutable.Map[(Dom, state.In), OutCacheEntry] = mutable.Map()

  override def getCache: Map[Dom, TrySturdy[Codom]] = outCache.groupBy(_._1._1).view.mapValues { m =>
    m.values.map(_.result).reduce((r1,r2) => Join(r1,r2).get)
  }.toMap

  enum Stability:
    case Stable
    case Unstable

  class OutCacheEntry(var result: TrySturdy[Codom], var out: state.Out, var stability: Stability) extends StableMaker:
    override def toString: String = s"OutCacheEntry($result, $out, $stability)"
    override def markPermanentlyStable(): Unit = this.stability = Stability.Stable

  object OutCacheEntry:
    def unapply(out: OutCacheEntry): (TrySturdy[Codom], state.Out, Stability) = (out.result, out.out, out.stability)

  


  /** Set of _active_ stack frames that have recurred.
   *  When a stack frame becomes inactive, it is also removed from this set.
   */
  private val corecurrentCalls: mutable.Set[Int] = mutable.BitSet()
  override def hasRecurrentCalls: Boolean = corecurrentCalls.nonEmpty

  override def toString: String =
    s"Stack: ${stack.keys.map(k => k.hashCode()).toString()}\n"+
    s"Cache: ${outCache.toList.map(k => s"${k._1._1} @ ${k._1._2.hashCode()} -> ${k._2.result} @ ${k._2.out.hashCode()}, ${k._2.stability}").sorted.mkString("\n       ")}\n" +
    s"In:    ${outCache.toList.map(k => s"${k._1._2.hashCode()}: ${k._1._2}").sorted.mkString("\n       ")}\n" +
    s"Out:   ${outCache.toList.map(k => s"${k._2.out.hashCode()}: ${k._2.out}").sorted.mkString("\n       ")}\n"

  /** Current height of the stack. */
  def height: Int = stack.size

  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is not recurrent, yields PushResult.Continue.
   *  If the frame is recurrent and has not been previously executed, yields PushResult.Recurrent with a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields PusHresult.Recurrent with the previous result.
   */
  def push(dom: Dom, in: state.In, currentOut: state.Out, iterate: Boolean): PushResult =
    if (Thread.currentThread().isInterrupted)
      throw new InterruptedException

    val widenedIn = inStateWidening.push(dom, in).get
    val state = (dom, widenedIn)

    stack.get(state) match
      // call is not recurrent
      case None =>
        outCache.get(state) match {
          case Some(OutCacheEntry(result, out, Stability.Stable)) => return PushResult.Recurrent(result, Some(out))
          case _ => // nothing
        }
        // push call to stack
        stack.put(state, stack.size)
        PushResult.Continue(Some(widenedIn))

      case Some(corecId) =>
        // call is recurrent
        corecurrentCalls += corecId
        inStateWidening.pop(dom, in)
        outCache.get(state) match
          case None => PushResult.Recurrent(TrySturdy(throw RecurrentCall(state)), None)
          case Some(OutCacheEntry(res, previousOut, _)) => PushResult.Recurrent(res, Some(previousOut))

  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  def pop(dom: Dom, in: state.In, result: TrySturdy[Codom], out: state.Out): PopResult =
    inStateWidening.pop(dom, in)
    stack.remove((dom, in))
    val isCorecurrent = corecurrentCalls.remove(stack.size)
    if (isCorecurrent)
      storeCorecurrentOutput(dom, in, result, out)
    else
      PopResult.Stable(StableMaker.empty)

  private def storeCorecurrentOutput(dom: Dom, in: state.In, result: TrySturdy[Codom], out: state.Out): PopResult = outCache.get((dom, in)) match
    case None =>
      val outCacheEntry = OutCacheEntry(result, out, Stability.Unstable)
      outCache.put((dom, in), outCacheEntry)
      PopResult.Unstable(result, None)
    case Some(outCacheEntry@OutCacheEntry(previousResult, previousOut, _)) =>
      val newResult = Widen(previousResult, result)
      val newOut = state.widenOut(dom)(previousOut, out)
      val changed = newResult.hasChanged || newOut.hasChanged
      if (changed) {
        outCacheEntry.stability = Stability.Unstable
        outCacheEntry.result = newResult.get
        outCacheEntry.out = newOut.get
        PopResult.Unstable(newResult.get, Some(newOut.get))
      } else {
        PopResult.Stable(outCacheEntry)
      }

object StackedStatesMinimal:
  def apply[Dom, Codom](state: State)
                       (inStateWidening: InStateWidening[Dom, state.In])
                       (using Finite[Dom], Join[Codom], Widen[Codom]): Stack[Dom, Codom, state.In, state.Out] =
    new StackedStatesMinimal(state)(inStateWidening)
