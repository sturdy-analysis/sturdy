package sturdy.language.wasm

import sturdy.data.MayJoin
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.language.wasm.generic.*
import sturdy.values.*
import sturdy.values.booleans.BooleanBranching
import sturdy.values.booleans.LiftedBooleanBranching
import sturdy.values.config.{UnsupportedConfiguration, unsupportedConfiguration}
import sturdy.values.convert.*
import sturdy.values.exceptions.Exceptional
import sturdy.values.floating.*
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.*
import sturdy.values.ordering.*
import swam.syntax.LoadInst
import swam.syntax.LoadNInst
import swam.syntax.MemoryInst
import swam.syntax.StoreInst
import swam.syntax.StoreNInst
import swam.syntax.{f32, f64, i32, i64}
import swam.FuncType
import swam.ValType

import java.nio.ByteOrder
import WasmFailure.*

trait Interpreter:
  type J[A] <: MayJoin[A]
  type I32
  type I64
  type F32
  type F64
  type Bool

  enum Value:
    case TopValue
    case Int32(i: I32)
    case Int64(l: I64)
    case Float32(f: F32)
    case Float64(d: F64)

    def asBoolean(using Failure): Bool = Interpreter.this.asBoolean(this)

    def asInt32(using f: Failure): I32 = this match
      case Int32(i) => i
      case TopValue => topI32
      case _ => f.fail(TypeError, s"Expected i32 but got $this")

    def asInt64(using f: Failure): I64 = this match
      case Int64(l) => l
      case TopValue => topI64
      case _ => f.fail(TypeError, s"Expected i64 but got $this")

    def asFloat32(using f: Failure): F32 = this match
      case Float32(f) => f
      case TopValue => topF32
      case _ => f.fail(TypeError, s"Expected f32 but got $this")

    def asFloat64(using f: Failure): F64 = this match
      case Float64(d) => d
      case TopValue => topF64
      case _ => f.fail(TypeError, s"Expected f64 but got $this")

  def asBoolean(v: Value)(using Failure): Bool
  def boolean(b: Bool): Value

  def topI32: I32
  def topI64: I64
  def topF32: F32
  def topF64: F64
  
  def typedTop(ty: ValType): Value = ty match
    case ValType.I32 => Value.Int32(topI32)
    case ValType.I64 => Value.Int64(topI64)
    case ValType.F32 => Value.Float32(topF32)
    case ValType.F64 => Value.Float64(topF64)

  given Top[Value] with
    def top = Value.TopValue

  given CombineValue[W <: Widening](using Combine[I32, W], Combine[I64, W], Combine[F32, W], Combine[F64, W]): Combine[Value, W] with
    import Value.*
    override def apply(v1: Value, v2: Value): MaybeChanged[Value] = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Combine[I32, W](i1, i2).map(Int32.apply)
      case (Int64(l1), Int64(l2)) => Combine[I64, W](l1, l2).map(Int64.apply)
      case (Float32(f1), Float32(f2)) => Combine[F32, W](f1, f2).map(Float32.apply)
      case (Float64(d1), Float64(d2)) => Combine[F64, W](d1, d2).map(Float64.apply)
      case _ => MaybeChanged(TopValue, v1)

  type Addr
  type Bytes
  type Size
  type ExcV
  type FuncIx
  type FunV

  given ValueWasmOps
    (using failure: Failure
     , i32Ops: IntegerOps[Int, I32]
     , i64Ops: IntegerOps[Long, I64]
     , f32Ops: FloatOps[Float, F32]
     , f64Ops: FloatOps[Double, F64]
     , i32EqOps: EqOps[I32, Bool]
     , i64EqOps: EqOps[I64, Bool]
     , f32EqOps: EqOps[F32, Bool]
     , f64EqOps: EqOps[F64, Bool]
     , i32CompareOps: OrderingOps[I32, Bool]
     , i64CompareOps: OrderingOps[I64, Bool]
     , f32CompareOps: OrderingOps[F32, Bool]
     , f64CompareOps: OrderingOps[F64, Bool]
     , i32UnsignedCompareOps: UnsignedOrderingOps[I32, Bool]
     , i64UnsignedCompareOps: UnsignedOrderingOps[I64, Bool]
     , convertI32I64: ConvertIntLong[I32, I64]
     , convertI32F32: ConvertIntFloat[I32, F32]
     , convertI32F64: ConvertIntDouble[I32, F64]
     , convertI64I32: ConvertLongInt[I64, I32]
     , convertI64F32: ConvertLongFloat[I64, F32]
     , convertI64F64: ConvertLongDouble[I64, F64]
     , convertF32I32: ConvertFloatInt[F32, I32]
     , convertF32I64: ConvertFloatLong[F32, I64]
     , convertF32F64: ConvertFloatDouble[F32, F64]
     , convertF64I32: ConvertDoubleInt[F64, I32]
     , convertF64I64: ConvertDoubleLong[F64, I64]
     , convertF64F32: ConvertDoubleFloat[F64, F32]
     , encodeI32: ConvertIntBytes[I32, Bytes]
     , encodeI64: ConvertLongBytes[I64, Bytes]
     , encodeF32: ConvertFloatBytes[F32, Bytes]
     , encodeF64: ConvertDoubleBytes[F64, Bytes]
     , decodeI32: ConvertBytesInt[Bytes, I32]
     , decodeI64: ConvertBytesLong[Bytes, I64]
     , decodeF32: ConvertBytesFloat[Bytes, F32]
     , decodeF64: ConvertBytesDouble[Bytes, F64]
     , boolBranchOpsV: BooleanBranching[Bool, Value]
     , boolBranchOpsUnit: BooleanBranching[Bool, Unit]
     , funOps: FunctionOps[FunctionInstance, FuncType, Unit, FunV]
     , excOps: Exceptional[WasmException[Value], ExcV, J]
     , specOps: SpecialWasmOperations[Value, Addr, Size, FuncIx, J]
         ): WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J] with

    final val functionOps: FunctionOps[FunctionInstance, FuncType, Unit, FunV] = funOps
    final val exceptOps: Exceptional[WasmException[Value], ExcV, J] = excOps
    val specialOps: SpecialWasmOperations[Value, Addr, Size, FuncIx, J] = specOps
    val branchOpsV: BooleanBranching[Value, Value] = new LiftedBooleanBranching[Value, Bool, Value](v => v.asBoolean)(using boolBranchOpsV)
    val branchOpsUnit: BooleanBranching[Value, Unit] = new LiftedBooleanBranching[Value, Bool, Unit](v => v.asBoolean)(using boolBranchOpsUnit)

    final val i32ops: IntegerOps[Int, Value] = new LiftedIntegerOps(_.asInt32, Value.Int32.apply)
    final val i64ops: IntegerOps[Long, Value] = new LiftedIntegerOps(_.asInt64, Value.Int64.apply)
    final val f32ops: FloatOps[Float, Value] = new LiftedFloatOps(_.asFloat32, Value.Float32.apply)
    final val f64ops: FloatOps[Double, Value] = new LiftedFloatOps(_.asFloat64, Value.Float64.apply)

    final val eqOps: EqOps[Value, Value] = new EqOps[Value, Value]:
      import Value.*
      override def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(EqOps.equ(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(EqOps.equ(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(EqOps.equ(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(EqOps.equ(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      override def neq(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(EqOps.neq(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(EqOps.neq(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(EqOps.neq(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(EqOps.neq(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")

    final val compareOps: OrderingOps[Value, Value] = new OrderingOps[Value, Value]:
      import Value.*
      override def lt(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(OrderingOps.lt(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(OrderingOps.lt(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(OrderingOps.lt(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(OrderingOps.lt(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      override def le(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(OrderingOps.le(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(OrderingOps.le(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(OrderingOps.le(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(OrderingOps.le(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      override def ge(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(OrderingOps.ge(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(OrderingOps.ge(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(OrderingOps.ge(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(OrderingOps.ge(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      override def gt(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(OrderingOps.gt(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(OrderingOps.gt(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(OrderingOps.gt(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(OrderingOps.gt(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")

    final val unsignedCompareOps: UnsignedOrderingOps[Value, Value] = new UnsignedOrderingOps[Value, Value]:
      import Value.*
      override def ltUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(UnsignedOrderingOps.ltUnsigned(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(UnsignedOrderingOps.ltUnsigned(l1, l2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")
      override def leUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(UnsignedOrderingOps.leUnsigned(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(UnsignedOrderingOps.leUnsigned(l1, l2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")
      override def geUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(UnsignedOrderingOps.geUnsigned(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(UnsignedOrderingOps.geUnsigned(l1, l2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")
      override def gtUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(UnsignedOrderingOps.gtUnsigned(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(UnsignedOrderingOps.gtUnsigned(l1, l2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")

    final val convert_i32_i64: ConvertIntLong[Value, Value] = new LiftedConvert(_.asInt32, Value.Int64.apply)
    final val convert_i32_f32: ConvertIntFloat[Value, Value] = new LiftedConvert(_.asInt32, Value.Float32.apply)
    final val convert_i32_f64: ConvertIntDouble[Value, Value] = new LiftedConvert(_.asInt32, Value.Float64.apply)
    final val convert_i64_i32: ConvertLongInt[Value, Value] = new LiftedConvert(_.asInt64, Value.Int32.apply)
    final val convert_i64_f32: ConvertLongFloat[Value, Value] = new LiftedConvert(_.asInt64, Value.Float32.apply)
    final val convert_i64_f64: ConvertLongDouble[Value, Value] = new LiftedConvert(_.asInt64, Value.Float64.apply)
    final val convert_f32_i32: ConvertFloatInt[Value, Value] = new LiftedConvert(_.asFloat32, Value.Int32.apply)
    final val convert_f32_i64: ConvertFloatLong[Value, Value] = new LiftedConvert(_.asFloat32, Value.Int64.apply)
    final val convert_f32_f64: ConvertFloatDouble[Value, Value] = new LiftedConvert(_.asFloat32, Value.Float64.apply)
    final val convert_f64_i32: ConvertDoubleInt[Value, Value] = new LiftedConvert(_.asFloat64, Value.Int32.apply)
    final val convert_f64_i64: ConvertDoubleLong[Value, Value] = new LiftedConvert(_.asFloat64, Value.Int64.apply)
    final val convert_f64_f32: ConvertDoubleFloat[Value, Value] = new LiftedConvert(_.asFloat64, Value.Float32.apply)

    val LITTLE_ENDIAN = SomeCC(ByteOrder.LITTLE_ENDIAN, false)
    override final val encode = new Convert[Value, Seq[Byte], Value, Bytes, SomeCC[StoreInst | StoreNInst]]:
      override def apply(from: Value, conf: SomeCC[StoreInst | StoreNInst]): Bytes = (from, conf.t) match
        case (Value.Int32(i), _: i32.Store8) => encodeI32(i, config.BytesSize.Byte && LITTLE_ENDIAN)
        case (Value.Int32(i), _: i32.Store16) => encodeI32(i, config.BytesSize.Short && LITTLE_ENDIAN)
        case (Value.Int32(i), _: i32.Store) => encodeI32(i, config.BytesSize.Int && LITTLE_ENDIAN)
        case (Value.Int64(l), _: i64.Store8) => encodeI64(l, config.BytesSize.Byte && LITTLE_ENDIAN)
        case (Value.Int64(l), _: i64.Store16) => encodeI64(l, config.BytesSize.Short && LITTLE_ENDIAN)
        case (Value.Int64(l), _: i64.Store32) => encodeI64(l, config.BytesSize.Int && LITTLE_ENDIAN)
        case (Value.Int64(l), _: i64.Store) => encodeI64(l, config.BytesSize.Long && LITTLE_ENDIAN)
        case (Value.Float32(f), _: f32.Store) => encodeF32(f, config.BytesSize.Float && LITTLE_ENDIAN)
        case (Value.Float64(d), _: f64.Store) => encodeF64(d, config.BytesSize.Double && LITTLE_ENDIAN)
        case _ => unsupportedConfiguration(conf, this)

    override final val decode = new Convert[Seq[Byte], Value, Bytes, Value, SomeCC[LoadInst | LoadNInst]]:
      override def apply(from: Bytes, conf: SomeCC[LoadInst | LoadNInst]): Value = conf.t match
        case _: i32.Load8S => Value.Int32(decodeI32(from, config.BytesSize.Byte && LITTLE_ENDIAN && config.Bits.Signed))
        case _: i32.Load8U => Value.Int32(decodeI32(from, config.BytesSize.Byte && LITTLE_ENDIAN && config.Bits.Unsigned))
        case _: i32.Load16S => Value.Int32(decodeI32(from, config.BytesSize.Short && LITTLE_ENDIAN && config.Bits.Signed))
        case _: i32.Load16U => Value.Int32(decodeI32(from, config.BytesSize.Short && LITTLE_ENDIAN && config.Bits.Unsigned))
        case _: i32.Load => Value.Int32(decodeI32(from, config.BytesSize.Int && LITTLE_ENDIAN && config.Bits.Signed))
        case _: i64.Load8S => Value.Int64(decodeI64(from, config.BytesSize.Byte && LITTLE_ENDIAN && config.Bits.Signed))
        case _: i64.Load8U => Value.Int64(decodeI64(from, config.BytesSize.Byte && LITTLE_ENDIAN && config.Bits.Unsigned))
        case _: i64.Load16S => Value.Int64(decodeI64(from, config.BytesSize.Short && LITTLE_ENDIAN && config.Bits.Signed))
        case _: i64.Load16U => Value.Int64(decodeI64(from, config.BytesSize.Short && LITTLE_ENDIAN && config.Bits.Unsigned))
        case _: i64.Load32S => Value.Int64(decodeI64(from, config.BytesSize.Int && LITTLE_ENDIAN && config.Bits.Signed))
        case _: i64.Load32U => Value.Int64(decodeI64(from, config.BytesSize.Int && LITTLE_ENDIAN && config.Bits.Unsigned))
        case _: i64.Load => Value.Int64(decodeI64(from, config.BytesSize.Long && LITTLE_ENDIAN && config.Bits.Signed))
        case _: f32.Load => Value.Float32(decodeF32(from, LITTLE_ENDIAN))
        case _: f64.Load => Value.Float64(decodeF64(from, LITTLE_ENDIAN))

  type Instance <: GenericInstance

  abstract class GenericInstance
    extends GenericInterpreter[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J]
