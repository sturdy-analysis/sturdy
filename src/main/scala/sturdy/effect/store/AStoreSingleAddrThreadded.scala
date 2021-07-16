package sturdy.effect.store

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)writteb to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreSingleAddrThreadded[Addr, V](_init: Map[Addr, (Boolean, V)])(using JoinValue[V])
  extends Store[Addr, V], AStoreGenericThreadded[Addr, V]:

  this.store = _init
  
  override type StoreJoin[A] = JoinValue[A]

  override def read[A](x: Addr, found: V => A, notFound: => A): StoreJoined[A] =
    store.get(x) match
      case None => notFound
      case Some((definite, v)) =>
        if definite then
          found(v)
        else
          joinValues(found(v), notFound)

  override def write(x: Addr, v: V): Unit =
    dirtyAddrs += x
    store += x -> ((true, v))
