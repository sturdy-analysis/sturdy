package sturdy.effect.store

import sturdy.data.{CombineFiniteKeyMap, JOptionA, JOptionC, NoJoin, WithJoin}
import sturdy.values.{Finite, Join, Widen}


class StandardStore[Addr, V](_init: Map[Addr, V] = Map())
                            (using Finite[Addr], Join[V], Widen[V]) extends Store[Addr, V, NoJoin]:
  override type State = Map[Addr, V]
  protected var store: State = _init

  def entries: Map[Addr, V] = store

  override def read(x: Addr): JOptionC[V] =
    JOptionC(store.get(x))

  override def write(x: Addr, v: V): Unit =
    store += (x -> v)

  override def free(x: Addr): Unit =
    store -= x

  override def getState: State = store

  override def setState(st: Map[Addr, V]): Unit = store = st

  override def join: Join[Map[Addr, V]] = implicitly
  override def widen: Widen[Map[Addr, V]] = implicitly