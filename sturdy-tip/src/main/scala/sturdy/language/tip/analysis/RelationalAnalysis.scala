package sturdy.language.tip.analysis

import apron.Manager
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Interval
import sturdy.Executor
import sturdy.apron.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.{Effect, EffectList, EffectStack, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.callframe.{DecidableCallFrame, DecidableMutableCallFrame, JoinableDecidableCallFrame, MutableCallFrame, RelationalCallFrame, given}
import sturdy.effect.callframe.RelationalCallFrame.given
import sturdy.effect.failure.CollectedFailures
import sturdy.effect.failure.Failure
import sturdy.effect.print.PrintBound
import sturdy.effect.print.given
import sturdy.effect.store.{AStoreThreaded, RecencyRelationalStore, RecencyStore, RelationalStore, Store}
import sturdy.effect.userinput.{AUserInput, AUserInputFun}
import sturdy.fix
import sturdy.fix.{StackConfig, State, context}
import sturdy.language.tip
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.*
import sturdy.language.tip.abstractions.{CfgConfig, ControlFlow, Fix, Functions, Records, References, isFunOrWhile}
import sturdy.language.tip.analysis.RelationalAnalysis.{Addr, RelType, RelationalVar}
import sturdy.util.Lazy
import sturdy.util.lazily
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.values.types.{BaseType, given}
import sturdy.values.utils.{ConvertInterval, given}

import scala.collection.immutable.BitSet


object RelationalAnalysis extends Interpreter,
  Functions.Powerset, Records.PreciseFieldsOrTop, ControlFlow, Fix:

  override type J[A] = WithJoin[A]

  type RelType = BaseType[Int]
  enum RelationalVar:
    case Local(x: String)
    case Temp(ty: RelType)
    case Alloc(label: Label)

  given Ordering[RelationalVar] = {
    case (RelationalVar.Local(x1), RelationalVar.Local(x2)) => x1.compareTo(x2)
    case (RelationalVar.Temp(ty1), RelationalVar.Temp(ty2)) => ty1.toString.compareTo(ty2.toString)
    case (RelationalVar.Alloc(l1), RelationalVar.Alloc(l2)) => l1.toString.compareTo(l2.toString)
    case (RelationalVar.Local(_), _) => -1
    case (_, RelationalVar.Alloc(_)) => -1
    case (_, RelationalVar.Local(_)) => 1
    case (RelationalVar.Alloc(_),_) => 1
  }
  given FiniteRelationalVar(using Finite[RelType]): Finite[RelationalVar] with {}
  type RelAddr = VirtualAddress[RelationalVar]

  override type VInt = ApronExpr[RelAddr, RelType]
  override type VBool = ApronCons[RelAddr, RelType]

  final type Addr = PowVirtualAddress[RelationalVar]
  final type PAddr = PowersetAddr[PhysicalAddress[RelationalVar],PhysicalAddress[RelationalVar]]
  final type VRef = AbstractReference[Addr]
  final type Environment = Map[String, Value]
  final type InitStore = Map[RelAddr, Value]

  final def asBoolean(v: Value)(using inst: Instance): VBool =
    v match
      case Value.BoolValue(toppedBool) => toppedBool
      case Value.IntValue(i) =>
        given Failure = inst.failure
        given EffectStack = inst.effectStack
        ApronCons.intNeq[RelAddr, RelType](i, ApronExpr.intLit[RelAddr, RelType](0))
      case Value.TopValue => topBool
      case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  final def asInt(v: Value)(using inst: Instance): VInt =
    v match
      case Value.BoolValue(bool) =>
        import inst.given
        BooleanSelection[VBool, VInt](bool, ApronExpr.intLit[RelAddr, RelType](1), ApronExpr.intLit[RelAddr, RelType](0))
      case Value.IntValue(i) => i
      case Value.TopValue => topInt
      case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  override def topInt(using inst: Instance): VInt =
    given Failure = inst.failure
    given EffectStack = inst.effectStack
    ApronExpr.intTop
  override def topBool(using inst: Instance): VBool =
    import inst.given
    ApronCons.top

  override def topReference(using self: Instance): VRef =
    ???

  class Instance(apronManager: Manager, initStore: InitStore, stackConfig: StackConfig, callSites: Int) extends GenericInstance:


    implicit val tempRelationalAlloc: AAllocatorFromContext[RelType, RelationalVar] = AAllocatorFromContext(RelationalVar.Temp.apply)
    implicit val localRelationaAlloc: AAllocatorFromContext[String, RelationalVar] = AAllocatorFromContext(RelationalVar.Local.apply)

    given Manager = apronManager

    type VirtAddr = VirtualAddress[RelationalVar]
    type PhysAddr = PhysicalAddress[RelationalVar]
    type PowVirtAddr = PowVirtualAddress[RelationalVar]
    type PowPhysAddr = PowersetAddr[PhysAddr,PhysAddr]
    type ApronExprPhysAddr = ApronExpr[PhysAddr, RelType]

    var exprConverter: ApronExprConverter[RelationalVar, RelType, Value] = null
    val relationalStore: RelationalStore[RelationalVar, RelType, PowPhysAddr,Value] = new RelationalStore[RelationalVar, RelType, PowPhysAddr,Value] (
      manager = apronManager,
      initialState = apron.Abstract1(apronManager, new apron.Environment()),
      initialTypeEnv = Map()
    ):
      override def getRelationalVal(v: Value): Option[ApronExprPhysAddr] =
        v match
          case Value.IntValue(iv) => Some(exprConverter.virtToPhys(iv))
          case _ => None

      override def makeRelationalVal(expr: ApronExprPhysAddr): Value =
        Value.IntValue(exprConverter.physToVirt(expr))

    val recencyStore: RecencyStore[RelationalVar, PowVirtAddr, Value] = new RecencyStore(relationalStore)
    exprConverter = ApronExprConverter(recencyStore, relationalStore)

    given apronState: ApronRecencyState[RelationalVar, RelType, Value] = new ApronRecencyState[RelationalVar, RelType, Value](tempRelationalAlloc, recencyStore, relationalStore)

    final class TipCallFrame extends RelationalCallFrame[String, String, Exp.Call, RelationalVar, RelType, Value](
      initData = "$main",
      initVars = Iterable.empty,
      localVariableAllocator = localRelationaAlloc,
      apronState
    ):
      override def makeRelationalVal(expr: ApronExprVirtAddr): Value = Value.IntValue(expr)

    override val callFrame: TipCallFrame = new TipCallFrame

    override val store: RecencyStore[RelationalVar, Addr, Value] = recencyStore

    override val alloc: AAllocatorFromContext[AllocationSite, Addr] =
      new AAllocatorFromContext(site =>
        PowVirtualAddress(recencyStore.alloc(allocSiteToAddr(site)))
      )
    def allocSiteToAddr(site: AllocationSite): RelationalVar =
      site match
        case AllocationSite.Alloc(e) => RelationalVar.Alloc(e.label)
        case AllocationSite.Record(r) => RelationalVar.Alloc(r.label)

    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInputFun[Value] = new AUserInputFun[RelationalAnalysis.Value](Value.IntValue(topInt))

    override def newEffectStack(effects: => Effect, inEffects: PartialFunction[Any, Effect], outEffects: PartialFunction[Any, Effect]): EffectStack =
      new EffectStack(
        AddressClosure(recencyStore.addressTranslation, removeStore(effects)),
        (dom: Any) => AddressClosure(recencyStore.addressTranslation, removeStore(inEffects(dom))),
        (dom: Any) => AddressClosure(recencyStore.addressTranslation, removeStore(outEffects(dom)))
      )

    private def removeStore(effect: Effect): Effect =
      effect match
        case EffectList(effects) => new EffectList(effects.filter(eff => eff != store))
        case _ => throw IllegalArgumentException(s"Expected EffectList, but got $effect")

    given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    given Failure = failure

    override def jv: WithJoin[Value] = implicitly

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)

    given EqOps[VRef, VBool] = new LiftedEqOps[VRef, VBool, VRef, Topped[Boolean]](identity, ApronCons.from)

    given EqOps[VFun, VBool] = new LiftedEqOps[VFun, VBool, VFun, Topped[Boolean]](identity, ApronCons.from)

    given EqOps[VRecord, VBool] = new LiftedEqOps[VRecord, VBool, VRecord, Topped[Boolean]](identity, ApronCons.from)

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override def execute(p: Program): Value =
      super.execute(p)

    override def copyState(from: Executor): Unit = {
      super.copyState(from)
    }

    val cfg = controlFlow[CallString](CfgConfig.AllNodes(sensitive = false))

    final override val fixpoint =
      callSiteSensitive(callSites,
        fix.log(cfg.logger,
          fix.dispatch(isFunOrWhile, Seq(
            fix.iter.topmost(stackConfig), fix.iter.topmost(stackConfig))
          )
        )
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(apronManager, initStore, stackConfig, callSites)