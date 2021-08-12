package sturdy.language.wasm.generic

import sturdy.effect.MayCompute

/** Operations specific to Wasm */
trait WasmOperations[V]:
  type WasmOpsJoin[A]
  type WasmOpsJoinComp

  def indexLookup[A](ix: V, list: Vector[A]): MayCompute[A, WasmOpsJoin, WasmOpsJoinComp]
