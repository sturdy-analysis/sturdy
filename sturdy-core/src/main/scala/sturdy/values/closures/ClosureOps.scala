package sturdy.values.closures

import sturdy.effect.failure.Failure
import sturdy.values.functions.FunctionOps

import scala.util.Random

trait ClosureOps[Var, Arg, Body, Env, R, V] {
  def closureValue(params: List[Var], body: Body, env: Env): V
  def invokeClosure(closure: V)(invoke: (List[Var], Body, Env) => R): R
}

case class Closure[Var, Body, Env](params: List[Var], body: Body, env: Env)

given ConcreteClosureOps[Var, Arg, Body, Env, V]: ClosureOps[Var, Arg, Body, Env, V, Closure[Var, Body, Env]] with
  override def closureValue(params: List[Var], body: Body, env: Env): Closure[Var, Body, Env] = Closure(params, body, env)
  override def invokeClosure(closure: Closure[Var, Body, Env])(invoke: (List[Var], Body, Env) => V): V =
    invoke(closure.params, closure.body, closure.env)
