package sturdy.values.closures


class LiftedClosureOps[Vars, Body, Env, Val, Cls, UCls](extract: Cls => UCls, inject: UCls => Cls)(using ops: ClosureOps[Vars, Body, Env, Val, UCls])
    extends ClosureOps[Vars, Body, Env, Val, Cls]:
  def closureValue(params: Vars, body: Body, env: Env): Cls = inject(ops.closureValue(params, body, env))
  def invokeClosure(closure: Cls, args: Val)(invoke: Body => Val): Val =
    ops.invokeClosure(extract(closure), args)(invoke)