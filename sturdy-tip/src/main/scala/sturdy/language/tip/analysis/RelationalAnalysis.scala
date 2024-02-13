package sturdy.language.tip.analysis

import apron.Manager
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Interval
import sturdy.Executor
import sturdy.apron.*
import sturdy.data.{JOption, JOptionC, NoJoin, WithJoin, given}
import sturdy.effect.{EffectStack, given}
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
import sturdy.fix.StackConfig
import sturdy.fix.context
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
    case (RelationalVar.Local(_), _) => -1
    case (RelationalVar.Temp(ty1), RelationalVar.Temp(ty2)) => ty1.toString.compareTo(ty2.toString)
    case (_, RelationalVar.Local(_)) => 1
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

      var intvars: BitSet = BitSet.empty

      override def getLocal(x: Int): JOptionC[Value] =
        if (intvars.contains(x))
          apronCallFrame.getLocal(x).map(exp => Value.IntValue(exp))
        else
          super.getLocal(x)

      override def setLocal(x: Int, v: Value): JOptionC[Unit] =
        if (intvars.contains(x)) v match
          case Value.IntValue(i) => apronCallFrame.setLocal(x, i)
          case _ => throw new IllegalStateException(s"May not change the type of a variable $x := $v (was non-integer variable)")
        else v match
          case Value.IntValue(_) => throw new IllegalStateException(s"May not change the type of a variable $x := $v (was integer variable)")
          case _ => super.setLocal(x, v)

      override def withNew[A](d: String, newVars: Iterable[(String, Option[Value])], site: Exp.Call)(f: => A): A =
        val snapIntvars = intvars
        intvars = BitSet.empty
        val newIntVars = newVars.zipWithIndex.collect {
          case ((x, Some(Value.IntValue(i))), ix) =>
            intvars += ix
            x -> Some(i)
        }
        try super.withNew(d, newVars, site)(
          apronCallFrame.withNew((), newIntVars, site)(f)
        ) finally {
          intvars = snapIntvars
        }

      override type State = (List[Value], BitSet, apronCallFrame.State)

      override def getState: State = (vars.toList, intvars, apronCallFrame.getState)

      override def setState(st: State): Unit =
        vars = st._1.toArray
        intvars = st._2
        apronCallFrame.setState(st._3)

      private given NoCombineBitSet[W <: Widening]: Combine[BitSet, W] with
        override def apply(v1: BitSet, v2: BitSet): MaybeChanged[BitSet] =
          if (v1 != v2)
            throw new IllegalArgumentException(s"BitSets may not differ here $v1 and $v2")
          else
            MaybeChanged.Unchanged(v1)

      override def join: Join[State] = (st1: State, st2: State) =>
        if (st1._2 != st2._2)
          throw new IllegalArgumentException(s"BitSets may not differ here $st1 and $st2")
        val MaybeChanged(vars, varsChanged) = Join(st1._1, st2._1)
        val MaybeChanged(apron, apronChanged) = apronCallFrame.join(st1._3, st2._3)
        MaybeChanged((vars, st1._2, apron), varsChanged || apronChanged)

      override def widen: Widen[State] = (st1: State, st2: State) =>
        if (st1._2 != st2._2)
          throw new IllegalArgumentException(s"BitSets may not differ here $st1 and $st2")
        val MaybeChanged(vars, varsChanged) = Widen(st1._1, st2._1)
        val MaybeChanged(apron, apronChanged) = apronCallFrame.widen(st1._3, st2._3)
        MaybeChanged((vars, st1._2, apron), varsChanged || apronChanged)


    override val store: AStoreThreaded[AllocationSiteAddr, Addr, Value] = new AStoreThreaded(initStore)
    override val alloc: AAllocatorFromContext[AllocationSite, Addr] = new AAllocatorFromContext(site =>
      PowersetAddr(References.allocationSiteAddr(site)))
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInputFun[Value] = new AUserInputFun[RelationalAnalysis.Value](Value.IntValue(topInt))

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