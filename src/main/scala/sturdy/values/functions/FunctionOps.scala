package sturdy.values.functions

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue
import sturdy.values.Powerset

/*
 * Function values (not closures).
 */
trait FunctionOps[F, A, R, V] {
  def funValue(fun: F): V
  def invokeFun(fun: V, args: Seq[A])(invoke: (F, Seq[A]) => R): R
}

given ConcreteFunctionOps[F, A, R]: FunctionOps[F, A, R, F] with
  def funValue(fun: F): F = 
    fun
  def invokeFun(fun: F, args: Seq[A])(invoke: (F, Seq[A]) => R): R = 
    invoke(fun, args)

given PowersetFunctionOps[F, A, R, V]
  (using ops: FunctionOps[F, A, R, V], j: JoinComputation)
  (using JoinValue[R]): FunctionOps[F, A, R, Powerset[V]] with
  def funValue(fun: F): Powerset[V] = Powerset(ops.funValue(fun))
  def invokeFun(funs: Powerset[V], args: Seq[A])(invoke: (F, Seq[A]) => R): R =
    j.joinComputations(funs.set.iterator.map(fun => () => ops.invokeFun(fun, args)(invoke)))
