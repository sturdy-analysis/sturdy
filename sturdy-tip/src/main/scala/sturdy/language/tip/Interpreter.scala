package sturdy.language.tip

import sturdy.effect.failure.Failure
import sturdy.fix.Widening
import sturdy.language.tip.ConcreteInterpreter.Value
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
    def asInt: VInt = this match
      case IntValue(i) => i
      case TopValue => topInt
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asFunction: VFun = this match
      case FunValue(f) => f
      case TopValue => topFun
      case _ => throw new IllegalArgumentException(s"Expected Function but got $this")
    def asReference: VRef = this match
      case RefValue(a) => a
      case TopValue => topReference
      case _ => throw new IllegalArgumentException(s"Expected Reference but got $this")
    def asRecord: VRecord = this match
      case RecValue(rec) => rec
      case TopValue => topRecord
      case _ => throw new IllegalArgumentException(s"Expected Record but got $this")

  import Value.*

  def topInt: VInt
  def topFun: VFun
  def topReference: VRef
  def topRecord: VRecord

  def asBoolean(v: Value): VBool
  def boolean(b: VBool): Value

  given Top[Value] with
    def top = TopValue

  type Addr
  type Effects <: GenericEffects[Value, Addr]

  given liftedIntOps(using IntOps[VInt]): IntOps[Value] = new LiftedIntOps(_.asInt, IntValue.apply)
  given liftedCompareOps(using CompareOps[VInt, VBool]): CompareOps[Value, Value] = new LiftedCompareOps(_.asInt, boolean)
  given liftedEqOps(using EqOps[VInt, VBool], EqOps[VRef, VBool], EqOps[VFun, VBool], EqOps[VRecord, VBool]): EqOps[Value, Value] with
    def equ(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => boolean(EqOps.equ(i1, i2))
      case (RefValue(a1), RefValue(a2)) => boolean(EqOps.equ(a1, a2))
      case (FunValue(f1), FunValue(f2)) => boolean(EqOps.equ(f1, f2))
      case (RecValue(r1), RecValue(r2)) => boolean(EqOps.equ(r1, r2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    def neq(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => boolean(EqOps.neq(i1, i2))
      case (RefValue(a1), RefValue(a2)) => boolean(EqOps.neq(a1, a2))
      case (FunValue(f1), FunValue(f2)) => boolean(EqOps.neq(f1, f2))
      case (RecValue(r1), RecValue(r2)) => boolean(EqOps.neq(r1, r2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
  given liftedFunctionOps(using FunctionOps[Function, Value, Value, VFun]): FunctionOps[Function, Value, Value, Value] = new LiftedFunctionOps(_.asFunction, FunValue.apply)
  given liftedRecordOps(using RecordOps[String, Value, VRecord]): RecordOps[String, Value, Value] = new LiftedRecordOps(_.asRecord, identity, RecValue.apply, identity)
  given liftedReferenceOps(using ReferenceOps[Addr, VRef], Failure):  ReferenceOps[Addr, Value] = new LiftedReferenceOps(_.asReference, RefValue.apply)

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


//  protected def create
//    (effects: Effects)
//    (using IntOps[VInt], CompareOps[VInt, VBool], FunctionOps[Function, Value, Value, VFun],
//           RecordOps[String, Value, VRecord], ReferenceOps[Addr, VRef],
//           EqOps[VInt, VBool], EqOps[VRef, VBool], EqOps[VFun, VBool], EqOps[VRecord, VBool])
//    (using effects.StoreJoin[Value], effects.StoreJoinComp, effects.StoreJoin[Unit], effects.BoolBranchJoin[Unit]): Instance = {
//    given Failure = effects
//    new Instance(effects)
//  }

//  class Instance(effects: Effects)
//                (using intOps: IntOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value], functionOps: FunctionOps[Function, Value, Value, Value], refOps: ReferenceOps[Addr, Value], recOps: RecordOps[String, Value, Value])
//                (using effects.StoreJoin[Value], effects.StoreJoinComp, effects.StoreJoin[Unit], effects.BoolBranchJoin[Unit])
//    extends GenericInterpreter[Value, Addr, Effects](using effects) {
//    override val phi: GenericPhi[Value] = Interpreter.this.phi
//  }
