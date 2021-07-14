package sturdy.effect.store

/*
 * A concrete store.
 */
trait CStore[Addr, V](_init: Map[Addr, V] = Map()) extends Store[Addr, V]:
  protected var store: Map[Addr, V] = _init

  def getStore: Map[Addr, V] = store

  override type StoreJoin[_] = Unit

  override def read[A](x: Addr, found: V => A, notFound: => A): StoreJoined[A] =
    store.get(x).map(found).getOrElse(notFound)

  override def write(x: Addr, v: V): Unit = store = store + (x -> v)
