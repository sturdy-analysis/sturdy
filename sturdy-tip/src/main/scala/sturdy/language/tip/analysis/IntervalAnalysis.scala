package sturdy.language.tip.analysis

import sturdy.{Executor, data, fix}
import sturdy.data.MayJoin
import sturdy.data.{WithJoin, given}
import sturdy.effect.{AnalysisState, Effectful, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.DecidableCallFrame
import sturdy.effect.callframe.JoinableConcreteCallFrame
import sturdy.effect.failure.{AFailureCollect, Failure}
import sturdy.effect.print.Print
import sturdy.effect.print.{APrintPrefix, given}
import sturdy.effect.store
import sturdy.effect.store
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.GenericInterpreter.{AllocationSite, Field, FixIn, FixOut}
import sturdy.language.tip.abstractions.*

object IntervalAnalysis extends Interpreter,
  Ints.Interval, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

  class Instance(initEnvironment: Environment, initStore: Store, stackedFrames: Boolean, callSites: Int) extends GenericInstance:
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
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(IntInterval.Top))

    var bounds: Set[Int] = Set()
    given Widen[IntInterval] = new IntIntervalWiden(bounds)
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override def execute(p: Program): Value =
      bounds = p.intLiterals
      super.execute(p)

    override def copyState(from: Executor): Unit = {
      super.copyState(from)
      bounds = from.asInstanceOf[Instance].bounds
    }

    final override val fixpoint =
      callSiteSensitive(callSites, fix.dispatch(isFunOrWhile, Seq(
        fix.iter.innermost(stackedFrames), fix.iter.innermost(stackedFrames)))
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackedFrames, callSites)

