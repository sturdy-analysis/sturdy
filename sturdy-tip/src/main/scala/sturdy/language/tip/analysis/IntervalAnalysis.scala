package sturdy.language.tip.analysis

import sturdy.data.{WithJoin, given}
import sturdy.effect.{Effectful, AnalysisState, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.JoinedDecidableCallFrame
import sturdy.effect.failure.{AFailureCollect, Failure}
import sturdy.effect.print.{APrintPrefix, given}
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.given
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.GenericInterpreter.{FixIn, AllocationSite, FixOut, Field}
import sturdy.language.tip.abstractions.*

object IntervalAnalysis extends Interpreter,
  Ints.Interval, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type MayJoin[A] = WithJoin[A]
  override type Ctx = CallString

  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

  type InState = Store
  type OutState = (Store, APrintPrefix.PrintResult[Value])
  type AllState = OutState

  class Effects(initEnvironment: Environment, initStore: Store)
    extends JoinedDecidableCallFrame[Unit, String, Addr]
      with AStoreMultiAddrThreadded[AllocationSiteAddr, Value](initStore)
      with AAllocationFromContext[AllocationSite, Addr](fromAllocationSite)
      with APrintPrefix[Value]
      with AUserInput[Value](Value.IntValue(IntInterval.Top))
      with AFailureCollect
      with AnalysisState[InState, OutState, AllState]:
    override def initialCallFrameData: Unit = ()
    override def initialCallFrameVars: Map[String, Addr] = initEnvironment
    override def getInState() = getStore
    override def setInState(in: InState) = setStore(in)
    override def getOutState() = (getStore, getPrinted)
    override def setOutState(out: OutState) = { setStore(out._1); setPrinted(out._2) }
    override def getAllState() = getOutState()
    override def setAllState(all: AllState) = setOutState(all)

  def apply(initEnvironment: Environment, initStore: Store, steps: Int): Instance =
    val effects = new Effects(initEnvironment, initStore)
    given Effects = effects
    new Instance(effects, steps)

  class Instance(effects: Effects, steps: Int)(using Failure, Effectful)
    extends GenericInstance(effects):

    given Effects = effects

    final def vintOps: IntegerOps[Int, VInt] = implicitly
    final def vcompareOps: OrderingOps[VInt, VBool] = implicitly
    final def vintEqOps: EqOps[VInt, VBool] = implicitly
    final def vrefEqOps: EqOps[VRef, VBool] = implicitly
    final def vfunEqOps: EqOps[VFun, VBool] = implicitly
    final def vrecEqOps: EqOps[VRecord, VBool] = ??? //new ARecordEqOps(using lazily(eqOps))
    final def vfunOps: FunctionOps[Function, Seq[Value], Value, VFun] = implicitly
    final def vrefOps: ReferenceOps[Addr, VRef] = implicitly
    final def vrecOps: RecordOps[Field, Value, VRecord] = implicitly
    final def vbranchOps: BooleanBranching[Topped[Boolean], Unit] = implicitly

    var bounds: Set[Int] = Set.empty
    given Widen[IntInterval] = new IntIntervalWiden(bounds)
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override def execute(p: Program): Value =
      bounds = p.intLiterals
      super.execute(p)

    val callSites = callSitesLogger()

    protected override def context = callSites.callString(2)
    protected override def contextFree = fix.log(callSites, _)
    override def contextSensitive = fix.dispatch(isFunOrWhile, Seq(
      // call
      fix.iter.topmost,
      // while
      fix.unwind(steps,
        fix.iter.innermost
      )
    ))
