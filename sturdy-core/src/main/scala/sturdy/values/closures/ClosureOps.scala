package sturdy.values.closures

import sturdy.effect.failure.Failure
import sturdy.values.functions.FunctionOps

import scala.util.Random

trait ClosureOps[Vars, Body, Env, R, V] {
  def closureValue(params: Vars, body: Body, env: Env): V
  def invokeClosure(closure: V)(invoke: (Vars, Body, Env) => R): R
}

case class Closure[Vars, Body, Env](params: Vars, body: Body, env: Env)

given ConcreteClosureOps[Vars, Body, Env, V]: ClosureOps[Vars, Body, Env, V, Closure[Vars, Body, Env]] with
  override def closureValue(params: Vars, body: Body, env: Env): Closure[Vars, Body, Env] = Closure(params, body, env)
  override def invokeClosure(closure: Closure[Vars, Body, Env])(invoke: (Vars, Body, Env) => V): V =
    invoke(closure.params, closure.body, closure.env)
