package sturdy.effect.environment

/*
 * A concrete environment.
 */
trait CEnvironment[Var, V](_init: Map[Var, V] = Map()) extends Environment[Var, V]:
  protected var env: Map[Var, V] = _init

  def getEnv: Map[Var, V] = env
  
  override type EnvJoin[_] = Unit
  
  override def lookup[A](x: Var, found: V => A, notFound: => A): EnvJoined[A] =
    env.get(x).map(found).getOrElse(notFound)

  override def bind(x: Var, v: V): Unit = env = env + (x -> v)

  override def scoped[A](f: => A): A =
    val snapshot = env
    try f finally {
      env = snapshot
    }
