package sturdy.effect.store

import sturdy.data.*
import sturdy.effect.Concrete

import scala.collection.mutable.ListBuffer

/*
 * A concrete store.
 */
class CStore[Addr, V](_init: Map[Addr, V] = Map()) extends MayStore[Addr, V, NoJoin], MustStore[Addr, V, NoJoin], Concrete:
  protected var store: Map[Addr, V] = _init
  
  def entries: Map[Addr, V] = store

  override def read(x: Addr): JOptionC[V] =
    JOptionC(store.get(x))

  override def write(x: Addr, v: V): Unit = 
    store += (x -> v)

  override def free(x: Addr): Unit =
    store -= x
