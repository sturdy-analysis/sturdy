//package sturdy.language.tip.analysis
//
//import apron.Manager
//import apron.Tcons1
//import apron.Texpr1CstNode
//import apron.Interval
//import sturdy.Executor
//import sturdy.apron.*
//import sturdy.data.{JOption, JOptionC, WithJoin, given}
//import sturdy.effect.{EffectStack, given}
//import sturdy.effect.allocation.AAllocatorFromContext
//import sturdy.effect.callframe.{ApronCallFrame, DecidableCallFrame, DecidableMutableCallFrame, JoinableDecidableCallFrame, given}
//import sturdy.effect.callframe.ApronCallFrame.given
//import sturdy.effect.failure.CollectedFailures
//import sturdy.effect.failure.Failure
//import sturdy.effect.print.PrintBound
//import sturdy.effect.print.given
//import sturdy.effect.store.AStoreThreaded
//import sturdy.effect.store.Store
//import sturdy.effect.userinput.{AUserInput, AUserInputFun}
//import sturdy.apron.given
//import sturdy.fix
//import sturdy.fix.StackConfig
//import sturdy.fix.context
//import sturdy.language.tip.AllocationSite
//import sturdy.language.tip.*
//import sturdy.language.tip.abstractions.{CfgConfig, ControlFlow, Fix, Functions, Records, References, isFunOrWhile}
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
//import sturdy.values.types.{BaseType, given}
//import sturdy.values.utils.{ConvertInterval, given}
//
//
//object RelationalAnalysis extends Interpreter,
//  Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, ControlFlow, Fix:
//
//  override type J[A] = WithJoin[A]
//  type RelCtx = Unit
//  override type VInt = Topped[ApronExpr[VirtualAddress[RelCtx], BaseType[Int]]]
//  override type VBool = Topped[ApronCons[VirtualAddress[RelCtx], BaseType[Boolean]]]
//
//  final def asBoolean(v: Value)(using inst: Instance): VBool = v match
//    case Value.BoolValue(toppedBool) => toppedBool
//    case Value.IntValue(toppedInt) => toppedInt.map(ApronCons.intNeq(_, ApronExpr.num(0)))
//    case Value.TopValue => Topped.Top
//    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")
//
//  final def asInt(v: Value)(using inst: Instance): VInt = v match
//    case Value.BoolValue(toppedBool) =>
//      toppedBool.flatMap { bv =>
//        import inst.{given_EffectStack, apron, failure, apronState}
//        given Failure = failure
//        val vIntOps = summon[IntegerOps[Int, VInt]]
//        inst.apron.ifThenElsePure(bv, widen = false)(vIntOps.integerLit(1))(vIntOps.integerLit(0))
//      }
//    case Value.IntValue(i) => i
//    case Value.TopValue => Topped.Top
//    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")
//
//  override def topInt(using inst: Instance): VInt = Topped.Top
//  override def topBool: VBool = Topped.Top
//
//  class Instance(apronManager: Manager, initStore: InitStore, stackConfig: StackConfig, callSites: Int) extends GenericInstance:
//    given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])
//
//    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
//    private given Failure = failure
//
//    val apronAlloc: ApronAlloc = ApronAlloc.default(apronManager)
//    implicit val apron: Apron = new Apron(apronManager, apronAlloc)(using failure)
//    implicit def apronState: ApronState = apron.getState
//
//    override def jv: WithJoin[Value] = implicitly
//
//    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
//
//    given EqOps[VRef, VBool] = new LiftedEqOps[VRef, VBool, VRef, Topped[Boolean]](identity, _.map(ApronCons.fromBool))
//    given EqOps[VFun, VBool] = new LiftedEqOps[VFun, VBool, VFun, Topped[Boolean]](identity, _.map(ApronCons.fromBool))
//    given EqOps[VRecord, VBool] = new LiftedEqOps[VRecord, VBool, VRecord, Topped[Boolean]](identity, _.map(ApronCons.fromBool))
//
//    override val intOps: IntegerOps[Int, Value] = implicitly
//    override val compareOps: OrderingOps[Value, Value] = implicitly
//    override val eqOps: EqOps[Value, Value] = implicitly
//    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
//    override val refOps: ReferenceOps[Addr, Value] = implicitly
//    override val recOps: RecordOps[Field, Value, Value] = implicitly
//    override val branchOps: BooleanBranching[Value, Unit] = implicitly
//
//    val apronCallFrame = ApronCallFrame[Unit, String, Exp.Call, RelCtx, BaseType[Int]]()
//    override val callFrame: DecidableCallFrame[String, String, Value, Exp.Call] = new DecidableCallFrame[String,String,Value,Exp.Call]:
//      var data: String = "$main"
//      var names: Map[Int, String] = _
//      var vars: Map[String, Value] = _
//
//      override def getLocal(x: Int): JOptionC[Value] =
//        getLocalByName(names(x))
//      override def getLocalByName(x: String): JOptionC[Value] =
//        vars.getOrElse(x, Value.IntValue(apronCallFrame.getLocalByName(x)))
//
//      override def withNew[A](d: String, vars: Iterable[(String, Option[Value])], site: Exp.Call)(f: => A): A = ???
//
//      override type State = this.type
//
//      override def getState: this.type = ???
//
//      override def setState(st: this.type): Unit = ???
//
//      override def join: Join[this.type] = ???
//
//      override def widen: Widen[this.type] = ???
//
//
//    //      JoinableDecidableCallFrame[String, String, Value, Exp.Call] = new JoinableDecidableCallFrame("$main", Iterable.empty)
//
////    private val relationalStore: Store[PowPhysicalAddress[AllocationSiteAddr], Value, J] = /* Some apron store here? */
//
//    override val store: AStoreThreaded[AllocationSiteAddr, Addr, Value] = new AStoreThreaded(initStore)
//    override val alloc: AAllocatorFromContext[AllocationSite, Addr] = new AAllocatorFromContext(site =>
//      PowersetAddr(References.allocationSiteAddr(site)))
//    override val print: PrintBound[Value] = new PrintBound
//    override val input: AUserInputFun[Value] = new AUserInputFun[RelationalAnalysis.Value](Value.IntValue(Topped.Top))
//
//    // TODO check
//    given Widen[ApronExpr] = new WidenApronExpr
//    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])
//
//    override def execute(p: Program): Value =
//      super.execute(p)
//
//    override def copyState(from: Executor): Unit = {
//      super.copyState(from)
//    }
//
//    val cfg = controlFlow[CallString](CfgConfig.AllNodes(sensitive = false))
//
//    final override val fixpoint =
//      callSiteSensitive(callSites,
//        fix.log(cfg.logger,
//          fix.dispatch(isFunOrWhile, Seq(
//            fix.iter.topmost(stackConfig), fix.iter.topmost(stackConfig))
//          )
//        )
//      ).fixpoint
//
//    override def newInstance: sturdy.Executor = new Instance(apronManager, initStore, stackConfig, callSites)