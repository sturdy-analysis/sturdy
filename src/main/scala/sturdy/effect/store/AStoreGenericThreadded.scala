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
trait AStoreGenericThreadded[Addr, V](using JoinValue[V])
  extends JoinComputation:

  protected var store: Map[Addr, (Boolean, V)] = Map()
  protected var dirtyAddrs: Set[Addr] = Set()

  def getStore: Map[Addr, (Boolean, V)] = store

  override def joinComputations[A](f: => A)(g: => A): Join[A] =
    val snapshot = store
    var joinedStore = store
    var joinedDirtyAddrs = dirtyAddrs

    // These addresses are definitely bound by f but were unbound before. We need to consilate them with g.
    var newDefiniteAddrsInF: Map[Addr, (Boolean, V)] = Map()

    val joinedResult = super.joinComputations {
      store = snapshot
      dirtyAddrs = Set()
      val fResult = f
      for (x <- dirtyAddrs) do
        val (definite, newVal) = store(x)
        joinedStore.get(x) match
          case None =>
            // This binding is new, so we add an entry for it
            joinedStore += x -> ((definite, newVal))
            if definite then
              // This binding is definite in f. If g does not definitely bind x, we must later mark this binding as non-definite.
              newDefiniteAddrsInF += x -> ((false, newVal))
          case Some((_, oldVal)) =>
            // This binding already existed in store before.
            if definite then
              // This binding is definite in f.
              joinedStore += x -> ((true, newVal))
              // If g does not definitely bind x, we must later mark this binding as non-definite _and_ join it with the old value (which is retained through g).
              newDefiniteAddrsInF += x -> ((false, joinValues(oldVal, newVal)))
            else
              // This binding is not definite in f
              joinedStore += x -> ((false, joinValues(oldVal, newVal)))
      fResult
    } {
      store = snapshot
      dirtyAddrs = Set()
      val gResult = g
      for (x <- dirtyAddrs) do
        joinedStore.get(x) match
          case None =>
            // This binding is new in g and thus did neither occur in f nor in the original store.
            joinedStore += x -> ((false, store(x)._2))
          case Some((oldDefinite, oldVal)) =>
            // This binding already existed in store before or was added by f.
            val (definite, newVal) = store(x)

            // If the binding was definite in f, then oldDefinite==true and oldVal==fVal.
            // If it was non-definite in f, then oldDefinite==false and oldVal==joinValues(prevVal, fVal).
            // If it was not bound by f, then oldDefinite==prevDefinite and oldVal==prevVal.

            if (definite) {
              // This binding is definite in g.
              joinedStore += x -> ((oldDefinite, joinValues(oldVal, newVal)))
            } else {
              // This binding is not definite in g
              newDefiniteAddrsInF.get(x) match {
                case Some((_, weakenedFVal)) =>
                  // Binding was definite in f, weaken it
                  joinedStore += x -> ((oldDefinite, joinValues(weakenedFVal, newVal)))
                case None =>
                  // Binding was not bound or non-definite in f
                  joinedStore += x -> ((oldDefinite, joinValues(oldVal, newVal)))
              }
            }
            newDefiniteAddrsInF -= x

      // g did not definitely bind x, hence weaken the binding of x
      joinedStore ++= newDefiniteAddrsInF
      gResult
    }

    store = joinedStore
    dirtyAddrs = joinedDirtyAddrs
    joinedResult

