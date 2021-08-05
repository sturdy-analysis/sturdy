package sturdy.effect.environment

/*
 * A concrete environment.
 */
trait CEnvironment[Var, V](_init: Map[Var, V] = Map()) extends ClosableEnvironment[Var, V, Map[Var, V]]:
  override type EnvJoin[_] = Unit

  protected var env: Map[Var, V] = _init
  def getEnv: Map[Var, V] = env
  def setEnv(env: Map[Var, V]) = this.env = env
  
  override def lookup[A](x: Var, found: V => A, notFound: => A): EnvJoined[A] =
    env.get(x).map(found).getOrElse(notFound)

  override def bind(x: Var, v: V): Unit = env = env + (x -> v)

  override def scoped[A](f: => A): A =
    val snapshot = env
    try f finally {
      env = snapshot
    }

  override def clear(): Unit =
    env = Map()