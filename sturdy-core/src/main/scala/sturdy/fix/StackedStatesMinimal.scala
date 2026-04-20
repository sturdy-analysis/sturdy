package sturdy.fix

import sturdy.effect.{CombineTrySturdy, RecurrentCall, TrySturdy}
import sturdy.values.*

import scala.collection.mutable
import Stability.*

enum Stability:
  case Stable
  case Unstable

trait StackMinimal[In, Out]:
  enum PushResult:
    case Skip(out: Option[Out])
    case Continue(in: In)

  enum PopResult:
    case Stable(out: Out, maker: StableMaker)
    case Unstable(out: Out)

  def push(in: In): PushResult
  def pop(in: In, out: Out): PopResult
  def height: Int
  def hasRecurrentCalls: Boolean



def minimalStackToStack[Dom,Codom,In,Out](s: StackMinimal[(Dom,In),(TrySturdy[Codom],Out)]): Stack[Dom,Codom,In,Out] = new Stack {
  override def push(dom: Dom, in: In, currentOut: Out, iterate: Boolean): PushResult =
    s.push((dom, in)) match {
      case s.PushResult.Skip(out) => out match {
        case None => PushResult.Skip(TrySturdy(throw RecurrentCall((dom, in))), None)
        case Some((result, out)) => PushResult.Skip(result, Some(out))
      }
      case s.PushResult.Continue((_, in)) => PushResult.Continue(Some(in))
    }
  override def pop(dom: Dom, in: In, codom: TrySturdy[Codom], out: Out): PopResult =
    s.pop((dom, in), (codom, out)) match {
      case s.PopResult.Stable(out, maker) => PopResult.Stable(maker)
      case s.PopResult.Unstable((result, out)) => PopResult.Unstable(result, Some(out))
    }
  override def height: Int = s.height
  override def hasRecurrentCalls: Boolean = s.hasRecurrentCalls
  override def getCache: Map[Dom, TrySturdy[Codom]] = ???
}

def inStateWideningMinimal[Dom, In](w: InStateWidening[Dom, In]): InStateWidening[Unit, (Dom, In)] = new InStateWidening {
  override def push(dom: Unit, in: (Dom, In)): MaybeChanged[(Dom, In)] =
    w.push(in._1, in._2).map(in2 => (in._1, in2))
  override def pop(dom: Unit, in: (Dom, In)): Unit = w.pop(in._1, in._2)
} 

final class StackedStatesMinimal[In, Out](val inWidening: InStateWidening[Unit, In], val outWidening: In => Widen[Out])
  extends StackMinimal[In, Out]:

  /** Set of active calls identified by their context and their stack position.
   * Each call can only be active once since a second invocation triggers a recurrent call.
   */
  private val stack: mutable.Map[In, Int] = mutable.Map()

  /** Cache of the outputs of previously executed co-recurrent stack frames. */
  private val outCache: mutable.Map[In, OutCacheEntry] = mutable.Map()


   case class OutCacheEntry(var out: Out, var stability: Stability) extends StableMaker:
    override def toString: String = s"OutCacheEntry($out, $stability)"
    override def markPermanentlyStable(): Unit = this.stability = Stability.Stable

  /** Set of _active_ stack frames that have recurred.
   *  When a stack frame becomes inactive, it is also removed from this set.
   */
  private val corecurrentCalls: mutable.Set[Int] = mutable.BitSet()
  override def hasRecurrentCalls: Boolean = corecurrentCalls.nonEmpty

  /** Current height of the stack. */
  def height: Int = stack.size

  def stackHeightIndent: String = "  " * (height)

  /** Pushes a frame on top of the stack and detects if the frame is recurrent.
   *
   *  If the frame is not recurrent, yields PushResult.Continue.
   *  If the frame is recurrent and has not been previously executed, yields PushResult.Recurrent with a `RecurrentCall` exception.
   *  If the frame is recurrent and has been previously executed, yields PusHresult.Recurrent with the previous result.
   */
  override def push(in: In): PushResult =
    if (Thread.currentThread().isInterrupted)
      throw new InterruptedException

    val widenedIn = inWidening.push((), in).get
    stack.get(widenedIn) match
      // call is not recurrent
      case None =>
        outCache.get(widenedIn) match {
          case Some(OutCacheEntry(out, Stable)) =>
            if (Fixpoint.DEBUG) println(s"${stackHeightIndent}READ PRIOR OUTPUT $widenedIn <- $out")
            inWidening.pop((), in)
            return PushResult.Skip(Some(out))
          case _ => // nothing
        }
        // push call to stacksdsaads
        if (Fixpoint.DEBUG) println(s"${stackHeightIndent}PUSH $widenedIn")
        stack.put(widenedIn, stack.size)
        PushResult.Continue(widenedIn)

      case Some(corecId) =>
        // call is recurrent
        corecurrentCalls += corecId
        inWidening.pop((), in)
        outCache.get(widenedIn) match
          case None =>
            if (Fixpoint.DEBUG) println(s"${stackHeightIndent}RECURRENT $widenedIn")
            PushResult.Skip(None)
          case Some(OutCacheEntry(previousOut, _)) =>
            if (Fixpoint.DEBUG) println(s"${stackHeightIndent}RECURRENT $widenedIn <- $previousOut")
            PushResult.Skip(Some(previousOut))

  /** Pops a frame from the stack and detects if this frame recurred recursively.
   *
   * If the frame recurred, updates the cache to store the result of this frame.
   */
  override def pop(in: In, out: Out): PopResult =
    inWidening.pop((), in)
    stack.remove(in)
    val isCorecurrent = corecurrentCalls.remove(stack.size)
    if (isCorecurrent)
      storeCorecurrentOutput(in, out)
    else {
      if (Fixpoint.DEBUG) println(s"${stackHeightIndent}POP STABLE $in <- $out")
      PopResult.Stable(out, StableMaker.empty)
    }

  private def storeCorecurrentOutput(in: In, out: Out): PopResult = outCache.get(in) match
    case None =>
      val outCacheEntry = new OutCacheEntry(out, Unstable)
      outCache.put(in, outCacheEntry)
      if (Fixpoint.DEBUG) println(s"${stackHeightIndent}POP UNSTABLE $in <- $out")
      PopResult.Unstable(out)
    case Some(entry@OutCacheEntry(previousOut, _)) =>
      outWidening(in)(previousOut, out) match
        case MaybeChanged.Changed(newOut) =>
          entry.stability = Stability.Unstable
          entry.out = newOut
          if (Fixpoint.DEBUG) println(s"${stackHeightIndent}POP UNSTABLE $in <- $newOut")
          PopResult.Unstable(newOut)
        case MaybeChanged.Unchanged(_) =>
          if (Fixpoint.DEBUG) println(s"${stackHeightIndent}POP STABLE $in <- $out")
          PopResult.Stable(out, entry)

object StackedStatesMinimal:
  def apply[Dom, Codom](state: State)
                       (inStateWidening: InStateWidening[Dom, state.In])
                       (using Finite[Dom], Join[Codom], Widen[Codom]): Stack[Dom, Codom, state.In, state.Out] =
    minimalStackToStack(
      new StackedStatesMinimal[(Dom,state.In), (TrySturdy[Codom],state.Out)](
        inStateWideningMinimal(inStateWidening),
        (in: (Dom,state.In)) => (v1: (TrySturdy[Codom], state.Out), v2: (TrySturdy[Codom], state.Out)) => {
          state.widenOut[TrySturdy[Codom]](in._1)(v1, v2)
        }
      )
    )
