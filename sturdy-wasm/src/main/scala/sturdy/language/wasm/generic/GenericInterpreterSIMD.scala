package sturdy.language.wasm.generic

import sturdy.data.{MayJoin, noJoin}
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.simd.LaneShape
import swam.syntax.*

import scala.reflect.ClassTag


class GenericInterpreterSIMD [V, J[_] <: MayJoin[_]]
  (stack: DecidableOperandStack[V], wasmOps: WasmOps[V, _, _, _, _, _, _, _, J])
  (using Failure):
  
  import wasmOps.*
  
  def evalSIMD(inst: Inst): V = {
    inst match {
      case unop: VectorUnop =>
        val v = stack.popOrAbort()
        evalSIMDUnop(unop, v)
      case binop: VectorBinop =>
        val (v1, v2) = stack.pop2OrAbort()
        evalSIMDBinop(binop, v1, v2)
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
      case _ => ???
    }
  }

  inline def evalSIMDUnop(op: VectorUnop, v: V): V = {
    op match {
      case unop: IVectorUnop =>
        unop.operation match {
          case VecUnopType.IAbs => v128ops.vectorAbs(getLaneShape(unop.shape), v)
          case VecUnopType.INeg => v128ops.vectorNeg(getLaneShape(unop.shape), v)
          case VecUnopType.IPopCnt => v128ops.vectorPopCount(getLaneShape(unop.shape), v)
        }
      case unop: FVectorUnop =>
        unop.operation match {
          case VecUnopType.FAbs => v128ops.vectorAbs(getLaneShape(unop.shape), v)
          case VecUnopType.FNeg => v128ops.vectorNeg(getLaneShape(unop.shape), v)
          case VecUnopType.FSqrt => v128ops.vectorSqrt(getLaneShape(unop.shape), v)
          case VecUnopType.FCeil => v128ops.vectorCeil(getLaneShape(unop.shape), v)
          case VecUnopType.FFloor => v128ops.vectorFloor(getLaneShape(unop.shape), v)
          case VecUnopType.FTrunc => v128ops.vectorTrunc(getLaneShape(unop.shape), v)
          case VecUnopType.FNearest => v128ops.vectorNearest(getLaneShape(unop.shape), v)
        }
      case i8x16.Popcnt => v128ops.vectorPopCount(LaneShape.I8, v)
    }
  }

  inline def evalSIMDBinop(op: VectorBinop, v1: V, v2: V): V = {
    op match {
      case minMaxop: VectorMinMaxop =>
        minMaxop.operation match {
          case VecBinopType.IMinU => v128ops.vectorMinU(getLaneShape(minMaxop.shape), v1, v2)
          case VecBinopType.IMinS => v128ops.vectorMinS(getLaneShape(minMaxop.shape), v1, v2)
          case VecBinopType.IMaxU => v128ops.vectorMaxU(getLaneShape(minMaxop.shape), v1, v2)
          case VecBinopType.IMaxS => v128ops.vectorMaxS(getLaneShape(minMaxop.shape), v1, v2)
        }
      case binop: VectorSatBinop =>
        binop.operation match {
          case VecBinopType.IAddSatU => v128ops.vectorAddSatU(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.IAddSatS => v128ops.vectorAddSatS(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.ISubSatU => v128ops.vectorSubSatU(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.ISubSatS => v128ops.vectorSubSatS(getLaneShape(binop.shape), v1, v2)
        }
      case binop: IVectorBinop =>
        binop.operation match {
          case VecBinopType.IAdd => v128ops.vectorAdd(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.ISub => v128ops.vectorSub(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.IMul => v128ops.vectorMul(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.IAvrgU => v128ops.vectorAvrgU(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.IQ15MulrSatS => v128ops.vectorQ15MulrSatS(getLaneShape(binop.shape), v1, v2)
        }
      case binop: FVectorBinop =>
        binop.operation match {
          case VecBinopType.FAdd => v128ops.vectorAdd(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.FSub => v128ops.vectorSub(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.FMul => v128ops.vectorMul(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.FDiv => v128ops.vectorDiv(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.FMin => v128ops.vectorMin(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.FMax => v128ops.vectorMax(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.FPMin => v128ops.vectorPMin(getLaneShape(binop.shape), v1, v2)
          case VecBinopType.FPMax => v128ops.vectorPMax(getLaneShape(binop.shape), v1, v2)
        }
    }
  }
  
  def defaultValue(): V = {
    evalSIMD(v128.Const(Array.fill(16)(0.toByte)))
  }
  
  private def getLaneShape(shape: VectorShape): LaneShape = shape match {
    case VectorIShape.i8x16 => LaneShape.I8
    case VectorIShape.i16x8 => LaneShape.I16
    case VectorIShape.i32x4 => LaneShape.I32
    case VectorIShape.i64x2 => LaneShape.I64
    case VectorFShape.f32x4 => LaneShape.F32
    case VectorFShape.f64x2 => LaneShape.F64
  }
