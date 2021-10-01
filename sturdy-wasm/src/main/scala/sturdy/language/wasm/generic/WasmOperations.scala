package sturdy.language.wasm.generic

import sturdy.data.Option
import swam.GlobalIdx

/** Operations specific to Wasm */
trait WasmOperations[V, Addr, Size, FuncIx, FunV, Symbol, Entry]:
  type WasmOpsJoin[A]

  def valueToAddr(v: V): Addr
  def valueToFuncIx(v: V): FuncIx
  
  def valToSize(v: V): Size
  def sizeToVal(sz: Size): V

  def funcIxToSymbol(funcIx: FuncIx): Symbol
  def globIxToSymbol(globalIdx: GlobalAddr): Symbol

  def funVToEntry(funV: FunV): Entry
  def globIToEntry(globI: GlobalInstance[V]): Entry
  def entryToFuncV(entry: Entry): FunV
  def entryToGlobI(entry: Entry): GlobalInstance[V]
  
  def indexLookup[A](ix: V, vec: Vector[A]): Option[WasmOpsJoin, A]
