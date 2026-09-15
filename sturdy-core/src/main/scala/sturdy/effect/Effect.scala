package sturdy.effect

import sturdy.data.CombineUnit
import sturdy.values.{Changed, Join, StackWidening, Widen}

import scala.reflect.ClassTag

/**
 * [[Effect]] is an interface for effectful computations, such as computations mutating variables or causing exceptions.
 *
 * [[Effect]]s carry an internal state that changes throughout the program evaluation.
 */
trait Effect:
  /** The internal state of the effect. */
  type State

  /**
   * Returns the internal state of the effect.
   * The returned state must not be mutated afterwards.
   */
  def getState: State

  /** Overwrite the current internal state of the effect with the given state. */
  def setState(st: State): Unit

  def setStateNonMonotonically(st: State): Unit = setState(st)

  /** Sets the effect state to bottom */
  def setBottom: Unit = {}

  /** Iterates over all addresses in the current effect. Used for abstract garbage collection */
  def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] = Iterator()

  /** Joins two internal states of the effect. */
  def join: Join[State]

  /** Widens two internal states of the effect. */
  def widen: Widen[State]

  /** Join that closes over values of type `Body`. Mainly used by the virtual recency abstract to close over virtual addresses. */
  def joinClosingOver[Body](using Join[Body]): Join[(Body,State)] = {
    case ((cod1,state1), (cod2,state2)) =>
      for {
        cod <- Join(cod1, cod2);
        state <- join(state1, state2)
      } yield((cod,state))
  }

  /** Widening that closes over values of type `Body`. Mainly used by the virtual recency abstract to close over virtual addresses. */
  def widenClosingOver[Body](using Widen[Body]): Widen[(Body,State)] = {
    case ((cod1, state1), (cod2, state2)) =>
      for {
        cod <- Widen(cod1, cod2);
        state <- widen(state1, state2)
      } yield ((cod, state))
  }

  def stackWiden: StackWidening[State] =
    (stack: List[State], call: State) =>
      stack match
        case Nil => Changed(call)
        case mostRecentCall :: _ => widen(mostRecentCall, call)

  /** [[ComputationJoiner]] joins two effectful computations, including this effect.
   */
  def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A]:
    private val original = getState
    private var afterFirst: State = _

    override def inbetween(fFailed: Boolean): Unit =
      afterFirst = getState
      setStateNonMonotonically(original)

    override def retainNone(): Unit =
      setBottom

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      setStateNonMonotonically(afterFirst)

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      () // do nothing

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      val afterSecond = getState
      val joined = join(afterFirst, afterSecond)
      setStateNonMonotonically(joined.get)
  )

trait Stateless extends Effect:
  final type State = Unit
  final def getState: Unit = ()
  final def setState(st: Unit): Unit = ()
  final def join: Join[Unit] = CombineUnit
  final def widen: Widen[Unit] = CombineUnit
  final def bottom: State = ()

trait Monotone extends Stateless
trait Concrete extends Stateless