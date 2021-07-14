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
trait AStoreKeysThreadded[Addr, Addrs <: Iterable[Addr], V](_init: Map[Addr, (Boolean, V)])(using JoinValue[V])
  extends Store[Addrs, V], JoinComputation:

  protected var store: Map[Addr, (Boolean, V)] = _init
  protected var dirtyAddrs: Set[Addr] = Set()

  override type StoreJoin[A] = JoinValue[A]

  def getStore: Map[Addr, (Boolean, V)] = store

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
  
  override def joinComputations[A](f: => A)(g: => A): Join[A] =
    val snapshot = store
    var joinedEnv = store
    var joinedDirtyAddrs = dirtyAddrs

    // These addresses are definitely bound by f but were unbound before. We need to consilate them with g.
    var newDefiniteAddrsInF: Map[Addr, (Boolean, V)] = Map()

    val joinedResult = super.joinComputations {
      store = snapshot
      dirtyAddrs = Set()
      val fResult = f
      for (x <- dirtyAddrs) do
        joinedEnv.get(x) match
          case None =>
            val tup@(definite, v) = store(x)
            joinedEnv += x -> tup
            if (definite) then
              // This binding is new and definite in f. If g does not definitely bind x, we must later weaken this binding.
              newDefiniteAddrsInF += x -> ((false, v))
          case Some((oldDefinite, oldVal)) =>
            // This binding already existed in env before.
            val (newDefinite, newVal) = store(x)
            joinedEnv += x -> ((oldDefinite && newDefinite, joinValues(oldVal, newVal)))
      fResult
    } {
      store = snapshot
      dirtyAddrs = Set()
      val gResult = g
      for (x <- dirtyAddrs) do
        joinedEnv.get(x) match
          case None =>
            // This binding is new in g and thus did _not_ occur in f.
            joinedEnv += x -> ((false, store(x)._2))
          case Some((oldDefinite, oldVal)) =>
            // This binding already existed in env before.
            val (newDefinite, newVal) = store(x)
            joinedEnv += x -> ((oldDefinite && newDefinite, joinValues(oldVal, newVal)))
            // we have used g to (possibly) weaken the binding of x
            newDefiniteAddrsInF -= x

      // g did not bind x, hence weaken the binding of x
      joinedEnv ++= newDefiniteAddrsInF
      gResult
    }

    store = joinedEnv
    dirtyAddrs = joinedDirtyAddrs
    joinedResult

