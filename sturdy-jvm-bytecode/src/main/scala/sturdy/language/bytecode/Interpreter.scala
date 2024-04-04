package sturdy.language.bytecode

import sturdy.data.MayJoin.NoJoin
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.language.bytecode.generic.{AllocationSite, BytecodeOps, GenericInterpreter, JvmExcept}
import sturdy.values.booleans.{BooleanBranching, LiftedBooleanBranching}
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.convert.*
import sturdy.values.relational.*
import generic.BytecodeFailure.*
import org.opalj.br.ObjectType
import sturdy.data.MayJoin
import sturdy.effect.except.{ConcreteExcept, Except}
import sturdy.values.exceptions.ConcreteExceptional
import sturdy.values.objects.{ConcreteObjectOps, LiftedObjectOps, ObjectOps, TypeOps}
import sturdy.values.arrays.{ArrayOps, ConcreteArrayOps, LiftedArrayOps}
trait Interpreter:
  //type I8
  //type I16
  type I32
  type I64
  type F32
  type F64
  type Bool
  
  type Mth
  type MthName
  type MthSig
  type Addr
  type Idx
  
  type TypeRep
  type NullVal
  type FieldName
  type OID
  type ObjType
  type ObjRep

  type AID
  type AType
  type ArrayRep

  val except: Except[JvmExcept[Value], JvmExcept[Value], MayJoin.NoJoin] = new ConcreteExcept
  enum Value:
    case TopValue
    //case Int8(b: I8)
    //case Int16(s: I16)
    case Int32(i: I32)
    case Int64(l: I64)
    case Float32(f: F32)
    case Float64(d: F64)
    case Obj(o: ObjRep)
    case Array(a: ArrayRep)
    case Null(n: NullVal)

    def asBoolean(using Failure): Bool = Interpreter.this.asBoolean(this)
    /*def asInt8: I8 = this match
      case Int8(b) => b
      case TopValue => topI8
      case _ => ??? //f.fail(TypeError, s"Expected i8 but got $this")
    def asInt16: I16 = this match
      case Int16(s) => s
      case TopValue => topI16
      case _ => ??? //f.fail(TypeError, s"Expected i32 but got $this")*/
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
    def asObj(using f: Failure): ObjRep = this match
      case Obj(o) => o
      case Null(n) => except.throws(JvmExcept.Throw(ObjectType("java/lang/NullPointerException")))
      case TopValue => topObj
      case _ => f.fail(TypeError, s"Expected obj but got $this")
    def asArray(using f: Failure): ArrayRep = this match
      case Array(a) => a
      case TopValue => topArray
      case _ => f.fail(TypeError, s"Expected array but got $this")
    def asNull(using f: Failure): NullVal = this match
      case Null(n) => n
      case TopValue => topNull
      case _ => f.fail(TypeError, s"Expected null but got $this")


  //def topI8: I8
  //def topI16: I16
  def topI32: I32
  def topI64: I64
  def topF32: F32
  def topF64: F64
  def topObj: ObjRep
  def topArray: ArrayRep
  def topNull: NullVal

  /*
  def typedTop(ty: ValType): Value = ty match
    case ValType.I32 => Value.Int32(topI32)
    case ValType.I64 => Value.Int64(topI64)
    case ValType.F32 => Value.Float32(topF32)
    case ValType.F64 => Value.Float64(topF64)

  */

  def asBoolean(v: Value)(using Failure): Bool
  def boolean(b: Bool): Value

  given ValueBytecodeOps
    (using failure: Failure
      //i8Ops: IntegerOps[Byte, I8]
    //, i16Ops: IntegerOps[Short, I16]
    , i32Ops: IntegerOps[Int, I32]
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
    , boolBranchOpsV: BooleanBranching[Bool, Value]
    , boolBranchOpsUnit: BooleanBranching[Bool, Unit]
    , i32CompareOps: OrderingOps[I32, Bool]
    , i64CompareOps: OrderingOps[I64, Bool]
    , f32CompareOps: OrderingOps[F32, Bool]
    , f64CompareOps: OrderingOps[F64, Bool]
    , i32EqOps: EqOps[I32, Bool]
    , i64EqOps: EqOps[I64, Bool]
    , f32EqOps: EqOps[F32, Bool]
    , f64EqOps: EqOps[F64, Bool]
    , objEqOps: EqOps[ObjRep, Bool]
    , arrayEqOps: EqOps[ArrayRep, Bool]
    , objTypeOps: TypeOps[ObjRep, TypeRep, Bool]
    , arrayTypeOps: TypeOps[ArrayRep, TypeRep, Bool]
    , nullTypeOps: TypeOps[NullVal, TypeRep, Bool]
    //, objOps: ObjectOps[Addr, Idx, Value, ObjType, ObjRep]
      ): BytecodeOps[Addr, Idx, Value, TypeRep] with


    val branchOpsV: BooleanBranching[Value, Value] = new LiftedBooleanBranching[Value, Bool, Value](v => v.asBoolean)(using boolBranchOpsV)
    val branchOpsUnit: BooleanBranching[Value, Unit] = new LiftedBooleanBranching[Value, Bool, Unit](v => v.asBoolean)(using boolBranchOpsUnit)

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

    final val eqOps: EqOps[Value, Value] = new EqOps[Value, Value]:
      import Value.*
      override def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(EqOps.equ(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(EqOps.equ(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(EqOps.equ(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(EqOps.equ(d1, d2))
        case (Obj(o1), Obj(o2)) => boolean(EqOps.equ(o1, o2))
        case (Array(a1), Array(a2)) => boolean(EqOps.equ(a1, a2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      override def neq(v1: Value, v2: Value): Value = (v1, v2) match
        case (Int32(i1), Int32(i2)) => boolean(EqOps.neq(i1, i2))
        case (Int64(l1), Int64(l2)) => boolean(EqOps.neq(l1, l2))
        case (Float32(f1), Float32(f2)) => boolean(EqOps.neq(f1, f2))
        case (Float64(d1), Float64(d2)) => boolean(EqOps.neq(d1, d2))
        case (Obj(o1), Obj(o2)) => boolean(EqOps.neq(o1, o2))
        case (Array(a1), Array(a2)) => boolean(EqOps.neq(a1, a2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")

    final val typeOps: TypeOps[Value, TypeRep, Value] = new TypeOps[Value, TypeRep, Value]:
      import Value.*

      override def instanceOf(v: Value, check: TypeRep): Value = v match
        case Obj(o1) => boolean(TypeOps.instanceOf(o1, check))
        case Array(a1) => boolean(TypeOps.instanceOf(a1, check))
        case Null(n1) => boolean(TypeOps.instanceOf(n1, check))
        case _ => throw new IllegalArgumentException(s"Expected values of type object or array but got $v")
    //final val f32compare: OrderingOps[Value, Value] = new LiftedOrderingOps(_.asFloat32, Value.Int32.apply)
    //final val f64compare: OrderingOps[Value, Value] = new LiftedOrderingOps(_.asFloat64, Value.Int32.apply)

  type Instance <: GenericInstance
  abstract class GenericInstance extends GenericInterpreter[Value, Addr, Idx, OID, AID, ObjType, ObjRep, TypeRep, NoJoin]

