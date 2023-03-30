package sturdy.language.tip.analysis

import sturdy.{Executor, data, fix}
import sturdy.data.MayJoin
import sturdy.data.{WithJoin, given}
import sturdy.effect.{EffectStack, store, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.print.Print
import sturdy.effect.print.PrintBound
import sturdy.effect.print.given
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix.iter.{IncrementalInnermost, StackLogger}
import sturdy.fix.{Combinator, CombinatorFixpoint, ContextualInStateWidening, InStateWidening, Stack, StackConfig, StackedStates}
import sturdy.incremental.Change
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

object IntervalAnalysis extends Interpreter,
  Ints.Interval, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

  class Instance(initEnvironment: Environment, initStore: Store, stackConfig: StackConfig, callSites: Int) extends GenericInstance:
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
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(NumericInterval(Int.MinValue, Int.MaxValue)))

    var bounds: Set[Int] = Set()
    given Widen[VInt] = new NumericIntervalWiden[Int](bounds, Int.MinValue, Int.MaxValue)
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override def execute(p: Program): Value =
      bounds = p.intLiterals
      super.execute(p)

    override def copyState(from: Executor): Unit = {
      super.copyState(from)
      bounds = from.asInstanceOf[Instance].bounds
    }

    override val fixpoint =
      callSiteSensitive(callSites, fix.dispatch(isFunOrWhile, Seq(
        fix.iter.innermost(stackConfig), fix.iter.innermost(stackConfig)))
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig, callSites)

  class InitialRunInstance(initEnvironment: Environment, initStore: Store, callSites: Int)
    extends Instance(initEnvironment, initStore, StackConfig.StackedStates() ,callSites):

    var stackLogger: StackLogger[FixIn, FixOut[Value]] = null

    /** Fixpoint algorithm for initial run.
     * The fixpoint algorithm logs the stacks that occur during the analysis. */
    override val fixpoint =
      callSiteSensitive(callSites, {
        val inStateWidening = new ContextualInStateWidening(implicitly)(using effectStack.widenIn)
        val stack: StackedStates[FixIn, FixOut[Value]] = new StackedStates(effectStack)(inStateWidening, true)
        stackLogger = new fix.iter.StackLogger(stack)
        val stackSuperclass = stack.asInstanceOf[Stack[FixIn, FixOut[Value], effectStack.In, effectStack.Out]]
        fix.dispatch(isFunOrWhile, Seq(
          fix.log(stackLogger, fix.iter.innermost(stackSuperclass)), fix.iter.innermost(StackConfig.StackedStates())))
      }).fixpoint

  class IncrementalUpdateInstance(changes: Iterator[Change[FixIn]],
                                  stackLogger: StackLogger[FixIn, FixOut[Value]],
                                  initEnvironment: Environment,
                                  initStore: Store,
                                  callSites: Int)
    extends Instance(initEnvironment, initStore, StackConfig.StackedStates() ,callSites):

    /** Fixpoint algorithm for an incremental update.
     * The fixpoint algorithm reanalyzes changes bottom-up from a changed `dom` to its dependencies. */
    override val fixpoint =
      callSiteSensitive(callSites, {
        def inStateWidening: InStateWidening[FixIn, effectStack.In] = new ContextualInStateWidening(implicitly)(using effectStack.widenIn)
        given isw: InStateWidening[FixIn, effectStack.In] = inStateWidening
        fix.dispatch(isFunOrWhile, Seq(
          new IncrementalInnermost(changes,stackLogger), fix.iter.innermost(StackConfig.StackedStates())))
      }).fixpoint


