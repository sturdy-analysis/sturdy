package stateful.whilelang

import stateful.{Join, JoinComputation}

trait Environment[Var, V] {
  type EnvironmentJoin[A]

  def lookup[A](x: Var, found: V => A, notFound: => A)(implicit j: EnvironmentJoin[A]): A
  def bind(x: Var, v: V): Unit
  def scoped[A](f: => A): A

  def lookupOrElse(x: Var, notFound: => V)(implicit j: EnvironmentJoin[V]): V =
    lookup(x, identity, notFound)

  def lookupAndThen[A](x: Var, notFound: => V)(f: V => A)(implicit j: EnvironmentJoin[A]): A =
    lookup(x, f, f(notFound))

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

trait EnvironmentAbs[Var, V] extends Environment[Var, V] with JoinComputation {
  var env: Map[Var, Set[V]] = Map()
  var dirtyVars: Set[Var] = Set()

  override type EnvironmentJoin[A] = Join[A]

  override def lookup[A](x: Var, found: V => A, notFound: => A)(implicit j: EnvironmentJoin[A]): A =
    env.get(x) match {
      case None => notFound
      case Some(set) =>
        if (set.isEmpty)
          notFound
        else if (set.size == 1)
          found(set.head)
        else
          set.tail.foldLeft(found(set.head))((v, a) => j.join(v, found(a)))
    }

  override def bind(x: Var, v: V): Unit = {
    dirtyVars += x
    env.get(x) match {
      case Some(set) => env += x -> (set + v)
      case None => env += x -> Set(v)
    }
  }

  override def scoped[A](f: => A): A = {
    val snapshot = env
    val snapshotDirty = dirtyVars
    try f finally {
      env = snapshot
      dirtyVars = snapshotDirty
    }
  }

  override def join[A](f: => A, g: => A)(implicit j: Join[A]): A = {
    val snapshot = env
    var joined = env
    var joinedDirtyVars = dirtyVars

    def track(fun: => A): A = {
      env = snapshot
      dirtyVars = Set()
      val a = fun
      for (x <- dirtyVars) {
        joinedDirtyVars += x
        joined.get(x) match {
          case None => joined += x -> env(x)
          case Some(set) => joined += x -> (set ++ env(x))
        }
      }
      a
    }

    val a = super.join(track(f), track(g))
    env = joined
    dirtyVars = joinedDirtyVars
    a
  }
}