package sturdy.effect.store

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue

import scala.collection.mutable.ListBuffer

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)writteb to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreMultiAddrThreadded[Addr, Addrs <: Iterable[Addr], V](_init: Map[Addr, (Boolean, V)])(using JoinValue[V])
  extends Store[Addrs, V], AStoreGenericThreadded[Addr, V]:

  this.store = _init
  
  override type StoreJoin[A] = JoinValue[A]
  
  override def read[A](xs: Addrs, found: V => A, notFound: => A): StoreJoined[A] = {
    var needsNotFound = false
    var as = ListBuffer[A]()
    for (x <- xs)
      store.get(x) match
        case None => needsNotFound = true
        case Some((definite, v)) =>
          as += found(v)
          if !definite then
            needsNotFound = true
    as.reduce(joinValues)
  }

  override def write(xs: Addrs, v: V): Unit =
    dirtyAddrs ++= xs
    if xs.size == 1 then
      store += xs.head -> ((true, v))
    else for x <- xs do
      store.get(x) match
        case None => store += x -> ((false, v))
        case Some((_, old)) => store += x -> ((false, joinValues(old, v)))

