package sturdy.effect.environment

import sturdy.data.Option

/*
 * The environment interface.
 */
trait Environment[Var, V]:
  type EnvJoin[A]

  def lookup(x: Var): Option[EnvJoin, V]
  def bind(x: Var, v: V): Unit
  def scoped[A](f: => A): A
  def clear(): Unit

  final def bindLocal[A](x: Var, v: V)(f: => A): A =
    scoped({bind(x, v); f})

  final def freshScoped[A](f: => A): A =
    scoped({clear(); f})
  