package sturdy.values.closures


class LiftedClosureOps[Vars, Body, Env, R, V, UV](extract: V => UV, inject: UV => V)(using ops: ClosureOps[Vars, Body, Env, R, UV])
    extends ClosureOps[Vars, Body, Env, R, V]:
  def closureValue(params: Vars, body: Body, env: Env): V = inject(ops.closureValue(params, body, env))
  def invokeClosure(closure: V)(invoke: (Vars, Body, Env) => R): R =
    ops.invokeClosure(extract(closure))(invoke)