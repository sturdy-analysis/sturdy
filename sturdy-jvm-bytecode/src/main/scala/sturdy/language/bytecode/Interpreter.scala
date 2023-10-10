package sturdy.language.bytecode

import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.language.bytecode.generic.{BytecodeOps, GenericInterpreter}
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.convert.*
trait Interpreter:
  //type I8
  //type I16
  type I32
  type I64
  type F32
  type F64

  enum Value:
    case TopValue
    //case Int8(b: I8)
    //case Int16(s: I16)
    case Int32(i: I32)
    case Int64(l: I64)
    case Float32(f: F32)
    case Float64(d: F64)


    /*def asInt8: I8 = this match
      case Int8(b) => b
      case TopValue => topI8
      case _ => ??? //f.fail(TypeError, s"Expected i8 but got $this")
    def asInt16: I16 = this match
      case Int16(s) => s
      case TopValue => topI16
      case _ => ??? //f.fail(TypeError, s"Expected i32 but got $this")*/
    def asInt32: I32 = this match
      case Int32(i) => i
      case TopValue => topI32
      case _ => ??? //f.fail(TypeError, s"Expected i32 but got $this")

    def asInt64: I64 = this match
      case Int64(l) => l
      case TopValue => topI64
      case _ => ??? //f.fail(TypeError, s"Expected i64 but got $this")

    def asFloat32: F32 = this match
      case Float32(f) => f
      case TopValue => topF32
      case _ => ??? //f.fail(TypeError, s"Expected f32 but got $this")

    def asFloat64: F64 = this match
      case Float64(d) => d
      case TopValue => topF64
      case _ => ??? //f.fail(TypeError, s"Expected f64 but got $this")

  //def topI8: I8
  //def topI16: I16
  def topI32: I32
  def topI64: I64
  def topF32: F32
  def topF64: F64

  /*
  def typedTop(ty: ValType): Value = ty match
    case ValType.I32 => Value.Int32(topI32)
    case ValType.I64 => Value.Int64(topI64)
    case ValType.F32 => Value.Float32(topF32)
    case ValType.F64 => Value.Float64(topF64)

  */

  given ValueBytecodeOps
    (using
      //i8Ops: IntegerOps[Byte, I8]
    //, i16Ops: IntegerOps[Short, I16]
      i32Ops: IntegerOps[Int, I32]
    , i64Ops: IntegerOps[Long, I64]
    , f32Ops: FloatOps[Float, F32]
    , f64Ops: FloatOps[Double, F64]
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
      ): BytecodeOps[Value] with

    //final val i8ops: IntegerOps[Byte, Value] = new LiftedIntegerOps(_.asInt8, Value.Int8.apply)
    //final val i16ops: IntegerOps[Short, Value] = new LiftedIntegerOps(_.asInt16, Value.Int16.apply)
    final val i32ops: IntegerOps[Int, Value] = new LiftedIntegerOps(_.asInt32, Value.Int32.apply)
    final val i64ops: IntegerOps[Long, Value] = new LiftedIntegerOps(_.asInt64, Value.Int64.apply)
    final val f32ops: FloatOps[Float, Value] = new LiftedFloatOps(_.asFloat32, Value.Float32.apply)
    final val f64ops: FloatOps[Double, Value] = new LiftedFloatOps(_.asFloat64, Value.Float64.apply)


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
  
  type Instance <: GenericInstance
  abstract class GenericInstance extends GenericInterpreter[Value]
