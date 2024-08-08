package sturdy.effect.environment

import sturdy.data.*
import sturdy.effect.{Concrete, environment}
import sturdy.values.MaybeChanged.Unchanged
import sturdy.values.{Changed, Combine, Finite, Join, MaybeChanged, StackWidening, Widen, Widening}
import sturdy.data.given
import sturdy.{effect, values}

class StandardEnvironment[Var, V](using Finite[Var])
                                 (using Join[V])
                                 (using Widen[V])
                                 (_init: Map[Var, V] = Map()) extends ClosableEnvironment[Var, V, Map[Var, V], NoJoin]:
  type State = Map[Var, V]
  protected var env: Map[Var, V] = _init

  override def lookup(x: Var): JOptionC[V] = JOptionC(env.get(x))
  override def bind(x: Var, v: V): Unit = env = env + (x -> v)

  override def scoped[A](f: => A): A =
    val snapshot = env
    try f finally {
      env = snapshot
    }

  override def clear(): Unit = env = Map()
  override def closeEnvironment: Map[Var, V] = env
  override def loadClosedEnvironment(env: Map[Var, V]): Unit = this.env = env

  final def getState: State = env
  final def setState(st: State): Unit = loadClosedEnvironment(st)

  final def join: Join[State] = implicitly
  final def widen: Widen[State] = implicitly


given CombineBox[V, W <: Widening](using j: Combine[V, W]): Combine[Box[V], W] with
  override def apply(v1: Box[V], v2: Box[V]): MaybeChanged[Box[V]] = j(v1.value, v2.value) match
    case MaybeChanged.Changed(a) => Changed(Box.Eager(a))
    case MaybeChanged.Unchanged(a) => Unchanged(v1)


class StandardCyclicEnvironment[Var, V](using Finite[Var])
                                       (using Join[V])
                                       (using Widen[V])
                                       (_init: Map[Var, V] = Map()) extends CyclicEnvironment[Var, V, NoJoin], ClosableEnvironment[Var, V, Map[Var, Box[V]], NoJoin]:

  type State = Map[Var, Box[V]]
  protected var env: Map[Var, Box[V]] = _init.view.mapValues(Box.Eager.apply).toMap

  override def lookup(x: Var): JOptionC[V] = JOptionC(env.get(x).map(_.value))
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

  final def getState: State = env
  final def setState(st: State): Unit = loadClosedEnvironment(st)

  final def join: Join[State] =  implicitly
  final def widen: Widen[State] = implicitly