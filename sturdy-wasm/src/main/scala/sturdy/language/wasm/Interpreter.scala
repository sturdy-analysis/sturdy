package sturdy.language.wasm

import sturdy.fix.Widening
import sturdy.language.wasm.generic.GenericInterpreter
import sturdy.language.wasm.generic.GenericInterpreter.GenericEffects
import sturdy.language.wasm.generic.WasmOperations
import sturdy.values.JoinValue
import sturdy.values.Top
import sturdy.values.convert.LiftedConvert
import sturdy.values.doubles.*
import sturdy.values.floats.*
import sturdy.values.ints.*
import sturdy.values.longs.*
import sturdy.values.relational.*

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

    def asBoolean: Bool = Interpreter.this.asBoolean(this)

    def asInt32: I32 = this match
      case Int32(i) => i
      case TopValue => topI32
      case _ => throw new IllegalArgumentException(s"Expected i32 but got $this")

    def asInt64: I64 = this match
      case Int64(l) => l
      case TopValue => topI64
      case _ => throw new IllegalArgumentException(s"Expected i64 but got $this")

    def asFloat32: F32 = this match
      case Float32(f) => f
      case TopValue => topF32
      case _ => throw new IllegalArgumentException(s"Expected f32 but got $this")

    def asFloat64: F64 = this match
      case Float64(d) => d
      case TopValue => topF64
      case _ => throw new IllegalArgumentException(s"Expected f64 but got $this")

  def topI32: I32
  def topI64: I64
  def topF32: F32
  def topF64: F64
  def asBoolean(v: Value): Bool
  def boolean(b: Bool): Value

  given liftedJoinValue(using JoinValue[I32], JoinValue[I64], JoinValue[F32], JoinValue[F64]): JoinValue[Value] with
    import Value.*
    override def joinValues(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(JoinValue.join(i1, i2))
      case (Int64(l1), Int64(l2)) => Int64(JoinValue.join(l1, l2))
      case (Float32(f1), Float32(f2)) => Float32(JoinValue.join(f1, f2))
      case (Float64(d1), Float64(d2)) => Float64(JoinValue.join(d1, d2))
      case _ => TopValue

  given liftedWideningValue(using Widening[I32], Widening[I64], Widening[F32], Widening[F64]): Widening[Value] with
    import Value.*
    override def widen(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(Widening.widen(i1, i2))
      case (Int64(l1), Int64(l2)) => Int64(Widening.widen(l1, l2))
      case (Float32(f1), Float32(f2)) => Float32(Widening.widen(f1, f2))
      case (Float64(d1), Float64(d2)) => Float64(Widening.widen(d1, d2))
      case _ => TopValue

  type Addr
  type Bytes
  type Size
  type ExcV
  type FuncIx
  type FunV
  type Effects <: GenericEffects[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV]

  type Instance <: GenericInstance[Effects]

  trait GenericInstance[Effects <: GenericEffects[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV]] 
    extends GenericInterpreter[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, Effects]:
    
    implicit def i32Ops: IntOps[I32]
    implicit def i64Ops: LongOps[I64]
    implicit def f32Ops: FloatOps[F32]
    implicit def f64Ops: DoubleOps[F64]
    implicit def i32EqOps: EqOps[I32, Bool]
    implicit def i64EqOps: EqOps[I64, Bool]
    implicit def f32EqOps: EqOps[F32, Bool]
    implicit def f64EqOps: EqOps[F64, Bool]
    implicit def i32CompareOps: CompareOps[I32, Bool]
    implicit def i64CompareOps: CompareOps[I64, Bool]
    implicit def f32CompareOps: CompareOps[F32, Bool]
    implicit def f64CompareOps: CompareOps[F64, Bool]
    implicit def i32UnsignedCompareOps: UnsignedCompareOps[I32, Bool]
    implicit def i64UnsignedCompareOps: UnsignedCompareOps[I64, Bool]
    implicit def convertI32I64: ConvertIntLong[I32, I64]
    implicit def convertI32F32: ConvertIntFloat[I32, F32]
    implicit def convertI32F64: ConvertIntDouble[I32, F64]
    implicit def convertI64I32: ConvertLongInt[I64, I32]
    implicit def convertI64F32: ConvertLongFloat[I64, F32]
    implicit def convertI64F64: ConvertLongDouble[I64, F64]
    implicit def convertF32I32: ConvertFloatInt[F32, I32]
    implicit def convertF32I64: ConvertFloatLong[F32, I64]
    implicit def convertF32F64: ConvertFloatDouble[F32, F64]
    implicit def convertF64I32: ConvertDoubleInt[F64, I32]
    implicit def convertF64I64: ConvertDoubleLong[F64, I64]
    implicit def convertF64F32: ConvertDoubleFloat[F64, F32]

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

