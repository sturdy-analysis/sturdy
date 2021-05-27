package stateful.whilelang

trait Store[Addr, V] {
  type StoreJoin[A]

  def read[A](x: Addr, found: V => A, notFound: => A)(implicit j: StoreJoin[A]): A
  def write(x: Addr, v: V): Unit

  def readOrElse(a: Addr, notFound: => V)(implicit j: StoreJoin[V]): V =
    read(a, identity, notFound)
}

trait StoreImpl[Addr, V] extends Store[Addr, V] {
  var store: Map[Addr, V] = Map()

  override type StoreJoin[_] = Unit

  override def read[A](x: Addr, found: V => A, notFound: => A)(implicit j: Unit): A =
    store.get(x).map(found).getOrElse(notFound)

  override def write(x: Addr, v: V): Unit = store = store + (x -> v)
}