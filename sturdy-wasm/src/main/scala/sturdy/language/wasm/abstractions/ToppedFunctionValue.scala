package sturdy.language.wasm.abstractions

import sturdy.data.{CombineUnit, MakeJoined, mapJoin, joinWithFailure}
import sturdy.effect.Effectful
import sturdy.effect.failure.Failure
import sturdy.language.wasm.generic.IndirectCallTypeMismatch
import sturdy.language.wasm.generic.{FunctionInstance, GenericInterpreter, GenericEffects}
import sturdy.values.Topped
import sturdy.values.functions.{ToppedFunctionOps, FunctionOps}
import swam.FuncType

trait ToppedFunctionValue:
  given WasmToppedFunctionOps[V,FunV]
    (using ops: FunctionOps[FunctionInstance, FuncType, Unit, FunV])
    (using interp: GenericInterpreter[V,_,_,_,_,_,_,_,_])
    (using Effectful, Failure): FunctionOps[FunctionInstance, FuncType, Unit, Topped[FunV]] =
    // (A, (F, A) => R) => R
    ToppedFunctionOps(using (ft, invoke) => {
      val funs = interp.module.functions.filter(_.funcType == ft)
      if (funs.isEmpty)
        Failure(IndirectCallTypeMismatch, s"Expected function of type $ft, but none found")
      else if (funs.size == interp.module.functions.size)
        mapJoin(funs, invoke(_, ft))
      else
        joinWithFailure(mapJoin(funs, invoke(_, ft)))(
          Failure(IndirectCallTypeMismatch, s"Expected function of type $ft, which fails for some potential target functions"))
    })


