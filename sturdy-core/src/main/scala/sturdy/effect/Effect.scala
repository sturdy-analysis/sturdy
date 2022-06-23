package sturdy.effect

import sturdy.data.CombineUnit
import sturdy.values.{Join, Widen}

trait Stateful:
  type State
  def getState: State
  def setState(st: State): Unit
  def join: Join[State]
  def widen: Widen[State]

trait Effect extends Stateful:
  def makeComputationJoiner[A]: Option[ComputationJoiner[A]]

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
  final def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = None
