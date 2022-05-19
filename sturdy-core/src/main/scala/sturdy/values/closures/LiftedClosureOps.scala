package sturdy.values.closures


class LiftedClosureOps[Var, Arg, Body, Env, R, V, UV](extract: V => UV, inject: UV => V)(using ops: ClosureOps[Var, Arg, Body, Env, R, UV])
    extends ClosureOps[Var, Arg, Body, Env, R, V]:
  def closureValue(params: List[Var], body: Body, env: Env): V = inject(ops.closureValue(params, body, env))
  def invokeClosure(closure: V)(invoke: (List[Var], Body, Env) => R): R =
    ops.invokeClosure(extract(closure))(invoke)