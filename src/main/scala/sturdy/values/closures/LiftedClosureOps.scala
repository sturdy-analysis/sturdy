package sturdy.values.closures

import sturdy.effect.environment.ClosableEnvironment


class LiftedClosureOps[Var, Arg, Body, Env, R, V, UV](extract: V => UV, inject: UV => V)(using ops: ClosureOps[Var, Arg, Body, Env, R, UV])
    extends ClosureOps[Var, Arg, Body, Env, R, V]:
  def closureValue(params: List[Var], body: Body, env: Env): V = inject(ops.closureValue(params, body, env))
  def invokeClosure(closure: V, args: List[Arg])(invoke: (List[Var], Body, List[Arg], Env) => R): R =
    ops.invokeClosure(extract(closure), args)(invoke)