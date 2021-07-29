package sturdy.effect.environment

/*
 * A concrete closable environment which extends a concrete environment
 */
trait CClosableEnvironment[Var, V]
    extends CEnvironment[Var, V], ClosableEnvironment[Var, V, Map[Var, V]]:

  override def getEnv: Map[Var, V] = env
  override def setEnv(env: Map[Var, V]): Unit = this.env = env
