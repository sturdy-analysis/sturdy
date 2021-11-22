package sturdy.effect.store

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.Effectful

/*
 * The store interface.
 */
trait Store[Addr, V, J[_] <: MayJoin[_]] extends Effectful:
  def read(x: Addr): JOption[J, V]
  def write(x: Addr, v: V): Unit
  def free(x: Addr): Unit
