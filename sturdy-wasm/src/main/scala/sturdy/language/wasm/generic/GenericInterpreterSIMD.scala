package sturdy.language.wasm.generic

import sturdy.data.MayJoin
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import swam.syntax.{Inst, LoadVector, LoadVectorLane, LoadVectorSplat, LoadVectorZero, StoreVector, StoreVectorLane, VVectorBinop, VVectorTestop, VVectorUnop, VectorBinop, VectorBitmask, VectorConvertop, VectorDot, VectorExtadd, VectorExtmul, VectorExtractLane, VectorInst, VectorNarrow, VectorRelop, VectorReplaceLane, VectorShiftop, VectorSplat, VectorTernop, VectorTestop, VectorUnop, i8x16, v128}

class GenericInterpreterSIMD [V, J[_] <: MayJoin[_]]
  (stack: DecidableOperandStack[V], wasmOps: WasmOps[V, _, _, _, _, _, _, _, J])
  (using Failure):
  
  import wasmOps.*
  
  def evalSIMD(inst: Inst): V = {
    inst match {
      case unop: VectorUnop => ???
      case binop: VectorBinop => ???
      case testop: VectorTestop => ???
      case relop: VectorRelop => ???
      case convertop: VectorConvertop => ???
      case ternop: VectorTernop => ???
      case shiftop: VectorShiftop => ???
      case splat: VectorSplat => ???
      case narrow: VectorNarrow => ???
      case bitmask: VectorBitmask => ???
      case dot: VectorDot => ???
      case extmul: VectorExtmul => ???
      case extadd: VectorExtadd => ???
      case lane: VectorExtractLane => ???
      case lane: VectorReplaceLane => ???
      case unop: VVectorUnop => ???
      case binop: VVectorBinop => ???
      case testop: VVectorTestop => ???
      case vector: LoadVector => ???
      case splat: LoadVectorSplat => ???
      case zero: LoadVectorZero => ???
      case lane: LoadVectorLane => ???
      case vector: StoreVector => ???
      case lane: StoreVectorLane => ???
      case v128.Load(align, offset) => ???
      case v128.Const(bytes) => v128ops.vectorLit(bytes)
      case i8x16.Shuffle(lanes) => ???
      case i8x16.Swizzle => ???
    }
  }
  
  def defaultValue(): V = {
    evalSIMD(v128.Const(Array.fill(16)(0.toByte)))
  }