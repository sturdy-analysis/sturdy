package sturdy.effect.store

import sturdy.data.{JOptionA, WithJoin}
import sturdy.effect.Effect

trait AStore[Addr, V] extends Store[Addr, V, WithJoin]:
  /** Overrides existing value with new value */
  def strongUpdate(addr: Addr, value: V): Unit
  /** Joins existing value with new updated value */
  def weakUpdate(addr: Addr, value: V): Unit
  /** Deletes memory at address */
  def delete(addr: Addr): Unit

  def read(addr: Addr): JOptionA[V]