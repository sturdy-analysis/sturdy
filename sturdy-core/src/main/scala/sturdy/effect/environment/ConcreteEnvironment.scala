package sturdy.effect.environment

import sturdy.data.*
import sturdy.effect.Concrete

/*
 * A concrete environment.
 */
class ConcreteEnvironment[Var, V](_init: Map[Var, V] = Map()) extends ClosableEnvironment[Var, V, Map[Var, V], NoJoin], Concrete:
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


class ConcreteCyclicEnvironment[Var, V](_init: Map[Var, V] = Map()) extends CyclicEnvironment[Var, V, NoJoin], ClosableEnvironment[Var, V, Map[Var, Box[V]], NoJoin], Concrete:
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



class AbstractCyclicEnvironment[Var, V,J[_] <: MayJoin[_]](_init: Map[Var, V] = Map()) extends CyclicEnvironment[Var, V, J], ClosableEnvironment[Var, V, Map[Var, Box[V]], J], Concrete:
  protected var env: Map[Var, Box[V]] = _init.view.mapValues(Box.Eager.apply).toMap

  override def lookup(x: Var): JOption[J,V] = ???
  override def bind(x: Var, v: V): Unit = env = env + (x -> Box.Eager(v))
  override def bindLazy(x: Var, v: => V): Unit = env = env + (x -> Box.Lazy(() => v))
  override def scoped[A](f: => A): A =
    val snapshot = env
    try f finally {
      env = snapshot
    }

  override def clear(): Unit = env = Map()
  override def closeEnvironment: Map[Var, Box[V]] = env
  override def loadClosedEnvironment(env: Map[Var, Box[V]]): Unit = this.env = env


////
// Extra class


class WithJoinConcreteCyclicEnvironment[Var, V](_init: Map[Var, V] = Map()) extends CyclicEnvironment[Var, V, WithJoin], ClosableEnvironment[Var, V, Map[Var, Box[V]], WithJoin], Concrete:
  protected var env: Map[Var, Box[V]] = _init.view.mapValues(Box.Eager.apply).toMap

  override def lookup(x: Var): JOptionA[V] =
    JOptionA(env.get(x).map(_.value))

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




////

