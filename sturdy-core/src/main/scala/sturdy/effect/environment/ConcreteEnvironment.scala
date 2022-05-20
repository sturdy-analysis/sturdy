package sturdy.effect.environment

import sturdy.data.*

/*
 * A concrete environment.
 */
class ConcreteEnvironment[Var, V](_init: Map[Var, V] = Map()) extends ClosableEnvironment[Var, V, Map[Var, V], NoJoin]:
  protected var env: Map[Var, V] = _init
  def getEnv: Map[Var, V] = env

  override def lookup(x: Var): JOptionC[V] =
    JOptionC(env.get(x))

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

  override type State = Map[Var, V]
  override def getState: State = env
  override def setState(s: Map[Var, V]): Unit = this.env = env


class ConcreteCyclicEnvironment[Var, V](_init: Map[Var, V] = Map()) extends CyclicEnvironment[Var, V, NoJoin] with ClosableEnvironment[Var, V, Map[Var, Box[V]], NoJoin]:
  protected var env: Map[Var, Box[V]] = _init.view.mapValues(Box.Eager.apply).toMap

  override def lookup(x: Var): JOptionC[V] =
    JOptionC(env.get(x).map(_.value))

  override def bind(x: Var, v: V): Unit = env = env + (x -> Box.Eager(v))
  override def bindLazy(x: Var, v: => V): Unit = env = env + (x -> Box.Lazy(() => v))

  override def scoped[A](f: => A): A =
    val snapshot = env
    try f finally {
      env = snapshot
    }

  override def clear(): Unit =
    env = Map()

  override def closeEnvironment: Map[Var, Box[V]] = env
  override def loadClosedEnvironment(env: Map[Var, Box[V]]): Unit = this.env = env

  override type State = Map[Var, Box[V]]
  override def getState: State = env
  override def setState(s: Map[Var, Box[V]]): Unit = this.env = env
