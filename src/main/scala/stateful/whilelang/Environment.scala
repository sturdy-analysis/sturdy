package stateful.whilelang

trait Environment[Var, V] {
  type EnvironmentJoin[A]

  def lookup[A](x: Var, found: V => A, notFound: => A)(implicit j: EnvironmentJoin[A]): A
  def bind(x: Var, v: V): Unit
  def scoped[A](f: => A): A

  def lookupOrElse(x: Var, notFound: => V)(implicit j: EnvironmentJoin[V]): V =
    lookup(x, identity, notFound)
  def bindLocal[A](x: Var, v: V)(f: => A): A =
    scoped({bind(x, v); f})
}

trait EnvironmentImpl[Var, V] extends Environment[Var, V] {
  var env: Map[Var, V] = Map()

  override type EnvironmentJoin[_] = Unit
  override def lookup[A](x: Var, found: V => A, notFound: => A)(implicit j: Unit): A =
    env.get(x).map(found).getOrElse(notFound)

  override def bind(x: Var, v: V): Unit = env = env + (x -> v)

  override def scoped[A](f: => A): A = {
    val snapshot = env
    try f finally {
      env = snapshot
    }
  }
}