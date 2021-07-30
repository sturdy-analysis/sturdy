package sturdy.effect.store

import sturdy.effect.AnalysisState
import sturdy.effect.JoinComputation
import sturdy.effect.store.AStoreGenericThreadded.StoreState
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
  protected def setStore(s: Map[Addr, V]): Unit =
    this.store = s
    this.dirtyAddrs = s.keySet

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

  def getStoreJoinedWith(other: Map[Addr, V]): Map[Addr, V] =
    var joined = other
    for (x <- this.dirtyAddrs)
      joined.get(x) match
        case None => joined += x -> store(x)
        case Some(otherV) =>
          val thisV = store(x)
          val joinedV = j.joinValues(otherV, thisV)
          joined += x -> joinedV
    joined

object AStoreGenericThreadded:
  case class StoreState[Addr, V](store: Map[Addr, V])(using j: JoinValue[V]) {
    def join(other: StoreState[Addr, V]): StoreState[Addr, V] =
      var joined = this.store
      for (x <- other.store.keySet)
        joined.get(x) match
          case None => joined += x -> other.store(x)
          case Some(thisV) =>
            val otherV = other.store(x)
            val joinedV = j.joinValues(otherV, thisV)
            joined += x -> joinedV
      StoreState(joined)
  }
