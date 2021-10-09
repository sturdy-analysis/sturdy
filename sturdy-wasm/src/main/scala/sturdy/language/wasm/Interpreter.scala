package sturdy.language.wasm

import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.language.wasm.generic.*
import sturdy.values.*
import sturdy.values.config.UnsupportedConfiguration
import sturdy.values.convert.*
import sturdy.values.doubles.*
import sturdy.values.floats.*
import sturdy.values.functions.FunctionOps
import sturdy.values.ints.*
import sturdy.values.longs.*
import sturdy.values.relational.*
import swam.syntax.LoadInst
import swam.syntax.LoadNInst
import swam.syntax.MemoryInst
import swam.syntax.StoreInst
import swam.syntax.StoreNInst
import swam.syntax.{f32, f64, i64, i32}

import java.nio.ByteOrder

trait Interpreter:
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

  def topI32: I32
  def topI64: I64
  def topF32: F32
  def topF64: F64
  def asBoolean(v: Value)(using Failure): Bool
  def boolean(b: Bool): Value

  given CombineValue[W <: Widening](using Combine[I32, W], Combine[I64, W], Combine[F32, W], Combine[F64, W]): Combine[Value, W] with
    import Value.*
    override def apply(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(Combine[I32, W](i1, i2))
      case (Int64(l1), Int64(l2)) => Int64(Combine[I64, W](l1, l2))
      case (Float32(f1), Float32(f2)) => Float32(Combine[F32, W](f1, f2))
      case (Float64(d1), Float64(d2)) => Float64(Combine[F64, W](d1, d2))
      case _ => TopValue

  type Addr
  type Bytes
  type Size
  type ExcV
  type FuncIx
  type FunV
  type Symbol
  type Entry
  type Effects <: GenericEffects[Value, Addr, Bytes, Size, ExcV, Symbol, Entry]

  given ValueWasmOps
    (using failure: Failure
         , i32Ops: IntOps[I32]
         , i64Ops: LongOps[I64]
         , f32Ops: FloatOps[F32]
         , f64Ops: DoubleOps[F64]
         , i32EqOps: EqOps[I32, Bool]
         , i64EqOps: EqOps[I64, Bool]
         , f32EqOps: EqOps[F32, Bool]
         , f64EqOps: EqOps[F64, Bool]
         , i32CompareOps: CompareOps[I32, Bool]
         , i64CompareOps: CompareOps[I64, Bool]
         , f32CompareOps: CompareOps[F32, Bool]
         , f64CompareOps: CompareOps[F64, Bool]
         , i32UnsignedCompareOps: UnsignedCompareOps[I32, Bool]
         , i64UnsignedCompareOps: UnsignedCompareOps[I64, Bool]
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
         , funOps: FunctionOps[FunctionInstance[Value], Nothing, Unit, FunV]
         , encodeI32: ConvertIntBytes[I32, Bytes]
         , encodeI64: ConvertLongBytes[I64, Bytes]
         , encodeF32: ConvertFloatBytes[F32, Bytes]
         , encodeF64: ConvertDoubleBytes[F64, Bytes]
         , decodeI32: ConvertBytesInt[Bytes, I32]
         , decodeI64: ConvertBytesLong[Bytes, I64]
         , decodeF32: ConvertBytesFloat[Bytes, F32]
         , decodeF64: ConvertBytesDouble[Bytes, F64]
         ): WasmOps[Value, FunV, Bytes] with

    final val functionOps: FunctionOps[FunctionInstance[Value], Nothing, Unit, FunV] = funOps

    final val intOps: IntOps[Value] = new LiftedIntOps(_.asInt32, Value.Int32.apply)
    final val longOps: LongOps[Value] = new LiftedLongOps(_.asInt64, Value.Int64.apply)
    final val floatOps: FloatOps[Value] = new LiftedFloatOps(_.asFloat32, Value.Float32.apply)
    final val doubleOps: DoubleOps[Value] = new LiftedDoubleOps(_.asFloat64, Value.Float64.apply)

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

    final val compareOps: CompareOps[Value, Value] = new CompareOps[Value, Value]:
      import Value.*
      override def lt(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(CompareOps.lt(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(CompareOps.lt(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(CompareOps.lt(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(CompareOps.lt(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      override def le(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(CompareOps.le(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(CompareOps.le(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(CompareOps.le(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(CompareOps.le(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      override def ge(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(CompareOps.ge(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(CompareOps.ge(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(CompareOps.ge(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(CompareOps.ge(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      override def gt(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(CompareOps.gt(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(CompareOps.gt(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(CompareOps.gt(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(CompareOps.gt(d1, d2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")

    final val unsignedCompareOps: UnsignedCompareOps[Value, Value] = new UnsignedCompareOps[Value, Value]:
      import Value.*
      override def ltUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(UnsignedCompareOps.ltUnsigned(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(UnsignedCompareOps.ltUnsigned(l1, l2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")
      override def leUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(UnsignedCompareOps.leUnsigned(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(UnsignedCompareOps.leUnsigned(l1, l2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")
      override def geUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(UnsignedCompareOps.geUnsigned(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(UnsignedCompareOps.geUnsigned(l1, l2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")
      override def gtUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(UnsignedCompareOps.gtUnsigned(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(UnsignedCompareOps.gtUnsigned(l1, l2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")

    final val convertIntLong: ConvertIntLong[Value, Value] = new LiftedConvert(_.asInt32, Value.Int64.apply)
    final val convertIntFloat: ConvertIntFloat[Value, Value] = new LiftedConvert(_.asInt32, Value.Float32.apply)
    final val convertIntDouble: ConvertIntDouble[Value, Value] = new LiftedConvert(_.asInt32, Value.Float64.apply)
    final val convertLongInt: ConvertLongInt[Value, Value] = new LiftedConvert(_.asInt64, Value.Int32.apply)
    final val convertLongFloat: ConvertLongFloat[Value, Value] = new LiftedConvert(_.asInt64, Value.Float32.apply)
    final val convertLongDouble: ConvertLongDouble[Value, Value] = new LiftedConvert(_.asInt64, Value.Float64.apply)
    final val convertFloatInt: ConvertFloatInt[Value, Value] = new LiftedConvert(_.asFloat32, Value.Int32.apply)
    final val convertFloatLong: ConvertFloatLong[Value, Value] = new LiftedConvert(_.asFloat32, Value.Int64.apply)
    final val convertFloatDouble: ConvertFloatDouble[Value, Value] = new LiftedConvert(_.asFloat32, Value.Float64.apply)
    final val convertDoubleInt: ConvertDoubleInt[Value, Value] = new LiftedConvert(_.asFloat64, Value.Int32.apply)
    final val convertDoubleLong: ConvertDoubleLong[Value, Value] = new LiftedConvert(_.asFloat64, Value.Int64.apply)
    final val convertDoubleFloat: ConvertDoubleFloat[Value, Value] = new LiftedConvert(_.asFloat64, Value.Float32.apply)

    override final val encode: Convert[Value, Seq[Byte], Value, Bytes, StoreInst | StoreNInst] = new Convert:
      override def apply(from: Value, conf: StoreInst | StoreNInst): Bytes = (from, conf) match
        case (Value.Int32(i), _: i32.Store8) => encodeI32(i, (config.BytesSize.Byte, ByteOrder.LITTLE_ENDIAN))
        case (Value.Int32(i), _: i32.Store16) => encodeI32(i, (config.BytesSize.Short, ByteOrder.LITTLE_ENDIAN))
        case (Value.Int32(i), _: i32.Store) => encodeI32(i, (config.BytesSize.Int, ByteOrder.LITTLE_ENDIAN))
        case (Value.Int64(l), _: i64.Store8) => encodeI64(l, (config.BytesSize.Byte, ByteOrder.LITTLE_ENDIAN))
        case (Value.Int64(l), _: i64.Store16) => encodeI64(l, (config.BytesSize.Short, ByteOrder.LITTLE_ENDIAN))
        case (Value.Int64(l), _: i64.Store32) => encodeI64(l, (config.BytesSize.Int, ByteOrder.LITTLE_ENDIAN))
        case (Value.Int64(l), _: i64.Store) => encodeI64(l, (config.BytesSize.Long, ByteOrder.LITTLE_ENDIAN))
        case (Value.Float32(f), _: f32.Store) => encodeF32(f, ByteOrder.LITTLE_ENDIAN)
        case (Value.Float64(d), _: f64.Store) => encodeF64(d, ByteOrder.LITTLE_ENDIAN)
        case _ => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)

    override final val decode: Convert[Seq[Byte], Value, Bytes, Value, LoadInst | LoadNInst] = new Convert:
      override def apply(from: Bytes, conf: LoadInst | LoadNInst): Value = conf match
        case _: i32.Load8S => Value.Int32(decodeI32(from, (config.BytesSize.Byte, ByteOrder.LITTLE_ENDIAN, config.Bits.Signed)))
        case _: i32.Load8U => Value.Int32(decodeI32(from, (config.BytesSize.Byte, ByteOrder.LITTLE_ENDIAN, config.Bits.Unsigned)))
        case _: i32.Load16S => Value.Int32(decodeI32(from, (config.BytesSize.Short, ByteOrder.LITTLE_ENDIAN, config.Bits.Signed)))
        case _: i32.Load16U => Value.Int32(decodeI32(from, (config.BytesSize.Short, ByteOrder.LITTLE_ENDIAN, config.Bits.Unsigned)))
        case _: i32.Load => Value.Int32(decodeI32(from, (config.BytesSize.Int, ByteOrder.LITTLE_ENDIAN, config.Bits.Signed)))
        case _: i64.Load8S => Value.Int64(decodeI64(from, (config.BytesSize.Byte, ByteOrder.LITTLE_ENDIAN, config.Bits.Signed)))
        case _: i64.Load8U => Value.Int64(decodeI64(from, (config.BytesSize.Byte, ByteOrder.LITTLE_ENDIAN, config.Bits.Unsigned)))
        case _: i64.Load16S => Value.Int64(decodeI64(from, (config.BytesSize.Short, ByteOrder.LITTLE_ENDIAN, config.Bits.Signed)))
        case _: i64.Load16U => Value.Int64(decodeI64(from, (config.BytesSize.Short, ByteOrder.LITTLE_ENDIAN, config.Bits.Unsigned)))
        case _: i64.Load32S => Value.Int64(decodeI64(from, (config.BytesSize.Int, ByteOrder.LITTLE_ENDIAN, config.Bits.Signed)))
        case _: i64.Load32U => Value.Int64(decodeI64(from, (config.BytesSize.Int, ByteOrder.LITTLE_ENDIAN, config.Bits.Unsigned)))
        case _: i64.Load => Value.Int64(decodeI64(from, (config.BytesSize.Long, ByteOrder.LITTLE_ENDIAN, config.Bits.Signed)))
        case _: f32.Load => Value.Float32(decodeF32(from, ByteOrder.LITTLE_ENDIAN))
        case _: f64.Load => Value.Float64(decodeF64(from, ByteOrder.LITTLE_ENDIAN))

  type Instance <: GenericInterpreter[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, Symbol, Entry, Effects]

  trait GenericInstance extends GenericInterpreter[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, Symbol, Entry, Effects]
