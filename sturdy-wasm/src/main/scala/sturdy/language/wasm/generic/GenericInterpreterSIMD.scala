package sturdy.language.wasm.generic

import sturdy.data.{*, given}
import sturdy.effect.bytememory.Memory
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.OperandStack
import sturdy.language.wasm.generic.WasmFailure.MemoryAccessOutOfBounds
import sturdy.values.convert.SomeCC
import sturdy.values.config.BitSign.*
import sturdy.values.config.Overflow.*
import sturdy.values.simd.LaneShape.*
import sturdy.values.simd.{Half, LaneShape, ShiftDirection, TruncMode}
import swam.syntax.*

import scala.reflect.ClassTag
import scala.util.boundary


class GenericInterpreterSIMD [V, Addr, Bytes, J[_] <: MayJoin[_]]
  (stack: OperandStack[V, NoJoin], mem: Memory[MemoryAddr, Addr, Bytes, _, J], wasmOps: WasmOps[V, Addr, Bytes, _, _, _, _, _, J])
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
    case testop: VVectorTestop => evalVVectorTestop(testop)
    case testop: VectorTestop => v128ops.vectorAllTrue(testop.shape.toLaneShape, stack.popOrAbort())
    case relop: VectorRelop => evalRelop(relop)
    case convertop: VectorConvertop => evalSIMDConvertop(convertop, stack.popOrAbort())
    case ternop: VectorTernop => evalTernop(ternop)
    case shiftop: VectorShiftop => evalShiftop(shiftop)
    case splat: VectorSplat => v128ops.splat(splat.shape.toLaneShape, stack.popOrAbort())
    case bitmask: VectorBitmask => v128ops.vectorBitmask(bitmask.shape.toLaneShape, stack.popOrAbort())
    case lane: VectorExtractLane => evalExtractLane(lane)
    case lane: VectorReplaceLane => evalReplaceLane(lane)
    case v128.Const(bytes) => v128ops.vectorLit(bytes)
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
      case VecShiftopType.IShl => v128ops.vectorShift(shiftop.shape.toLaneShape, ShiftDirection.Left, Raw, v, shift)
      case VecShiftopType.IShrU => v128ops.vectorShift(shiftop.shape.toLaneShape, ShiftDirection.Right, Unsigned, v, shift)
      case VecShiftopType.IShrS => v128ops.vectorShift(shiftop.shape.toLaneShape, ShiftDirection.Right, Signed, v, shift)
    }
  }

  private def evalExtractLane(lane: VectorExtractLane): V = {
    val v = stack.popOrAbort()
    lane.operation match {
      case VecExtractLaneType.ExtractU => v128ops.extractLane(lane.shape.toLaneShape, Unsigned, v, lane.lane)
      case VecExtractLaneType.ExtractS => v128ops.extractLane(lane.shape.toLaneShape, Signed,  v, lane.lane)
      case VecExtractLaneType.Extract => v128ops.extractLane(lane.shape.toLaneShape, Raw, v, lane.lane)
    }
  }

  private def evalReplaceLane(lane: VectorReplaceLane): V = {
    val (v, value) = stack.pop2OrAbort()
    v128ops.replaceLane(lane.shape.toLaneShape, v, lane.lane, value)
  }

  private def evalVVectorUnop(unop: VVectorUnop, v: V): V = unop match {
    case v128.Not => v128ops.vectorNot(V128, v)
  }

  private def evalVVectorBinop(binop: VVectorBinop, v1: V, v2: V): V = {
    binop match {
      case v128.And => v128ops.vectorAnd(V128, v1, v2)
      case v128.AndNot => v128ops.vectorAnd(V128, v1, v128ops.vectorNot(V128, v2))
      case v128.Or => v128ops.vectorOr(V128, v1, v2)
      case v128.Xor => v128ops.vectorXor(V128, v1, v2)
    }
  }

  private def evalVVectorTestop(testop: VVectorTestop): V =
    v128ops.vectorAnyTrue(V128, stack.popOrAbort())

  def evalLoadVectorBytes(inst: Inst, memIdx: MemoryAddr, addr: Addr): JOption[J, Bytes] = {
    inst match {
      case v128.Load(_, _) => mem.read(memIdx, addr, VecBytes)
      case _ => throw new IllegalArgumentException(s"Unsupported SIMD load instruction: $inst")
    }
  }

  def evalLoadVector(inst: Inst, memIdx: MemoryAddr, addr: Addr)(using f: Failure)(using J[Bytes]): JOptionA[V] =
    boundary:
      inst match
        case loadSplat: LoadVectorSplat =>
          val bytes = mem.read(memIdx, addr, loadSplat.shape.N / 8).getOrElse(f.fail(MemoryAccessOutOfBounds, "Memory access out of bounds during SIMD load splat"))
          val numV = decode(bytes, SomeCC(loadSplat, false))
          val vec = loadSplat.shape match {
            case VectorSplatShape.i8_splat => v128ops.splat(I8, numV)
            case VectorSplatShape.i16_splat => v128ops.splat(I16, numV)
            case VectorSplatShape.i32_splat => v128ops.splat(I32, numV)
            case VectorSplatShape.i64_splat => v128ops.splat(I64, numV)
          }
          JOptionA.some(vec)

        case loadZero: LoadVectorZero =>
          val bytes = mem.read(memIdx, addr, loadZero.shape.N / 8).getOrElse(f.fail(MemoryAccessOutOfBounds, "Memory access out of bounds during SIMD load zero"))
          val numV = decode(bytes, SomeCC(loadZero, false))
          val vec = loadZero.shape match {
            case VectorZeroShape.i32_zero => v128ops.zeroPad(I32, numV)
            case VectorZeroShape.i64_zero => v128ops.zeroPad(I64, numV)
          }
          JOptionA.some(vec)

        case loadExtend: LoadVector =>
          val bytes = mem.read(memIdx, addr, 8).getOrElse(f.fail(MemoryAccessOutOfBounds, "Memory access out of bounds during SIMD load extend"))
          val vec = decode(bytes, SomeCC(loadExtend, false))
          JOptionA.some(vec)
        case _ => throw new IllegalArgumentException(s"Unsupported SIMD load instruction: $inst")

  def evalLoadVectorLane(inst: LoadVectorLane, memIdx: MemoryAddr, addr: Addr, vec: V)(using f: Failure)(using J[Bytes]): JOptionA[V] = {
      val shape = laneWidthToLaneShape(inst.laneWidth)
      val bytes = mem.read(memIdx, addr, inst.laneWidth / 8).getOrElse(f.fail(MemoryAccessOutOfBounds, "Memory access out of bounds during SIMD load lane"))
      val newVec = v128ops.replaceLane(shape, vec, inst.lane, decode(bytes, SomeCC(inst, false)))
      JOptionA.some(newVec)
  }

  def evalStoreVector(inst: Inst, memIdx: MemoryAddr, addr: Addr, vec: V): JOption[J, Unit] = {
    inst match {
      case storeVec: StoreVector =>
        val bytes = encode(vec, SomeCC(storeVec, false))
        mem.write(memIdx, addr, bytes)
      case storeVecLane: StoreVectorLane =>
        val shape = laneWidthToLaneShape(storeVecLane.laneWidth)
        val bytes = if storeVecLane.laneWidth <= VecBytes then
          encode(v128ops.extractLane(shape, Unsigned, vec, storeVecLane.lane), SomeCC(storeVecLane, false))
        else
          encode(v128ops.extractLane(shape, Raw, vec, storeVecLane.lane), SomeCC(storeVecLane, false))
        mem.write(memIdx, addr, bytes)
      case _ => throw new IllegalArgumentException(s"Unsupported SIMD store instruction: $inst")
    }
  }

  inline def evalSIMDUnop(op: VectorUnop, v: V): V = {
    op match {
      case unop: IVectorUnop =>
        unop.operation match {
          case VecUnopType.IAbs => v128ops.vectorAbs(unop.shape.toLaneShape, v)
          case VecUnopType.INeg => v128ops.vectorNeg(unop.shape.toLaneShape, v)
          case VecUnopType.IPopCnt => v128ops.vectorPopCount(unop.shape.toLaneShape, v)
          case VecUnopType.IExtaddS => v128ops.vectorExtAdd(unop.shape.toLaneShape, Signed, v)
          case VecUnopType.IExtaddU => v128ops.vectorExtAdd(unop.shape.toLaneShape, Unsigned, v)
          case _ => throw new IllegalArgumentException(s"Unsupported integer vector unop: ${unop.operation}")
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
          case _ => throw new IllegalArgumentException(s"Unsupported float vector unop: ${unop.operation}")
        }
      case i8x16.Popcnt => v128ops.vectorPopCount(LaneShape.I8, v)
      case vunop: VVectorUnop => evalVVectorUnop(vunop, v)
    }
  }

  inline def evalSIMDBinop(op: VectorBinop, v1: V, v2: V): V = {
    op match {
      case minMaxop: VectorMinMaxop =>
        minMaxop.operation match {
          case VecBinopType.IMinU => v128ops.vectorMin(minMaxop.shape.toLaneShape, Unsigned, v1, v2)
          case VecBinopType.IMinS => v128ops.vectorMin(minMaxop.shape.toLaneShape, Signed, v1, v2)
          case VecBinopType.IMaxU => v128ops.vectorMax(minMaxop.shape.toLaneShape, Unsigned, v1, v2)
          case VecBinopType.IMaxS => v128ops.vectorMax(minMaxop.shape.toLaneShape, Signed, v1, v2)
          case _ => throw new IllegalArgumentException(s"Unsupported vector min/max operation: ${minMaxop.operation}")
        }
      case binop: VectorSatBinop =>
        binop.operation match {
          case VecBinopType.IAddSatU => v128ops.vectorAdd(binop.shape.toLaneShape, JumpToBounds, Unsigned, v1, v2)
          case VecBinopType.IAddSatS => v128ops.vectorAdd(binop.shape.toLaneShape, JumpToBounds, Signed, v1, v2)
          case VecBinopType.ISubSatU => v128ops.vectorSub(binop.shape.toLaneShape, JumpToBounds, Unsigned, v1, v2)
          case VecBinopType.ISubSatS => v128ops.vectorSub(binop.shape.toLaneShape, JumpToBounds, Signed, v1, v2)
          case _ => throw new IllegalArgumentException(s"Unsupported vector sat binop: ${binop.operation}")
        }
      case binop: IVectorBinop =>
        binop.operation match {
          case VecBinopType.IAdd => v128ops.vectorAdd(binop.shape.toLaneShape, Allow, Raw, v1, v2)
          case VecBinopType.ISub => v128ops.vectorSub(binop.shape.toLaneShape, Allow, Raw, v1, v2)
          case VecBinopType.IMul => v128ops.vectorMul(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.IAvrgU => v128ops.vectorAvrgU(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.IQ15MulrSatS => v128ops.vectorQ15MulrSatS(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.ISwizzle => v128ops.swizzleLanes(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.IShuffle => op match 
            case i8x16.Shuffle(lanes) => v128ops.shuffleLanes(LaneShape.I8, v1, v2, lanes.toArray)
            case _ => throw new IllegalArgumentException(s"Unsupported shuffle operation: $op")
          case VecBinopType.IDot => v128ops.vectorDotS(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.IExtmulLowS =>
            val shape = binop.shape.toLaneShape
            val halfShape = getHalfShape(shape)
            v128ops.vectorMul(shape, v128ops.vectorExtend(halfShape, shape, Half.Low, Signed, v1), v128ops.vectorExtend(halfShape, shape, Half.Low, Signed, v2))
          case VecBinopType.IExtmulLowU =>
            val shape = binop.shape.toLaneShape
            val halfShape = getHalfShape(shape)
            v128ops.vectorMul(shape, v128ops.vectorExtend(halfShape, shape, Half.Low, Unsigned, v1), v128ops.vectorExtend(halfShape, shape, Half.Low, Unsigned, v2))
          case VecBinopType.IExtmulHighS =>
            val shape = binop.shape.toLaneShape
            val halfShape = getHalfShape(shape)
            v128ops.vectorMul(shape, v128ops.vectorExtend(halfShape, shape, Half.High, Signed, v1), v128ops.vectorExtend(halfShape, shape, Half.High, Signed, v2))
          case VecBinopType.IExtmulHighU =>
            val shape = binop.shape.toLaneShape
            val halfShape = getHalfShape(shape)
            v128ops.vectorMul(shape, v128ops.vectorExtend(halfShape, shape, Half.High, Unsigned, v1), v128ops.vectorExtend(halfShape, shape, Half.High, Unsigned, v2))
          case _ => throw new IllegalArgumentException(s"Unsupported integer vector binop: ${binop.operation}")
        }
      case binop: FVectorBinop =>
        binop.operation match {
          case VecBinopType.FAdd => v128ops.vectorAdd(binop.shape.toLaneShape, Allow, Raw, v1, v2)
          case VecBinopType.FSub => v128ops.vectorSub(binop.shape.toLaneShape, Allow, Raw, v1, v2)
          case VecBinopType.FMul => v128ops.vectorMul(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FDiv => v128ops.vectorDiv(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FMin => v128ops.vectorMin(binop.shape.toLaneShape, Raw, v1, v2)
          case VecBinopType.FMax => v128ops.vectorMax(binop.shape.toLaneShape, Raw, v1, v2)
          case VecBinopType.FPMin => v128ops.vectorPMin(binop.shape.toLaneShape, v1, v2)
          case VecBinopType.FPMax => v128ops.vectorPMax(binop.shape.toLaneShape, v1, v2)
          case _ => throw new IllegalArgumentException(s"Unsupported float vector binop: ${binop.operation}")
        }
      case vbinop: VVectorBinop => evalVVectorBinop(vbinop, v1, v2)
    }
  }
  
  private def getHalfShape(shape: LaneShape): LaneShape = shape match {
    case LaneShape.I16 => LaneShape.I8
    case LaneShape.I32 => LaneShape.I16
    case LaneShape.I64 => LaneShape.I32
    case _ => throw new IllegalArgumentException(s"Unsupported lane shape for half: $shape")
  }

  inline def evalSIMDRelop(op: VectorRelop, v1: V, v2: V): V =
    op match
      case relop: IVectorRelop =>
        relop.operation match
          case VecRelopType.IEq => v128ops.vectorEq(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.INe => v128ops.vectorNe(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.ILtU => v128ops.vectorLt(relop.shape.toLaneShape, Unsigned, v1, v2)
          case VecRelopType.ILtS => v128ops.vectorLt(relop.shape.toLaneShape, Signed, v1, v2)
          case VecRelopType.ILeU => v128ops.vectorOr(V128, v128ops.vectorLt(relop.shape.toLaneShape, Unsigned, v1, v2), v128ops.vectorEq(relop.shape.toLaneShape, v1, v2))
          case VecRelopType.ILeS => v128ops.vectorOr(V128, v128ops.vectorLt(relop.shape.toLaneShape, Signed, v1, v2), v128ops.vectorEq(relop.shape.toLaneShape, v1, v2))
          case VecRelopType.IGtU => v128ops.vectorLt(relop.shape.toLaneShape, Unsigned, v2, v1)
          case VecRelopType.IGtS => v128ops.vectorLt(relop.shape.toLaneShape, Signed, v2, v1)
          case VecRelopType.IGeU => v128ops.vectorOr(V128, v128ops.vectorLt(relop.shape.toLaneShape, Unsigned, v2, v1), v128ops.vectorEq(relop.shape.toLaneShape, v1, v2))
          case VecRelopType.IGeS => v128ops.vectorOr(V128, v128ops.vectorLt(relop.shape.toLaneShape, Signed, v2, v1), v128ops.vectorEq(relop.shape.toLaneShape, v1, v2))
          case _ => throw new IllegalArgumentException(s"Unsupported integer vector relop: ${relop.operation}")

      case relop: FVectorRelop =>
        relop.operation match
          case VecRelopType.FEq => v128ops.vectorEq(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.FNe => v128ops.vectorNe(relop.shape.toLaneShape, v1, v2)
          case VecRelopType.FLt => v128ops.vectorLt(relop.shape.toLaneShape, Signed, v1, v2)
          case VecRelopType.FLe => v128ops.vectorOr(V128, v128ops.vectorLt(relop.shape.toLaneShape, Signed, v1, v2), v128ops.vectorEq(relop.shape.toLaneShape, v1, v2))
          case VecRelopType.FGt => v128ops.vectorLt(relop.shape.toLaneShape, Signed, v2, v1)
          case VecRelopType.FGe => v128ops.vectorOr(V128, v128ops.vectorLt(relop.shape.toLaneShape, Signed, v2, v1), v128ops.vectorEq(relop.shape.toLaneShape, v1, v2))
          case _ => throw new IllegalArgumentException(s"Unsupported float vector relop: ${relop.operation}")

  inline def evalSIMDConvertop(op: VectorConvertop, v: V): V =
    op match
      case i8x16.NarrowI16x8S => v128ops.vectorNarrow(LaneShape.I16, LaneShape.I8, Signed, v, stack.popOrAbort())
      case i8x16.NarrowI16x8U => v128ops.vectorNarrow(LaneShape.I16, LaneShape.I8, Unsigned, v, stack.popOrAbort())

      case i16x8.ExtendLowI8x16S => v128ops.vectorExtend(LaneShape.I8, LaneShape.I16, Half.Low, Signed, v)
      case i16x8.ExtendHighI8x16S => v128ops.vectorExtend(LaneShape.I8, LaneShape.I16, Half.High, Signed, v)
      case i16x8.ExtendLowI8x16U => v128ops.vectorExtend(LaneShape.I8, LaneShape.I16, Half.Low, Unsigned, v)
      case i16x8.ExtendHighI8x16U => v128ops.vectorExtend(LaneShape.I8, LaneShape.I16, Half.High, Unsigned, v)

      case i16x8.NarrowI32x4S => v128ops.vectorNarrow(LaneShape.I32, LaneShape.I16, Signed, v, stack.popOrAbort())
      case i16x8.NarrowI32x4U => v128ops.vectorNarrow(LaneShape.I32, LaneShape.I16, Unsigned, v, stack.popOrAbort())

      case i32x4.ExtendLowI16x8S => v128ops.vectorExtend(LaneShape.I16, LaneShape.I32, Half.Low, Signed, v)
      case i32x4.ExtendHighI16x8S => v128ops.vectorExtend(LaneShape.I16, LaneShape.I32, Half.High, Signed, v)
      case i32x4.ExtendLowI16x8U => v128ops.vectorExtend(LaneShape.I16, LaneShape.I32, Half.Low, Unsigned, v)
      case i32x4.ExtendHighI16x8U => v128ops.vectorExtend(LaneShape.I16, LaneShape.I32, Half.High, Unsigned, v)

      case i32x4.TruncSatF32x4S     => v128ops.vectorTruncSat(LaneShape.F32, TruncMode.Sat, Signed, v)
      case i32x4.TruncSatF32x4U     => v128ops.vectorTruncSat(LaneShape.F32, TruncMode.Sat, Unsigned, v)
      case i32x4.TruncSatF64x2SZero => v128ops.vectorTruncSat(LaneShape.F64, TruncMode.SatZero, Signed, v)
      case i32x4.TruncSatF64x2UZero => v128ops.vectorTruncSat(LaneShape.F64, TruncMode.SatZero, Unsigned, v)


      case i64x2.ExtendLowI32x4S => v128ops.vectorExtend(LaneShape.I32, LaneShape.I64, Half.Low, Signed, v)
      case i64x2.ExtendHighI32x4S => v128ops.vectorExtend(LaneShape.I32, LaneShape.I64, Half.High, Signed, v)
      case i64x2.ExtendLowI32x4U => v128ops.vectorExtend(LaneShape.I32, LaneShape.I64, Half.Low, Unsigned, v)
      case i64x2.ExtendHighI32x4U => v128ops.vectorExtend(LaneShape.I32, LaneShape.I64, Half.High, Unsigned, v)

      case f32x4.ConvertI32x4S => v128ops.vectorConvert(LaneShape.I32, Signed, v)
      case f32x4.ConvertI32x4U => v128ops.vectorConvert(LaneShape.I32, Unsigned, v)

      case f32x4.DemoteF64x2Zero => v128ops.vectorDemoteZero(LaneShape.F64, v)

      case f64x2.ConvertLowI32x4S => v128ops.vectorConvertLow(LaneShape.I32, Signed, v)
      case f64x2.ConvertLowI32x4U => v128ops.vectorConvertLow(LaneShape.I32, Unsigned, v)

      case f64x2.PromoteLowF32x4 => v128ops.vectorPromoteLow(LaneShape.F32, v)

  def defaultValue(): V = {
    evalSIMD(v128.Const(Array.fill(VecBytes)(0.toByte)))
  }

