package sturdy.effect

import sturdy.data.CombineUnit
import sturdy.values.{Changed, Join, MaybeChanged, StackWidening, Widen}

trait Stateful:
  type State
  def getState: State
  def setState(st: State): Unit
  def join: Join[State]
  def widen: Widen[State]
  def stackWiden: StackWidening[State] =
    (stack: List[State], call: State) =>
      stack match
        case Nil => Changed(call)
        case mostRecentCall :: _ => widen(mostRecentCall, call)

trait Effect extends Stateful:
  def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A]:
    private val original = getState
    private var afterFirst: State = _

    override def inbetween(): Unit =
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


  protected def defaultComputationJoiner[A] = new ComputationJoiner[A] {
    val snapshot = getState
    var firstState: State = _

    override def inbetween(): Unit =
      firstState = getState
      setState(snapshot)

    override def retainNone(): Unit =
      setState(snapshot)

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      setState(firstState)

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      {} // nothing

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      val joined = join(firstState, getState)
      setState(joined.get)
  }

trait Monotone extends Effect:
  final override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = None

trait Stateless extends Monotone:
  final type State = Unit
  final def getState: Unit = ()
  final def setState(st: Unit): Unit = ()

  final def join: Join[Unit] = CombineUnit
  final def widen: Widen[Unit] = CombineUnit

trait Concrete extends Effect:
  final type State = Unit
  final def getState: Unit = ()
  final def setState(st: Unit): Unit = ()

  final def join: Join[Unit] = CombineUnit
  final def widen: Widen[Unit] = CombineUnit
  final override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = None
