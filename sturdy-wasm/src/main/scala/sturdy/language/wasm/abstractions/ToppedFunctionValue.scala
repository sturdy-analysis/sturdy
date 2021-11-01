package sturdy.language.wasm.abstractions

import sturdy.data.{CombineUnit, mapJoin, MakeJoined}
import sturdy.effect.Effectful
import sturdy.language.wasm.generic.{GenericInterpreter, GenericEffects, FunctionInstance}
import sturdy.values.Topped
import sturdy.values.functions.FunctionOps

trait ToppedFunctionValue:
  given ToppedFunctionOps[V,FunV]
    (using interp: GenericInterpreter[V,_,_,_,_,_,_,_,_])(using Effectful)
    (using ops: FunctionOps[FunctionInstance, Nothing, Unit, FunV]): FunctionOps[FunctionInstance, Nothing, Unit, Topped[FunV]] with
    def funValue(fun: FunctionInstance): Topped[FunV] = Topped.Actual(ops.funValue(fun))
    def invokeFun(funV: Topped[FunV], args: Seq[Nothing])(invoke: (FunctionInstance, Seq[Nothing]) => Unit): Unit = funV match
      case Topped.Actual(fun) => ops.invokeFun(fun, args)(invoke)
      case Topped.Top => mapJoin(interp.module.functions, fun => invoke(fun, args))


