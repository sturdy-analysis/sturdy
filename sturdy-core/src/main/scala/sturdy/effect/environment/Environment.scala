package sturdy.effect.environment

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.Effect

/**
 * [[Environment]] is a mapping from variables to values.
 * Variables are immutable.
 */
trait Environment[Var, V, J[_] <: MayJoin[_]] extends Effect:
  def lookup(x: Var): JOption[J, V]
  def bind(x: Var, v: V): Unit
  def scoped[A](f: => A): A
  def clear(): Unit

  final def bindLocal[A](x: Var, v: V)(f: => A): A =
    scoped({bind(x, v); f})

  final def freshScoped[A](f: => A): A =
    scoped({clear(); f})

trait ClosableEnvironment[Var, V, Env, J[_] <: MayJoin[_]] extends Environment[Var, V, J]:
  def closeEnvironment: Env
  def loadClosedEnvironment(env: Env): Unit
