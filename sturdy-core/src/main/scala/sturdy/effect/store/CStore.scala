package sturdy.effect.store

import sturdy.effect.CMayCompute
import sturdy.effect.CMayCompute.*
import sturdy.effect.NoJoin

import scala.collection.mutable.ListBuffer

/*
 * A concrete store.
 */
trait CStore[Addr, V](_init: Map[Addr, V] = Map()) extends Store[Addr, V]:
  override type StoreJoin[A] = NoJoin[A]
  override type StoreJoinComp = Unit

  protected var store: Map[Addr, V] = _init
  def getStore: Map[Addr, V] = store

  override def read(x: Addr): CMayCompute[V] =
    store.get(x) match
      case Some(v) => Computes(v)
      case None => ComputesNot()

  override def write(x: Addr, v: V): Unit = 
    store += (x -> v)

  override def free(x: Addr): Unit =
    store -= x

