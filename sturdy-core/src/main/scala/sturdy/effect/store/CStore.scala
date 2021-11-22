package sturdy.effect.store

import sturdy.data.*

import scala.collection.mutable.ListBuffer

/*
 * A concrete store.
 */
class CStore[Addr, V](_init: Map[Addr, V] = Map()) extends Store[Addr, V, NoJoin]:
  protected var store: Map[Addr, V] = _init

  override def read(x: Addr): JOptionC[V] =
    JOptionC(store.get(x))

  override def write(x: Addr, v: V): Unit = 
    store += (x -> v)

  override def free(x: Addr): Unit =
    store -= x

  override type State = Map[Addr, V]
  override def getState: Map[Addr, V] = store
  override def setState(s: Map[Addr, V]): Unit = store = s