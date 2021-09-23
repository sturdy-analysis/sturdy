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
import sturdy.language.tip.abstractions.*
import Fix.*

object SignAnalysis extends Interpreter,
  Ints.Sign, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites:

  given Lazy[JoinValue[Value]] = lazily(liftedJoinValue)

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

  def apply(initEnvironment: Environment, initStore: Store, steps: Int): Instance =
    val effects = new Effects(initEnvironment, initStore)
    given Failure = effects
    given JoinComputation = effects
    new Instance(effects, steps)

  class Instance(effects: Effects, steps: Int)(using Failure, JoinComputation)
    extends Interpreter with GenericInterpreter[Value, Addr, Effects](effects):

    given Effects = effects

    final val vintOps: IntOps[VInt] = implicitly
    final val vcompareOps: CompareOps[VInt, VBool] = implicitly
    final val vintEqOps: EqOps[VInt, VBool] = implicitly
    final val vrefEqOps: EqOps[VRef, VBool] = implicitly
    final val vfunEqOps: EqOps[VFun, VBool] = implicitly
    final val vrecEqOps: EqOps[VRecord, VBool] = new ARecordEqOps(using lazily(eqOps))
    final val vfunOps: FunctionOps[Function, Value, Value, VFun] = implicitly
    final val vrefOps: ReferenceOps[Addr, VRef] = implicitly
    final val vrecOps: RecordOps[String, Value, VRecord] = implicitly

    given Lazy[fix.Widening[Value]] = lazily(liftedWidening)

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

