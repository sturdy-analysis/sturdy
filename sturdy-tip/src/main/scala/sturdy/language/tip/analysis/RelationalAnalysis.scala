package sturdy.language.tip.analysis

import apron.Manager
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Interval
import sturdy.Executor
import sturdy.apron.*
import sturdy.data.{JOption, JOptionC, NoJoin, WithJoin, given}
import sturdy.effect.{Effect, EffectStack, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.callframe.{ApronCallFrame, DecidableCallFrame, DecidableMutableCallFrame, JoinableDecidableCallFrame, MutableCallFrame, given}
import sturdy.effect.callframe.ApronCallFrame.given
import sturdy.effect.failure.CollectedFailures
import sturdy.effect.failure.Failure
import sturdy.effect.print.PrintBound
import sturdy.effect.print.given
import sturdy.effect.store.AStoreThreaded
import sturdy.effect.store.Store
import sturdy.effect.userinput.{AUserInput, AUserInputFun}
import sturdy.apron.given
import sturdy.fix
import sturdy.fix.{StackConfig, State, context}
import sturdy.language.tip
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.*
import sturdy.language.tip.abstractions.{CfgConfig, ControlFlow, Fix, Functions, Records, References, isFunOrWhile}
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
  Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, ControlFlow, Fix:

  override type J[A] = WithJoin[A]

  type RelType = BaseType[Int]
  enum RelationalVar:
    case Local(x: String)
    case Temp(ty: RelType)
  given Ordering[RelationalVar] = {
    case (RelationalVar.Local(x1), RelationalVar.Local(x2)) => x1.compareTo(x2)
    case (RelationalVar.Local(_), RelationalVar.Temp(_)) => -1
    case (RelationalVar.Temp(_), RelationalVar.Local(_)) => 1
    case (RelationalVar.Temp(ty1), RelationalVar.Temp(ty2)) => ty1.toString.compareTo(ty2.toString)
  }
  given FiniteRelationalVar(using Finite[RelType]): Finite[RelationalVar] with {}
  type RelAddr = VirtualAddress[RelationalVar]

  override type VInt = ApronExpr[RelAddr, RelType]
  override type VBool = ApronCons[RelAddr, RelType]

  final def asBoolean(v: Value)(using inst: Instance): VBool = v match
    case Value.BoolValue(toppedBool) => toppedBool
    case Value.IntValue(i) =>
      given Failure = inst.failure
      given EffectStack = inst.effectStack
      ApronCons.intNeq[RelAddr, RelType](i, ApronExpr.intLit[RelAddr, RelType](0))
    case Value.TopValue => topBool
    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  final def asInt(v: Value)(using inst: Instance): VInt = v match
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

  class Instance(apronManager: Manager, initStore: InitStore, stackConfig: StackConfig, callSites: Int) extends GenericInstance:
    given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    given Failure = failure

    override def jv: WithJoin[Value] = implicitly

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)

    implicit val tempRelationalAlloc: AAllocatorFromContext[RelType, RelationalVar] = AAllocatorFromContext(RelationalVar.Temp.apply)
    implicit val localRelationaAlloc: AAllocatorFromContext[String, RelationalVar] = AAllocatorFromContext(RelationalVar.Local.apply)

    given Manager = apronManager
    implicit val apronCallFrame: ApronCallFrame[Unit, String, Exp.Call, RelationalVar, RelType] =
      ApronCallFrame[Unit, String, Exp.Call, RelationalVar, RelType]((), Iterable.empty)

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

    override val callFrame: DecidableMutableCallFrame[String, String, Value, Exp.Call] = new
        DecidableMutableCallFrame[String, String, Value, Exp.Call]("$main", Iterable.empty):

      override def getLocal(x: Int): JOptionC[Value] =
        (apronCallFrame.getLocal(x), super.getLocal(x)) match
          case (JOptionC.None(), JOptionC.None()) => JOptionC.None()
          case (JOptionC.Some(exp), JOptionC.None()) => JOptionC.Some(Value.IntValue(exp))
          case (JOptionC.None(), JOptionC.Some(v)) => JOptionC.Some(v)
          case (JOptionC.Some(exp), JOptionC.Some(v)) => JOptionC.Some(Join(Value.IntValue(exp), v).get)

      override def setLocal(x: Int, v: Value): JOptionC[Unit] =
        v match
          case Value.IntValue(i) => apronCallFrame.setLocal(x, i)
          case _ => super.setLocal(x, v)

      override def withNew[A](d: String, newVars: Iterable[(String, Option[Value])], site: Exp.Call)(f: => A): A =
        val newIntVars = newVars.map {
          case (x,Some(Value.IntValue(exp))) => (x,Some(exp))
          case (x,_) => (x, None)
        }
        val newOtherVars = newVars.map {
          case (x,v@Some(Value.IntValue(_))) => (x,None)
          case (x,v) => (x, v)
        }

        super.withNew(d, newOtherVars, site)(
          apronCallFrame.withNew((), newIntVars, site)(
            f
          )
        )

      case class CallFrameState(nonRelational: List[Value], relational: apronCallFrame.State)
      override type State = CallFrameState

      override def getState: State = CallFrameState(vars.toList, apronCallFrame.getState)

      override def setState(st: State): Unit =
        vars = st.nonRelational.toArray
        apronCallFrame.setState(st.relational)

      override def mapState(st: State, f: [A] => A => A): State =
        CallFrameState(st.nonRelational.map(f[Value]), apronCallFrame.mapState(st.relational, f))

      override def join: Join[State] = combineStates[Widening.No](_,_,implicitly, apronCallFrame.join)
      override def widen: Widen[State] = combineStates[Widening.Yes](_,_,implicitly, apronCallFrame.widen)

      def combineStates[W <: Widening](st1: State, st2: State, combineSuper: Combine[List[Value],W], combineApronCallFrame: Combine[apronCallFrame.State, W]): MaybeChanged[State] =
        val MaybeChanged(vars, varsChanged) = combineSuper(st1.nonRelational, st2.nonRelational)
        val MaybeChanged(apron, apronChanged) = combineApronCallFrame(st1.relational, st2.relational)
        MaybeChanged(CallFrameState(vars, apron), varsChanged || apronChanged)


    override val store: AStoreThreaded[AllocationSiteAddr, Addr, Value] = new AStoreThreaded(initStore)
    override val alloc: AAllocatorFromContext[AllocationSite, Addr] = new AAllocatorFromContext(site =>
      PowersetAddr(References.allocationSiteAddr(site)))
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInputFun[Value] = new AUserInputFun[RelationalAnalysis.Value](Value.IntValue(topInt))

    override def newEffectStack(effects: => List[Effect], inEffects: PartialFunction[Any, List[Effect]], outEffects: PartialFunction[Any, List[Effect]]): EffectStack =
      class ResolveVirtualAddressesEffectStack extends EffectStack(effects, inEffects, outEffects):
        override protected def getEffectState(effects: List[Effect]): List[Any] =
          effects.map(effect =>
            effect.mapState(effect.getState, [A] => (a: A) => resolveVirtualAddresses[A](a))
          )
        override protected def setEffectState(effects: List[Effect], states: List[Any]): Unit =
          effects.zip(states).foreach{
            case (effect,state) =>
              val newState = effect.mapState(state.asInstanceOf, [A] => (a: A) => unresolveVirtualAddresses[A](a))
              effect.setState(newState)
          }
        private def resolveVirtualAddresses[A](a: A): A =
          a match
            case addr: VirtualAddress.Virtual[?] => addr.resolve.asInstanceOf[A]
            case _ => a

        private def unresolveVirtualAddresses[A](a: A): A =
          a match
            case addr: VirtualAddress.Resolved[?] => addr.unresolve.asInstanceOf[A]
            case _ => a

      new ResolveVirtualAddressesEffectStack

    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

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