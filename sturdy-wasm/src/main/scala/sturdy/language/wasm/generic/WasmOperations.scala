package sturdy.language.wasm.generic

import sturdy.data.Option
import sturdy.values.doubles.*
import sturdy.values.floats.*
import sturdy.values.functions.FunctionOps
import sturdy.values.ints.*
import sturdy.values.longs.*
import sturdy.values.relational.CompareOps
import sturdy.values.relational.EqOps
import sturdy.values.relational.UnsignedCompareOps
import swam.GlobalIdx

trait WasmOps[V, FunV]:
  val intOps: IntOps[V]
  val longOps: LongOps[V]
  val floatOps: FloatOps[V]
  val doubleOps: DoubleOps[V]
  val eqOps: EqOps[V, V]
  val compareOps: CompareOps[V, V]
  val unsignedCompareOps: UnsignedCompareOps[V, V]
  val convertIntLong: ConvertIntLong[V, V]
  val convertIntFloat: ConvertIntFloat[V, V]
  val convertIntDouble: ConvertIntDouble[V, V]
  val convertLongInt: ConvertLongInt[V, V]
  val convertLongFloat: ConvertLongFloat[V, V]
  val convertLongDouble: ConvertLongDouble[V, V]
  val convertFloatInt: ConvertFloatInt[V, V]
  val convertFloatLong: ConvertFloatLong[V, V]
  val convertFloatDouble: ConvertFloatDouble[V, V]
  val convertDoubleInt: ConvertDoubleInt[V, V]
  val convertDoubleLong: ConvertDoubleLong[V, V]
  val convertDoubleFloat: ConvertDoubleFloat[V, V]
  val functionOps: FunctionOps[FunctionInstance[V], Nothing, Unit, FunV]

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
