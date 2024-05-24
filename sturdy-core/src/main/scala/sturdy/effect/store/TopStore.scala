package sturdy.effect.store

import sturdy.data.{JOption, JOptionA, MayJoin, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.values.{Join, Top, Widen}

class TopStore[Addr, V](using t: Top[V]) extends Store[Addr, V, WithJoin]{
  override def read(x: Addr): JOptionA[V] = JOptionA.noneSome(t.top)
  override def write(x: Addr, v: V): Unit = ()
  override def free(x: Addr): Unit = ()

  override type State = Unit
  override def getState: Unit = ()
  override def setState(st: Unit): Unit = ()
  override def join: Join[Unit] = implicitly
  override def widen: Widen[Unit] = implicitly
}
