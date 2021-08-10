package sturdy.effect.environment

import sturdy.effect.CMayCompute
import sturdy.effect.NoJoin

/*
 * A concrete environment.
 */
trait CEnvironment[Var, V](_init: Map[Var, V] = Map()) extends Environment[Var, V]:
  override type EnvJoin[A] = NoJoin[A]
  override type EnvJoinComp = Unit

  protected var env: Map[Var, V] = _init
  def getEnv: Map[Var, V] = env
  
  override def lookup(x: Var): CMayCompute[V] =
    CMayCompute(env.get(x))

  override def bind(x: Var, v: V): Unit = env = env + (x -> v)

  override def scoped[A](f: => A): A =
    val snapshot = env
    try f finally {
      env = snapshot
    }

  override def clear(): Unit =
    env = Map()