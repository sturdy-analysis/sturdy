package sturdy.values.functions

import sturdy.effect.Effectful
import sturdy.values.Join
import sturdy.values.Powerset

/*
 * Function values (not closures).
 */
trait FunctionOps[F, A, R, V] {
  def funValue(fun: F): V
  def invokeFun(fun: V, a: A)(invoke: (F, A) => R): R
}

given ConcreteFunctionOps[F, A, R]: FunctionOps[F, A, R, F] with
  def funValue(fun: F): F = 
    fun
  def invokeFun(fun: F, a: A)(invoke: (F, A) => R): R = 
    invoke(fun, a)

