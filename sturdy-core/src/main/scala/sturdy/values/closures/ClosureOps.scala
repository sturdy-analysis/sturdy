package sturdy.values.closures

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{Join, Powerset, Structural, Topped}
import sturdy.values.functions.FunctionOps

import scala.util.Random

trait ClosureOps[Vars, Body, Env, R, V] {
  def closureValue(params: Vars, body: Body, env: Env): V
  def invokeClosure(closure: V)(invoke: (Vars, Body, Env) => R): R
}

case class Closure[Vars, Body, Env](params: Vars, body: Body, env: Env)

given StructuralClosure[Vars, Body, Env]: Structural[Closure[Vars, Body, Env]] with {}

given ConcreteClosureOps[Vars, Body, Env, V]: ClosureOps[Vars, Body, Env, V, Closure[Vars, Body, Env]] with
  override def closureValue(params: Vars, body: Body, env: Env): Closure[Vars, Body, Env] = Closure(params, body, env)
  override def invokeClosure(closure: Closure[Vars, Body, Env])(invoke: (Vars, Body, Env) => V): V =
    invoke(closure.params, closure.body, closure.env)

given PowersetClosureOps[Vars, Body, Env, R]
  (using ops: ClosureOps[Vars, Body, Env, R, Closure[Vars, Body, Env]])
  (using EffectStack, Join[R]): 
  ClosureOps[Vars, Body, Env, R, Powerset[Closure[Vars, Body, Env]]] with
  
  override def closureValue(params: Vars, body: Body, env: Env): Powerset[Closure[Vars, Body, Env]] =
    Powerset(ops.closureValue(params, body, env))

  override def invokeClosure(closure: Powerset[Closure[Vars, Body, Env]])
                            (invoke: (Vars, Body, Env) => R): R =
    closure.foldJoin(c => invoke(c.params, c.body, c.env))
    
  
    
  