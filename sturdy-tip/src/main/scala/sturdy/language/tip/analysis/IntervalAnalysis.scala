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
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.ints.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.GenericInterpreter.{AllocationSite, GenericPhi, FixIn, FixOut, given}

object IntervalAnalysis extends Interpreter:
  override type VBool = Topped[Boolean]
  override type VInt = IntInterval
  override type VRef = Powerset[AllocationSiteRef]
  override type VFun = Powerset[Function]
  override type VRecord = ARecord[String, Value]

  given JoinValue[VRecord] = new ARecordJoin(using lazily(liftedJoinValue))
  given EqOps[VRecord, VBool] = new ARecordEqOps(using lazily(liftedEqOps))

  override def topInt: IntInterval = IntInterval.Top
  override def topReference: Powerset[AllocationSiteRef] = ???
  override def topFun: Powerset[Function] = ???
  override def topRecord: ARecord[String, Value] = ARecord.Top()

  override def asBoolean(v: Value): VBool = v match
    case Value.IntValue(i) => EqOps.equ(i, IntInterval(0, 0)).map(!_)
    case Value.TopValue => Topped.Top
    case _ => throw new IllegalArgumentException(s"Expected Int but got $this")

  override def boolean(b: VBool): Value = Value.IntValue(b match
    case Topped.Top => IntInterval(0, 1)
    case Topped.Actual(true) => IntInterval(1, 1)
    case Topped.Actual(false) => IntInterval(0, 0)
  )

  override type Addr = Powerset[AllocationSiteAddr]
  def fromAllocationSite(asite: AllocationSite): Addr = Powerset(asite match
    case AllocationSite.Alloc(e) => AllocationSiteAddr.Alloc(e.label)(true)
    case AllocationSite.ParamBinding(fun, p) => AllocationSiteAddr.Variable(s"${fun.name}:$p")(true)
    case AllocationSite.LocalBinding(fun, v) => AllocationSiteAddr.Variable(s"${fun.name}:$v")(true)
    case AllocationSite.Record(r) => AllocationSiteAddr.Alloc(r.label)(true)
  )
  type Environment = Map[String, Addr]
  type Store = Map[AllocationSiteAddr, Value]
  class Effects(initEnvironment: Environment, initStore: Store)
    extends ABoolBranching[Value]
      with CCallFrame[Unit, String, Addr]((), initEnvironment)
      with AStoreMultiAddrThreadded[AllocationSiteAddr, Value](initStore)
      with AAllocationFromContext[AllocationSite, Addr](fromAllocationSite)
      with APrintPrefix[Value]
      with AUserInput[Value](Value.IntValue(IntInterval.Top))
      with AFailureCollect
      with AnalysisState[Map[AllocationSiteAddr, Value], (Map[AllocationSiteAddr, Value], APrintPrefix.PrintResult[Value])]:
    override def getInState(): InState = getStore
    override def setInState(in: InState): Unit = setStore(in)
    override def getOutState(): OutState = (getStore, getPrinted)
    override def setOutState(out: OutState): Unit =
      setStore(out._1)
      setPrinted(out._2)
    override def isOutStateStable(old: OutState, now: OutState): Boolean = old._1 == now._1

  def apply(initEnvironment: Environment, initStore: Store, steps: Int): IntervalAnalysis =
    val effects = new Effects(initEnvironment, initStore)
    given Failure = effects
    given JoinComputation = effects
    new IntervalAnalysis(steps)(using effects)

import IntervalAnalysis.{*, given}
class IntervalAnalysis(steps: Int)(using effects: Effects)
    (using intOps: IntOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value], functionOps: FunctionOps[Function, Value, Value, Value], refOps: ReferenceOps[Addr, Value], recOps: RecordOps[String, Value, Value])
    (using JoinComputation)
  extends GenericInterpreter[Value, Addr, Effects](effects):

  var bounds: Set[Int] = Set.empty
  given fix.Widening[IntInterval] = new IntIntervalWiden(bounds)
  given fix.Widening[VRecord] = new ARecordWidening(using lazily(liftedWidening))

  override def execute(p: Program): Value =
    bounds = p.intLiterals
    super.execute(p)

  def isCallOrWhile(dom: FixIn): Int = dom match
    case FixIn.EnterFunction(_) => 0
    case FixIn.Run(Stm.While(_, _)) => 1
    case _ => -1


  type Ctx = List[Exp.Call]
  val callSites = fix.context.callSites[FixIn, Exp.Call] {
    case FixIn.Eval(c: Exp.Call) => Some(c)
    case _ => None
  }

  val phi =
    fix.log(callSites,
      fix.contextSensitive(callSites.callString(2),
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
