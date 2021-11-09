package sturdy.language.wasm.generic

import sturdy.data.Option
import sturdy.values.booleans.BooleanBranching
import swam.{FuncType, GlobalIdx}
import sturdy.values.convert.*
import sturdy.values.exceptions.Exceptional
import sturdy.values.floating.*
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.*
import sturdy.values.relational.CompareOps
import sturdy.values.relational.EqOps
import sturdy.values.relational.UnsignedCompareOps
import swam.syntax.LoadInst
import swam.syntax.LoadNInst
import swam.syntax.MemoryInst
import swam.syntax.StoreInst
import swam.syntax.StoreNInst

trait WasmOps[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, MayJoin[_]]:
  val intOps: IntegerOps[Int, V]
  val longOps: IntegerOps[Long, V]
  val floatOps: FloatingOps[Float, V]
  val doubleOps: FloatingOps[Double, V]
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
  val functionOps: FunctionOps[FunctionInstance, Nothing, Unit, FunV]
  val encode: Convert[V, Seq[Byte], V, Bytes, SomeCC[StoreInst | StoreNInst]]
  val decode: Convert[Seq[Byte], V, Bytes, V, SomeCC[LoadInst | LoadNInst]]
  val exceptOps: Exceptional[WasmException[V], ExcV, MayJoin]
  val specialOps: SpecialWasmOperations[V, Addr, Size, FuncIx, FunV, MayJoin]
  val branchOps: BooleanBranching[V, MayJoin]

/** Operations specific to Wasm */
trait SpecialWasmOperations[V, Addr, Size, FuncIx, FunV, MayJoin[_]]:
  def valueToAddr(v: V): Addr
  def valueToFuncIx(v: V): FuncIx
  
  def valToSize(v: V): Size
  def sizeToVal(sz: Size): V

  def indexLookup[A](ix: V, vec: Vector[A]): Option[MayJoin, A]

  def invokeHostFunction(hostFunc: HostFunction, args: List[V]): List[V]
