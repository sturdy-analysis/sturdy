package sturdy.language.tip

import sturdy.data.MayJoin
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.language.tip.GenericInterpreter.{FixIn, FixOut, Field, TypeError, AllocationSite}
import sturdy.values.MaybeChanged
import sturdy.values.booleans.*
import sturdy.values.{Finite, Top, Combine, Widening}
import sturdy.values.functions.{LiftedFunctionOps, FunctionOps}
import sturdy.values.integer.{IntegerOps, LiftedIntegerOps}
import sturdy.values.records.{LiftedRecordOps, RecordOps}
import sturdy.values.references.{ReferenceOps, LiftedReferenceOps}
import sturdy.values.relational.{OrderingOps, EqOps, LiftedOrderingOps}

trait Interpreter:
  type J[A] <: MayJoin[A]
  type VBool
  type VInt
  type VRef
  type VFun
  type VRecord

  enum Value:
    case TopValue
    case IntValue(i: VInt)
    case RefValue(addr: VRef)
    case FunValue(fun: VFun)
    case RecValue(rec: VRecord)

    def asBoolean(using Failure): VBool = Interpreter.this.asBoolean(this)
    def asInt(using inst: Instance): VInt = this match
      case IntValue(i) => i
      case TopValue => topInt
      case _ => inst.failure(TypeError, s"Expected Int but got $this")
    def asFunction(using inst: Instance): VFun = this match
      case FunValue(f) => f
      case TopValue => topFun
      case _ => inst.failure(TypeError, s"Expected Function but got $this")
    def asReference(using inst: Instance): VRef = this match
      case RefValue(a) => a
      case TopValue => topReference
      case _ => inst.failure(TypeError, s"Expected Reference but got $this")
    def asRecord(using inst: Instance): VRecord = this match
      case RecValue(rec) => rec
      case TopValue => topRecord
      case _ => inst.failure(TypeError, s"Expected Record but got $this")

  def topInt(using Instance): VInt
  def topFun(using Instance): VFun
  def topReference(using Instance): VRef
  def topRecord(using Instance): VRecord

  def asBoolean(v: Value)(using Failure): VBool
  def boolean(b: VBool): Value

  given Top[Value] with
    def top = Value.TopValue

  type Addr

  given CombineValue[W <: Widening](using Combine[VInt, W], Combine[VFun, W], Combine[VRef, W], Combine[VRecord, W]): Combine[Value, W] with
    import Value.*
    override def apply(v1: Value, v2: Value): MaybeChanged[Value] = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => Combine[VInt, W](i1, i2).map(IntValue.apply)
      case (FunValue(funs1), FunValue(funs2)) => Combine[VFun, W](funs1, funs2).map(FunValue.apply)
      case (RefValue(addrs1), RefValue(addrs2)) => Combine[VRef, W](addrs1, addrs2).map(RefValue.apply)
      case (RecValue(rec1), RecValue(rec2)) => Combine[VRecord, W](rec1, rec2).map(RecValue.apply)
      case _ => MaybeChanged(TopValue, v1)

  given FiniteValue(using Finite[VInt], Finite[VFun], Finite[VRef], Finite[VRecord]): Finite[Value] with {}

  type Instance <: GenericInstance
  abstract class GenericInstance
    extends GenericInterpreter[Value, Addr, J]:

    given Instance = this.asInstanceOf[Instance]

    def vintOps: IntegerOps[Int, VInt]
    def vcompareOps: OrderingOps[VInt, VBool]
    def vintEqOps: EqOps[VInt, VBool]
    def vrefEqOps: EqOps[VRef, VBool]
    def vfunEqOps: EqOps[VFun, VBool]
    def vrecEqOps: EqOps[VRecord, VBool]
    def vfunOps: FunctionOps[Function, Seq[Value], Value, VFun]
    def vrefOps: ReferenceOps[Addr, VRef]
    def vrecOps: RecordOps[Field, Value, VRecord]
    def vbranchOps: BooleanBranching[VBool, Unit]

    import Value.*
    final val intOps = new LiftedIntegerOps[Int, Value, VInt](_.asInt, IntValue.apply)(using vintOps)
    final val compareOps = new LiftedOrderingOps[Value, Value, VInt, VBool](_.asInt, boolean)(using vcompareOps)
    final val eqOps = new EqOps[Value, Value]:
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (IntValue(i1), IntValue(i2)) => boolean(vintEqOps.equ(i1, i2))
        case (RefValue(a1), RefValue(a2)) => boolean(vrefEqOps.equ(a1, a2))
        case (FunValue(f1), FunValue(f2)) => boolean(vfunEqOps.equ(f1, f2))
        case (RecValue(r1), RecValue(r2)) => boolean(vrecEqOps.equ(r1, r2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = (v1, v2) match
        case (IntValue(i1), IntValue(i2)) => boolean(vintEqOps.neq(i1, i2))
        case (RefValue(a1), RefValue(a2)) => boolean(vrefEqOps.neq(a1, a2))
        case (FunValue(f1), FunValue(f2)) => boolean(vfunEqOps.neq(f1, f2))
        case (RecValue(r1), RecValue(r2)) => boolean(vrecEqOps.neq(r1, r2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    final val functionOps = new LiftedFunctionOps[Function, Seq[Value], Value, Value, VFun](_.asFunction, FunValue.apply)(using vfunOps)
    final val refOps = new LiftedReferenceOps[Value, Addr, VRef](_.asReference, RefValue.apply)(using vrefOps)
    final val recOps = new LiftedRecordOps[Field, Value, Value, Value, VRecord](_.asRecord, identity, RecValue.apply, identity)(using vrecOps)
    final val branchOps = new LiftedBooleanBranching[Value, VBool, Unit](v => v.asBoolean)(using vbranchOps)
