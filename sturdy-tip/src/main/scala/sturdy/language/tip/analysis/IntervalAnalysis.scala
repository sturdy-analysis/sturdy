package sturdy.language.tip.analysis

import sturdy.data.{WithJoin, given}
import sturdy.effect.{Effectful, AnalysisState, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.CCallFrame
import sturdy.effect.failure.{AFailureCollect, Failure}
import sturdy.effect.print.{APrintPrefix, given}
import sturdy.effect.store.AStoreMultiAddrThreadded
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
import sturdy.language.tip.GenericInterpreter.{FixIn, GenericPhi, AllocationSite, FixOut}
import sturdy.language.tip.abstractions.*

object IntervalAnalysis extends Interpreter,
  Ints.Interval, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type MayJoin[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

  type InState = Store
  type OutState = (Store, APrintPrefix.PrintResult[Value])
  type AllState = OutState

  class Effects(initEnvironment: Environment, initStore: Store)
    extends CCallFrame[Unit, String, Addr]((), initEnvironment)
      with AStoreMultiAddrThreadded[AllocationSiteAddr, Value](initStore)
      with AAllocationFromContext[AllocationSite, Addr](fromAllocationSite)
      with APrintPrefix[Value]
      with AUserInput[Value](Value.IntValue(IntInterval.Top))
      with AFailureCollect
      with AnalysisState[InState, OutState, AllState]:
    override def getInState() = getStore
    override def setInState(in: InState) = setStore(in)
    override def getOutState() = (getStore, getPrinted)
    override def setOutState(out: OutState) = { setStore(out._1); setPrinted(out._2) }
    override def getAllState() = getOutState()
    override def setAllState(all: AllState) = setOutState(all)

  def apply(initEnvironment: Environment, initStore: Store, steps: Int, cfgOnlyCalls: Boolean): Instance =
    val effects = new Effects(initEnvironment, initStore)
    given Effects = effects
    new Instance(effects, steps, cfgOnlyCalls)

  class Instance(effects: Effects, steps: Int, cfgOnlyCalls: Boolean)(using Failure, Effectful)
    extends GenericInstance(effects):

    given Effects = effects

    final def vintOps: IntOps[VInt] = implicitly
    final def vcompareOps: CompareOps[VInt, VBool] = implicitly
    final def vintEqOps: EqOps[VInt, VBool] = implicitly
    final def vrefEqOps: EqOps[VRef, VBool] = implicitly
    final def vfunEqOps: EqOps[VFun, VBool] = implicitly
    final def vrecEqOps: EqOps[VRecord, VBool] = ??? //new ARecordEqOps(using lazily(eqOps))
    final def vfunOps: FunctionOps[Function, Value, Value, VFun] = implicitly
    final def vrefOps: ReferenceOps[Addr, VRef] = implicitly
    final def vrecOps: RecordOps[String, Value, VRecord] = implicitly
    final def vbranchOps: BooleanBranching[Topped[Boolean], MayJoin] = implicitly

    var bounds: Set[Int] = Set.empty
    given Widen[IntInterval] = new IntIntervalWiden(bounds)
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override def execute(p: Program): Value =
      bounds = p.intLiterals
      super.execute(p)

    val callSites = callSitesLogger()
    val cfg = control[List[Exp.Call], Value](sensitive = true, cfgOnlyCalls)

    val phi =
      fix.log(callSites,
        fix.contextSensitive(callSites.callString(2),
          fix.log(cfg.logger,
            fix.dispatch(isFunOrWhile, Seq(
              // call
              fix.iter.topmost,
              // while
              fix.unwind(steps,
                fix.iter.innermost
              )
            ))
          )
        )
      )
