package sturdy.effect.environment

import sturdy.effect.MayCompute

/*
 * The environment interface.
 */
trait Environment[Var, V]:
  type EnvJoin[A]
  type EnvJoinComp

  def lookup(x: Var): MayCompute[V, EnvJoin, EnvJoinComp]
  def bind(x: Var, v: V): Unit

  def scoped[A](f: => A): A
  def clear(): Unit

  final def bindLocal[A](x: Var, v: V)(f: => A): A =
    scoped({bind(x, v); f})

  final def freshScoped[A](f: => A): A =
    scoped({clear(); f})