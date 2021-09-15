package sturdy.language.tip

import sturdy.effect.failure.Failure
import sturdy.fix.Widening
import sturdy.language.tip.GenericInterpreter.{AllocationSite, FixIn, FixOut, GenericEffects, GenericPhi}
import sturdy.values.{Top, JoinValue}
import sturdy.values.functions.{FunctionOps, LiftedFunctionOps}
import sturdy.values.ints.{IntOps, LiftedIntOps}
import sturdy.values.records.{LiftedRecordOps, RecordOps}
import sturdy.values.references.{LiftedReferenceOps, ReferenceOps}
import sturdy.values.relational.{CompareOps, EqOps, LiftedCompareOps}

trait Interpreter:
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

    def asBoolean: VBool = Interpreter.this.asBoolean(this)
    def asInt(using Instance): VInt = this match
      case IntValue(i) => i
      case TopValue => topInt
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asFunction(using Instance): VFun = this match
      case FunValue(f) => f
      case TopValue => topFun
      case _ => throw new IllegalArgumentException(s"Expected Function but got $this")
    def asReference(using Instance): VRef = this match
      case RefValue(a) => a
      case TopValue => topReference
      case _ => throw new IllegalArgumentException(s"Expected Reference but got $this")
    def asRecord(using Instance): VRecord = this match
      case RecValue(rec) => rec
      case TopValue => topRecord
      case _ => throw new IllegalArgumentException(s"Expected Record but got $this")

  def topInt(using self: Instance): VInt
  def topFun(using self: Instance): VFun
  def topReference(using self: Instance): VRef
  def topRecord(using self: Instance): VRecord

  def asBoolean(v: Value): VBool
  def boolean(b: VBool): Value

  given Top[Value] with
    def top = Value.TopValue

  type Addr
  type Effects <: GenericEffects[Value, Addr]

  given liftedJoinValue(using JoinValue[VInt], JoinValue[VFun], JoinValue[VRef], JoinValue[VRecord]): JoinValue[Value] with
    import Value.*
    override def joinValues(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => IntValue(JoinValue.join(i1, i2))
      case (FunValue(funs1), FunValue(funs2)) => FunValue(JoinValue.join(funs1, funs2))
      case (RefValue(addrs1), RefValue(addrs2)) => RefValue(JoinValue.join(addrs1, addrs2))
      case (RecValue(rec1), RecValue(rec2)) => RecValue(JoinValue.join(rec1, rec2))
      case _ => TopValue

  given liftedWidening(using Widening[VInt], Widening[VFun], Widening[VRef], Widening[VRecord]): Widening[Value] with
    import Value.*
    override def widen(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => IntValue(Widening.widen(i1, i2))
      case (FunValue(funs1), FunValue(funs2)) => FunValue(Widening.widen(funs1, funs2))
      case (RefValue(addrs1), RefValue(addrs2)) => RefValue(Widening.widen(addrs1, addrs2))
      case (RecValue(rec1), RecValue(rec2)) => RecValue(Widening.widen(rec1, rec2))
      case _ => TopValue

  type Instance <: GenericInstance
  trait GenericInstance
    extends GenericInterpreter[Value, Addr, Effects]:

    given Instance = this.asInstanceOf[Instance]
    given Failure = effects

    def vintOps: IntOps[VInt]
    def vcompareOps: CompareOps[VInt, VBool]
    def vintEqOps: EqOps[VInt, VBool]
    def vrefEqOps: EqOps[VRef, VBool]
    def vfunEqOps: EqOps[VFun, VBool]
    def vrecEqOps: EqOps[VRecord, VBool]
    def vfunOps: FunctionOps[Function, Value, Value, VFun]
    def vrefOps: ReferenceOps[Addr, VRef]
    def vrecOps: RecordOps[String, Value, VRecord]

    import Value.*
    final val intOps = new LiftedIntOps[Value, VInt](_.asInt, IntValue.apply)(using vintOps)
    final val compareOps = new LiftedCompareOps[Value, Value, VInt, VBool](_.asInt, boolean)(using vcompareOps)
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
    final val functionOps = new LiftedFunctionOps[Function, Value, Value, Value, VFun](_.asFunction, FunValue.apply)(using vfunOps)
    final val refOps = new LiftedReferenceOps[Value, Addr, VRef](_.asReference, RefValue.apply)(using vrefOps)
    final val recOps = new LiftedRecordOps[String, Value, Value, Value, VRecord](_.asRecord, identity, RecValue.apply, identity)(using vrecOps)
