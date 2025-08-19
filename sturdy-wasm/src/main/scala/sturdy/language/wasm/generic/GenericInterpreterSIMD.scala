package sturdy.language.wasm.generic

import sturdy.data.{given, *}
import sturdy.effect.bytememory.Memory
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.convert.SomeCC
import sturdy.values.simd.LaneShape.*
import sturdy.values.simd.{Half, LaneShape, TruncMode}
import swam.syntax.*

import scala.reflect.ClassTag
import scala.util.boundary
import scala.util.boundary.break


class GenericInterpreterSIMD [V, Addr, Bytes, J[_] <: MayJoin[_]]
  (stack: DecidableOperandStack[V], mem: Memory[MemoryAddr, Addr, Bytes, _, J], wasmOps: WasmOps[V, Addr, Bytes, _, _, _, _, _, J])
  (using Failure):
  
  import wasmOps.*

  extension (shape: VectorShape)
    private def toLaneShape: LaneShape = shape match
      case VectorIShape.i8x16 => LaneShape.I8
      case VectorIShape.i16x8 => LaneShape.I16
      case VectorIShape.i32x4 => LaneShape.I32
      case VectorIShape.i64x2 => LaneShape.I64
      case VectorFShape.f32x4 => LaneShape.F32
      case VectorFShape.f64x2 => LaneShape.F64
      case _ => throw new IllegalArgumentException(s"Unsupported vector shape: $shape")

  private def laneWidthToLaneShape(width: Int): LaneShape = width match
    case 8 => LaneShape.I8
    case 16 => LaneShape.I16
    case 32 => LaneShape.I32
    case 64 => LaneShape.I64
    case _ => throw new IllegalArgumentException(s"Unsupported lane width: $width")

  private val VecBytes: Int = 16

  def evalSIMD(inst: Inst): V = inst match {
    case unop: VectorUnop => evalSIMDUnop(unop, stack.popOrAbort())
    case binop: VectorBinop => evalBinop(binop)
    case testop: VectorTestop => v128ops.vectorAllTrue(testop.shape.toLaneShape, stack.popOrAbort())
    case relop: VectorRelop => evalRelop(relop)
    case convertop: VectorConvertop => evalSIMDConvertop(convertop, stack.popOrAbort())
    case ternop: VectorTernop => evalTernop(ternop)
    case shiftop: VectorShiftop => evalShiftop(shiftop)
    case splat: VectorSplat => v128ops.splat(splat.shape.toLaneShape, stack.popOrAbort())
    case bitmask: VectorBitmask => v128ops.vectorBitmask(bitmask.shape.toLaneShape, stack.popOrAbort())
    case dot: VectorDot => evalDot(dot)
    case extmul: VectorExtmul => evalExtmulOp(extmul)
    case extadd: VectorExtadd => evalExtadd(extadd)
    case lane: VectorExtractLane => evalExtractLane(lane)
    case lane: VectorReplaceLane => evalReplaceLane(lane)
    case unop: VVectorUnop => evalVVectorUnop(unop)
    case binop: VVectorBinop => evalVVectorBinop(binop)
    case testop: VVectorTestop => evalVVectorTestop(testop)
    case v128.Const(bytes) => v128ops.vectorLit(bytes)
    case i8x16.Shuffle(lanes) =>
      val (a, b) = stack.pop2OrAbort()
      v128ops.shuffleLanes(LaneShape.I8, a, b, lanes.toArray)
    case i8x16.Swizzle =>
      val (a, s) = stack.pop2OrAbort()
      v128ops.swizzleLanes(LaneShape.I8, a, s)
    case _ => throw new IllegalArgumentException(s"Unsupported SIMD instruction: $inst")
  }

  private def evalBinop(binop: VectorBinop): V = {
    val (v1, v2) = stack.pop2OrAbort()
    evalSIMDBinop(binop, v1, v2)
  }

  private def evalRelop(relop: VectorRelop): V = {
    val (v1, v2) = stack.pop2OrAbort()
    evalSIMDRelop(relop, v1, v2)
  }

  private def evalTernop(ternop: VectorTernop): V = {
    val c = stack.popOrAbort()
    val v2 = stack.popOrAbort()
    val v1 = stack.popOrAbort()
    ternop match {
      case v128.BitSelect => v128ops.vectorBitselect(V128, v1, v2, c)
    }
  }

  private def evalShiftop(shiftop: VectorShiftop): V = {
    val (v, shift) = stack.pop2OrAbort()
    shiftop.operation match {
      case VecShiftopType.IShl => v128ops.vectorShiftLeft(shiftop.shape.toLaneShape, v, shift)
      case VecShiftopType.IShrU => v128ops.vectorShiftRightU(shiftop.shape.toLaneShape, v, shift)
      case VecShiftopType.IShrS => v128ops.vectorShiftRightS(shiftop.shape.toLaneShape, v, shift)
    }
  }

  private def evalDot(dot: VectorDot): V = {
    val (v1, v2) = stack.pop2OrAbort()
    dot match {
      case i32x4.DotI16x8S => v128ops.vectorDotS(LaneShape.I16, v1, v2)
    }
  }

  private def evalExtmulOp(extmul: VectorExtmul): V = {
    val (v1, v2) = stack.pop2OrAbort()
    evalExtmul(extmul, v1, v2)
  }

  private def evalExtadd(extadd: VectorExtadd): V = {
    val v = stack.popOrAbort()
    extadd match {
      case i16x8.ExtaddPairwiseI8x16S => v128ops.vectorExtAddS(LaneShape.I8, v)
      case i16x8.ExtaddPairwiseI8x16U => v128ops.vectorExtAddU(LaneShape.I8, v)
      case i32x4.ExtaddPairwiseI16x8S => v128ops.vectorExtAddS(LaneShape.I16, v)
      case i32x4.ExtaddPairwiseI16x8U => v128ops.vectorExtAddU(LaneShape.I16, v)
    }
  }

  private def evalExtractLane(lane: VectorExtractLane): V = {
    val v = stack.popOrAbort()
    lane.operation match {
      case VecExtractLaneType.ExtractU => v128ops.extractLaneU(lane.shape.toLaneShape, v, lane.lane)
      case VecExtractLaneType.ExtractS => v128ops.extractLaneS(lane.shape.toLaneShape, v, lane.lane)
      case VecExtractLaneType.Extract => v128ops.extractLane(lane.shape.toLaneShape, v, lane.lane)
    }
  }

  private def evalReplaceLane(lane: VectorReplaceLane): V = {
    val (v, value) = stack.pop2OrAbort()
    v128ops.replaceLane(lane.shape.toLaneShape, v, lane.lane, value)
  }

  private def evalVVectorUnop(unop: VVectorUnop): V = unop match {
    case v128.Not => v128ops.vectorNot(V128, stack.popOrAbort())
  }

  private def evalVVectorBinop(binop: VVectorBinop): V = {
    val (v1, v2) = stack.pop2OrAbort()
    binop match {
      case v128.And => v128ops.vectorAnd(V128, v1, v2)
      case v128.AndNot => v128ops.vectorAndNot(V128, v1, v2)
      case v128.Or => v128ops.vectorOr(V128, v1, v2)
      case v128.Xor => v128ops.vectorXor(V128, v1, v2)
    }
  }

  private def evalVVectorTestop(testop: VVectorTestop): V =
    v128ops.vectorAnyTrue(V128, stack.popOrAbort())

  def evalLoadVectorBytes(inst: Inst, memIdx: MemoryAddr, addr: Addr): JOption[J, Bytes] = {
    inst match {
      case v128.Load(_, _) => mem.read(memIdx, addr, VecBytes)
    }
  }

  def evalLoadVector(inst: Inst, memIdx: MemoryAddr, addr: Addr)(using J[Bytes]): JOptionA[V] =
    boundary:
      inst match
        case loadLane: LoadVectorLane =>
          val v = stack.popOrAbort()
          val shape = laneWidthToLaneShape(loadLane.laneWidth)
          val bytes = mem.read(memIdx, addr, loadLane.laneWidth / 8)
          var noneSome = false
          val vecBytes = bytes match {
            case JOptionA.Some(vec) => vec
            case JOptionC.Some(vec) => vec
            case JOptionA.NoneSome(vec) =>
              noneSome = true
              vec
            case JOptionA.None() => break(JOptionA.none)
            case JOptionC.None() => break(JOptionA.none)
          }
          val vec = v128ops.replaceLane(shape, v, loadLane.lane, decode(vecBytes, SomeCC(loadLane, false)))
          if noneSome then JOptionA.noneSome(vec) else JOptionA.some(vec)

        case loadSplat: LoadVectorSplat =>
          val bytes = mem.read(memIdx, addr, loadSplat.shape.N / 8)
          var noneSome = false
          val vecBytes = bytes match {
            case JOptionA.Some(vec) => vec
            case JOptionC.Some(vec) => vec
            case JOptionA.NoneSome(vec) =>
              noneSome = true
              vec
            case JOptionA.None() => break(JOptionA.none)
            case JOptionC.None() => break(JOptionA.none)
          }
          val numV = decode(vecBytes, SomeCC(loadSplat, false))
          val vec = loadSplat.shape match {
            case VectorSplatShape.i8_splat => v128ops.splat(I8, numV)
            case VectorSplatShape.i16_splat => v128ops.splat(I16, numV)
            case VectorSplatShape.i32_splat => v128ops.splat(I32, numV)
            case VectorSplatShape.i64_splat => v128ops.splat(I64, numV)
          }
          if noneSome then JOptionA.noneSome(vec) else JOptionA.some(vec)

        case loadZero: LoadVectorZero =>
          val bytes = mem.read(memIdx, addr, loadZero.shape.N / 8)
          var noneSome = false
          val vecBytes = bytes match {
            case JOptionA.Some(vec) => vec
            case JOptionC.Some(vec) => vec
            case JOptionA.NoneSome(vec) =>
              noneSome = true
              vec
            case JOptionA.None() => break(JOptionA.none)
            case JOptionC.None() => break(JOptionA.none)
          }
          val numV = decode(vecBytes, SomeCC(loadZero, false))
          val vec = loadZero.shape match {
            case VectorZeroShape.i32_zero => v128ops.zeroPad(I32, numV)
            case VectorZeroShape.i64_zero => v128ops.zeroPad(I64, numV)
          }
          if noneSome then JOptionA.noneSome(vec) else JOptionA.some(vec)

        case loadExtend: LoadVector =>
          val bytes = mem.read(memIdx, addr, 8)
          var noneSome = false
          val vecBytes = bytes match {
            case JOptionA.Some(vec) => vec
            case JOptionC.Some(vec) => vec
            case JOptionA.NoneSome(vec) =>
              noneSome = true
              vec
            case JOptionA.None() => break(JOptionA.none)
            case JOptionC.None() => break(JOptionA.none)
          }
          val vec = decode(vecBytes, SomeCC(loadExtend, false))
          if noneSome then JOptionA.noneSome(vec) else JOptionA.some(vec)

  def evalStoreVector(inst: Inst, memIdx: MemoryAddr, addr: Addr): JOption[J, Unit] = {
    inst match {
      case storeVec: StoreVector =>
        val v = stack.popOrAbort()
        val bytes = encode(v, SomeCC(storeVec, false))
        mem.write(memIdx, addr, bytes)
      case storeVecLane: StoreVectorLane =>
        val v = stack.popOrAbort()
        val shape = laneWidthToLaneShape(storeVecLane.laneWidth)
        val bytes = if storeVecLane.laneWidth <= VecBytes then
          encode(v128ops.extractLaneU(shape, v, storeVecLane.lane), SomeCC(storeVecLane, false))
        else
          encode(v128ops.extractLane(shape, v, storeVecLane.lane), SomeCC(storeVecLane, false))
        mem.write(memIdx, addr, bytes)
    }
  }

  inline def evalSIMDUnop(op: VectorUnop, v: V): V = {
    op match {
      case unop: IVectorUnop =>
        unop.operation match {
          case VecUnopType.IAbs => v128ops.vectorAbs(unop.shape.toLaneShape, v)
          case VecUnopType.INeg => v128ops.vectorNeg(unop.shape.toLaneShape, v)
          case VecUnopType.IPopCnt => v128ops.vectorPopCount(unop.shape.toLaneShape, v)
        }
      case unop: FVectorUnop =>
        unop.operation match {
          case VecUnopType.FAbs => v128ops.vectorAbs(unop.shape.toLaneShape, v)
          case VecUnopType.FNeg => v128ops.vectorNeg(unop.shape.toLaneShape, v)
          case VecUnopType.FSqrt => v128ops.vectorSqrt(unop.shape.toLaneShape, v)
          case VecUnopType.FCeil => v128ops.vectorCeil(unop.shape.toLaneShape, v)
          case VecUnopType.FFloor => v128ops.vectorFloor(unop.shape.toLaneShape, v)
          case VecUnopType.FTrunc => v128ops.vectorTrunc(unop.shape.toLaneShape, v)
          case VecUnopType.FNearest => v128ops.vectorNearest(unop.shape.toLaneShape, v)
        }
      case i8x16.Popcnt => v128ops.vectorPopCount(LaneShape.I8, v)
    }
  }

  inline def evalSIMDBinop(op: VectorBinop, v1: V, v2: V): V = {
    op match {
      case minMaxop: VectorMinMaxop =>
        minMaxop.operation match {
          case VecBinopType.IMinU => v128ops.vectorMinU(minMaxop.shape.toLaneShape, v1, v2)
          case VecBinopType.IMinS => v128ops.vectorMinS(minMaxop.shape.toLaneShape, v1, v2)
          case VecBinopType.IMaxU => v128ops.vectorMaxU(minMaxop.shape.toLaneShape, v1, v2)
          case VecBinopType.IMaxS => v128ops.vectorMaxS(minMaxop.shape.toLaneShape, v1, v2)
        }
      case binop: VectorSatBinop =>
        binop.operation match {
          case VecBinopType.IAddSatU => v128ops.vectorAddSatU(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.IAddSatS => v128ops.vectorAddSatS(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.ISubSatU => v128ops.vectorSubSatU(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.ISubSatS => v128ops.vectorSubSatS(binop.shape.toLaneShape, v1, v2)
        }
      case binop: IVectorBinop =>
        binop.operation match {
          case VecBinopType.IAdd => v128ops.vectorAdd(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.ISub => v128ops.vectorSub(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.IMul => v128ops.vectorMul(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.IAvrgU => v128ops.vectorAvrgU(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.IQ15MulrSatS => v128ops.vectorQ15MulrSatS(binop.shape.toLaneShape, v1, v2)
        }
      case binop: FVectorBinop =>
        binop.operation match {
          case VecBinopType.FAdd => v128ops.vectorAdd(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FSub => v128ops.vectorSub(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FMul => v128ops.vectorMul(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FDiv => v128ops.vectorDiv(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FMin => v128ops.vectorMin(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FMax => v128ops.vectorMax(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FPMin => v128ops.vectorPMin(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FPMax => v128ops.vectorPMax(binop.shape.toLaneShape, v1, v2)
        }
    }
  }

  inline def evalSIMDRelop(op: VectorRelop, v1: V, v2: V): V = {
    op match {
      case relop: IVectorRelop =>
        relop.operation match {
          case VecRelopType.IEq => v128ops.vectorEq(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.INe => v128ops.vectorNe(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.ILtU => v128ops.vectorLtU(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.ILtS => v128ops.vectorLtS(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.ILeU => v128ops.vectorLeU(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.ILeS => v128ops.vectorLeS(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.IGtU => v128ops.vectorGtU(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.IGtS => v128ops.vectorGtS(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.IGeU => v128ops.vectorGeU(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.IGeS => v128ops.vectorGeS(relop.shape.toLaneShape, v1, v2)
        }
      case relop: FVectorRelop =>
        relop.operation match {
          case VecRelopType.FEq => v128ops.vectorEq(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.FNe => v128ops.vectorNe(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.FLt => v128ops.vectorLtS(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.FLe => v128ops.vectorLeS(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.FGt => v128ops.vectorGtS(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.FGe => v128ops.vectorGeS(relop.shape.toLaneShape, v1, v2)
        }
    }
  }

  inline def evalExtmul(op: VectorExtmul, v1: V, v2: V): V = {
    op match {
      case i16x8.ExtmulLowI8x16S => v128ops.vectorMul(I16, v128ops.vectorExtendS(I8, I16, Half.Low, v1), v128ops.vectorExtendS(I8, I16, Half.Low, v2))
      case i16x8.ExtmulHighI8x16S => v128ops.vectorMul(I16, v128ops.vectorExtendS(I8, I16, Half.High, v1), v128ops.vectorExtendS(I8, I16, Half.High, v2))
      case i16x8.ExtmulLowI8x16U => v128ops.vectorMul(I16, v128ops.vectorExtendU(I8, I16, Half.Low, v1), v128ops.vectorExtendU(I8, I16, Half.Low, v2))
      case i16x8.ExtmulHighI8x16U => v128ops.vectorMul(I16, v128ops.vectorExtendU(I8, I16, Half.High, v1), v128ops.vectorExtendU(I8, I16, Half.High, v2))
      case i32x4.ExtmulLowI16x8S => v128ops.vectorMul(I32, v128ops.vectorExtendS(I16, I32, Half.Low, v1), v128ops.vectorExtendS(I16, I32, Half.Low, v2))
      case i32x4.ExtmulHighI16x8S => v128ops.vectorMul(I32, v128ops.vectorExtendS(I16, I32, Half.High, v1), v128ops.vectorExtendS(I16, I32, Half.High, v2))
      case i32x4.ExtmulLowI16x8U => v128ops.vectorMul(I32, v128ops.vectorExtendU(I16, I32, Half.Low, v1), v128ops.vectorExtendU(I16, I32, Half.Low, v2))
      case i32x4.ExtmulHighI16x8U => v128ops.vectorMul(I32, v128ops.vectorExtendU(I16, I32, Half.High, v1), v128ops.vectorExtendU(I16, I32, Half.High, v2))
      case i64x2.ExtmulLowI32x4S => v128ops.vectorMul(I64, v128ops.vectorExtendS(I32, I64, Half.Low, v1), v128ops.vectorExtendS(I32, I64, Half.Low, v2))
      case i64x2.ExtmulHighI32x4S => v128ops.vectorMul(I64, v128ops.vectorExtendS(I32, I64, Half.High, v1), v128ops.vectorExtendS(I32, I64, Half.High, v2))
      case i64x2.ExtmulLowI32x4U => v128ops.vectorMul(I64, v128ops.vectorExtendU(I32, I64, Half.Low, v1), v128ops.vectorExtendU(I32, I64, Half.Low, v2))
      case i64x2.ExtmulHighI32x4U => v128ops.vectorMul(I64, v128ops.vectorExtendU(I32, I64, Half.High, v1), v128ops.vectorExtendU(I32, I64, Half.High, v2))
    }
  }

  inline def evalSIMDConvertop(op: VectorConvertop, v: V): V = {
    op match {
      case i8x16.NarrowI16x8S => v128ops.vectorNarrowS(LaneShape.I16, LaneShape.I8, v, stack.popOrAbort())
      case i8x16.NarrowI16x8U => v128ops.vectorNarrowU(LaneShape.I16, LaneShape.I8, v, stack.popOrAbort())
      case i16x8.ExtendLowI8x16S => v128ops.vectorExtendS(LaneShape.I8, LaneShape.I16, Half.Low, v)
      case i16x8.ExtendHighI8x16S => v128ops.vectorExtendS(LaneShape.I8, LaneShape.I16, Half.High, v)
      case i16x8.ExtendLowI8x16U => v128ops.vectorExtendU(LaneShape.I8, LaneShape.I16, Half.Low, v)
      case i16x8.ExtendHighI8x16U => v128ops.vectorExtendU(LaneShape.I8, LaneShape.I16, Half.High, v)
      case i16x8.NarrowI32x4S => v128ops.vectorNarrowS(LaneShape.I32, LaneShape.I16, v, stack.popOrAbort())
      case i16x8.NarrowI32x4U => v128ops.vectorNarrowU(LaneShape.I32, LaneShape.I16, v, stack.popOrAbort())
      case i32x4.ExtendLowI16x8S => v128ops.vectorExtendS(LaneShape.I16, LaneShape.I32, Half.Low, v)
      case i32x4.ExtendHighI16x8S => v128ops.vectorExtendS(LaneShape.I16, LaneShape.I32, Half.High, v)
      case i32x4.ExtendLowI16x8U => v128ops.vectorExtendU(LaneShape.I16, LaneShape.I32, Half.Low, v)
      case i32x4.ExtendHighI16x8U => v128ops.vectorExtendU(LaneShape.I16, LaneShape.I32, Half.High, v)
      case i32x4.TruncSatF32x4S => v128ops.vectorTruncSatS(LaneShape.F32, TruncMode.Sat, v)
      case i32x4.TruncSatF32x4U => v128ops.vectorTruncSatU(LaneShape.F32, TruncMode.Sat, v)
      case i32x4.TruncSatF64x2SZero => v128ops.vectorTruncSatS(LaneShape.F64, TruncMode.SatZero, v)
      case i32x4.TruncSatF64x2UZero => v128ops.vectorTruncSatU(LaneShape.F64, TruncMode.SatZero, v)
      case i64x2.ExtendLowI32x4S => v128ops.vectorExtendS(LaneShape.I32, LaneShape.I64, Half.Low, v)
      case i64x2.ExtendHighI32x4S => v128ops.vectorExtendS(LaneShape.I32, LaneShape.I64, Half.High, v)
      case i64x2.ExtendLowI32x4U => v128ops.vectorExtendU(LaneShape.I32, LaneShape.I64, Half.Low, v)
      case i64x2.ExtendHighI32x4U => v128ops.vectorExtendU(LaneShape.I32, LaneShape.I64, Half.High, v)
      case f32x4.ConvertI32x4S => v128ops.vectorConvertS(LaneShape.I32, v)
      case f32x4.ConvertI32x4U => v128ops.vectorConvertU(LaneShape.I32, v)
      case f32x4.DemoteF64x2Zero => v128ops.vectorDemoteZero(LaneShape.F64, v)
      case f64x2.ConvertLowI32x4S => v128ops.vectorConvertLowS(LaneShape.I32, v)
      case f64x2.ConvertLowI32x4U => v128ops.vectorConvertLowU(LaneShape.I32, v)
      case f64x2.PromoteLowF32x4 => v128ops.vectorPromoteLow(LaneShape.F32, v)
    }
  }
  
  def defaultValue(): V = {
    evalSIMD(v128.Const(Array.fill(VecBytes)(0.toByte)))
  }

