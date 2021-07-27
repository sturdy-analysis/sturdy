package sturdy.language.tip.analysis

import sturdy.effect.JoinComputation
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.environment.{AEnvironmentStaticScope, CEnvironment}
import sturdy.effect.failure.{AFailureCollect, Failure}
import sturdy.effect.print.APrintPrefix
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.language.tip.{Function, GenericInterpreter}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.ints.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.GenericInterpreter.*

object SignAnalysis:
  enum Value:
    case TopValue
    case IntValue(i: IntSign)
    case RefValue(addr: Refs)
    case FunValue(fun: Powerset[Function])

    def asBoolean: Topped[Boolean] = this match
      case IntValue(i) => i match
        case IntSign.Zero => Topped.Actual(false)
        case IntSign.Pos | IntSign.Neg => Topped.Actual(true)
        case _ => Topped.Top
      case TopValue => Topped.Top
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asInt: IntSign = this match
      case IntValue(i) => i
      case TopValue => IntSign.TopSign
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asFunction: Powerset[Function] = this match
      case FunValue(fs) => fs
      case TopValue => ???
      case _ => throw new IllegalArgumentException(s"Expected Function but got $this")
    def asReference: Refs = this match
      case RefValue(a) => a
      case _ => throw new IllegalArgumentException(s"Expected Reference but got $this")

  import Value._

  given JoinValue[Value] with
    override def joinValues(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => IntValue(IntSignJoin.joinValues(i1, i2))
      case (FunValue(funs1), FunValue(funs2)) => FunValue(funs1 ++ funs2)
      case (RefValue(addrs1), RefValue(addrs2)) => RefValue(addrs1 ++ addrs2)
      case _ => TopValue

  def boolValue(b: Topped[Boolean]): Value = IntValue(b match
    case Topped.Top => IntSign.ZeroOrPos
    case Topped.Actual(true) => IntSign.Pos
    case Topped.Actual(false) => IntSign.Zero
  )

  type Refs = Powerset[AllocationSiteRef]
  type Addr = AllocationSiteAddr
  type PowAddr = Powerset[AllocationSiteAddr]
  def fromAllocationSite(asite: AllocationSite): PowAddr = Powerset(asite match
    case AllocationSite.Alloc(ealloc) => AllocationSiteAddr.Alloc(ealloc.label)(true)
    case AllocationSite.ParamBinding(fun, p) => AllocationSiteAddr.Variable(s"${fun.name}:$p")(true)
    case AllocationSite.LocalBinding(fun, v) => AllocationSiteAddr.Variable(s"${fun.name}:$v")(true)
  )
  type Environment = Map[String, Powerset[Addr]]
  type Store = Map[Addr, Value]
  class Effects(initEnvironment: Environment, initStore: Store)
    extends ABoolBranching[Value]
      with AEnvironmentStaticScope[String, PowAddr] with CEnvironment[String, PowAddr](initEnvironment)
      with AStoreMultiAddrThreadded[Addr, Value](initStore)
      with AAllocationFromContext[AllocationSite, PowAddr](fromAllocationSite)
      with APrintPrefix[Value]
      with AUserInput[Value](IntValue(IntSign.TopSign))
      with AFailureCollect
//  type Fix = FixUnwind[FixIn[Value], FixOut[Value]]

  def apply(initEnvironment: Environment, initStore: Store, steps: Int): SignAnalysis = {
    val effects = new Effects(initEnvironment, initStore)

    given JoinComputation = effects
    given Failure = effects
    given IntOps[Value] = new LiftedIntOps[Value, IntSign](_.asInt, IntValue.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps[Value, Value, IntSign, Topped[Boolean]](_.asInt, boolValue)
    given EqOps[Function, Boolean] = new EqualsEqOps[Function]
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (IntValue(i1), IntValue(i2)) => boolValue(EqOps.equ(i1, i2))
        case (RefValue(a1), RefValue(a2)) => boolValue(if (a1 == a2) Topped.Top else Topped.Actual(false))
        case (FunValue(f1), FunValue(f2)) => boolValue(EqOps.equ(f1, f2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = boolValue(equ(v1, v2).asBoolean.map(!_))
    given FunctionOps[Function, Value, Value, Value] = new LiftedFunctionOps[Function, Value, Value, Value, Powerset[Function]](_.asFunction, FunValue.apply)
    given ReferenceOps[PowAddr, Value] = new LiftedReferenceOps[Value, Powerset[AllocationSiteAddr], Powerset[AllocationSiteRef]](_.asReference, RefValue.apply)

    new SignAnalysis(steps)(using effects)
  }

import SignAnalysis.*

class SignAnalysis(steps: Int)
  (using effectOps: Effects)
  (using intOps: IntOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value],
   functionOps: FunctionOps[Function, Value, Value, Value], refOps: ReferenceOps[PowAddr, Value])
    extends GenericInterpreter[Value, PowAddr, Effects]:

  val phi = fix.unwind(steps, fix.const[FixIn[Value], FixOut[Value]]({
    case FixIn.Eval(_) => FixOut.Eval(Value.TopValue)
    case FixIn.Run(_) => FixOut.Run(())
    case FixIn.Call(_, _) => FixOut.Call(Value.TopValue)
  }))
