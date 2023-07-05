package sturdy.effect

import sturdy.data.CombineUnit
import sturdy.values.{Join, Widen}

trait Effect:
  type State
  def getState: State
  def setState(st: State): Unit
  def join: Join[State]
  def widen: Widen[State]

  final def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A]:
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

trait Stateless extends Effect:
  final type State = Unit
  final def getState: Unit = ()
  final def setState(st: Unit): Unit = ()
  final def join: Join[Unit] = CombineUnit
  final def widen: Widen[Unit] = CombineUnit

trait Monotone extends Stateless
trait Concrete extends Stateless
