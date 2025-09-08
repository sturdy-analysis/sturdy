package sturdy.effect.store

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.Effect

/**
 * [[Store]] is a mapping from addresses to values.
 * The value of an address can be mutated or freed.
 */
trait Store[Addr, V, J[_] <: MayJoin[_]] extends Effect:
  def read(x: Addr): JOption[J, V]
  def write(x: Addr, v: V): Unit
  def move(from: Addr, to: Addr): Unit =
    read(from).map(
      value =>
        write(to, value)
        free(from)
    )
  def copy(from: Addr, to: Addr): Unit =
    read(from).map(
      value =>
        write(to, value)
    )

  def free(x: Addr): Unit

  final def readOrElse(x: Addr, default: => V)(using J[V]): V =
    read(x).getOrElse(default)


trait StoreWithPureOps[Addr, V, J[_] <: MayJoin[_]] extends Store[Addr, V, J]:
  def readPure(x: Addr, state: State): (JOption[J, V],State)
  def writePure(x: Addr, v: V, state: State): State
  def movePure(from: Addr, to: Addr, state0: State): State =
    val (result, state1) = readPure(from, state0)
    var state = state1
    result.map(value =>
      state = writePure(to, value, state)
      state = freePure(from, state)
    )
    state
  def copyPure(from: Addr, to: Addr, state0: State): State =
    val (result, state1) = readPure(from, state0)
    var state = state1
    result.map(value =>
      state = writePure(to, value, state)
    )
    state
  def freePure(x: Addr, state: State): State

  def withInternalState[A](f: State => (A,State)): A
  def modifyInternalState(f: State => State): Unit = withInternalState(s => ((), f(s)))

  override def read(x: Addr): JOption[J, V] =
    withInternalState(state => readPure(x,state))

  override def write(x: Addr, v: V): Unit =
    modifyInternalState(writePure(x, v, _))

  override inline def move(from: Addr, to: Addr): Unit =
    modifyInternalState(movePure(from, to, _))

  override inline def copy(from: Addr, to: Addr): Unit =
    modifyInternalState(copyPure(from, to, _))

  override inline def free(x: Addr): Unit =
    modifyInternalState(freePure(x, _))