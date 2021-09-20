package sturdy.values.functions

import sturdy.values.Topped
import sturdy.effect.Effectful
import sturdy.values.Top

given ToppedFunctionOps[F, A, R, V]
  (using fallback: (Seq[A], (F, Seq[A]) => R) => R)
  (using ops: FunctionOps[F, A, R, V]): FunctionOps[F, A, R, Topped[V]] with
  def funValue(fun: F): Topped[V] = Topped.Actual(ops.funValue(fun))
  def invokeFun(funV: Topped[V], args: Seq[A])(invoke: (F, Seq[A]) => R): R = funV match
    case Topped.Actual(fun) => ops.invokeFun(fun, args)(invoke)
    case Topped.Top => fallback(args, invoke)
