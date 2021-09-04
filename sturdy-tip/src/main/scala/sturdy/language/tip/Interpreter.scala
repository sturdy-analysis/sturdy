package sturdy.language.tip

import sturdy.values.relational.EqOps
import sturdy.values.references.ReferenceOps
import sturdy.values.references.LiftedReferenceOps
import sturdy.effect.failure.Failure
import sturdy.values.functions.LiftedFunctionOps
import sturdy.values.relational.CompareOps
import sturdy.values.relational.LiftedCompareOps
import sturdy.effect.branching.CBoolBranching
import sturdy.values.ints.IntOps
import sturdy.effect.print.CPrint
import sturdy.values.ints.LiftedIntOps
import sturdy.values.records.RecordOps
import sturdy.effect.store.CStore
import sturdy.effect.callframe.CCallFrame
import sturdy.values.functions.FunctionOps
import sturdy.effect.userinput.CUserInput
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.CFailure
import sturdy.fix
import sturdy.language.tip.ConcreteInterpreter.Value
import sturdy.language.tip.GenericInterpreter.{AllocationSite, GenericEffects, GenericPhi, FixIn, FixOut}
import sturdy.values.records.LiftedRecordOps

trait Interpreter:
  type VBool
  type VInt
  type VRef
  type VFun
  type VRecord

  enum Value:
    case IntValue(i: VInt)
    case RefValue(addr: VRef)
    case FunValue(fun: VFun)
    case RecValue(rec: VRecord)

    def asBoolean: VBool = Interpreter.this.asBoolean(this)
    def asInt: VInt = this match
      case IntValue(i) => i
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asFunction: VFun = this match
      case FunValue(f) => f
      case _ => throw new IllegalArgumentException(s"Expected Function but got $this")
    def asReference: VRef = this match
      case RefValue(a) => a
      case _ => throw new IllegalArgumentException(s"Expected Reference but got $this")
    def asRecord: VRecord = this match
      case RecValue(rec) => rec
      case _ => throw new IllegalArgumentException(s"Expected Record but got $this")

  import Value._

  def asBoolean(v: Value): VBool
  def boolean(b: VBool): Value

  type Addr
  type Effects <: GenericEffects[Value, Addr]

  val phi: GenericPhi[Value]

  protected def create
    (effects: Effects)
    (using IntOps[VInt], CompareOps[VInt, VBool], FunctionOps[Function, Value, Value, VFun],
           RecordOps[String, Value, VRecord], ReferenceOps[Addr, VRef],
           EqOps[VInt, VBool], EqOps[VRef, VBool], EqOps[VFun, VBool], EqOps[VRecord, VBool])
    (using effects.StoreJoin[Value], effects.StoreJoinComp, effects.StoreJoin[Unit], effects.BoolBranchJoin[Unit]): Instance = {

    given Failure = effects
    given IntOps[Value] = new LiftedIntOps(_.asInt, IntValue.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps(_.asInt, boolean)
    given EqOps[Value, Value] with
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
    given FunctionOps[Function, Value, Value, Value] = new LiftedFunctionOps(_.asFunction, FunValue.apply)
    given RecordOps[String, Value, Value] = new LiftedRecordOps(_.asRecord, identity, RecValue.apply, identity)
    given ReferenceOps[Addr, Value] = new LiftedReferenceOps(_.asReference, RefValue.apply)

    new Instance(effects)
  }

  class Instance(effects: Effects)
                (using intOps: IntOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value], functionOps: FunctionOps[Function, Value, Value, Value], refOps: ReferenceOps[Addr, Value], recOps: RecordOps[String, Value, Value])
                (using effects.StoreJoin[Value], effects.StoreJoinComp, effects.StoreJoin[Unit], effects.BoolBranchJoin[Unit])
    extends GenericInterpreter[Value, Addr, Effects](using effects) {
    override val phi: GenericPhi[Value] = Interpreter.this.phi
  }
