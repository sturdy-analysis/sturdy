package sturdy.effect.store

import sturdy.effect.AnalysisState
import sturdy.effect.Effectful
import sturdy.effect.TrySturdy
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
      case Some(old) => j(old, v).ifChanged(store += x -> _)

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
