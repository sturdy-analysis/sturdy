package sturdy.effect.store

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue

import scala.collection.mutable.ListBuffer

/*
 * An abstract threadded store. The store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreGenericThreadded[Addr, V](using j: JoinValue[V])
  extends JoinComputation:

  protected var store: Map[Addr, V] = Map()
  protected var dirtyAddrs: Set[Addr] = Set()

  def getStore: Map[Addr, V] = store
  protected def setStore(m: Map[Addr, V]): Unit =
    this.store = m

  protected def weakUpdate(x: Addr, v: V): Unit =
    dirtyAddrs += x
    store.get(x) match
      case None => store += x -> v
      case Some(old) => store += x -> j.joinValues(old, v)
  
  override def joinComputations[A](f: => A)(g: => A): Join[A] =
    val snapshot = store
    var snapshotDirtyAddrs = dirtyAddrs
    dirtyAddrs = Set()

    var fStore: Map[Addr, V] = null
    var fDirtyAddrs: Set[Addr] = null

    val joinedResult = super.joinComputations(f) {
      fStore = store
      fDirtyAddrs = dirtyAddrs

      store = snapshot
      dirtyAddrs = Set()
      g
    }

    for (x <- fDirtyAddrs)
      weakUpdate(x, fStore(x))

    dirtyAddrs ++= snapshotDirtyAddrs
    dirtyAddrs ++= fDirtyAddrs

    joinedResult

  def getStoreJoinedWith(previous: Map[Addr, V]): Map[Addr, V] =
    var joined = previous
    for (x <- this.dirtyAddrs)
      joined.get(x) match
        case None => joined += x -> store(x)
        case Some(old) =>
          val now = store(x)
          val joinedV = j.joinValues(old, now)
          if (joinedV != now)
            throw new IllegalStateException(s"New store is not larger than old store, was $old now $now")
          joined += x -> joinedV
    joined
