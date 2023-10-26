package sturdy.language.tip.analysis

import sturdy.data.{MayJoin, WithJoin, given}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.print.{Print, PrintBound, given}
import sturdy.effect.{Stateless, store, given}
import sturdy.effect.store.{*, given}
import sturdy.effect.userinput.AUserInput
import sturdy.fix.StackConfig
import sturdy.language.tip.abstractions.*
import sturdy.language.tip.{AllocationSite, Field, FixIn, FixOut, TipFailure, *, given}
import sturdy.util.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{*, given}
import sturdy.{Executor, data, fix}

object IntervalRecencyAnalysis extends Interpreter,
  Ints.Interval, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSitesRecency, Fix:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

  class Instance(initEnvironment: Environment, initStore: InitStore, stackConfig: StackConfig, callSites: Int) extends GenericInstance:
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

    private val initPhysicalStore = initStore.map { (addr,v) => PhysicalAddress(addr, Recency.Recent) -> v }.toMap
    private val astore: AStoreThreaded[PhysicalAddress[AllocationSiteAddr], PowPhysicalAddress[AllocationSiteAddr], Value] = new AStoreThreaded(initPhysicalStore)

    override val store: RecencyStore[AllocationSiteAddr, Addr, Value] = ??? // new RecencyStore[AllocationSiteAddr, Addr, Value](astore)
    override val alloc: Allocator[Addr, AllocationSite] = new Allocator with Stateless:
      override def alloc(ctx: AllocationSite): PowVirtualAddress[AllocationSiteAddr] =
        PowVirtualAddress(store.alloc(References.allocationSiteAddr(ctx)))

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

    final override val fixpoint =
      callSiteSensitive(callSites, fix.dispatch(isFunOrWhile, Seq(
        fix.iter.innermost(stackConfig), fix.iter.innermost(stackConfig)))
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig, callSites)

