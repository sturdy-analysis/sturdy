package sturdy.language.tip.analysis

import sturdy.data.{WithJoin, given}
import sturdy.effect.{AnalysisState, Effectful, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.JoinedCallFrame
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
import sturdy.language.tip.GenericInterpreter.{AllocationSite, FixIn, FixOut, Field, given}
import sturdy.language.tip.abstractions.*

object SignAnalysis extends Interpreter,
  Ints.Sign, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type MayJoin[A] = WithJoin[A]
  override type Ctx = Parameters

  given Lazy[Join[Value]] = lazily(CombineValue)

  type InState = Store
  type OutState = (Store, APrintPrefix.PrintResult[Value])
  type AllState = OutState

  class Effects(initEnvironment: Environment, initStore: Store)
    extends JoinedCallFrame[Unit, String, Addr]
      with AStoreMultiAddrThreadded[AllocationSiteAddr, Value](initStore)
      with AAllocationFromContext[AllocationSite, Addr](fromAllocationSite)
      with APrintPrefix[Value]
      with AUserInput[Value](Value.IntValue(IntSign.TopSign))
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

    final def vintOps: IntOps[VInt] = implicitly
    final def vcompareOps: CompareOps[VInt, VBool] = implicitly
    final def vintEqOps: EqOps[VInt, VBool] = implicitly
    final def vrefEqOps: EqOps[VRef, VBool] = implicitly
    final def vfunEqOps: EqOps[VFun, VBool] = implicitly
    final def vrecEqOps: EqOps[VRecord, VBool] = ??? // new ARecordEqOps(using lazily(eqOps))
    final def vfunOps: FunctionOps[Function, Value, Value, VFun] = implicitly
    final def vrefOps: ReferenceOps[Addr, VRef] = implicitly
    final def vrecOps: RecordOps[Field, Value, VRecord] = implicitly
    final def vbranchOps: BooleanBranching[Topped[Boolean], MayJoin] = implicitly

    given Lazy[Widen[Value]] = lazily(CombineValue)

    given Finite[Parameters] with {}

    protected override def context = parameters
    protected override def contextFree = identity
    override def contextSensitive = fix.dispatch(isFunOrWhile, Seq(
      // call
      fix.iter.topmost,
      // while
      fix.unwind(steps,
        fix.iter.innermost
      )
    ))
