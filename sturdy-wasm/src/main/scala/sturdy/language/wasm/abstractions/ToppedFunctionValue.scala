package sturdy.language.wasm.abstractions

import sturdy.data.CombineUnit
import sturdy.effect.Effectful
import sturdy.language.wasm.generic.{GenericInterpreter, GenericEffects, FunctionInstance}
import sturdy.values.Topped
import sturdy.values.functions.FunctionOps

trait ToppedFunctionValue:
  given ToppedFunctionOps[V,FunV]
    (using effect: Effectful, interp: GenericInterpreter[V,_,_,_,_,_,_,_,_,_,_])
    (using ops: FunctionOps[FunctionInstance[V], Nothing, Unit, FunV]): FunctionOps[FunctionInstance[V], Nothing, Unit, Topped[FunV]] with
    def funValue(fun: FunctionInstance[V]): Topped[FunV] = Topped.Actual(ops.funValue(fun))
    def invokeFun(funV: Topped[FunV], args: Seq[Nothing])(invoke: (FunctionInstance[V], Seq[Nothing]) => Unit): Unit = funV match
      case Topped.Actual(fun) => ops.invokeFun(fun, args)(invoke)
      case Topped.Top =>
        val invokeAllFuns = interp.module.functions.map(fun => () => invoke(fun, args))
        effect.joinComputationsIterable(invokeAllFuns)


