package sturdy.effect.store

import sturdy.data.JOption

/*
 * The store interface.
 */
trait Store[Addr, V, MayJoin[_]]:
  def read(x: Addr): JOption[MayJoin, V]
  def write(x: Addr, v: V): Unit
  def free(x: Addr): Unit
