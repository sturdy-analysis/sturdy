package sturdy.effect

import sturdy.data.CombineUnit
import sturdy.values.{Join, Widen}

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

  /** Joins two internal states of the effect. */
  def join: Join[State]

  /** Widens two internal states of the effect. */
  def widen: Widen[State]

  /** [[ComputationJoiner]] joins two effectful computations, including this effect.
   */
  def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A]:
    private val original = getState
    private var afterFirst: State = _

    override def inbetween(fFailed: Boolean): Unit =
      afterFirst = getState
      setState(original)

    override def retainNone(): Unit =
      setState(original)

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      setState(afterFirst)

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      () // do nothing

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      val afterSecond = getState
      val joined = join(afterFirst, afterSecond)
      setState(joined.get)
  )

trait Stateless extends Effect:
  final type State = Unit
  final def getState: Unit = ()
  final def setState(st: Unit): Unit = ()
  final def join: Join[Unit] = CombineUnit
  final def widen: Widen[Unit] = CombineUnit

trait Monotone extends Stateless
trait Concrete extends Stateless
