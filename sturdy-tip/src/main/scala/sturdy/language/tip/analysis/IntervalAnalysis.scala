package sturdy.language.tip.analysis

import sturdy.control.{ControlEvent, ControlEventGraphBuilder, ControlObservable, ControlObserver, RecordingControlObserver}
import sturdy.{Executor, data, fix}
import sturdy.data.MayJoin
import sturdy.data.{WithJoin, given}
import sturdy.effect.given
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.ObservableExcept
import sturdy.effect.failure.{CollectedFailures, Failure, ObservableFailure}
import sturdy.effect.print.Print
import sturdy.effect.print.PrintBound
import sturdy.effect.print.given
import sturdy.effect.store
import sturdy.effect.store
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.StackConfig
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
  Ints.Interval, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix, Control:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

  class Instance(initEnvironment: Environment, initStore: Store, stackConfig: StackConfig, callSites: Int) extends GenericInstance, ControlObservable[TipControl.Atom, TipControl.Section, TipControl.Exc]:
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

    val cfgLogger = controlLogger[CallString](callSites > 0)

    val controlObserver = new RecordingControlObserver[TipControl.Atom, TipControl.Section, TipControl.Exc](true)
    this.addControlObserver(controlObserver)
    
    val controlEventGraphBuilder = new ControlEventGraphBuilder[TipControl.Atom, TipControl.Section, TipControl.Exc]
    this.addControlObserver(controlEventGraphBuilder)

    final override val fixpoint =
      fix.log(controlEventLogger(this, branchOps),
        callSiteSensitive(callSites,
          fix.log(cfgLogger.logger,
            fix.dispatch(isFunOrWhile, Seq(
              fix.iter.innermost(stackConfig), fix.iter.innermost(stackConfig)
            ))
          )
        )
      ).fixpoint
      
    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig, callSites)

