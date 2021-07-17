package sturdy.effect.store

import sturdy.effect.JoinComputation
import sturdy.values.*

import scala.collection.mutable.ListBuffer

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)writteb to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreMultiAddrThreadded[Addr, V](_init: Map[Addr, (Boolean, V)])(using JoinValue[V])
  extends Store[Powerset[Addr], V], AStoreGenericThreadded[Addr, V]:

  this.store = _init
  
  override type StoreJoin[A] = JoinValue[A]
  
  override def read[A](xs: Powerset[Addr], found: V => A, notFound: => A): StoreJoined[A] = {
    var needsNotFound = false
    var as = ListBuffer[A]()
    for (x <- xs.set)
      store.get(x) match
        case None => needsNotFound = true
        case Some((definite, v)) =>
          as += found(v)
          if !definite then
            needsNotFound = true
    as.reduce(joinValues)
  }

  override def write(xs: Powerset[Addr], v: V): Unit =
    val addrs = xs.set
    dirtyAddrs ++= addrs
    if addrs.size == 1 then
      store += addrs.head -> ((true, v))
    else for x <- addrs do
      store.get(x) match
        case None => store += x -> ((false, v))
        case Some((_, old)) => store += x -> ((false, joinValues(old, v)))

