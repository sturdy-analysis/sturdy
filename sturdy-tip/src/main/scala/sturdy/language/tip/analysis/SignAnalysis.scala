package sturdy.language.tip.analysis

import sturdy.effect.noJoin
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

object SignAnalysis extends Interpreter:
  override type VBool = Topped[Boolean]
  override type VInt = IntSign
  override type VRef = Powerset[AllocationSiteRef]
  override type VFun = Powerset[Function]
  override type VRecord = ARecord[String, Value]

  given Lazy[JoinValue[Value]] = lazily(liftedJoinValue)
  given Lazy[EqOps[Value, Value]] = lazily(liftedEqOps)

  override def topInt: IntSign = IntSign.TopSign
  override def topReference: Powerset[AllocationSiteRef] = ???
  override def topFun: Powerset[Function] = ???
  override def topRecord: ARecord[String, Value] = ARecord.Top()

  override def asBoolean(v: Value): VBool = v match
    case Value.IntValue(i) => i match
      case IntSign.Zero => Topped.Actual(false)
      case IntSign.Pos | IntSign.Neg => Topped.Actual(true)
      case _ => Topped.Top
    case Value.TopValue => Topped.Top
    case _ => throw new IllegalArgumentException(s"Expected Int but got $this")

  def boolean(b: Topped[Boolean]): Value = Value.IntValue(b match
    case Topped.Top => IntSign.ZeroOrPos
    case Topped.Actual(true) => IntSign.Pos
    case Topped.Actual(false) => IntSign.Zero
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
      with AUserInput[Value](Value.IntValue(IntSign.TopSign))
      with AFailureCollect
      with AnalysisState[Map[AllocationSiteAddr, Value], (Map[AllocationSiteAddr, Value], APrintPrefix.PrintResult[Value])]:
    override def getInState(): InState = getStore
    override def setInState(in: InState): Unit = setStore(in)
    override def getOutState(): OutState = (getStore, getPrinted)
    override def setOutState(out: OutState): Unit =
      setStore(out._1)
      setPrinted(out._2)
    override def isOutStateStable(old: OutState, now: OutState): Boolean = old._1 == now._1

  def apply(initEnvironment: Environment, initStore: Store, steps: Int): SignAnalysis =
    val effects = new Effects(initEnvironment, initStore)
    given Failure = effects
    given JoinComputation = effects
    new SignAnalysis(steps)(using effects)

import SignAnalysis.{*, given}
class SignAnalysis(steps: Int)(using effects: Effects)
                  (using intOps: IntOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value], functionOps: FunctionOps[Function, Value, Value, Value], refOps: ReferenceOps[Addr, Value], recOps: RecordOps[String, Value, Value])
                  (using JoinComputation)
  extends GenericInterpreter[Value, Addr, Effects](effects):

  given fix.Widening[VRecord] = new ARecordWidening(using lazily(liftedWidening))

  def isCallOrWhile(dom: FixIn): Int = dom match
    case FixIn.EnterFunction(_) => 0
    case FixIn.Run(Stm.While(_, _)) => 1
    case _ => -1


  type Ctx = List[Exp.Call]
  val callSites = fix.context.callSites[FixIn, Exp.Call] {
    case FixIn.Eval(c: Exp.Call) => Some(c)
    case _ => None
  }

  val parameters: fix.context.Sensitivity[FixIn, Map[Addr, Value]] = fix.context.parametersFromStore {
    case FixIn.EnterFunction(f) => Some(f.params.map(p => effects.getLocal(p).get))
    case _ => None
  }

  val phi =
    fix.contextSensitive(parameters,
      fix.dispatch(isCallOrWhile, Seq(
        // call
        fix.iter.topmost,
        // while
        fix.unwind(steps,
          fix.iter.innermost
        )
      ))
    )
