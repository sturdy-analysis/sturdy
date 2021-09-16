package sturdy.effect.environment

import sturdy.effect.MayComputeConcrete
import sturdy.effect.NoJoin

/*
 * A concrete environment.
 */
trait CEnvironment[Var, V](_init: Map[Var, V] = Map()) extends Environment[Var, V], ClosableEnvironment[Map[Var, V]]:
  override type EnvJoin[A] = NoJoin[A]

  protected var env: Map[Var, V] = _init
  def getEnv: Map[Var, V] = env
  
  override def lookup(x: Var): MayComputeConcrete[V] =
    MayComputeConcrete(env.get(x))

  override def bind(x: Var, v: V): Unit = env = env + (x -> v)

  override def scoped[A](f: => A): A =
    val snapshot = env
    try f finally {
      env = snapshot
    }

  override def clear(): Unit =
    env = Map()

  override def closeEnvironment: Map[Var, V] = env
  override def loadClosedEnvironment(env: Map[Var, V]): Unit = this.env = env
  