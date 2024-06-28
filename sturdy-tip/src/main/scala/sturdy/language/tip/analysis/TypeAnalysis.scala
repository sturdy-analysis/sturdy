package sturdy.language.tip.analysis

import sturdy.control.{ControlEvent, ControlObservable, RecordingControlObserver}
import sturdy.data.{WithJoin, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind, ObservableFailure}
import sturdy.effect.given
import sturdy.effect.print.{PrintBound, PrintFiniteAlphabet, given}
import sturdy.effect.store.{AStoreThreaded, Store}
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.context.FiniteParameters
import sturdy.fix.{StackConfig, given}
import sturdy.language.tip.abstractions.*
import sturdy.language.tip.analysis.IntervalAnalysis.controlEventLogger
import sturdy.language.tip.{AllocationSite, Field, FixIn, FixOut, TipFailure, *, given}
import sturdy.util.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.{*, given}
import sturdy.values.{*, given}

object TypeAnalysis extends Interpreter,
  Ints.Types, Functions.Types, Records.PreciseFieldsOrTop, References.AllocationSites, Fix, Control:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue)
  given Lazy[Widen[Value]] = lazily(CombineValue)

  class Instance(initEnvironment: Environment, initStore: InitStore, stackConfig: StackConfig) extends GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    private given Failure = failure

    given BoolTypeEqOps[T]: EqOps[T, BaseType[Boolean]] with
      override def equ(v1: T, v2: T): BaseType[Boolean] = BaseType[Boolean]
      override def neq(v1: T, v2: T): BaseType[Boolean] = BaseType[Boolean]

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
    override val intOps: IntegerOps[Int, Value] = implicitly
    override val boolOps: BooleanOps[Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: JoinableDecidableCallFrame[String, String, Value, Exp.Call] = new JoinableDecidableCallFrame("$main", Iterable.empty)
    override val store: AStoreThreaded[AllocationSiteAddr, Addr, Value] = new AStoreThreaded(initStore)
    override val alloc: AAllocatorFromContext[AllocationSite, Addr] = new AAllocatorFromContext(site =>
      PowersetAddr(References.allocationSiteAddr(site))
    )
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(BaseType[Int]))

    override val fixpoint =
      fix.log(controlEventLogger(this),
        fix.filter((dom: FixIn) => isFunOrWhile(dom) >= 0,
          contextInsensitive(fix.iter.innermost(stackConfig)))).fixpoint
    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig)

  class DAIInstance(initEnvironment: Environment, initStore: InitStore) extends Instance(initEnvironment, initStore, StackConfig.StackedStates()):
    override val fixpoint = new fix.DAIFixpoint((dom: FixIn) => isFunOrWhile(dom))
    override def newInstance: sturdy.Executor = new DAIInstance(initEnvironment, initStore)
