package sturdy.language.wasm.generic

import sturdy.data.Option

/** Operations specific to Wasm */
trait WasmOperations[V, Addr, Size, FuncIx]:
  type WasmOpsJoin[A]

  def valueToAddr(v: V): Addr
  def valueToFuncIx(v: V): FuncIx
  
  def valToSize(v: V): Size
  def sizeToVal(sz: Size): V
  
  def indexLookup[A](ix: V, vec: Vector[A]): Option[WasmOpsJoin, A]
