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
  def free(x: Addr): Unit

  final def readOrElse(x: Addr, default: => V)(using J[V]): V =
    read(x).getOrElse(default)

/** Tracks possible values for an address. When a read succeeds, the address may have that value or be unbound. */
trait MayStore[Addr, V, J[_] <: MayJoin[_]] extends Store[Addr, V, J]
/** Tracks definite values for an address. When a read succeeds, the address must have that value and is definitely bound. */
trait MustStore[Addr, V, J[_] <: MayJoin[_]] extends Store[Addr, V, J]
