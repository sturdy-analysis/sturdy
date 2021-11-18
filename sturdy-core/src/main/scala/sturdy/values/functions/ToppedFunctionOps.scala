package sturdy.values.functions

import sturdy.values.Topped
import sturdy.effect.Effectful
import sturdy.values.Top

given ToppedFunctionOps[F, A, R, V]
  (using ops: FunctionOps[F, A, R, V], topOps: FunctionOps[F, A, R, Unit]): FunctionOps[F, A, R, Topped[V]] with
  def funValue(fun: F): Topped[V] = Topped.Actual(ops.funValue(fun))
  def invokeFun(funV: Topped[V], a: A)(invoke: (F, A) => R): R = funV match
    case Topped.Actual(fun) => ops.invokeFun(fun, a)(invoke)
    case Topped.Top => topOps.invokeFun((), a)(invoke)
