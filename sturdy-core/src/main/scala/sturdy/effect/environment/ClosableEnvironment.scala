package sturdy.effect.environment

/*
 * Closable environment interface
 */
trait ClosableEnvironment[_Env]:
  type Env = _Env
  def closeEnvironment: Env
  def loadClosedEnvironment(env: Env): Unit
