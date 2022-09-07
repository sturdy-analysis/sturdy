package sturdy.language.tip

import sturdy.data.MayJoin
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.language.tip.*
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
  type VString

  enum Value:
    case TopValue
    case IntValue(i: VInt)
    case RefValue(addr: VRef)
    case FunValue(fun: VFun)
    case RecValue(rec: VRecord)
    case StringValue(s: VString)

    def asBoolean(using Failure): VBool = Interpreter.this.asBoolean(this)
    def asInt(using failure: Failure): VInt = this match
      case IntValue(i) => i
      case TopValue => topInt
      case _ => failure(TipFailure.TypeError, s"Expected Int but got $this")
    def asFunction(using inst: Instance): VFun = this match
      case FunValue(f) => f
      case TopValue => topFun
      case _ => inst.failure(TipFailure.TypeError, s"Expected Function but got $this")
    def asReference(using inst: Instance): VRef = this match
      case RefValue(a) => a
      case TopValue => topReference
      case _ => inst.failure(TipFailure.TypeError, s"Expected Reference but got $this")
    def asRecord(using failure: Failure): VRecord = this match
      case RecValue(rec) => rec
      case TopValue => topRecord
      case _ => failure(TipFailure.TypeError, s"Expected Record but got $this")

  def topInt: VInt
  def topFun(using Instance): VFun
  def topReference(using Instance): VRef
  def topRecord: VRecord
  def topBool: VBool

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

  import Value.*
  given ValueIntegerOps(using Failure, IntegerOps[Int, VInt]): IntegerOps[Int, Value] =
    new LiftedIntegerOps[Int, Value, VInt](_.asInt, IntValue.apply)
  given ValueOrderingOps(using Failure, OrderingOps[VInt, VBool]): OrderingOps[Value, Value] =
    new LiftedOrderingOps[Value, Value, VInt, VBool](_.asInt, boolean)
  given ValueEqOps(using EqOps[VInt, VBool], EqOps[VRef, VBool], EqOps[VFun, VBool], EqOps[VRecord, VBool]): EqOps[Value, Value] with
    def equ(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => boolean(EqOps.equ(i1, i2))
      case (RefValue(a1), RefValue(a2)) => boolean(EqOps.equ(a1, a2))
      case (FunValue(f1), FunValue(f2)) => boolean(EqOps.equ(f1, f2))
      case (RecValue(r1), RecValue(r2)) => boolean(EqOps.equ(r1, r2))
      case (TopValue, _) | (_, TopValue) => boolean(topBool)
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    def neq(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => boolean(EqOps.neq(i1, i2))
      case (RefValue(a1), RefValue(a2)) => boolean(EqOps.neq(a1, a2))
      case (FunValue(f1), FunValue(f2)) => boolean(EqOps.neq(f1, f2))
      case (RecValue(r1), RecValue(r2)) => boolean(EqOps.neq(r1, r2))
      case (TopValue, _) | (_, TopValue) => boolean(topBool)
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
  given ValueFunctionOps(using Instance, FunctionOps[Function, Seq[Value], Value, VFun]): FunctionOps[Function, Seq[Value], Value, Value] =
    new LiftedFunctionOps[Function, Seq[Value], Value, Value, VFun](_.asFunction, FunValue.apply)
  given ValueReferenceOps(using Instance, ReferenceOps[Addr, VRef]): ReferenceOps[Addr, Value] =
    new LiftedReferenceOps[Value, Addr, VRef](_.asReference, RefValue.apply)
  given ValueRecordOps(using Failure, RecordOps[Field, Value, VRecord]): RecordOps[Field, Value, Value] =
    new LiftedRecordOps[Field, Value, Value, Value, VRecord](_.asRecord, identity, RecValue.apply, identity)
  given ValueBranchingOps(using Failure, BooleanBranching[VBool, Unit]): BooleanBranching[Value, Unit] =
    new LiftedBooleanBranching[Value, VBool, Unit](v => v.asBoolean)
  given ValueBooleanSelection(using Failure, BooleanSelection[VBool, VBool]): BooleanSelection[Value, VBool] =
    new LiftedBooleanSelection(_.asBoolean)

  type Instance <: GenericInstance
  abstract class GenericInstance extends GenericInterpreter[Value, Addr, J]:
    given Instance = this.asInstanceOf[Instance]
