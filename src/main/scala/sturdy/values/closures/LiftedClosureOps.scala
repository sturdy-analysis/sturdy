package sturdy.values.closures

import sturdy.effect.environment.ClosableEnvironment

class LiftedClosureOps[Var, Addr, Env, Cls, Body, Arg, V](extract: V => Cls, inject: Cls => V)
    (using ops: ClosureOps[Var, Addr, Env, Body, Arg, V, Cls])
    (using ClosableEnvironment[Var, Addr, Env])
    extends ClosureOps[Var, Addr, Env, Body, Arg, V, V]:
  def closureValue(params: List[Var], body: Body): V =
    inject(ops.closureValue(params, body))
  def invokeClosure(closure: V, args: List[Arg])(invoke: (List[Var], Body, List[Arg]) => V): V =
    ops.invokeClosure(extract(closure), args)(invoke)
