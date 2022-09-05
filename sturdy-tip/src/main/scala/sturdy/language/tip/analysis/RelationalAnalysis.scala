package sturdy.language.tip.analysis

import apron.Manager
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Texpr1Node
import sturdy.Executor
import sturdy.data.{WithJoin, given}
import sturdy.effect.{EffectStack, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.callframe.ApronCallFrame.given
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.CollectedFailures
import sturdy.effect.failure.Failure
import sturdy.effect.print.PrintBound
import sturdy.effect.print.given
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.{AUserInput, AUserInputFun}
import sturdy.apron.{Apron, ApronAllocRoundRobin, given}
import sturdy.fix
import sturdy.fix.StackConfig
import sturdy.fix.context
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.*
import sturdy.language.tip.abstractions.{Fix, Functions, Records, References, isFunOrWhile}
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
  override type VInt = Topped[Texpr1Node]
  override type VBool = Topped[Tcons1]

  final def asBoolean(v: Value)(using inst: Instance): VBool = v match
    case Value.BoolValue(toppedBool) => toppedBool
    case Value.IntValue(toppedInt) => toppedInt.map(inst.apron.makeConstraint(_, Tcons1.DISEQ))
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

  class Instance(apronManager: Manager, initEnvironment: Environment, initStore: Store, stackConfig: StackConfig, callSites: Int) extends GenericInstance:
    given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

    val apronAlloc = new ApronAllocRoundRobin(apronManager)
    implicit val apron: Apron = new Apron(apronManager, apronAlloc)
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)

    given EqOps[VRef, VBool] = new LiftedEqOps[VRef, VBool, VRef, Topped[Boolean]](identity, _.map(apron.makeConstantConstraint))
    given EqOps[VFun, VBool] = new LiftedEqOps[VFun, VBool, VFun, Topped[Boolean]](identity, _.map(apron.makeConstantConstraint))
    given EqOps[VRecord, VBool] = new LiftedEqOps[VRecord, VBool, VRecord, Topped[Boolean]](identity, _.map(apron.makeConstantConstraint))

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: ApronCallFrame[Unit, String, Value] = new ApronCallFrame(
      apron,
      (),
      { case Value.IntValue(t) => t.toOption; case _ => None },
      _ => None,
      iv => Value.IntValue(Topped.Actual(iv)),
      _ => Value.TopValue,
      initEnvironment
    )

    override val store: AStoreMultiAddrThreadded[AllocationSiteAddr, Value] = new AStoreMultiAddrThreadded(initStore)
    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInputFun[Value] = new AUserInputFun[RelationalAnalysis.Value](Value.IntValue(Topped.Top))

    // TODO check
    given Widen[Texpr1Node] = new WideningTexpr1Node
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

    override def newInstance: sturdy.Executor = new Instance(apronManager, initEnvironment, initStore, stackConfig, callSites)