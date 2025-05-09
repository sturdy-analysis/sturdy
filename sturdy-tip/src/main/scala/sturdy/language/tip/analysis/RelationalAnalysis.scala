package sturdy.language.tip.analysis

import apron.*
import sturdy.{Executor, data, fix, language}
import sturdy.apron.{*, given}
import sturdy.control.ControlObservable
import sturdy.data.{*, given}
import sturdy.effect.{Effect, EffectList, EffectStack, TrySturdy, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.callframe.{DecidableCallFrame, DecidableMutableCallFrame, JoinableDecidableCallFrame, MutableCallFrame, RelationalCallFrame, SplitCallFrame, given}
import sturdy.effect.callframe.RelationalCallFrame.given
import sturdy.effect.failure.CollectedFailures
import sturdy.effect.failure.Failure
import sturdy.effect.print.{PrintBound, PrintBoundSerializable, Serializer, given}
import sturdy.effect.store.{AStoreThreaded, RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, Store}
import sturdy.effect.userinput.{AUserInput, AUserInputFun}
import sturdy.fix.{DomLogger, Logger, StackConfig, State, context}
import sturdy.language.tip
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.*
import sturdy.language.tip.abstractions.{CfgConfig, Control, ControlFlow, Fix, Functions, Records, References, isFunOrWhile}
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

import scala.collection.immutable.BitSet
import scala.collection.mutable


object RelationalAnalysis extends Interpreter,
  Functions.Powerset, Records.PreciseFieldsOrTop, Control, Fix:

  override type J[A] = WithJoin[A]

  type RelType = BaseType[Int]

  enum RelationalVar:
    case Local(x: String)
    case Temp(in: FixIn)
    case Alloc(label: Label)
    case Print(ty: RelType)

  given Ordering[RelationalVar] = {
    // Print <= Alloc <= Temp <= Local
    case (RelationalVar.Local(x1), RelationalVar.Local(x2)) => x1.compareTo(x2)
    case (RelationalVar.Temp(ty1), RelationalVar.Temp(ty2)) => ty1.toString.compareTo(ty2.toString)
    case (RelationalVar.Alloc(l1), RelationalVar.Alloc(l2)) => l1.toString.compareTo(l2.toString)
    case (RelationalVar.Print(ty1), RelationalVar.Print(ty2)) => ty1.toString.compareTo(ty2.toString)
    case (RelationalVar.Print(_),_) => -1
    case (_,RelationalVar.Print(_)) => 1
    case (RelationalVar.Alloc(_),_) => -1
    case (_,RelationalVar.Alloc(_)) => 1
    case (RelationalVar.Temp(_),_) => -1
    case (_,RelationalVar.Temp(_)) => 1
  }
  given FiniteRelationalVar(using Finite[RelType]): Finite[RelationalVar] with {}
  type RelAddr = VirtualAddress[RelationalVar]

  override final type VInt = ApronExpr[RelAddr, RelType]
  override final type VBool = ApronCons[RelAddr, RelType]
  override final type VRef = AbstractReference[Addr]

  final type Addr = PowVirtualAddress[RelationalVar]
  final type PAddr = PowersetAddr[PhysicalAddress[RelationalVar],PhysicalAddress[RelationalVar]]
  final type Environment = Map[String, Value]
  final type InitStore = Map[RelAddr, Value]

  final def asBoolean(v: Value)(using inst: Instance): VBool =
    v match
      case Value.BoolValue(toppedBool) => toppedBool
      case Value.IntValue(i) =>
        given Failure = inst.failure
        given EffectStack = inst.effectStack
        ApronCons.neq[RelAddr, RelType](i, ApronExpr.intLit[RelAddr, RelType](0, BaseType[Int]))
      case Value.TopValue => topBool
      case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  final def asInt(v: Value)(using inst: Instance): VInt =
    v match
      case Value.BoolValue(bool) =>
        import inst.given
        BooleanSelection[VBool, VInt](bool, ApronExpr.intLit[RelAddr, RelType](1, BaseType[Int]), ApronExpr.intLit[RelAddr, RelType](0, BaseType[Int]))
      case Value.IntValue(i) => i
      case Value.TopValue => topInt
      case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  override def topInt(using inst: Instance): VInt =
    given Failure = inst.failure
    given EffectStack = inst.effectStack
    ApronExpr.top(BaseType[Int])
  override def topBool(using inst: Instance): VBool =
    import inst.given
    ApronCons.top(BaseType[Int])

  override def topReference(using self: Instance): VRef =
    val addrs = self.store.virtualAddresses
    AbstractReference.NullAddr(addrs, false)

  class Instance(apronManager: Manager, initStore: InitStore, stackConfig: StackConfig, callSites: Int) extends GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:

    implicit val tempRelationalAlloc: AAllocatorFromContext[RelType, RelationalVar] = AAllocatorFromContext(_ => RelationalVar.Temp(domLogger.currentDom.getOrElse(FixIn.EnterFunction(functions("main")))))
    implicit val localRelationaAlloc: AAllocatorFromContext[(String, Any, Option[Any]), RelationalVar] = AAllocatorFromContext((v,_,_) => RelationalVar.Local(v))

    given Manager = apronManager

    type VirtAddr = VirtualAddress[RelationalVar]
    type PhysAddr = PhysicalAddress[RelationalVar]
    type PowVirtAddr = PowVirtualAddress[RelationalVar]
    type PowPhysAddr = PowersetAddr[PhysAddr,PhysAddr]
    type ApronExprPhysAddr = ApronExpr[PhysAddr, RelType]

    val addressTranslation: AddressTranslation[RelationalVar] = AddressTranslation.empty
    var exprConverter: ApronExprConverter[RelationalVar, RelType, Value] = null
    var apronState: ApronRecencyState[RelationalVar, RelType, Value] = null
    given lazyApronState: Lazy[ApronState[VirtualAddress[RelationalVar], RelType]] = lazily(apronState)
    given lazyExprConverter: Lazy[ApronExprConverter[RelationalVar, RelType, Value]] = lazily(exprConverter)

    given relationalValue: RelationalValue[Value, VirtAddr, RelType] with
      override def getRelationalVal(v: Value): Option[ApronExpr[VirtAddr, RelType]] =
        v match
          case Value.IntValue(expr) => Some(expr)
          case _ => None

      override def makeRelationalVal(expr: ApronExpr[VirtAddr, RelType]): Value =
        Value.IntValue(expr)


    val relationalStore: RelationalStore[RelationalVar, RelType, PowPhysAddr,Value] = new RelationalStore[RelationalVar, RelType, PowPhysAddr,Value] (
      manager = apronManager,
      initialState = apron.Abstract1(apronManager, new apron.Environment()),
      initialMetaData = Map()
    )
    val recencyStore: RecencyStore[RelationalVar, PowVirtAddr, Value] = new RecencyStore(relationalStore, addressTranslation)
    exprConverter = ApronExprConverter(recencyStore, relationalStore)
    apronState = new ApronRecencyState[RelationalVar, RelType, Value](tempRelationalAlloc, recencyStore, relationalStore)
    given ApronState[VirtualAddress[RelationalVar], RelType] = apronState

    val callFrame: RelationalCallFrame[String, String, Value, Exp.Call, RelationalVar, RelType] = new RelationalCallFrame(
      initData = "$main",
      initVars = Iterable.empty,
      localVariableAllocator = localRelationaAlloc,
      apronState
    )

//    Sven: Currently Unsound. No time to debug the issue.
//    val baseCallFrame: JoinableDecidableCallFrame[String, String, Value, Exp.Call] = new JoinableDecidableCallFrame("$main", Iterable.empty)
//    override val callFrame: SplitCallFrame[String, String, Value, Exp.Call] = new SplitCallFrame(baseCallFrame, relCallFrame, useRelationalCallframe) {
//      override def stackWiden: StackWidening[State] =
//        (stack: List[State], call: State) =>
//          //      Unchanged(call)
//          if (stack.contains(call))
//            Unchanged(call)
//          else
//            Changed(call)
//    }
//    def useRelationalCallframe(name: String, v: Value, site: Exp.Call): Boolean =
//      val fun = site match { case Exp.Call(Exp.Var(f), _) => currentProgram.functions.get(f); case _ => None }
//      lazy val loopVar = fun.forall(f => f.loopVars.contains(name))
//      lazy val isRecursive = fun.forall(f => currentProgram.isRecursive(f.name))
//      lazy val tooManyLocals = fun.forall(f => f.locals.size > 10)
//      v match
//        case Value.IntValue(i) => loopVar || isRecursive || tooManyLocals
//        case Value.BoolValue(b) => loopVar || isRecursive || tooManyLocals
//        case _ => false

    override val store: RecencyStore[RelationalVar, Addr, Value] = recencyStore

    override val alloc: AAllocatorFromContext[AllocationSite, Addr] =
      new AAllocatorFromContext(site =>
        PowVirtualAddress(recencyStore.alloc(allocSiteToAddr(site)))
      )
    def allocSiteToAddr(site: AllocationSite): RelationalVar =
      site match
        case AllocationSite.Alloc(e) => RelationalVar.Alloc(e.label)
        case AllocationSite.Record(r) => RelationalVar.Alloc(r.label)

    given serializeValue: Serializer[Value,Value] = {
      case Value.IntValue(expr) =>
        val addr = recencyStore.alloc(RelationalVar.Print(expr._type))
        recencyStore.joinRecentIntoOld(PowVirtualAddress(addr)) // Ensure the allocated address is old
        apronState.assign(addr, expr)
        Value.IntValue(ApronExpr.addr(addr, expr._type))
      case v => v
    }

    override val print: PrintBoundSerializable[Value,Value] = new PrintBoundSerializable[Value,Value]
    override val input: AUserInputFun[Value] = new AUserInputFun[RelationalAnalysis.Value](Value.IntValue(topInt))

    override def newEffectStack(effects: => Effect, inEffects: PartialFunction[Any, Effect], outEffects: PartialFunction[Any, Effect]): EffectStack =
      new EffectStack(
        RecencyClosure(recencyStore,EffectList(callFrame, alloc, print, input, failure)),
        {
          case _: FixIn.Run | _: FixIn.EnterFunction => RecencyClosure(recencyStore, EffectList(callFrame, print, failure))
          case _: FixIn.Eval => RecencyClosure(recencyStore, EffectList(callFrame, alloc, input, failure))
        },
        {
          case _: FixIn.Run | _: FixIn.EnterFunction => RecencyClosure(recencyStore, EffectList(callFrame, print, failure))
          case _: FixIn.Eval => RecencyClosure(recencyStore, EffectList(callFrame, alloc, failure))
        }
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

    given EqOps[VRef, VBool] = new LiftedEqOps[VRef, VBool, VRef, Topped[Boolean]](identity, ApronCons.from(BaseType[Int]))

    given EqOps[VFun, VBool] = new LiftedEqOps[VFun, VBool, VFun, Topped[Boolean]](identity, ApronCons.from(BaseType[Int]))

    given EqOps[VRecord, VBool] = new LiftedEqOps[VRecord, VBool, VRecord, Topped[Boolean]](identity, ApronCons.from(BaseType[Int]))

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    var currentProgram: Program = _
    override def execute(p: Program): Value =
      currentProgram = p
      super.execute(p)

    override def copyState(from: Executor): Unit = {
      super.copyState(from)
    }

    def getInterval(value: Value): Value =
      value match
        case Value.IntValue(expr) => Value.IntValue(ApronExpr.constant(apronState.getInterval(expr), expr._type))
        case v => v

    class FunctionCallLogger extends Logger[FixIn, FixOut[Value]]:
      val stack: mutable.Stack[(FixIn, IndexedSeq[(Value, Value)], effectStack.State)] = mutable.Stack.empty

      override def enter(dom: FixIn): Unit =
        dom match
          case FixIn.EnterFunction(Function(name, params, locals, body, ret)) =>
            val args = params.indices.flatMap {
              i =>
                callFrame.getLocal(i).map(v => (v, getInterval(v))).option(None)(Some(_))
            }
            val state = effectStack.getState
            Predef.print("  ".repeat(stack.size))
            println(s"CALL   $name(${args.mkString(",")}) @ ${state.hashCode()}")
            stack.push((dom, args, state))
          case _ => {}

      override def exit(dom: FixIn, codom: TrySturdy[FixOut[Value]]): Unit =
        dom match
          case FixIn.EnterFunction(fun) =>
            val (_, args, inState) = stack.pop
            val result =
              codom.map {
                case FixOut.ExitFunction(v) => FixOut.ExitFunction((v, getInterval(v)))
                case FixOut.Eval(v) => FixOut.Eval((v,getInterval(v)))
                case FixOut.Run() => FixOut.Run()
              }
            val outState = effectStack.getState
            Predef.print("  ".repeat(stack.size))
            println(s"RETURN ${fun.name}(${args.mkString(",")}) @ ${inState.hashCode} = $result @ ${outState.hashCode()}")
          case _ => {}

    val funLogger: FunctionCallLogger = new FunctionCallLogger
    val domLogger: DomLogger[FixIn] = new DomLogger
    val observedStackConfig = stackConfig.withObservers(Seq(this.triggerControlEvent))

    final override val fixpoint =
      fix.log(controlEventLogger(this),
        callSiteSensitive(callSites,
          fix.log(fix.manyLogger(List(domLogger)),
            fix.dispatch(isFunOrWhile, Seq(
              fix.iter.topmost(observedStackConfig), fix.iter.topmost(observedStackConfig))
            )
          )
        )
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(apronManager, initStore, stackConfig, callSites)