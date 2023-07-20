package sturdy.effect.store

import sturdy.data.given
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effect
import sturdy.effect.TrySturdy
import sturdy.values.{Finite, Join, Widen}

import scala.collection.mutable.ListBuffer

/**
 * An abstract threadded store. The store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreGenericThreadded[Addr, V](using Join[V], Widen[V], Finite[Addr]) extends Effect:

  protected var store: Map[Addr, V] = Map()
  protected var dirtyAddrs: Set[Addr] = Set()

  protected def weakUpdate(x: Addr, v: V): Unit =
    dirtyAddrs += x
    store.get(x) match
      case None => store += x -> v
      case Some(old) => Join(old, v).ifChanged(store += x -> _)

  private class AStoreGenericJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = store
    private val snapshotDirtyAddrs = dirtyAddrs
    dirtyAddrs = Set()
    private var fStore: Map[Addr, V] = _
    private var fDirtyAddrs: Set[Addr] = _

    override def inbetween(fFailed: Boolean): Unit =
      fStore = store
      fDirtyAddrs = dirtyAddrs
      store = snapshot
      dirtyAddrs = Set()

    override def retainNone(): Unit =
      store = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      store = fStore
      dirtyAddrs = snapshotDirtyAddrs ++ fDirtyAddrs

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      dirtyAddrs ++= snapshotDirtyAddrs

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for (x <- fDirtyAddrs)
        weakUpdate(x, fStore(x))
      dirtyAddrs ++= snapshotDirtyAddrs
      dirtyAddrs ++= fDirtyAddrs
  }

  override type State = Map[Addr, V]
  override def getState: Map[Addr, V] = store
  override def setState(s: Map[Addr, V]): Unit =
    this.store = s
    this.dirtyAddrs = s.keySet
  override def join: Join[Map[Addr, V]] = implicitly
  override def widen: Widen[Map[Addr, V]] = implicitly

