package sturdy.effect.store

import sturdy.effect.MayCompute

/*
 * The store interface.
 */
trait Store[Addr, V]:
  type StoreJoin[A]
  type StoreJoinComp

  def read(x: Addr): MayCompute[V, StoreJoin, StoreJoinComp]
  def write(x: Addr, v: V): Unit
  def free(x: Addr): Unit
