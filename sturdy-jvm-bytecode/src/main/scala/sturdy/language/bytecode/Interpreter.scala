package sturdy.language.bytecode

import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.language.bytecode.generic.{BytecodeOps, GenericInterpreter}
import sturdy.values.floating.*
import sturdy.values.integer.*
trait Interpreter:
  type I32
  type I64
  type F32
  type F64

  enum Value:
    case TopValue
    case Int32(i: I32)
    case Int64(l: I64)
    case Float32(f: F32)
    case Float64(d: F64)

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
      i32Ops: IntegerOps[Int, I32]
    , i64Ops: IntegerOps[Long, I64]
    , f32Ops: FloatOps[Float, F32]
    , f64Ios: FloatOps[Double, F64]
  
      ): BytecodeOps[Value] with
  
  
    final val i32ops: IntegerOps[Int, Value] = new LiftedIntegerOps(_.asInt32, Value.Int32.apply)
    final val i64ops: IntegerOps[Long, Value] = new LiftedIntegerOps(_.asInt64, Value.Int64.apply)
    final val f32ops: FloatOps[Float, Value] = new LiftedFloatOps(_.asFloat32, Value.Float32.apply)
    final val f64ops: FloatOps[Double, Value] = new LiftedFloatOps(_.asFloat64, Value.Float64.apply)
  
  
  type Instance <: GenericInstance
  abstract class GenericInstance extends GenericInterpreter[Value]
