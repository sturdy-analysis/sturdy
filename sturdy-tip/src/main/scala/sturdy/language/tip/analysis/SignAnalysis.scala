package sturdy.language.tip.analysis

import sturdy.data.{WithJoin, given}
import sturdy.effect.AnalysisState
import sturdy.effect.{Effectful, AnalysisState, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.JoinableConcreteCallFrame
import sturdy.effect.failure.{AFailureCollect, Failure}
import sturdy.effect.print.{APrintPrefix, given}
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.Fixpoint
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
import sturdy.language.tip.GenericInterpreter.{Field, FixIn, AllocationSite, FixOut, given}
import sturdy.language.tip.abstractions.*

object SignAnalysis extends Interpreter,
  Ints.Sign, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue)

  class Instance(initEnvironment: Environment, initStore: Store, stackedFrames: Boolean) extends GenericInstance:
    override def jv: WithJoin[Value] = implicitly

    override val failure: AFailureCollect = new AFailureCollect
    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: JoinableConcreteCallFrame[Unit, String, Value] = new JoinableConcreteCallFrame((), initEnvironment)
    override val store: AStoreMultiAddrThreadded[AllocationSiteAddr, Value] = new AStoreMultiAddrThreadded(initStore)
    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
    override val print: APrintPrefix[Value] = new APrintPrefix
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(IntSign.TopSign))

//    given Join[InState] = implicitly
//    given Join[OutState] = implicitly
//    given Widen[InState] = implicitly
//    given Widen[OutState] = implicitly
    given Lazy[Widen[Value]] = lazily(CombineValue)

    override val fixpoint =
      fix.filter((dom: FixIn) => isFunOrWhile(dom) >= 0,
        parameterSensitive(this, fix.iter.innermost(stackedFrames))).fixpoint
    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackedFrames)

  class DAIInstance(initEnvironment: Environment, initStore: Store) extends Instance(initEnvironment, initStore, true):
    override val fixpoint = new fix.DAIFixpoint((dom: FixIn) => isFunOrWhile(dom))
    override def newInstance: sturdy.Executor = new DAIInstance(initEnvironment, initStore)
