package sturdy.language.wasm.generic

import sturdy.data.Option
import sturdy.values.booleans.BooleanBranching
import swam.{FuncType, GlobalIdx}
import sturdy.values.convert.*
import sturdy.values.exceptions.Exceptional
import sturdy.values.floating.*
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.*
import sturdy.values.relational.OrderingOps
import sturdy.values.relational.EqOps
import sturdy.values.relational.UnsignedCompareOps
import swam.syntax.LoadInst
import swam.syntax.LoadNInst
import swam.syntax.MemoryInst
import swam.syntax.StoreInst
import swam.syntax.StoreNInst

trait WasmOps[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, MayJoin[_]]:
  val i32ops: IntegerOps[Int, V]
  val i64ops: IntegerOps[Long, V]
  val f32ops: FloatOps[Float, V]
  val f64ops: FloatOps[Double, V]
  val eqOps: EqOps[V, V]
  val compareOps: OrderingOps[V, V]
  val unsignedCompareOps: UnsignedCompareOps[V, V]
  val convert_i32_i64: ConvertIntLong[V, V]
  val convert_i32_f32: ConvertIntFloat[V, V]
  val convert_i32_f64: ConvertIntDouble[V, V]
  val convert_i64_i32: ConvertLongInt[V, V]
  val convert_i64_f32: ConvertLongFloat[V, V]
  val convert_i64_f64: ConvertLongDouble[V, V]
  val convert_f32_i32: ConvertFloatInt[V, V]
  val convert_f32_i64: ConvertFloatLong[V, V]
  val convert_f32_f64: ConvertFloatDouble[V, V]
  val convert_f64_i32: ConvertDoubleInt[V, V]
  val convert_f64_i64: ConvertDoubleLong[V, V]
  val convert_f64_f32: ConvertDoubleFloat[V, V]
  val functionOps: FunctionOps[FunctionInstance, FuncType, Unit, FunV]
  val encode: Convert[V, Seq[Byte], V, Bytes, SomeCC[StoreInst | StoreNInst]]
  val decode: Convert[Seq[Byte], V, Bytes, V, SomeCC[LoadInst | LoadNInst]]
  val exceptOps: Exceptional[WasmException[V], ExcV, MayJoin]
  val specialOps: SpecialWasmOperations[V, Addr, Size, FuncIx, MayJoin]
  val branchOpsV: BooleanBranching[V, V]
  val branchOpsUnit: BooleanBranching[V, Unit]

/** Operations specific to Wasm */
trait SpecialWasmOperations[V, Addr, Size, FuncIx, MayJoin[_]]:
  def valueToAddr(v: V): Addr
  def valueToFuncIx(v: V): FuncIx
  
  def valToSize(v: V): Size
  def sizeToVal(sz: Size): V

  def indexLookup[A](ix: V, vec: Vector[A]): Option[MayJoin, A]

  def invokeHostFunction(hostFunc: HostFunction, args: List[V]): List[V]
