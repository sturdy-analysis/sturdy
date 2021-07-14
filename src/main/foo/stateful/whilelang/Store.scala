package stateful.whilelang

import stateful.{JoinComputation, Join}


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

trait StoreAbs[Addr, V] extends Store[Addr, V] with JoinComputation {
  type St = Map[Addr, (Boolean,V)]

  // true = must, false = may
  var store: St = Map()

  override type StoreJoin[A] = Join[A]
  override def read[A](x: Addr, found: V => A, notFound: => A)(implicit j: StoreJoin[A]): A =
    store.get(x) match {
      case None => notFound
      case Some((true, v)) => found(v)
      case Some((false, v)) => join(found(v), notFound)
    }
  override def write(x: Addr, v: V): Unit = store = store + (x -> (true, v))

  val storeJoinVal: Join[V]
  def joinStores(st1: St, st2: St): St = {
    var joined: St = Map()
    for ((a1, (b1, v1)) <- st1) {
      st2.get(a1) match {
        case Some((b2, v2)) =>
          joined += a1 -> (b1 && b2, storeJoinVal.join(v1, v2))
        case None =>
          joined += a1 -> (false, v1)
      }
    }
    for ((a2, (_, v2)) <- st2 if !st1.contains(a2))
      joined += a2 -> (false, v2)
    joined
  }

  override def join[A](f: => A, g: => A)(implicit j: Join[A]): A = {
    val snapshot = store
    var newStores: List[St] = List()

    def track(fun: => A): A = {
      store = snapshot
      val a = fun
      newStores = store :: newStores
      a
    }

    val a = super.join(track(f), track(g))
    store = newStores.reduce(joinStores)
    a
  }
}