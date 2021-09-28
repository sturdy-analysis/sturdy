package sturdy.values.functions

import sturdy.values.Powerset
import sturdy.effect.Effectful
import sturdy.values.Join

given PowersetFunctionOps[F, A, R, V]
  (using ops: FunctionOps[F, A, R, V], j: Effectful)
  (using Join[R]): FunctionOps[F, A, R, Powerset[V]] with
  def funValue(fun: F): Powerset[V] = Powerset(ops.funValue(fun))
  def invokeFun(funs: Powerset[V], args: Seq[A])(invoke: (F, Seq[A]) => R): R =
    j.joinComputationsIterable(funs.set.map(fun => () => ops.invokeFun(fun, args)(invoke)))
