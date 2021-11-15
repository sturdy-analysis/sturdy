package sturdy.effect.store

import sturdy.effect.AnalysisState
import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.Effectful
import sturdy.effect.TrySturdy
import sturdy.values.Join

import scala.collection.mutable.ListBuffer

/**
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

  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    private val snapshot = store
    private val snapshotDirtyAddrs = dirtyAddrs
    dirtyAddrs = Set()
    private var fStore: Map[Addr, V] = _
    private var fDirtyAddrs: Set[Addr] = _

    override def inbetween_(): Unit =
      fStore = store
      fDirtyAddrs = dirtyAddrs
      store = snapshot
      dirtyAddrs = Set()

    override def retainNone_(): Unit =
      store = snapshot

    override def retainFirst_(fRes: TrySturdy[A]): Unit =
      store = fStore
      dirtyAddrs = snapshotDirtyAddrs ++ fDirtyAddrs

    override def retainSecond_(gRes: TrySturdy[A]): Unit =
      dirtyAddrs ++= snapshotDirtyAddrs

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for (x <- fDirtyAddrs)
        weakUpdate(x, fStore(x))
      dirtyAddrs ++= snapshotDirtyAddrs
      dirtyAddrs ++= fDirtyAddrs
  }
