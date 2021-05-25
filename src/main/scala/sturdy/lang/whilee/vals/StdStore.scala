package sturdy.lang.whilee.vals

import sturdy.common.order.CompleteVal
import sturdy.lang.whilee.HasStore

trait StdStore[S] extends HasStore[S] {
  val emptyStore: S

  private var _store: S = emptyStore

  override def getStore: S = _store
  override def putStore(st: S): Unit = _store = st

  override val completeStore: CompleteVal[S] = ???

//  override def join[A, B](c: CompleteVal[B], f: A => B, g: A => B): A => B = ???
}
