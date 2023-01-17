package sturdy.language.tip.analysis

import apron.Manager
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Interval
import sturdy.Executor
import sturdy.apron.*
import sturdy.data.{WithJoin, given}
import sturdy.effect.{EffectStack, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.callframe.ApronCallFrame.given
import sturdy.effect.callframe.given
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.CollectedFailures
import sturdy.effect.failure.Failure
import sturdy.effect.print.PrintBound
import sturdy.effect.print.given
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.{AUserInput, AUserInputFun}
import sturdy.apron.{Apron, given}
import sturdy.fix
import sturdy.fix.StackConfig
import sturdy.fix.context
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.*
import sturdy.language.tip.abstractions.{Fix, References, Functions, isFunOrWhile, Records}
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
import sturdy.values.utils.{ConvertInterval, given}


object RelationalAnalysis extends Interpreter,
  Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type J[A] = WithJoin[A]
  override type VInt = Topped[ApronExpr]
  override type VBool = Topped[ApronCons]

  final def asBoolean(v: Value)(using inst: Instance): VBool = v match
    case Value.BoolValue(toppedBool) => toppedBool
    case Value.IntValue(toppedInt) => toppedInt.map(ApronCons.neq(_, ApronExpr.num(0)))
    case Value.TopValue => Topped.Top
    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  final def asInt(v: Value)(using inst: Instance): VInt = v match
    case Value.BoolValue(toppedBool) =>
      toppedBool.flatMap { bv =>
        import inst.{given_EffectStack, apron, failure}
        given Failure = failure
        val vIntOps = summon[IntegerOps[Int, VInt]]
        inst.apron.ifThenElsePure(bv, widen = false)(vIntOps.integerLit(1))(vIntOps.integerLit(0))
      }
    case Value.IntValue(i) => i
    case Value.TopValue => Topped.Top
    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  override def topInt(using inst: Instance): VInt = Topped.Top
  override def topBool: VBool = Topped.Top

  class Instance(apronManager: Manager, stackConfig: StackConfig, callSites: Int) extends GenericInstance:
    given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

    val apronAlloc: ApronAlloc = ApronAlloc.default(apronManager)
    implicit val apron: Apron = new Apron(apronManager, apronAlloc)
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)

    given EqOps[VRef, VBool] = new LiftedEqOps[VRef, VBool, VRef, Topped[Boolean]](identity, _.map(ApronCons.fromBool))
    given EqOps[VFun, VBool] = new LiftedEqOps[VFun, VBool, VFun, Topped[Boolean]](identity, _.map(ApronCons.fromBool))
    given EqOps[VRecord, VBool] = new LiftedEqOps[VRecord, VBool, VRecord, Topped[Boolean]](identity, _.map(ApronCons.fromBool))

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: ApronCallFrame[String, String, Value] = new ApronCallFrame(
      apron,
      "$main",
      { 
        case Value.IntValue(Topped.Top) => scala.Some(ApronExpr.top)
        case Value.IntValue(Topped.Actual(v)) => scala.Some(v)
        case _ => None 
      },
      _ => None,
      iv => Value.IntValue(Topped.Actual(iv)),
      _ => Value.TopValue,
      Iterable.empty
    )

    override val store: AStoreMultiAddrThreadded[AllocationSiteAddr, Value] = new AStoreMultiAddrThreadded(Map.empty)
    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInputFun[Value] = new AUserInputFun[RelationalAnalysis.Value](Value.IntValue(Topped.Top))

    // TODO check
    given Widen[ApronExpr] = new WidenApronExpr
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override def execute(p: Program): Value =
      super.execute(p)

    override def copyState(from: Executor): Unit = {
      super.copyState(from)
    }

    final override val fixpoint =
      callSiteSensitive(callSites, fix.dispatch(isFunOrWhile, Seq(
        fix.iter.innermost(stackConfig), fix.iter.innermost(stackConfig)))
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(apronManager, stackConfig, callSites)