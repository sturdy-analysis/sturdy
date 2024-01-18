package sturdy.effect.store.must

import sturdy.data.{JoinMustMap, MustMap, WidenMustMap}
import sturdy.effect.{ComputationJoiner, Effect, TrySturdy}
import sturdy.values.{Finite, Join, Widen}

import scala.collection.mutable.ListBuffer

/**
 * An abstract threadded store. The store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AbstractMustStore[Addr, V](using Join[V], Widen[V], Finite[Addr]) extends Effect:

  protected var store: MustMap[Addr, V] = MustMap()
  protected var dirtyAddrs: Set[Addr] = Set()

  protected def weakUpdate(x: Addr, v: V, definite: Boolean): Unit =
    dirtyAddrs += x
    store.get(x) match
      case None =>
        if (definite)
          store += x -> v
      case Some(old) => Join(old, v).ifChanged(store += x -> _)

  override type State = MustMap[Addr, V]
  override def getState: MustMap[Addr, V] = store
  override def setState(s: MustMap[Addr, V]): Unit =
    this.store = s
    this.dirtyAddrs = s.m.keySet
  override def join: Join[MustMap[Addr, V]] = implicitly
  override def widen: Widen[MustMap[Addr, V]] = implicitly
