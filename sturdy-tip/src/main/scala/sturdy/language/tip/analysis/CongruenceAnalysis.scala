package sturdy.language.tip.analysis

import sturdy.data.{WithJoin, given}
import sturdy.effect.given
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.print.{CPrint, PrintBound, given}
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.context.FiniteParameters
import sturdy.fix.{StackConfig, given}
import sturdy.language.tip.TipFailure
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.{AllocationSite, Field, FixIn, FixOut}
import sturdy.language.tip.abstractions.*
import sturdy.language.tip.analysis.IntervalAnalysis.callSiteSensitive

object CongruenceAnalysis extends Interpreter,
  Ints.CongruenceClass, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue)

  class Instance(initEnvironment: Environment, initStore: Store, stackConfig: StackConfig) extends GenericInstance:
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: JoinableDecidableCallFrame[Unit, String, Value] = new JoinableDecidableCallFrame((), initEnvironment)
    override val store: AStoreMultiAddrThreadded[AllocationSiteAddr, Value] = new AStoreMultiAddrThreadded(initStore)
    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(Congruence.top))

    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override val fixpoint =
      fix.filter((dom: FixIn) => isFunOrWhile(dom) >= 0,
        callSiteSensitive(1, fix.iter.innermost(stackConfig))).fixpoint
    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig)

  class DAIInstance(initEnvironment: Environment, initStore: Store) extends Instance(initEnvironment, initStore, StackConfig.StackedStates()):
    override val fixpoint = new fix.DAIFixpoint((dom: FixIn) => isFunOrWhile(dom))
    override def newInstance: sturdy.Executor = new DAIInstance(initEnvironment, initStore)
