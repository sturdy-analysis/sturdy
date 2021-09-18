package sturdy.effect.store

import sturdy.data.Option

/*
 * The store interface.
 */
trait Store[Addr, V]:
  type StoreJoin[A]

  def read(x: Addr): Option[StoreJoin, V]
  def write(x: Addr, v: V): Unit
  def free(x: Addr): Unit
