package sturdy.language.wasm.generic

import sturdy.effect.MayCompute

/** Operations specific to Wasm */
trait WasmOperations[V, Addr, Size]:
  type WasmOpsJoin[A]
  type WasmOpsJoinComp

  def valueToAddr(v: V): Addr
  def valToSize(v: V): Size
  def sizeToVal(sz: Size): V
  
  def indexLookup[A](ix: V, vec: Vector[A]): MayCompute[A, WasmOpsJoin, WasmOpsJoinComp]
