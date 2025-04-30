package sturdy.language.wasm.generic

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.language.wasm.ConcreteInterpreter.FuncReference
import sturdy.language.wasm.ConcreteInterpreter.ExternReference
import sturdy.language.wasm.abstractions.CfgNode.Instruction
import sturdy.values.booleans.BooleanBranching
import swam.{FuncType, GlobalIdx, ReferenceType, TableIdx}
import sturdy.values.convert.*
import sturdy.values.exceptions.Exceptional
import sturdy.values.floating.*
import sturdy.values.functions.FunctionOps
import sturdy.values.references.{ReferenceOps, given}
import sturdy.values.integer.*
import sturdy.values.ordering.OrderingOps
import sturdy.values.ordering.EqOps
import sturdy.values.ordering.UnsignedOrderingOps
import swam.syntax.{Inst, LoadInst, LoadNInst, MemoryInst, ReferenceInst, StoreInst, StoreNInst}


trait WasmOps[V, Addr, Bytes, Size, ExcV, Index, FunV, J[_] <: MayJoin[_]]:
  val i32ops: IntegerOps[Int, V]
  val i64ops: IntegerOps[Long, V]
  val f32ops: FloatOps[Float, V]
  val f64ops: FloatOps[Double, V]
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
  val decode: Convert[Seq[Byte], V, Bytes, V, SomeCC[LoadInst | LoadNInst]]
  val exceptOps: Exceptional[WasmException[V], ExcV, J]
  val specialOps: SpecialWasmOperations[V, Addr, Size, Index, FunV, J]
  val branchOpsV: BooleanBranching[V, V]
  val branchOpsUnit: BooleanBranching[V, Unit]

/** Operations specific to Wasm */
trait SpecialWasmOperations[V, Addr, Size, Index, FunV, J[_] <: MayJoin[_]]:
  def valToAddr(v: V): Addr
  def valToIdx(v: V): Index
  
  def valToSize(v: V): Size
  def sizeToVal(sz: Size): V
  def intToVal(i: Int): V
  def instToVal(i: Inst): V
  def valToInt(v: V): Int
  def numToRef(v: V): V
  def funcRefToInt(r: V): Int

  def makeNullRef(t: ReferenceType): V
  def makeRef(f: FunctionInstance): V
  def makeRef(i: FunV): V
  def makeExternRef(f: Int): V
  def isNull(r: V): V

  def funcInstToFunV(f: FunctionInstance): FunV
  def validateTableElem(tabSz: Int, e: Int): Boolean


  def indexLookup[A](ix: V, vec: Vector[A]): JOption[J, A]
  def invokeHostFunction(hostFunc: HostFunction, args: List[V]): List[V]
