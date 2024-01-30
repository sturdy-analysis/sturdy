package sturdy.language.tip.analysis

import sturdy.control.{ControlEvent, ControlObservable, RecordingControlObserver}
import sturdy.data.{WithJoin, given}
import sturdy.effect.given
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind, ObservableFailure}
import sturdy.effect.print.PrintFiniteAlphabet
import sturdy.effect.print.given
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
import sturdy.language.tip.analysis.IntervalAnalysis.controlEventLogger

object SignAnalysis extends Interpreter,
  Ints.Sign, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix, Control:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue)

  class Instance(initEnvironment: Environment, initStore: Store, stackConfig: StackConfig) extends GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc]:
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures with ObservableFailure(this)

    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: ObservedBooleanBranching[Value, Unit] = new ObservedBooleanBranching

    override val callFrame: JoinableDecidableCallFrame[Unit, String, Value] = new JoinableDecidableCallFrame((), initEnvironment)
    override val store: AStoreMultiAddrThreadded[AllocationSiteAddr, Value] = new AStoreMultiAddrThreadded(initStore)
    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
    override val print: PrintFiniteAlphabet[Value] = new PrintFiniteAlphabet
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(IntSign.TopSign))

    given Lazy[Finite[Value]] = lazily(FiniteValue)

    val controlObserver = new RecordingControlObserver[Control.Atom, Control.Section, Control.Exc](true)
    this.addControlObserver(controlObserver)

    override val fixpoint =
      fix.log(controlEventLogger(this, branchOps),
        fix.filter((dom: FixIn) => isFunOrWhile(dom) >= 0,
          parameterSensitive(this, fix.iter.innermost(stackConfig)))).fixpoint
    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig)

  class DAIInstance(initEnvironment: Environment, initStore: Store) extends Instance(initEnvironment, initStore, StackConfig.StackedStates()):
    override val fixpoint = new fix.DAIFixpoint((dom: FixIn) => isFunOrWhile(dom))
    override def newInstance: sturdy.Executor = new DAIInstance(initEnvironment, initStore)
