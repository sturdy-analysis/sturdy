package sturdy.effect.store

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)writteb to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreThreadded[Addr, V](_init: Map[Addr, (Boolean, V)])(using JoinValue[V])
  extends Store[Addr, V], JoinComputation:
  
  override type StoreJoin[A] = JoinValue[A]

  protected var store: Map[Addr, (Boolean, V)] = _init
  protected var dirtyAddrs: Set[Addr] = Set()
  
  def getStore: Map[Addr, (Boolean, V)] = store

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

