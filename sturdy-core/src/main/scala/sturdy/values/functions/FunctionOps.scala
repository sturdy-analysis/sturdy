package sturdy.values.functions

import sturdy.effect.Effect
import sturdy.values.Join
import sturdy.values.Powerset

/*
 * Function values (not closures).
 */
trait FunctionOps[Fun, A, R, FunV] {
  def funValue(fun: Fun): FunV
  def invokeFun(fun: FunV, a: A)(invoke: (Fun, A) => R): R
}

given ConcreteFunctionOps[F, A, R]: FunctionOps[F, A, R, F] with
  def funValue(fun: F): F = 
    fun
  def invokeFun(fun: F, a: A)(invoke: (F, A) => R): R = 
    invoke(fun, a)

