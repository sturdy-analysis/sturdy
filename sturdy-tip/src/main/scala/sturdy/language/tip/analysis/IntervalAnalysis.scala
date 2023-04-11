package sturdy.language.tip.analysis

import sturdy.{Executor, data, fix}
import sturdy.data.{JoinTuple2, MayJoin, WithJoin, given}
import sturdy.effect.{EffectStack, TrySturdy, store, given}
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
import sturdy.fix.callgraph.CallGraphLogger
import sturdy.fix.summary.{CacheSummary, ContextSensitiveSummary, SingletonSummary, SummaryLogger}
import sturdy.fix.{Combinator, CombinatorFixpoint, Contextual, ContextualInStateWidening, InStateWidening, Stack, StackConfig, StackedStates}
import sturdy.incremental.{Change, Identifiable, ListDelta}
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

    override val fixpoint: ContextFunction1[EffectStack, CombinatorFixpoint[FixIn, FixOut[Value]]] =
      callSiteSensitive(callSites, fix.dispatch(isFunOrWhile, Seq(
        fix.iter.innermost(stackConfig), fix.iter.innermost(stackConfig)))
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig, callSites)

  class InitialRunInstance(val initEnvironment: Environment, val initStore: Store, val callSites: Int)
    extends Instance(initEnvironment, initStore, StackConfig.StackedStates() ,callSites):

    var callGraphLogger: CallGraphLogger[FixIn, CallString, Exp.Call, Function] = null
    var summaryLogger: SummaryLogger[FixIn, FixOut[Value]] = null
    var stack: StackedStates[FixIn, FixOut[Value]] & Stack[FixIn, FixOut[Value], effectStack.In, effectStack.Out] = null

    /** Fixpoint algorithm for initial run.
     * The fixpoint algorithm logs the stacks that occur during the analysis. */
    override val fixpoint: ContextFunction1[EffectStack, CombinatorFixpoint[FixIn, FixOut[IntervalAnalysis.Value]]] =
      callSiteSensitive(callSites, {
        
        // Setup loggers that record data needed for incremental updates
        callGraphLogger = new CallGraphLogger(implicitly)({
          case FixIn.EnterFunction(f) => Some(f)
          case _ => None
        })

        given Structural[List[Any]] with {}
        summaryLogger = new SummaryLogger(using effectStack)(dom =>
          ContextSensitiveSummary[CallString, effectStack.In, (TrySturdy[FixOut[Value]], effectStack.Out)](
            SingletonSummary[effectStack.In, (TrySturdy[FixOut[Value]], effectStack.Out)](
              effectStack.widenIn(dom),
              JoinTuple2[TrySturdy[FixOut[Value]], effectStack.Out, Widening.Yes](using implicitly, effectStack.widenOut(dom)))
          )
        )

        val inStateWidening = new ContextualInStateWidening(implicitly)(using effectStack.widenIn)
        stack = new StackedStates[FixIn,FixOut[Value]](effectStack)(inStateWidening, true).asInstanceOf

        fix.dispatch(isFunOrWhile, Seq(
          fix.log(fix.manyLogger(callGraphLogger,summaryLogger), fix.iter.innermost(stack)), fix.iter.innermost(StackConfig.StackedStates())))
      }).fixpoint
//
//  class IncrementalUpdateInstance(initialRun: InitialRunInstance)
//    extends Instance(initialRun.initEnvironment, initialRun.initStore, StackConfig.StackedStates(), initialRun.callSites):
//
//    given inStateWidening: InStateWidening[FixIn, effectStack.In] = initialRun.stack.inStateWidening.asInstanceOf[InStateWidening[FixIn, effectStack.In]]
//    val incremental: IncrementalFixpoint[FixIn, FixOut[Value]] = new IncrementalFixpoint(initialRun.stackLogger)
//
//    def apply(initialProgram: Program, updatedProgram: Program) =
//      val changes = ListDelta.sub(initialProgram.funs.toList, updatedProgram.funs.toList)
//      this.functions = changes.keepOld.asInstanceOf[Map[String,sturdy.language.tip.Function]]
//      incremental.update(changes.map(FixIn.EnterFunction(_,None)), fixed)
//
//    /** Fixpoint algorithm for an incremental update.
//     * The fixpoint algorithm reanalyzes changes bottom-up from a changed `dom` to its dependencies. */
//    override val fixpoint =
//      callSiteSensitive(initialRun.callSites, {
//        fix.dispatch(isFunOrWhile, Seq(fix.iter.innermost(incremental.dstack.asInstanceOf[Stack[FixIn, FixOut[Value], effectStack.In, effectStack.Out]]), fix.iter.innermost(StackConfig.StackedStates())))
//      }).fixpoint

given FixInIdentifier: Identifiable[FixIn] with
  type Id = String
  extension (f: FixIn)
    override def id: String =
      f match
        case FixIn.EnterFunction(f) => f.id
        case FixIn.Eval(e) => e.toString
        case FixIn.Run(s) => s.toString

  override def eqv(x: FixIn, y: FixIn) = Eq.derived[FixIn].eqv(x,y)