package sturdy.effect.store

import scala.collection.mutable.ListBuffer

/*
 * A concrete store.
 */
trait CStore[Addr, V](_init: Map[Addr, V] = Map()) extends Store[Addr, V]:
  override type StoreJoin[_] = Unit

  protected var store: Map[Addr, V] = _init
  def getStore: Map[Addr, V] = store

  override def read[A](x: Addr, found: V => A, notFound: => A): StoreJoined[A] =
    store.get(x) match
      case Some(v) => found(v)
      case None => notFound

  override def write(x: Addr, v: V): Unit = 
    store += (x -> v)

  override def free(x: Addr): Unit =
    store -= x

  override def scopedAddresses[A](xs: Iterable[Addr])(f: => A): A =
    val before = ListBuffer[(Addr, Option[V])]()
    for (x <- xs)
      before += x -> store.get(x)
    try f finally {
      for ((x, mv) <- before) mv match
        case None => store -= x
        case Some(old) => store += x -> old
    }
