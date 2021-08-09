package sturdy.effect.store

/*
 * The store interface.
 */
trait Store[Addr, V]:
  type StoreJoin[A]
  final type StoreJoined[A] = StoreJoin[A] ?=> A

  def read[A](x: Addr, found: V => A, notFound: => A): StoreJoined[A]
  def write(x: Addr, v: V): Unit
  def free(x: Addr): Unit
  def scopedAddresses[A](xs: Iterable[Addr])(f: => A): A

  def readOrElse(a: Addr, notFound: => V): StoreJoined[V] =
    read(a, identity, notFound)

  def readOrElseAndThen[A](a: Addr, notFound: => V)(f: V => A): StoreJoined[A] =
    read(a, f, f(notFound))
