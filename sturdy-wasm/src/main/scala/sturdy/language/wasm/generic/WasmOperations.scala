package sturdy.language.wasm.generic

import sturdy.data.{JOption, MayJoin}
import sturdy.values.booleans.BooleanBranching
import sturdy.values.convert.*
import sturdy.values.exceptions.Exceptional
import sturdy.values.floating.*
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.*
import sturdy.values.ordering.{EqOps, OrderingOps, UnsignedOrderingOps}
import sturdy.values.simd.SIMDOps
import swam.syntax.*
import swam.{FuncType, ReferenceType}


trait WasmOps[V, Addr, Bytes, Size, ExcV, Index, FunV, RefV, J[_] <: MayJoin[_]]:
  val i32ops: IntegerOps[Int, V]
  val i64ops: IntegerOps[Long, V]
  val f32ops: FloatOps[Float, V]
  val f64ops: FloatOps[Double, V]
  val v128ops: SIMDOps[Array[Byte], V, V, Byte]
  val eqOps: EqOps[V, V]
  val compareOps: OrderingOps[V, V]
  val unsignedCompareOps: UnsignedOrderingOps[V, V]
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
  val decode: Convert[Seq[Byte], V, Bytes, V, SomeCC[LoadInst | LoadNInst | VectorLoadInst]]
  val exceptOps: Exceptional[WasmException[V], ExcV, J]
  val specialOps: SpecialWasmOperations[V, Addr, Bytes, Size, Index, FunV, RefV, J]
  val branchOpsV: BooleanBranching[V, V]
  val branchOpsUnit: BooleanBranching[V, Unit]

/** Operations specific to Wasm */
trait SpecialWasmOperations[V, Addr, Bytes, Size, Index, FunV, RefV, J[_] <: MayJoin[_]]:
  def valToAddr(v: V): Addr
  def valToIdx(v: V): Index
  def valToRef(v: V, funcs: Vector[FunctionInstance]): RefV
  def refToVal(r: RefV): V
  def liftBytes(b: Seq[Byte]): Bytes
  
  def valToSize(v: V): Size

  /**
   * Convert a Size to a value.
   * @param sz the size to convert
   * @return sz wrapped in a i32 value
   */
  def sizeToVal(sz: Size): V
  def refVToFunV(r: RefV): FunV

  def makeNullRefV(t: ReferenceType): RefV
  def funVToRefV(i: FunV): RefV

  /**
   * Check if a reference is null.
   * @param r the reference to check
   * @return Boolean WASM value indicating if the reference is null (0=false for non-null, 1=true for null)
   */
  def isNullRef(r: V): V

  def funcInstToFunV(f: FunctionInstance): FunV
  def funVToFuncInst(f: FunV): FunctionInstance

  def indexLookup[A](ix: V, vec: Vector[A]): JOption[J, A]
  def invokeHostFunction(hostFunc: HostFunction, args: List[V]): List[V]
