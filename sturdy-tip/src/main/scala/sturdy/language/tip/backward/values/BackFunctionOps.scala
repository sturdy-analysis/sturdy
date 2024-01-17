package sturdy.language.tip.backward.values

import sturdy.data.{mapJoin, JoinTuple2, MakeJoined}
import sturdy.effect.EffectStack
import sturdy.values.{Join, Powerset}

trait BackFunctionOps[Fun, A, R, FunV]:
  def funValue(fun: Fun): FunV
  def invokeFunBack(fun: FunV, ret: R)(invokeBack: (Fun, R) => (A,R)): (A,R)

class LiftedBackFunctionOps[F, A, R, V, UV](extract: V => UV, inject: UV => V)(using ops: BackFunctionOps[F, A, R, UV]) extends BackFunctionOps[F, A, R, V]:
  def funValue(fun: F): V =
    inject(ops.funValue(fun))
  def invokeFunBack(fun: V, ret: R)(invoke: (F, R) => (A,R)): (A,R) =
    ops.invokeFunBack(extract(fun), ret)(invoke)

given ConcreteBackFunctionOps[F, A, R]: BackFunctionOps[F, A, R, F] with
  def funValue(fun: F): F =
    fun
  def invokeFunBack(fun: F, ret: R)(invoke: (F, R) => (A,R)): (A,R) =
    invoke(fun, ret)

given PowersetBackFunctionOps[F, A, R, V]
  (using ops: BackFunctionOps[F, A, R, V])
  (using EffectStack, Join[A], Join[R]): BackFunctionOps[F, A, R, Powerset[V]] with
  def funValue(fun: F): Powerset[V] = Powerset(ops.funValue(fun))
  def invokeFunBack(funs: Powerset[V], ret: R)(invoke: (F, R) => (A,R)): (A,R) =
    mapJoin(funs.set, fun => ops.invokeFunBack(fun, ret)(invoke))
