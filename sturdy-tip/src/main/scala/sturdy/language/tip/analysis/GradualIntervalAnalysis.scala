package sturdy.language.tip.analysis

import sturdy.control.ControlObservable
import sturdy.{Executor, data, fix}
import sturdy.data.MayJoin
import sturdy.data.{WithJoin, given}
import sturdy.effect.given
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.allocation.Allocator
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.ObservableExcept
import sturdy.effect.failure.{CollectedFailures, Failure, ObservableFailure}
import sturdy.effect.print.Print
import sturdy.effect.print.PrintBound
import sturdy.effect.print.given
import sturdy.effect.store
import sturdy.effect.store.{*, given}
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
import sturdy.values.ordering.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.{AllocationSite, Field, FixIn, FixOut}
import sturdy.language.tip.abstractions.*
import sturdy.control.ControlObservable
import sturdy.{Executor, data, fix}
import sturdy.data.MayJoin
import sturdy.data.{WithJoin, given}
import sturdy.effect.given
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.allocation.Allocator
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.ObservableExcept
import sturdy.effect.failure.{CollectedFailures, Failure, ObservableFailure}
import sturdy.effect.print.Print
import sturdy.effect.print.PrintBound
import sturdy.effect.print.given
import sturdy.effect.store
import sturdy.effect.store.{*, given}
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.StackConfig
import sturdy.gradual.values.integer.{GradualizedIntegerOps, IntegerOpsGradualization, OverflowGradualization, OverflowNumericIntervalGradualization, OverflowOptimisticNumericIntervalOps}
import sturdy.language.tip.TipFailure
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.{AllocationSite, Field, FixIn, FixOut}
import sturdy.language.tip.abstractions.*
import sturdy.language.tip.analysis.IntervalAnalysis.{VInt, gradualLogger}

class GradualIntervalAnalysis extends Interpreter,
  Ints.Interval, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix, Control:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

  class Instance(initEnvironment: Environment, initStore: InitStore, stackConfig: StackConfig, callSites: Int) extends GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures with ObservableFailure(this)

    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)

    given gl: TipGradualLogger[VInt, Value] = gradualLogger()
    val eo: TipElaborationOps[VInt] = implicitly

    override val intOps: IntegerOps[Int, Value] = gradualIntegerOps[OverflowGradualization]
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

    val observedStackConfig = stackConfig.withObservers(Seq(this.triggerControlEvent))

    final override val fixpoint =
      fix.log(controlEventLogger(this),
        callSiteSensitive(callSites,
          fix.log(cfgLogger.logger,
            fix.dispatch(isFunOrWhile, Seq(
              fix.iter.innermost(observedStackConfig), fix.iter.innermost(observedStackConfig)
            ))
          )
        )
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig, callSites)

