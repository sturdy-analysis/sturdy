package sturdy.effect.store

import sturdy.effect.AnalysisState
import sturdy.effect.Effectful
import sturdy.effect.store.AStoreGenericThreadded.StoreState
import sturdy.values.Join

import scala.collection.mutable.ListBuffer

/*
 * An abstract threadded store. The store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreGenericThreadded[Addr, V](using j: Join[V])
  extends Effectful:

  protected var store: Map[Addr, V] = Map()
  protected var dirtyAddrs: Set[Addr] = Set()

  def getStore: Map[Addr, V] = store
  protected def setStore(s: Map[Addr, V]): Unit =
    this.store = s
    this.dirtyAddrs = s.keySet

  protected def weakUpdate(x: Addr, v: V): Unit =
    dirtyAddrs += x
    store.get(x) match
      case None => store += x -> v
      case Some(old) => store += x -> j(old, v)

  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    val snapshot = store
    var snapshotDirtyAddrs = dirtyAddrs
    dirtyAddrs = Set()

    super.joinComputations(f) {
      val fStore = store
      val fDirtyAddrs = dirtyAddrs
      store = snapshot
      dirtyAddrs = Set()

      try g finally {
        for (x <- fDirtyAddrs)
          weakUpdate(x, fStore(x))

        dirtyAddrs ++= snapshotDirtyAddrs
        dirtyAddrs ++= fDirtyAddrs
      }
    }

  def getStoreJoinWith(other: Map[Addr, V]): Map[Addr, V] =
    var joined = other
    for (x <- this.dirtyAddrs)
      joined.get(x) match
        case None => joined += x -> store(x)
        case Some(otherV) =>
          val thisV = store(x)
          val joinedV = j(otherV, thisV)
          joined += x -> joinedV
    joined

object AStoreGenericThreadded:
  case class StoreState[Addr, V](store: Map[Addr, V])(using j: Join[V]) {
    def join(other: StoreState[Addr, V]): StoreState[Addr, V] =
      var joined = this.store
      for ((x, v) <- other.store)
        joined.get(x) match
          case None => joined += x -> v
          case Some(thisV) =>
            val joinedV = j(v, thisV)
            joined += x -> joinedV
      StoreState(joined)
  }
