package sturdy.effect.store.may

import sturdy.data.{JoinMayMap, MayMap, WidenFiniteKeyMayMap}
import sturdy.effect.{ComputationJoiner, Effect, TrySturdy}
import sturdy.values.{Finite, Join, Widen}

import scala.collection.mutable.ListBuffer

/**
 * An abstract threadded store. The store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AbstractMayStore[Addr, V](using Join[V], Widen[V], Finite[Addr]) extends Effect:

  protected var store: MayMap[Addr, V] = MayMap()
  protected var dirtyAddrs: Set[Addr] = Set()

  protected def weakUpdate(x: Addr, v: V): Unit =
    dirtyAddrs += x
    store.get(x) match
      case None => store += x -> v
      case Some(old) => Join(old, v).ifChanged(store += x -> _)

  override type State = MayMap[Addr, V]
  override def getState: MayMap[Addr, V] = store
  override def setState(s: MayMap[Addr, V]): Unit =
    this.store = s
    this.dirtyAddrs = s.m.keySet
  override def join: Join[MayMap[Addr, V]] = implicitly
  override def widen: Widen[MayMap[Addr, V]] = implicitly
