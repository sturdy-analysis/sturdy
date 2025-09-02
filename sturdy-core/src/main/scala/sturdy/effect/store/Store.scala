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

trait StoreWithImmutableOps[Addr, V, J[_] <: MayJoin[_]] extends Store[Addr, V, J]:
  def read(x: Addr, state: State): JOption[J, V]
  def write(x: Addr, v: V, state: State): State
  def move(from: Addr, to: Addr, state0: State): State =
    var state = state0
    read(from, state).map(
      value =>
        state = write(to, value, state)
        state = free(from, state)
    )
    state
  def copy(from: Addr, to: Addr, state0: State): State =
    var state = state0
    read(from, state).map(
      value =>
        state = write(to, value, state)
    )
    state
  def free(x: Addr, state: State): State

  def withInternalState[A](f: State => (A,State)): A

  override def read(x: Addr): JOption[J, V] =
    withInternalState(state => (read(x,state), state))

  override inline def write(x: Addr, v: V): Unit =
    withInternalState(state => ((),write(x, v, state)))

  override inline def move(from: Addr, to: Addr): Unit =
    withInternalState(state => ((), move(from, to, state)))

  override inline def copy(from: Addr, to: Addr): Unit =
    withInternalState(state => ((), copy(from, to, state)))

  override inline def free(x: Addr): Unit =
    withInternalState(state => ((), free(x, state)))