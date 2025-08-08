package sturdy.language.tip

import sturdy.data.MayJoin
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.language.tip.*
import sturdy.values.MaybeChanged
import sturdy.values.MaybeChanged.Unchanged
import sturdy.values.booleans.*
import sturdy.values.{Combine, Finite, Top, Widening}
import sturdy.values.functions.{FunctionOps, LiftedFunctionOps}
import sturdy.values.integer.{IntegerOps, LiftedIntegerOps}
import sturdy.values.records.{LiftedRecordOps, RecordOps}
import sturdy.values.references.{LiftedReferenceOps, ReferenceOps}
import sturdy.values.ordering.{EqOps, LiftedOrderingOps, OrderingOps}

/**
 * Trait [[Interpreter]] allows sharing code between concrete and abstract interpreters.
 * The trait defines different types of values and conversions between them.
 */
trait Interpreter:
  type J[A] <: MayJoin[A]
  type VBool
  type VInt
  type VRef
  type VFun
  type VRecord

  enum Value:
    case TopValue
    case BoolValue(b: VBool)
    case IntValue(i: VInt)
    case RefValue(addr: VRef)
    case FunValue(fun: VFun)
    case RecValue(rec: VRecord)

    def mapValues(f: [A] => A => A): Value =
      this match
        case TopValue => this
        case BoolValue(b) => BoolValue(f[VBool](b))
        case IntValue(i) => IntValue(f[VInt](i))
        case RefValue(addr) => RefValue(f[VRef](addr))
        case FunValue(fun) => FunValue(f[VFun](fun))
        case RecValue(rec) => RecValue(f[VRecord](rec))

    def asBoolean(using inst: Instance): VBool = Interpreter.this.asBoolean(this)
    def asInt(using inst: Instance): VInt = Interpreter.this.asInt(this)
    def asFunction(using inst: Instance): VFun = this match
      case FunValue(f) => f
      case TopValue => topFun
      case _ => inst.failure(TipFailure.TypeError, s"Expected Function but got $this")
    def asReference(using inst: Instance): VRef = this match
      case RefValue(a) => a
      case TopValue => topReference
      case _ => inst.failure(TipFailure.TypeError, s"Expected Reference but got $this")
    def asRecord(using inst: Instance): VRecord = this match
      case RecValue(rec) => rec
      case TopValue => topRecord
      case _ => inst.failure(TipFailure.TypeError, s"Expected Record but got $this")

  def topInt(using Instance): VInt
  def topFun(using Instance): VFun
  def topReference(using Instance): VRef
  def topRecord: VRecord
  def topBool(using Instance): VBool

  def asBoolean(v: Value)(using Instance): VBool
  def asInt(v: Value)(using Instance): VInt

  given Top[Value] with
    def top = Value.TopValue

  type Addr

  given CombineValue[W <: Widening](using Combine[VInt, W], Combine[VFun, W], Combine[VRef, W], Combine[VRecord, W]): Combine[Value, W] with
    import Value.*
    override def apply(v1: Value, v2: Value): MaybeChanged[Value] = (v1, v2) match
      case _ if v1 eq v2 => Unchanged(v1)
      case (IntValue(i1), IntValue(i2)) => Combine[VInt, W](i1, i2).map(IntValue.apply)
      case (FunValue(funs1), FunValue(funs2)) => Combine[VFun, W](funs1, funs2).map(FunValue.apply)
      case (RefValue(addrs1), RefValue(addrs2)) => Combine[VRef, W](addrs1, addrs2).map(RefValue.apply)
      case (RecValue(rec1), RecValue(rec2)) => Combine[VRecord, W](rec1, rec2).map(RecValue.apply)
      case _ => MaybeChanged(TopValue, v1)

  given FiniteValue(using Finite[VInt], Finite[VFun], Finite[VRef], Finite[VRecord]): Finite[Value] with {}

  import Value.*
  given ValueIntegerOps(using Instance, IntegerOps[Int, VInt]): IntegerOps[Int, Value] =
    new LiftedIntegerOps[Int, Value, VInt](_.asInt, IntValue.apply)
  given ValueOrderingOps(using Instance, OrderingOps[VInt, VBool]): OrderingOps[Value, Value] =
    new LiftedOrderingOps[Value, Value, VInt, VBool](_.asInt, Value.BoolValue.apply)
  given ValueEqOps(using EqOps[VInt, VBool], /*EqOps[VBool, VBool],*/ EqOps[VRef, VBool], EqOps[VFun, VBool], EqOps[VRecord, VBool], Instance): EqOps[Value, Value] with
    def equ(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => Value.BoolValue(EqOps.equ(i1, i2))
      //case (BoolValue(i1), BoolValue(i2)) => Value.TopValue
      case (RefValue(a1), RefValue(a2)) => Value.BoolValue(EqOps.equ(a1, a2))
      case (FunValue(f1), FunValue(f2)) => Value.BoolValue(EqOps.equ(f1, f2))
      case (RecValue(r1), RecValue(r2)) => Value.BoolValue(EqOps.equ(r1, r2))
      case (TopValue, _) | (_, TopValue) => Value.BoolValue(topBool)
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    def neq(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => Value.BoolValue(EqOps.neq(i1, i2))
      //case (BoolValue(i1), BoolValue(i2)) => Value.TopValue
      case (RefValue(a1), RefValue(a2)) => Value.BoolValue(EqOps.neq(a1, a2))
      case (FunValue(f1), FunValue(f2)) => Value.BoolValue(EqOps.neq(f1, f2))
      case (RecValue(r1), RecValue(r2)) => Value.BoolValue(EqOps.neq(r1, r2))
      case (TopValue, _) | (_, TopValue) => Value.BoolValue(topBool)
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
  given ValueFunctionOps(using Instance, FunctionOps[Function, Seq[Value], Value, VFun]): FunctionOps[Function, Seq[Value], Value, Value] =
    new LiftedFunctionOps[Function, Seq[Value], Value, Value, VFun](_.asFunction, FunValue.apply)
  given ValueReferenceOps(using Instance, ReferenceOps[Addr, VRef]): ReferenceOps[Addr, Value] =
    new LiftedReferenceOps[Value, Addr, VRef](_.asReference, RefValue.apply)
  given ValueRecordOps(using Instance, RecordOps[Field, Value, VRecord]): RecordOps[Field, Value, Value] =
    new LiftedRecordOps[Field, Value, Value, Value, VRecord](_.asRecord, identity, RecValue.apply, identity)
  given ValueBranchingOps(using Instance, BooleanBranching[VBool, Unit]): BooleanBranching[Value, Unit] =
    new LiftedBooleanBranching[Value, VBool, Unit](v => v.asBoolean)
  given ValueBooleanSelection[R](using Instance, BooleanSelection[VBool, R]): BooleanSelection[Value, R] =
    new LiftedBooleanSelection(_.asBoolean)

  /**
   * Instances instantiate the interpreter, which is needed because the semantics are stateful.
   * Specifically, each evaluation of a program requires a new instances with a fresh frame, fresh store, etc.
   */
  type Instance <: GenericInstance
  abstract class GenericInstance extends GenericInterpreter[Value, Addr, J]:
    protected given Instance = this.asInstanceOf[Instance]
