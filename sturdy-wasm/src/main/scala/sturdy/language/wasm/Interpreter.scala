package sturdy.language.wasm

import sturdy.fix.Widening
import sturdy.language.wasm.generic.GenericInterpreter
import sturdy.language.wasm.generic.WasmOperations
import sturdy.values.JoinValue
import sturdy.values.Top
import sturdy.values.convert.LiftedConvert
import sturdy.values.doubles.*
import sturdy.values.floats.*
import sturdy.values.ints.*
import sturdy.values.longs.*
import sturdy.values.relational.CompareOps
import sturdy.values.relational.EqOps

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

  given liftedIntOps(using IntOps[I32]): IntOps[Value] = new LiftedIntOps(_.asInt32, Value.Int32.apply)
  given liftedLongOps(using LongOps[I64]): LongOps[Value] = new LiftedLongOps(_.asInt64, Value.Int64.apply)
  given liftedFloatOps(using FloatOps[F32]): FloatOps[Value] = new LiftedFloatOps(_.asFloat32, Value.Float32.apply)
  given liftedDoubleOps(using DoubleOps[F64]): DoubleOps[Value] = new LiftedDoubleOps(_.asFloat64, Value.Float64.apply)
  given liftedEqOps(using EqOps[I32, I32], EqOps[I64, I32], EqOps[F32, I32], EqOps[F64, I32]): EqOps[Value, Value] = new EqOps[Value, Value]:
    import Value.*
    override def equ(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(EqOps.equ(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(EqOps.equ(l1, l2))
      case (Float32(f1), Float32(f2)) => Int32(EqOps.equ(f1, f2))
      case (Float64(d1), Float64(d2)) => Int32(EqOps.equ(d1, d2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    override def neq(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(EqOps.neq(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(EqOps.neq(l1, l2))
      case (Float32(f1), Float32(f2)) => Int32(EqOps.neq(f1, f2))
      case (Float64(d1), Float64(d2)) => Int32(EqOps.neq(d1, d2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
  given liftedCompareOps(using CompareOps[I32, I32], CompareOps[I64, I32], CompareOps[F32, I32], CompareOps[F64, I32]): CompareOps[Value, Value] = new CompareOps[Value, Value]:
    import Value.*
    override def lt(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(CompareOps.lt(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(CompareOps.lt(l1, l2))
      case (Float32(f1), Float32(f2)) => Int32(CompareOps.lt(f1, f2))
      case (Float64(d1), Float64(d2)) => Int32(CompareOps.lt(d1, d2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    override def le(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(CompareOps.le(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(CompareOps.le(l1, l2))
      case (Float32(f1), Float32(f2)) => Int32(CompareOps.le(f1, f2))
      case (Float64(d1), Float64(d2)) => Int32(CompareOps.le(d1, d2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    override def ge(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(CompareOps.ge(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(CompareOps.ge(l1, l2))
      case (Float32(f1), Float32(f2)) => Int32(CompareOps.ge(f1, f2))
      case (Float64(d1), Float64(d2)) => Int32(CompareOps.ge(d1, d2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    override def gt(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(CompareOps.gt(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(CompareOps.gt(l1, l2))
      case (Float32(f1), Float32(f2)) => Int32(CompareOps.gt(f1, f2))
      case (Float64(d1), Float64(d2)) => Int32(CompareOps.gt(d1, d2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
  given liftedIntCompareOps(using IntCompareOps[I32, I32], IntCompareOps[I64, I32]): IntCompareOps[Value, Value] = new IntCompareOps[Value, Value]:
    import Value.*
    override def ltUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(IntCompareOps.ltUnsigned(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(IntCompareOps.ltUnsigned(l1, l2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")
    override def leUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(IntCompareOps.leUnsigned(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(IntCompareOps.leUnsigned(l1, l2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")
    override def geUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(IntCompareOps.geUnsigned(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(IntCompareOps.geUnsigned(l1, l2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")
    override def gtUnsigned(v1: Value, v2: Value): Value = (v1, v2) match
      case (Int32(i1), Int32(i2)) => Int32(IntCompareOps.gtUnsigned(i1, i2))
      case (Int64(l1), Int64(l2)) => Int32(IntCompareOps.gtUnsigned(l1, l2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal integer type but got $v1 and $v2")

  given liftedConvertIntLong(using ConvertIntLong[I32, I64]): ConvertIntLong[Value, Value] = new LiftedConvert(_.asInt32, Value.Int64.apply)
  given liftedConvertIntFloat(using ConvertIntFloat[I32, F32]): ConvertIntFloat[Value, Value] = new LiftedConvert(_.asInt32, Value.Float32.apply)
  given liftedConvertIntDouble(using ConvertIntDouble[I32, F64]): ConvertIntDouble[Value, Value] = new LiftedConvert(_.asInt32, Value.Float64.apply)
  given liftedConvertLongInt(using ConvertLongInt[I64, I32]): ConvertLongInt[Value, Value] = new LiftedConvert(_.asInt64, Value.Int32.apply)
  given liftedConvertLongFloat(using ConvertLongFloat[I64, F32]): ConvertLongFloat[Value, Value] = new LiftedConvert(_.asInt64, Value.Float32.apply)
  given liftedConvertLongDouble(using ConvertLongDouble[I64, F64]): ConvertLongDouble[Value, Value] = new LiftedConvert(_.asInt64, Value.Float64.apply)
  given liftedConvertFloatInt(using ConvertFloatInt[F32, I32]): ConvertFloatInt[Value, Value] = new LiftedConvert(_.asFloat32, Value.Int32.apply)
  given liftedConvertFloatLong(using ConvertFloatLong[F32, I64]): ConvertFloatLong[Value, Value] = new LiftedConvert(_.asFloat32, Value.Int64.apply)
  given liftedConvertFloatDouble(using ConvertFloatDouble[F32, F64]): ConvertFloatDouble[Value, Value] = new LiftedConvert(_.asFloat32, Value.Float64.apply)
  given liftedConvertDoubleInt(using ConvertDoubleInt[F64, I32]): ConvertDoubleInt[Value, Value] = new LiftedConvert(_.asFloat64, Value.Int32.apply)
  given liftedConvertDoubleLong(using ConvertDoubleLong[F64, I64]): ConvertDoubleLong[Value, Value] = new LiftedConvert(_.asFloat64, Value.Int64.apply)
  given liftedConvertDoubleFloat(using ConvertDoubleFloat[F64, F32]): ConvertDoubleFloat[Value, Value] = new LiftedConvert(_.asFloat64, Value.Float32.apply)

  type Addr
  type Bytes
  type Size
  type Effects <: GenericInterpreter.Effects[Value, Addr, Bytes, Size]

  type Instance <: GenericInstance
  abstract class GenericInstance
    (effects: Effects, wasmOps: WasmOperations[Value])
    (using IntOps[I32], LongOps[I64], FloatOps[F32], DoubleOps[F64],
     EqOps[I32, I32], EqOps[I64, I32], EqOps[F32, I32], EqOps[F64, I32],
     CompareOps[I32, I32], CompareOps[I64, I32], CompareOps[F32, I32], CompareOps[F64, I32], IntCompareOps[I32, I32], IntCompareOps[I64, I32],
     ConvertIntLong[I32, I64], ConvertIntFloat[I32, F32], ConvertIntDouble[I32, F64],
     ConvertLongInt[I64, I32], ConvertLongFloat[I64, F32], ConvertLongDouble[I64, F64],
     ConvertFloatInt[F32, I32], ConvertFloatLong[F32, I64], ConvertFloatDouble[F32, F64],
     ConvertDoubleInt[F64, I32], ConvertDoubleLong[F64, I64], ConvertDoubleFloat[F64, F32]
    )
    (using effects.BoolBranchJoin[Unit],
     effects.MemoryJoin[Unit], effects.MemoryJoin[Value], effects.MemoryJoinComp,
     effects.TableJoin[Unit], effects.TableJoinComp,
     wasmOps.WasmOpsJoin[Unit], wasmOps.WasmOpsJoinComp)
    extends GenericInterpreter[Value, Addr, Bytes, Size](effects, wasmOps)
