package sturdy.language.tip.analysis

import sturdy.effect.{AnalysisState, JoinComputation}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.callframe.CCallFrame
import sturdy.effect.failure.{AFailureCollect, Failure}
import sturdy.effect.print.{APrintPrefix, given}
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.AStoreGenericThreadded.StoreState
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.given
import sturdy.language.tip.{Function, GenericInterpreter, Exp, Stm, Program}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.ints.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.GenericInterpreter.{*, given}

object IntervalAnalysis:
  enum Value:
    case TopValue
    case IntValue(i: IntInterval)
    case RefValue(addr: Refs)
    case FunValue(fun: Powerset[Function])
    case RecValue(rec: ARecord[String, Value])

    def asBoolean: Topped[Boolean] = this match
      case IntValue(i) => EqOps.equ(i, IntInterval(0, 0)).map(!_)
      case TopValue => Topped.Top
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asInt: IntInterval = this match
      case IntValue(i) => i
      case TopValue => IntInterval.Top
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asFunction: Powerset[Function] = this match
      case FunValue(fs) => fs
      case TopValue => ???
      case _ => throw new IllegalArgumentException(s"Expected Function but got $this")
    def asReference: Refs = this match
      case RefValue(a) => a
      case TopValue => ???
      case _ => throw new IllegalArgumentException(s"Expected Reference but got $this")
    def asRecord: ARecord[String, Value] = this match
      case RecValue(rec) => rec
      case TopValue => ???
      case _ => throw new IllegalArgumentException(s"Expected Record but got $this")

  import Value._

  given widenValue(using bounds: => Set[Int]): fix.Widening[Value] with
    override def widen(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => IntValue(intIntervalWiden.widen(i1, i2))
      case (FunValue(funs1), FunValue(funs2)) => FunValue(funs1 ++ funs2)
      case (RefValue(addrs1), RefValue(addrs2)) => RefValue(addrs1 ++ addrs2)
      case (RecValue(rec1), RecValue(rec2)) => RecValue(joinARecord.joinValues(rec1, rec2))
      case _ => TopValue

  given JoinValue[Value] with
    override def joinValues(v1: Value, v2: Value): Value = (v1, v2) match
      case (IntValue(i1), IntValue(i2)) => IntValue(intIntervalJoin.joinValues(i1, i2))
      case (FunValue(funs1), FunValue(funs2)) => FunValue(funs1 ++ funs2)
      case (RefValue(addrs1), RefValue(addrs2)) => RefValue(addrs1 ++ addrs2)
      case (RecValue(rec1), RecValue(rec2)) => RecValue(joinARecord.joinValues(rec1, rec2))
      case _ => TopValue

  def boolValue(b: Topped[Boolean]): Value = IntValue(b match
    case Topped.Top => IntInterval(0, 1)
    case Topped.Actual(true) => IntInterval(1, 1)
    case Topped.Actual(false) => IntInterval(0, 0)
  )

  type Refs = Powerset[AllocationSiteRef]
  type Addr = AllocationSiteAddr
  type PowAddr = Powerset[AllocationSiteAddr]
  def fromAllocationSite(asite: AllocationSite): PowAddr = Powerset(asite match
    case AllocationSite.Alloc(e) => AllocationSiteAddr.Alloc(e.label)(true)
    case AllocationSite.ParamBinding(fun, p) => AllocationSiteAddr.Variable(s"${fun.name}:$p")(true)
    case AllocationSite.LocalBinding(fun, v) => AllocationSiteAddr.Variable(s"${fun.name}:$v")(true)
    case AllocationSite.Record(r) => AllocationSiteAddr.Alloc(r.label)(true)
  )
  type Environment = Map[String, PowAddr]
  type Store = Map[Addr, Value]
  class Effects(initEnvironment: Environment, initStore: Store)
    extends ABoolBranching[Value]
      with CCallFrame[Unit, String, PowAddr]((), initEnvironment)
      with AStoreMultiAddrThreadded[Addr, Value](initStore)
      with AAllocationFromContext[AllocationSite, PowAddr](fromAllocationSite)
      with APrintPrefix[Value]
      with AUserInput[Value](IntValue(IntInterval.Top))
      with AFailureCollect
      with AnalysisState[Map[Addr, Value], (Map[Addr, Value], APrintPrefix.PrintResult[Value])] {

    override def getInState(): InState = getStore
    override def setInState(in: InState): Unit = setStore(in)

    override def getOutState(): OutState = (getStore, getPrinted)
    override def setOutState(out: OutState): Unit =
      setStore(out._1)
      setPrinted(out._2)

    override def isOutStateStable(old: OutState, now: OutState): Boolean = old._1 == now._1
  }

  def apply(initEnvironment: Environment, initStore: Store, steps: Int): IntervalAnalysis = {
    val effects = new Effects(initEnvironment, initStore)

    given JoinComputation = effects
    given Failure = effects
    given IntOps[Value] = new LiftedIntOps(_.asInt, IntValue.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps(_.asInt, boolValue)
    given EqOps[Function, Boolean] = new EqualsEqOps[Function]
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (IntValue(i1), IntValue(i2)) => boolValue(EqOps.equ(i1, i2))
        case (RefValue(a1), RefValue(a2)) => boolValue(EqOps.equ(a1, a2))
        case (FunValue(f1), FunValue(f2)) => boolValue(EqOps.equ(f1, f2))
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = boolValue(equ(v1, v2).asBoolean.map(!_))
    given FunctionOps[Function, Value, Value, Value] = new LiftedFunctionOps(_.asFunction, FunValue.apply)
    given RecordOps[String, Value, Value] = new LiftedRecordOps(_.asRecord, identity, RecValue.apply, identity)
    given ReferenceOps[PowAddr, Value] = new LiftedReferenceOps(_.asReference, RefValue.apply)

    new IntervalAnalysis(steps)(using effects)
  }

import IntervalAnalysis.{*, given}

class IntervalAnalysis(steps: Int)
                  (using effectOps: Effects)
                  (using intOps: IntOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value],
                   functionOps: FunctionOps[Function, Value, Value, Value], recOps: RecordOps[String, Value, Value], refOps: ReferenceOps[PowAddr, Value])
  extends GenericInterpreter[Value, PowAddr, Effects]:

  def isCallOrWhile(dom: FixIn[Value]): Int = dom match {
    case FixIn.EnterFunction(_) => 0
    case FixIn.Run(Stm.While(_, _)) => 1
    case _ => -1
  }

  override def execute(p: Program): Value =
    bounds = p.intLiterals
    super.execute(p)

  implicit var bounds: Set[Int] = Set.empty

  type Ctx = List[Exp.Call]

  val callSites = fix.context.callSites[FixIn[Value], Exp.Call] {
    case FixIn.Eval(c: Exp.Call) => Some(c)
    case _ => None
  }
  private implicit val contextual: fix.Contextual[Ctx, FixIn[Value], FixOut[Value], effectOps.InState, effectOps.OutState] =
    fix.contextual(callSites.callString(2), callSites.isCall)

  val phi =
    fix.log(callSites,
      fix.contextSensitive(
        fix.dispatch(isCallOrWhile, Seq(
          // call
          fix.iter.topmost,
          // while
          fix.unwind(steps,
            fix.iter.innermost
          )
        ))
      )
    )
