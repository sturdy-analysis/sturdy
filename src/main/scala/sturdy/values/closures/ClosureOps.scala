package sturdy.values.closures

import sturdy.effect.failure.Failure
import sturdy.effect.environment.CClosableEnvironment
import sturdy.effect.environment.ClosableEnvironment

import scala.util.Random

trait ClosureOps[Var, Addr, Env, Body, Arg, invV, clsV](using ClosableEnvironment[Var, Addr, Env]) {
  def closureValue(params: List[Var], body: Body): clsV
  def invokeClosure(closure: clsV, args: List[Arg])
                   (invoke: (List[Var], Body, List[Arg]) => invV): invV
}

given ConcreteClosureOps[Var, Addr, Env, Body, Arg, invV](using env: ClosableEnvironment[Var, Addr, Env]):
    ClosureOps[Var, Addr, Env, Body, Arg, invV, (List[Var], Env, Body)] with
  override def closureValue(params: List[Var], body: Body): (List[Var], Env, Body) =
    val funEnv = env.getEnv
    (params, funEnv, body)
  override def invokeClosure(closure: (List[Var], Env, Body), args: List[Arg])
                   (invoke: (List[Var], Body, List[Arg]) => invV): invV =
    val (params, funEnv, body) = closure
    env.scoped {
      env.setEnv(funEnv)
      invoke(params, body, args)
    }
