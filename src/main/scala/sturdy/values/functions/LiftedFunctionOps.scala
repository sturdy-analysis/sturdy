package sturdy.values.functions

import sturdy.effect.failure.Failure

class LiftedFunctionOps[F, A, R, V, UV](extract: V => UV, inject: UV => V)(using ops: FunctionOps[F, A, R, UV]) extends FunctionOps[F, A, R, V]:
  def funValue(fun: F): V =
    inject(ops.funValue(fun))
  def invokeFun(fun: V, args: Seq[A])(invoke: (F, Seq[A]) => R): R =
    ops.invokeFun(extract(fun), args)(invoke)
