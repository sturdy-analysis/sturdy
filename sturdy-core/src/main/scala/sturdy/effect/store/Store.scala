package sturdy.effect.store

import sturdy.data.Option

/*
 * The store interface.
 */
trait Store[Addr, V, MayJoin[_]]:
  def read(x: Addr): Option[MayJoin, V]
  def write(x: Addr, v: V): Unit
  def free(x: Addr): Unit
