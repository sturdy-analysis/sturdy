package sturdy.values.functions

import sturdy.data.{mapJoin, MakeJoined}
import sturdy.values.Powerset
import sturdy.effect.EffectStack
import sturdy.values.Join

given PowersetFunctionOps[F, A, R, V]
  (using ops: FunctionOps[F, A, R, V])
  (using EffectStack, Join[R]): FunctionOps[F, A, R, Powerset[V]] with
  def funValue(fun: F): Powerset[V] = Powerset(ops.funValue(fun))
  def invokeFun(funs: Powerset[V], a: A)(invoke: (F, A) => R): R =
    mapJoin(funs.set, fun => ops.invokeFun(fun, a)(invoke))
