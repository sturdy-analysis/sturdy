package sturdy.values.closures

import sturdy.effect.failure.Failure
import sturdy.values.functions.FunctionOps

import scala.util.Random

trait ClosureOps[Var, Arg, Body, Env, R, V] {
  def closureValue(params: List[Var], body: Body, env: Env): V
  def invokeClosure(closure: V, args: List[Arg])(invoke: (List[Var], Body, List[Arg], Env) => R): R
}

given ConcreteClosureOps[Var, Arg, Body, Env, V]: ClosureOps[Var, Arg, Body, Env, V, (List[Var], Env, Body)] with
  override def closureValue(params: List[Var], body: Body, env: Env): (List[Var], Env, Body) = (params, env, body)
  override def invokeClosure(closure: (List[Var], Env, Body), args: List[Arg])(invoke: (List[Var], Body, List[Arg], Env) => V): V =
    val (params, env, body) = closure
    invoke(params, body, args, env)
