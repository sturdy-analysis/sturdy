package sturdy.language.tip.analysis

import sturdy.effect.JoinComputation
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.environment.{AEnvironmentStaticScope, CEnvironment}
import sturdy.effect.store.AStoreSingleAddrThreadded
import sturdy.effect.failure.{AFailureCollect, Failure}
import sturdy.effect.store.Store
import sturdy.fix.CFixpoint
import sturdy.language.tip.{Function, GenericInterpreter}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.ints.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.util.given
import sturdy.language.tip.GenericInterpreter.*

object SignAnalysis:
  enum Value:
    case IntValue(i: IntSign)
    case RefValue(addr: Option[Addr])
    case FunValue(fun: Powerset[Function])

    def asBoolean: Topped[Boolean] = this match
      case IntValue(i) => i match
        case IntSign.Zero => Topped.Actual(false)
        case IntSign.Pos | IntSign.Neg => Topped.Actual(true)
        case _ => Topped.Top
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asInt: IntSign = this match
      case IntValue(i) => i
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asFunction: Powerset[Function] = this match
      case FunValue(fs) => fs
      case _ => throw new IllegalArgumentException(s"Expected Function but got $this")
    def asReference: Option[Addr] = this match
      case RefValue(a) => a
      case _ => throw new IllegalArgumentException(s"Expected Reference but got $this")

  import Value._

  given JoinValue[Value] with
    override def joinValues(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => IntValue(IntSignJoin.joinValues(i1, i2))
      case (FunValue(funs1), FunValue(funs2)) => FunValue(funs1 ++ funs2)
      // TODO; RefValues
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")

  def boolValue(b: Topped[Boolean]): Value = IntValue(b match
    case Topped.Top => IntSign.ZeroOrPos
    case Topped.Actual(true) => IntSign.Pos
    case Topped.Actual(false) => IntSign.Zero
  )

  // TODO abstract Addr
  type Addr = Int
  type Environment = Map[String, Int]
  type Store = Map[Int, (Boolean, Value)]
  class Effects(initEnvironment: Environment, initStore: Store)
    extends ABoolBranching[Value]
      with AEnvironmentStaticScope[String, Addr] with CEnvironment[String, Addr](initEnvironment)
      with AStoreSingleAddrThreadded[Addr, Value](initStore)
      with AAllocationFromContext[Addr, AllocationSite](???)
      with AFailureCollect
  type Fix = CFixpoint[FixIn[Value], FixOut[Value]]

  def apply(initEnvironment: Environment, initStore: Store): SignAnalysis = {
    val effects = new Effects(initEnvironment, initStore)
    val fixpoint = new CFixpoint[FixIn[Value], FixOut[Value]]

    given JoinComputation = effects
    given Failure = effects
    given IntOps[Value] = new LiftedIntOps[Value, IntSign](_.asInt, IntValue.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps[Value, Value, IntSign, Topped[Boolean]](_.asInt, boolValue)
    given EqOps[Function, Boolean] = new EqualsEqOps[Function]
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (IntValue(i1), IntValue(i2)) => boolValue(summon[EqOps[IntSign, Topped[Boolean]]].equ(i1, i2))
        // TODO
//        case (RefValue(a1), RefValue(a2)) => boolValue(a1 == a2)
        case (FunValue(f1), FunValue(f2)) => boolValue(summon[EqOps[Powerset[Function], Topped[Boolean]]].equ(f1, f2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = boolValue(equ(v1, v2).asBoolean.map(!_))
    given FunctionOps[Function, Value, Value, Value] = new LiftedFunctionOps[Function, Value, Value, Value, Powerset[Function]](_.asFunction, FunValue.apply)
    given ReferenceOps[Addr, Value] = new LiftedReferenceOps[Value, Addr, Option[Addr]](_.asReference, RefValue.apply)

    new SignAnalysis(using effects)(using fixpoint)
  }

import SignAnalysis.*

class SignAnalysis
  (using effectOps: Effects)
  (using fix: Fix)
  (using intOps: IntOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value],
   functionOps: FunctionOps[Function, Value, Value, Value], refOps: ReferenceOps[Addr, Value])
    extends GenericInterpreter[Value, Addr, Effects, Fix]
