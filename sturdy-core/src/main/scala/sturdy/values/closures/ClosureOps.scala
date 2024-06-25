package sturdy.values.closures

import sturdy.data.*
import sturdy.effect.environment.ClosableEnvironment
import sturdy.effect.failure.Failure
import sturdy.values.functions.FunctionOps

import scala.util.Random

trait ClosureOps[Vars, Body, Env, Val, Cls] {
  def closureValue(params: Vars, body: Body, env: Env): Cls
  def invokeClosure(closure: Cls, argument: Val)(invoke: Body => Val): Val
}

case class Closure[Vars, Body, Env](params: Vars, body: Body, env: Env)

given ConcreteClosureOps[Vars, Body, Env, Val](using environment: ClosableEnvironment[Vars, Val, Env, NoJoin]): ClosureOps[Vars, Body, Env, Val, Closure[Vars, Body, Env]] with
  override def closureValue(params: Vars, body: Body, env: Env): Closure[Vars, Body, Env] = Closure(params, body, env)
  override def invokeClosure(closure: Closure[Vars, Body, Env], args: Val)(invoke: Body => Val): Val =
    environment.scoped {
      environment.loadClosedEnvironment(closure.env)
      environment.bind(closure.params, args)
      invoke(closure.body)
    }
