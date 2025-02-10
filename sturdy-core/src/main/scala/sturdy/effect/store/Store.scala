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


/*

forward store:
    1. initially empty
    2. alloc address
    3. write to address
    4. read from address, may fail

backward store:
    1. initially top value for all valid addresses
    2. read expected value from address: refine stored value
    3. store value in address: refine value, drop constraints on stored value
    4. alloc address: nothing
 */

trait BackwardStore[Addr, V, J[_] <: MayJoin[_]] extends Effect:
  /** Assert expected value is in cell. */
  def read(x: Addr => Addr): V => JOption[J, V]
  def write(x: Addr => Addr, v: V => V): Unit
  