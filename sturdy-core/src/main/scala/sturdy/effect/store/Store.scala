package sturdy.effect.store

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.Effect

/**
 * The store interface.
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