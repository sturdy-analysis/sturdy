package sturdy.effect.environment

/*
 * The environment interface.
 */
trait Environment[Var, V]:
  type EnvJoin[A]
  final type EnvJoined[A] = EnvJoin[A] ?=> A

  def lookup[A](x: Var, found: V => A, notFound: => A): EnvJoined[A]
  def bind(x: Var, v: V): Unit
  def scoped[A](f: => A): A
  def clear(): Unit

  final def lookupOrElse(x: Var, notFound: => V): EnvJoined[V] =
    lookup(x, identity, notFound)

  final def lookupOrElseAndThen[A](x: Var, notFound: => V)(f: V => A): EnvJoined[A] =
    lookup(x, f, f(notFound))

  final def bindLocal[A](x: Var, v: V)(f: => A): A =
    scoped({bind(x, v); f})

  final def freshScoped[A](f: => A): A =
    scoped({clear(); f})
  