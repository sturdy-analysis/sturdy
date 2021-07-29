package sturdy.effect.environment

/*
 * Closable environment interface
 */
trait ClosableEnvironment[Var, V, Env] extends Environment[Var, V]:
  def getEnv: Env
  def setEnv(env: Env): Unit