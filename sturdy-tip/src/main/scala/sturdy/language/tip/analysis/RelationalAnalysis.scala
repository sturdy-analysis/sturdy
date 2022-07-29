//package sturdy.language.tip.analysis
//
//import apron.Manager
//import apron.Tcons1
//import apron.Texpr1CstNode
//import apron.Texpr1Node
//import sturdy.Executor
//import sturdy.data.{WithJoin, given}
//import sturdy.effect.given
//import sturdy.effect.allocation.AAllocationFromContext
//import sturdy.effect.callframe.ApronCallFrame
//import sturdy.effect.callframe.JoinableDecidableCallFrame
//import sturdy.effect.failure.CollectedFailures
//import sturdy.effect.failure.Failure
//import sturdy.effect.print.PrintBound
//import sturdy.effect.print.given
//import sturdy.effect.store.AStoreMultiAddrThreadded
//import sturdy.effect.store.Store
//import sturdy.effect.userinput.AUserInput
//import sturdy.fix
//import sturdy.fix.StackConfig
//import sturdy.fix.context
//import sturdy.language.tip.AllocationSite
//import sturdy.language.tip.*
//import sturdy.language.tip.abstractions.Fix
//import sturdy.language.tip.abstractions.Functions
//import sturdy.language.tip.abstractions.Records
//import sturdy.language.tip.abstractions.References
//import sturdy.util.Lazy
//import sturdy.util.lazily
//import sturdy.values.{*, given}
//import sturdy.values.booleans.{*, given}
//import sturdy.values.integer.{*, given}
//import sturdy.values.functions.{*, given}
//import sturdy.values.records.{*, given}
//import sturdy.values.references.{*, given}
//import sturdy.values.ordering.{*, given}
//import sturdy.util.{*, given}
//import sturdy.language.tip.{*, given}
//
//object RelationalAnalysis extends Interpreter,
//  Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:
//
//  override type J[A] = WithJoin[A]
//
//  override type VInt = Texpr1Node
//  override type VBool = Tcons1
//
//  final def asBoolean(v: Value)(using inst: Instance): VBool = v match
//    case Value.BoolValue(b) => b
//    case Value.IntValue(i) =>
//      inst.callFrame.makeConstraint(i, Tcons1.DISEQ)
//    case Value.TopValue =>
//      inst.callFrame.makeConstraint(inst.callFrame.topInt, Tcons1.EQ)
//    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")
//
//  final def asInt(v: Value)(using inst: Instance): VInt = v match
//    case Value.BoolValue(b) =>
//      inst.callFrame.ifThenElse(b) {
//        inst.intOps.integerLit(1)
//      } {
//        inst.intOps.integerLit(0)
//      }(using inst.effectStack)
//    case Value.IntValue(i) => i
//    case Value.TopValue =>
//      inst.callFrame.topInt
//    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")
//
////  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])
//
//  class Instance(apronManager: Manager, initEnvironment: Environment, initStore: Store, stackConfig: StackConfig, callSites: Int) extends GenericInstance:
//    override def jv: WithJoin[Value] = implicitly
//
//    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
//    private given Failure = failure
//
//    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
//    override val intOps: IntegerOps[Int, Value] = implicitly
//    override val compareOps: OrderingOps[Value, Value] = implicitly
//    override val eqOps: EqOps[Value, Value] = implicitly
//    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
//    override val refOps: ReferenceOps[Addr, Value] = implicitly
//    override val recOps: RecordOps[Field, Value, Value] = implicitly
//    override val branchOps: BooleanBranching[Value, Unit] = implicitly
//
//    override val callFrame: ApronCallFrame[Unit, String] = new ApronCallFrame(apronManager, (), initEnvironment)
//    override val store: AStoreMultiAddrThreadded[AllocationSiteAddr, Value] = new AStoreMultiAddrThreadded(initStore)
//    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
//    override val print: PrintBound[Value] = new PrintBound
//    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(NumericInterval(Int.MinValue, Int.MaxValue)))
//
//    var bounds: Set[Int] = Set()
//    given Widen[VInt] = new NumericIntervalWiden[Int](bounds, Int.MinValue, Int.MaxValue)
//    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])
//
//    override def execute(p: Program): Value =
//      bounds = p.intLiterals
//      super.execute(p)
//
//    override def copyState(from: Executor): Unit = {
//      super.copyState(from)
//      bounds = from.asInstanceOf[Instance].bounds
//    }
//
//    final override val fixpoint =
//      callSiteSensitive(callSites, fix.dispatch(isFunOrWhile, Seq(
//        fix.iter.innermost(stackConfig), fix.iter.innermost(stackConfig)))
//      ).fixpoint
//
//    override def newInstance: sturdy.Executor = new Instance(apronManager, initEnvironment, initStore, stackConfig, callSites)
//
//
