package sturdy.effect.store

import sturdy.effect.MayCompute

/*
 * The store interface.
 */
trait Store[Addr, V]:
  type StoreJoin[A]

  def read(x: Addr): MayCompute[StoreJoin, V]
  def write(x: Addr, v: V): Unit
  def free(x: Addr): Unit
